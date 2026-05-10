package tienda.api.pedidos.repository;
import org.springframework.data.jpa.repository.JpaRepository;
import tienda.api.pedidos.model.Pedido;

public interface PedidoRepository extends JpaRepository<Pedido, Long> {
}
