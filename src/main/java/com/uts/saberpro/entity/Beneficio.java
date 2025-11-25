package com.uts.saberpro.entity;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "beneficios")
public class Beneficio {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "estudiante_id", nullable = false)
    private Estudiante estudiante;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoBeneficio tipoBeneficio;
    
    @Column(nullable = false)
    private Double notaAsignada;
    
    @Column(nullable = false)
    private Double porcentajeDescuento;
    
    @Column(nullable = false)
    private LocalDate fechaAsignacion;
    
    @Column(nullable = false)
    private Boolean activo;
    
    // ENUM para tipos de beneficio según el Acuerdo 01-009
    public enum TipoBeneficio {
        EXONERACION_TRABAJO_GRADO,
        DESCUENTO_50_DERECHOS_GRADO,
        DESCUENTO_100_DERECHOS_GRADO
    }
    
    // Constructores
    public Beneficio() {
        this.fechaAsignacion = LocalDate.now();
        this.activo = true;
    }
    
    public Beneficio(Estudiante estudiante, TipoBeneficio tipoBeneficio, 
                    Double notaAsignada, Double porcentajeDescuento) {
        this();
        this.estudiante = estudiante;
        this.tipoBeneficio = tipoBeneficio;
        this.notaAsignada = notaAsignada;
        this.porcentajeDescuento = porcentajeDescuento;
    }
    
    // Getters y Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public Estudiante getEstudiante() { return estudiante; }
    public void setEstudiante(Estudiante estudiante) { this.estudiante = estudiante; }
    
    public TipoBeneficio getTipoBeneficio() { return tipoBeneficio; }
    public void setTipoBeneficio(TipoBeneficio tipoBeneficio) { this.tipoBeneficio = tipoBeneficio; }
    
    public Double getNotaAsignada() { return notaAsignada; }
    public void setNotaAsignada(Double notaAsignada) { this.notaAsignada = notaAsignada; }
    
    public Double getPorcentajeDescuento() { return porcentajeDescuento; }
    public void setPorcentajeDescuento(Double porcentajeDescuento) { this.porcentajeDescuento = porcentajeDescuento; }
    
    public LocalDate getFechaAsignacion() { return fechaAsignacion; }
    public void setFechaAsignacion(LocalDate fechaAsignacion) { this.fechaAsignacion = fechaAsignacion; }
    
    public Boolean getActivo() { return activo; }
    public void setActivo(Boolean activo) { this.activo = activo; }
    
    @Override
    public String toString() {
        return "Beneficio{" +
                "id=" + id +
                ", estudiante=" + (estudiante != null ? estudiante.getNumeroDocumento() : "null") +
                ", tipoBeneficio=" + tipoBeneficio +
                ", notaAsignada=" + notaAsignada +
                ", porcentajeDescuento=" + porcentajeDescuento +
                ", fechaAsignacion=" + fechaAsignacion +
                ", activo=" + activo +
                '}';
    }
}