package tienda.api.logistica.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tienda.api.logistica.model.GuiaDespacho;
import tienda.api.logistica.repository.LogisticaRepository;
import tienda.api.logistica.client.AuthClient;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class LogisticaService {

    @Autowired
    private LogisticaRepository logisticaRepository;

    @Autowired
    private org.springframework.amqp.rabbit.core.RabbitTemplate rabbitTemplate;

    @Autowired
    private AuthClient authClient;

    private void publicarEvento(String routingKey, Long pedidoId, Long choferId, String estado) {
        try {
            java.util.Map<String, Object> event = java.util.Map.of(
                "pedidoId", pedidoId,
                "choferId", choferId != null ? choferId : -1L,
                "estado", estado
            );
            rabbitTemplate.convertAndSend(tienda.api.logistica.config.RabbitMQConfig.EXCHANGE, routingKey, event);
        } catch (Exception e) {
            System.err.println("Fallo publicando evento de logistica: " + e.getMessage());
        }
    }

    public GuiaDespacho crearGuiaParaPedido(Long pedidoId, String direccionCompleta) {
        if (logisticaRepository.findByPedidoId(pedidoId).isPresent()) {
            throw new RuntimeException("Ya existe una guia de despacho para este pedido");
        }
        GuiaDespacho guia = GuiaDespacho.builder()
                .pedidoId(pedidoId)
                .estado("POR_ARMAR")
                .fechaCreacion(LocalDateTime.now())
                .fechaActualizacion(LocalDateTime.now())
                .direccionCompleta(direccionCompleta)
                .build();
        GuiaDespacho guardada = logisticaRepository.save(guia);
        publicarEvento("logistica.estado.cambiado", pedidoId, null, "POR_ARMAR");
        return guardada;
    }

    public List<GuiaDespacho> listarPendientes() {
        return logisticaRepository.findByEstado("POR_ARMAR");
    }

    public GuiaDespacho marcarComoArmado(Long id) {
        GuiaDespacho guia = logisticaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Guia no encontrada con el ID: " + id));
        if (!"POR_ARMAR".equals(guia.getEstado()) && !"PENDIENTE".equals(guia.getEstado())) {
            throw new RuntimeException("La guia no esta en estado POR_ARMAR ni PENDIENTE, estado actual: " + guia.getEstado());
        }
        guia.setEstado("LISTO_PARA_CHOFER");
        guia.setFechaActualizacion(LocalDateTime.now());
        GuiaDespacho guardada = logisticaRepository.save(guia);
        publicarEvento("logistica.estado.cambiado", guia.getPedidoId(), null, "LISTO_PARA_CHOFER");
        return guardada;
    }

    public GuiaDespacho asignarChofer(Long id, Long choferId) {
        GuiaDespacho guia = logisticaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Guia no encontrada"));
        guia.setChoferId(choferId);
        guia.setEstado("ASIGNADO");
        guia.setFechaActualizacion(LocalDateTime.now());
        GuiaDespacho guardada = logisticaRepository.save(guia);
        publicarEvento("logistica.ruta.asignada", guia.getPedidoId(), choferId, "ASIGNADO");
        return guardada;
    }

    public List<GuiaDespacho> listarMisViajes(Long choferId) {
        return logisticaRepository.findByChoferId(choferId);
    }

    public GuiaDespacho actualizarEstado(Long id, String estado, Long choferId) {
        GuiaDespacho guia = logisticaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Guia no encontrada"));

        if (!choferId.equals(guia.getChoferId()) && !choferId.equals(-1L)) {
            throw new RuntimeException("No tienes permiso para actualizar esta guia");
        }

        guia.setEstado(estado);
        guia.setFechaActualizacion(LocalDateTime.now());
        GuiaDespacho guardada = logisticaRepository.save(guia);
        publicarEvento("logistica.estado.cambiado", guia.getPedidoId(), guia.getChoferId(), estado);
        return guardada;
    }

    public List<GuiaDespacho> listarTodas() {
        return logisticaRepository.findAll();
    }

    public GuiaDespacho reasignarChofer(Long id, Long nuevoChoferId) {
        GuiaDespacho guia = logisticaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Guia no encontrada"));
        guia.setChoferId(nuevoChoferId);
        guia.setEstado("ASIGNADO");
        guia.setFechaActualizacion(LocalDateTime.now());
        GuiaDespacho guardada = logisticaRepository.save(guia);
        publicarEvento("logistica.ruta.asignada", guia.getPedidoId(), nuevoChoferId, "ASIGNADO_REASIGNACION");
        return guardada;
    }

    public void cancelarRuta(Long id) {
        GuiaDespacho guia = logisticaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Guia no encontrada"));
        guia.setEstado("CANCELADA");
        guia.setFechaActualizacion(LocalDateTime.now());
        logisticaRepository.save(guia);
        publicarEvento("logistica.estado.cambiado", guia.getPedidoId(), guia.getChoferId(), "CANCELADA");
    }

    public void autoAsignarRutas() {
        List<Long> choferes = authClient.getChoferes();
        if (choferes == null || choferes.isEmpty()) {
            throw new RuntimeException("No hay choferes disponibles para asignar");
        }

        List<GuiaDespacho> pendientes = logisticaRepository.findByEstado("LISTO_PARA_CHOFER");
        if (pendientes.isEmpty()) {
            return; // No hay nada que asignar
        }

        int choferIndex = 0;
        for (GuiaDespacho guia : pendientes) {
            Long choferId = choferes.get(choferIndex);
            
            guia.setChoferId(choferId);
            guia.setEstado("ASIGNADO");
            guia.setFechaActualizacion(LocalDateTime.now());
            logisticaRepository.save(guia);
            publicarEvento("logistica.ruta.asignada", guia.getPedidoId(), choferId, "ASIGNADO");

            choferIndex = (choferIndex + 1) % choferes.size();
        }
    }
}
