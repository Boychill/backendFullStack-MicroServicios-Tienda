package tienda.api.pedidos.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
public class Pedido {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @NotNull(message = "El ID del usuario es requerido")
    private Long usuarioId;
    
    @NotNull(message = "El total del pedido no puede ser nulo")
    @DecimalMin(value = "0.0", inclusive = false, message = "El total debe ser mayor a cero")
    private BigDecimal total;
    
    @NotBlank(message = "El estado del pedido no puede estar vacío")
    private String estado; 
    private String transaccionId;
    private LocalDateTime fechaCreacion;
    private String direccionCompleta;

    @OneToMany(mappedBy = "pedido", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private java.util.List<ItemPedido> items = new java.util.ArrayList<>();

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getUsuarioId() { return usuarioId; }
    public void setUsuarioId(Long usuarioId) { this.usuarioId = usuarioId; }
    public BigDecimal getTotal() { return total; }
    public void setTotal(BigDecimal total) { this.total = total; }
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
    public String getTransaccionId() { return transaccionId; }
    public void setTransaccionId(String transaccionId) { this.transaccionId = transaccionId; }
    public String getDireccionCompleta() { return direccionCompleta; }
    public void setDireccionCompleta(String direccionCompleta) { this.direccionCompleta = direccionCompleta; }
    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(LocalDateTime fechaCreacion) { this.fechaCreacion = fechaCreacion; }
    public java.util.List<ItemPedido> getItems() { return items; }
    public void setItems(java.util.List<ItemPedido> items) { this.items = items; }
}
