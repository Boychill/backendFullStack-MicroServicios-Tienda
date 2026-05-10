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
    private PedidoClient pedidoClient;

    public VentasAnalyticsDto calcularVentas() {
        List<PedidoDto> pedidos = pedidoClient.obtenerTodosLosPedidos();
        
        int totalPedidos = pedidos.size();
        BigDecimal ingresoNeto = pedidos.stream()
                .map(PedidoDto::getTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
                
        Map<String, BigDecimal> ventasPorFecha = pedidos.stream()
                .filter(p -> p.getFechaCreacion() != null)
                .collect(Collectors.toMap(
                        p -> p.getFechaCreacion().toLocalDate().toString(),
                        PedidoDto::getTotal,
                        BigDecimal::add
                ));
                
        return new VentasAnalyticsDto(totalPedidos, ingresoNeto, ventasPorFecha);
    }
}
