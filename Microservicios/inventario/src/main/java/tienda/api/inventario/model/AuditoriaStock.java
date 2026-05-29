package tienda.api.inventario.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "auditoria_stock")
public class AuditoriaStock {
    
    public enum TipoMovimiento {
        INGRESO,
        EGRESO
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long productoId;

    private Long bodegaId; // Opcional, dependiendo de si es general o especifico

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoMovimiento tipoMovimiento;

    @Column(nullable = false)
    private Integer cantidadAfectada;

    @Column(nullable = false)
    private LocalDateTime fechaMovimiento;

    private String motivoReferencia;
    
    private String responsableId;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getProductoId() { return productoId; }
    public void setProductoId(Long productoId) { this.productoId = productoId; }
    public Long getBodegaId() { return bodegaId; }
    public void setBodegaId(Long bodegaId) { this.bodegaId = bodegaId; }
    public TipoMovimiento getTipoMovimiento() { return tipoMovimiento; }
    public void setTipoMovimiento(TipoMovimiento tipoMovimiento) { this.tipoMovimiento = tipoMovimiento; }
    public Integer getCantidadAfectada() { return cantidadAfectada; }
    public void setCantidadAfectada(Integer cantidadAfectada) { this.cantidadAfectada = cantidadAfectada; }
    public LocalDateTime getFechaMovimiento() { return fechaMovimiento; }
    public void setFechaMovimiento(LocalDateTime fechaMovimiento) { this.fechaMovimiento = fechaMovimiento; }
    public String getMotivoReferencia() { return motivoReferencia; }
    public void setMotivoReferencia(String motivoReferencia) { this.motivoReferencia = motivoReferencia; }
    public String getResponsableId() { return responsableId; }
    public void setResponsableId(String responsableId) { this.responsableId = responsableId; }
}
