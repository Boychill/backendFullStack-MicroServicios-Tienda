package tienda.api.pagos.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tienda.api.pagos.model.Pago;

public interface PagoRepository extends JpaRepository<Pago, Long> {
    Pago findByPedidoId(Long pedidoId);
}
