package tienda.api.auth.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Entity
public class Direccion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El email del usuario es requerido")
    @Column(nullable = false)
    private String usuarioEmail;

    private String alias;

    @NotBlank(message = "La dirección escrita no puede estar vacía")
    private String direccionEscrita;
    
    @NotNull(message = "La latitud es requerida")
    private Double latitud;

    @NotNull(message = "La longitud es requerida")
    private Double longitud;
    
    private Boolean esPrincipal = false;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getUsuarioEmail() { return usuarioEmail; }
    public void setUsuarioEmail(String usuarioEmail) { this.usuarioEmail = usuarioEmail; }
    public String getAlias() { return alias; }
    public void setAlias(String alias) { this.alias = alias; }
    public String getDireccionEscrita() { return direccionEscrita; }
    public void setDireccionEscrita(String direccionEscrita) { this.direccionEscrita = direccionEscrita; }
    public Double getLatitud() { return latitud; }
    public void setLatitud(Double latitud) { this.latitud = latitud; }
    public Double getLongitud() { return longitud; }
    public void setLongitud(Double longitud) { this.longitud = longitud; }
    public Boolean getEsPrincipal() { return esPrincipal; }
    public void setEsPrincipal(Boolean esPrincipal) { this.esPrincipal = esPrincipal; }
}
