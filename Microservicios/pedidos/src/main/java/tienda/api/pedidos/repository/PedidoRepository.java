package tienda.api.pedidos.repository;
import org.springframework.data.jpa.repository.JpaRepository;
import tienda.api.pedidos.model.Pedido;
import java.util.List;

public interface PedidoRepository extends JpaRepository<Pedido, Long> {
    List<Pedido> findByUsuarioIdOrderByFechaCreacionDesc(Long usuarioId);
}
