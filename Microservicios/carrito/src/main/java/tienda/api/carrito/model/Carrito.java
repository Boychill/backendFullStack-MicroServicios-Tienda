package tienda.api.carrito.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
public class Carrito {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank(message = "El email del usuario no puede estar vacío")
    @Column(unique = true, nullable = false)
    private String usuarioEmail;
    
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, mappedBy = "carritoId")
    private List<CartItem> items = new ArrayList<>();
    
    @NotNull(message = "El total no puede ser nulo")
    private BigDecimal total = BigDecimal.ZERO;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getUsuarioEmail() { return usuarioEmail; }
    public void setUsuarioEmail(String usuarioEmail) { this.usuarioEmail = usuarioEmail; }
    public List<CartItem> getItems() { return items; }
    public void setItems(List<CartItem> items) { this.items = items; }
    public BigDecimal getTotal() { return total; }
    public void setTotal(BigDecimal total) { this.total = total; }
}
