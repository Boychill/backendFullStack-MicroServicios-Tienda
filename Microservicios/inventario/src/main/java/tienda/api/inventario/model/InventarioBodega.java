package tienda.api.inventario.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

@Entity
public class InventarioBodega {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "El ID de la bodega es requerido")
    @Column(nullable = false)
    private Long bodegaId;

    @NotNull(message = "El ID del producto es requerido")
    @Column(nullable = false)
    private Long productoId;

    @Min(value = 0, message = "La cantidad disponible no puede ser negativa")
    private Integer cantidadDisponible;
    
    @Min(value = 0, message = "La cantidad reservada no puede ser negativa")
    private Integer cantidadReservada;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getBodegaId() { return bodegaId; }
    public void setBodegaId(Long bodegaId) { this.bodegaId = bodegaId; }
    public Long getProductoId() { return productoId; }
    public void setProductoId(Long productoId) { this.productoId = productoId; }
    public Integer getCantidadDisponible() { return cantidadDisponible; }
    public void setCantidadDisponible(Integer cantidadDisponible) { this.cantidadDisponible = cantidadDisponible; }
    public Integer getCantidadReservada() { return cantidadReservada; }
    public void setCantidadReservada(Integer cantidadReservada) { this.cantidadReservada = cantidadReservada; }
}
