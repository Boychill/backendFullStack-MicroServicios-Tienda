package tienda.api.logistica.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "guias_despacho")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GuiaDespacho {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private Long pedidoId;

    private Long choferId; // null until assigned

    @Column(nullable = false)
    private String estado; // PENDIENTE, ASIGNADO, EN_RUTO, ENTREGADO

    @Column(nullable = false)
    private LocalDateTime fechaCreacion;

    private LocalDateTime fechaActualizacion;
    private String direccionCompleta;
}
