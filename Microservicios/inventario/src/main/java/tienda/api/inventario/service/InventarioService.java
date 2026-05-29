package tienda.api.inventario.service;

import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tienda.api.inventario.client.CatalogoClient;
import tienda.api.inventario.dto.DescuentoRequest;
import tienda.api.inventario.dto.IngresoRequest;
import tienda.api.inventario.model.Bodega;
import tienda.api.inventario.model.InventarioBodega;
import tienda.api.inventario.repository.BodegaRepository;
import tienda.api.inventario.repository.InventarioBodegaRepository;
import tienda.api.inventario.repository.AuditoriaStockRepository;
import tienda.api.inventario.model.AuditoriaStock;
import tienda.api.inventario.dto.ReversionRequest;

import java.util.List;
import java.util.Map;
import java.time.LocalDateTime;

@Service
public class InventarioService {

    @Autowired private BodegaRepository bodegaRepository;
    @Autowired private InventarioBodegaRepository inventarioRepository;
    @Autowired private AuditoriaStockRepository auditoriaStockRepository;
    @Autowired private CatalogoClient catalogoClient;
    @Autowired private org.springframework.amqp.rabbit.core.RabbitTemplate rabbitTemplate;

    public Bodega crearBodega(Bodega bodega) {
        return bodegaRepository.save(bodega);
    }
    
    public List<Bodega> listarBodegas() {
        return bodegaRepository.findAll();
    }

    @Transactional
    public InventarioBodega registrarIngreso(IngresoRequest req) {
        Bodega bodega = bodegaRepository.findById(req.getBodegaId())
                .orElseThrow(() -> new RuntimeException("Bodega no existe"));

        InventarioBodega inv = inventarioRepository.findByBodegaIdAndProductoId(req.getBodegaId(), req.getProductoId())
            .orElseGet(() -> {
                InventarioBodega nuevo = new InventarioBodega();
                nuevo.setBodegaId(bodega.getId());
                nuevo.setProductoId(req.getProductoId());
                nuevo.setCantidadDisponible(0);
                nuevo.setCantidadReservada(0);
                return nuevo;
            });

        inv.setCantidadDisponible(inv.getCantidadDisponible() + req.getCantidadFisica());
        InventarioBodega guardado = inventarioRepository.save(inv);
        
        AuditoriaStock auditoria = new AuditoriaStock();
        auditoria.setProductoId(req.getProductoId());
        auditoria.setBodegaId(bodega.getId());
        auditoria.setTipoMovimiento(AuditoriaStock.TipoMovimiento.INGRESO);
        auditoria.setCantidadAfectada(req.getCantidadFisica());
        auditoria.setFechaMovimiento(LocalDateTime.now());
        auditoria.setMotivoReferencia("Ingreso manual a bodega");
        auditoria.setResponsableId(getUserIdOrSystem());
        auditoriaStockRepository.save(auditoria);

        sincronizarCatalogo(req.getProductoId());
        return guardado;
    }

    @Transactional
    public String descontarStockLote(List<Map<String, Object>> items, Long ordenId) {
        for (Map<String, Object> item : items) {
            Long pId = Long.parseLong(item.get("productoId").toString());
            Integer cantSolicitada = Integer.parseInt(item.get("cantidad").toString());

            Integer totalStock = inventarioRepository.sumStockByProductoId(pId);
            if(totalStock == null || totalStock < cantSolicitada) {
                throw new RuntimeException("Stock Insuficiente. Requerido: " + cantSolicitada);
            }

            List<InventarioBodega> lotes = inventarioRepository.findByProductoId(pId);
            int remanente = cantSolicitada;

            for (InventarioBodega lote : lotes) {
                if (remanente <= 0) break;
                int disp = lote.getCantidadDisponible();
                if (disp > 0) {
                    int restar = Math.min(disp, remanente);
                    lote.setCantidadDisponible(disp - restar);
                    inventarioRepository.save(lote);
                    remanente -= restar;
                }
            }

            AuditoriaStock auditoria = new AuditoriaStock();
            auditoria.setProductoId(pId);
            auditoria.setTipoMovimiento(AuditoriaStock.TipoMovimiento.EGRESO);
            auditoria.setCantidadAfectada(cantSolicitada);
            auditoria.setFechaMovimiento(LocalDateTime.now());
            auditoria.setMotivoReferencia("Venta de orden " + ordenId);
            auditoria.setResponsableId("SISTEMA"); // Origen de pedidos
            auditoriaStockRepository.save(auditoria);

            sincronizarCatalogo(pId);
        }
        return "Descuento en lote procesado exitosamente.";
    }

    @Transactional
    public void revertirLoteAsincrono(Map<String, Object> event) {
        Long ordenId = Long.parseLong(event.get("ordenId").toString());
        List<Map<String, Object>> items = (List<Map<String, Object>>) event.get("items");

        for (Map<String, Object> item : items) {
            Long pId = Long.parseLong(item.get("productoId").toString());
            Integer cantDevuelta = Integer.parseInt(item.get("cantidad").toString());

            List<InventarioBodega> lotes = inventarioRepository.findByProductoId(pId);
            if (!lotes.isEmpty()) {
                InventarioBodega loteElegido = lotes.get(0);
                loteElegido.setCantidadDisponible(loteElegido.getCantidadDisponible() + cantDevuelta);
                inventarioRepository.save(loteElegido);

                AuditoriaStock auditoria = new AuditoriaStock();
                auditoria.setProductoId(pId);
                auditoria.setBodegaId(loteElegido.getBodegaId());
                auditoria.setTipoMovimiento(AuditoriaStock.TipoMovimiento.INGRESO);
                auditoria.setCantidadAfectada(cantDevuelta);
                auditoria.setFechaMovimiento(LocalDateTime.now());
                auditoria.setMotivoReferencia("Reversion asincrona de orden " + ordenId);
                auditoria.setResponsableId("SISTEMA"); // Asincrono
                auditoriaStockRepository.save(auditoria);

                sincronizarCatalogo(pId);
            }
        }
    }

