package tienda.api.reportes.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tienda.api.reportes.client.PedidoClient;
import tienda.api.reportes.dto.PedidoDto;
import tienda.api.reportes.dto.VentasAnalyticsDto;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class AnalyticsService {

    @Autowired
    private tienda.api.reportes.client.PedidoClient pedidoClient;

    @Autowired
    private tienda.api.reportes.client.AuthClient authClient;

    public VentasAnalyticsDto calcularVentas() {
        List<PedidoDto> pedidos = pedidoClient.obtenerTodosLosPedidos();
        
        int totalPedidos = pedidos.size();
        BigDecimal ingresoNeto = pedidos.stream().filter(p -> "PAGADO".equals(p.getEstado()))
                .map(PedidoDto::getTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
                
        Map<String, BigDecimal> ventasPorFecha = pedidos.stream().filter(p -> "PAGADO".equals(p.getEstado()))
                .filter(p -> p.getFechaCreacion() != null)
                .collect(Collectors.toMap(
                        p -> p.getFechaCreacion().toLocalDate().toString(),
                        PedidoDto::getTotal,
                        BigDecimal::add
                ));
        
        Long totalUsuarios = 0L;
        try {
            Map<String, Long> userCount = authClient.obtenerTotalUsuarios();
            totalUsuarios = userCount.get("total");
        } catch (Exception e) {
            System.err.println("No se pudo obtener el total de usuarios: " + e.getMessage());
        }

        // Calcular Producto Más Vendido
        Long productoMasVendidoId = -1L;
        try {
            Map<Long, Integer> ventasPorProducto = new java.util.HashMap<>();
            for (PedidoDto p : pedidos) {
                if ("PAGADO".equals(p.getEstado()) && p.getItems() != null) {
                    for (tienda.api.reportes.dto.ItemPedidoDto item : p.getItems()) {
                        ventasPorProducto.put(item.getProductoId(), 
                            ventasPorProducto.getOrDefault(item.getProductoId(), 0) + item.getCantidad());
                    }
                }
            }
            if (!ventasPorProducto.isEmpty()) {
                productoMasVendidoId = java.util.Collections.max(ventasPorProducto.entrySet(), Map.Entry.comparingByValue()).getKey();
            }
        } catch (Exception e) {
            System.err.println("Error calculando el producto mas vendido: " + e.getMessage());
        }
                
        return new VentasAnalyticsDto(totalPedidos, ingresoNeto, ventasPorFecha, productoMasVendidoId, totalUsuarios);
    }
}
