package tienda.api.inventario.service;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class StockCompensationListener {

    @Autowired
    private InventarioService inventarioService;

    @RabbitListener(queues = "pedidos.compensacion.stock.queue")
    public void handleStockCompensation(Map<String, Object> event) {
        try {
            System.out.println("====== [INVENTARIO SAGA] Recibido evento de compensacion de stock para orden ID: " + event.get("ordenId") + " ======");
            inventarioService.revertirLoteAsincrono(event);
        } catch (Exception e) {
            System.err.println("Error procesando compensacion asincrona de inventario: " + e.getMessage());
        }
    }
}