    @Transactional
    public String descontarStock(DescuentoRequest request) {
        Long pId = request.getProductoId();
        Integer cantSolicitada = request.getCantidadDescontar();

        Integer totalStock = inventarioRepository.sumStockByProductoId(pId);
        if(totalStock == null || totalStock < cantSolicitada) {
            throw new RuntimeException("Stock Insuficiente en el ecosistema de bodegas. (Requerido: " + cantSolicitada + " | Disponible: " + (totalStock == null ? 0 : totalStock) + ")");
        }

        List<InventarioBodega> lotes = inventarioRepository.findByProductoId(pId);
        int remanente = cantSolicitada;

        for (InventarioBodega lote : lotes) {
            if (remanente <= 0) break;
            int disp = lote.getCantidadDisponible();
            if (disp > 0) {
                int restar = Math.min(disp, remanente);
                lote.setCantidadDisponible(disp - restar);
                inventarioRepository.save(lote);
                remanente -= restar;
            }
        }

        if (remanente > 0) {
            throw new RuntimeException("Error fatal en sincronia. Las bodegas reportan falta de " + remanente + " unidades fisicas.");
        }

        AuditoriaStock auditoria = new AuditoriaStock();
        auditoria.setProductoId(pId);
        auditoria.setTipoMovimiento(AuditoriaStock.TipoMovimiento.EGRESO);
        auditoria.setCantidadAfectada(cantSolicitada);
        auditoria.setFechaMovimiento(LocalDateTime.now());
        auditoria.setMotivoReferencia("Venta de orden " + request.getOrdenId());
        auditoria.setResponsableId(getUserIdOrSystem());
        auditoriaStockRepository.save(auditoria);

        sincronizarCatalogo(pId);
        return "Descuento recursivo distribuido a traves de bodegas con exito.";
    }

    @Transactional
    public String revertirDescuento(ReversionRequest request) {
        Long pId = request.getProductoId();
        Integer cantDevuelta = request.getCantidad();

        List<InventarioBodega> lotes = inventarioRepository.findByProductoId(pId);
        if (lotes.isEmpty()) {
            throw new RuntimeException("No se encontro ninguna bodega asociada al producto " + pId + " para devolver el stock.");
        }
        
        InventarioBodega loteElegido = lotes.get(0);
        loteElegido.setCantidadDisponible(loteElegido.getCantidadDisponible() + cantDevuelta);
        inventarioRepository.save(loteElegido);

        AuditoriaStock auditoria = new AuditoriaStock();
        auditoria.setProductoId(pId);
        auditoria.setBodegaId(loteElegido.getBodegaId());
        auditoria.setTipoMovimiento(AuditoriaStock.TipoMovimiento.INGRESO);
        auditoria.setCantidadAfectada(cantDevuelta);
        auditoria.setFechaMovimiento(LocalDateTime.now());
        auditoria.setMotivoReferencia("Reversion/Devolucion de orden " + request.getOrdenId());
        auditoria.setResponsableId(getUserIdOrSystem());
        auditoriaStockRepository.save(auditoria);

        sincronizarCatalogo(pId);
        return "Descuento revertido y devuelto a bodega exitosamente.";
    }

    private void sincronizarCatalogo(Long productoId) {
        try {
            Integer subtotal = inventarioRepository.sumStockByProductoId(productoId);
            Map<String, Object> event = Map.of(
                "productoId", productoId,
                "stock", subtotal == null ? 0 : subtotal
            );
            rabbitTemplate.convertAndSend(tienda.api.inventario.config.RabbitMQConfig.EXCHANGE, tienda.api.inventario.config.RabbitMQConfig.ROUTING_KEY_STOCK, event);
            System.out.println("Evento StockActualizadoEvent publicado (MAP) para producto: " + productoId);
        } catch (Exception e) {
            System.err.println("Visibilidad de Catalogo Fallida (RabbitMQ): " + e.getMessage());
        }
    }

    @Transactional
    public void desactivarProducto(Long productoId, Boolean activo) {
        if(!activo) {
            List<InventarioBodega> lotes = inventarioRepository.findByProductoId(productoId);
            for(InventarioBodega lote : lotes) {
                if(lote.getCantidadDisponible() > 0) {
                    lote.setCantidadReservada(lote.getCantidadReservada() + lote.getCantidadDisponible());
                    lote.setCantidadDisponible(0);
                    inventarioRepository.save(lote);
                }
            }
            sincronizarCatalogo(productoId);
        }
    }

    private String getUserIdOrSystem() {
        try {
            org.springframework.security.core.Authentication auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.getName() != null && !auth.getName().isEmpty() && !auth.getName().equals("anonymousUser")) {
                return auth.getName();
            }
        } catch (Exception ignored) {}
        return "SISTEMA";
    }
}
