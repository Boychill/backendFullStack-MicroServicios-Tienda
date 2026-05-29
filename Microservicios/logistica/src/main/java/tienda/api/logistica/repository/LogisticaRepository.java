package tienda.api.logistica.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tienda.api.logistica.model.GuiaDespacho;

import java.util.List;
import java.util.Optional;

public interface LogisticaRepository extends JpaRepository<GuiaDespacho, Long> {
    List<GuiaDespacho> findByChoferId(Long choferId);
    List<GuiaDespacho> findByEstado(String estado);
    Optional<GuiaDespacho> findByPedidoId(Long pedidoId);
}
