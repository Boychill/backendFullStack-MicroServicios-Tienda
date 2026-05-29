package tienda.api.notificaciones.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "notificaciones")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notificacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long receptorId; // "admin" or user email

    @Column(nullable = false)
    private String rolReceptor; // ROLE_ADMIN, ROLE_USER, ROLE_CHOFER

    @Column(nullable = false)
    private String tipo; // COMPRA_CONFIRMADA, DEVOLUCION, ESTADO_ENVIO, ALERTA_FALLO

    @Column(nullable = false, length = 500)
    private String mensaje;

    @Column(nullable = false)
    private boolean leida;

    @Column(nullable = false)
    private LocalDateTime fechaCreacion;
}
