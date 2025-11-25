package com.uts.saberpro.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Entity
@Table(name = "resultados_prueba")
public class ResultadoPrueba {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "estudiante_id", nullable = false)
    private Estudiante estudiante;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoPrueba tipoPrueba;
    
    @Column(nullable = false)
    private Integer puntajeGlobal;
    
    @Column(nullable = false)
    private Integer anioPrueba;
    
    @Column(nullable = false)
    private Integer periodo;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoPrueba estado;
    
    private String observaciones;
    
    @Column(nullable = false)
    private LocalDateTime fechaRegistro;
    
    private LocalDateTime fechaActualizacion;
    
    @ElementCollection
    @CollectionTable(name = "competencias_detalle", 
                    joinColumns = @JoinColumn(name = "resultado_id"))
    @MapKeyColumn(name = "competencia")
    @Column(name = "puntaje")
    private Map<String, Integer> competencias = new HashMap<>();
    
    // Constructores
    public ResultadoPrueba() {
        this.fechaRegistro = LocalDateTime.now();
        this.estado = EstadoPrueba.PENDIENTE;
    }
    
    public ResultadoPrueba(Estudiante estudiante, TipoPrueba tipoPrueba, Integer puntajeGlobal, 
                          Integer anioPrueba, Integer periodo) {
        this();
        this.estudiante = estudiante;
        this.tipoPrueba = tipoPrueba;
        this.puntajeGlobal = puntajeGlobal;
        this.anioPrueba = anioPrueba;
        this.periodo = periodo;
    }
    
    // Métodos del ciclo de vida
    @PrePersist
    protected void onCreate() {
        if (fechaRegistro == null) {
            fechaRegistro = LocalDateTime.now();
        }
        if (estado == null) {
            estado = EstadoPrueba.PENDIENTE;
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        fechaActualizacion = LocalDateTime.now();
    }
    
    // Getters y Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public Estudiante getEstudiante() { return estudiante; }
    public void setEstudiante(Estudiante estudiante) { this.estudiante = estudiante; }
    
    public TipoPrueba getTipoPrueba() { return tipoPrueba; }
    public void setTipoPrueba(TipoPrueba tipoPrueba) { this.tipoPrueba = tipoPrueba; }
    
    public Integer getPuntajeGlobal() { return puntajeGlobal; }
    public void setPuntajeGlobal(Integer puntajeGlobal) { this.puntajeGlobal = puntajeGlobal; }
    
    public Integer getAnioPrueba() { return anioPrueba; }
    public void setAnioPrueba(Integer anioPrueba) { this.anioPrueba = anioPrueba; }
    
    public Integer getPeriodo() { return periodo; }
    public void setPeriodo(Integer periodo) { this.periodo = periodo; }
    
    public EstadoPrueba getEstado() { return estado; }
    public void setEstado(EstadoPrueba estado) { this.estado = estado; }
    
    public String getObservaciones() { return observaciones; }
    public void setObservaciones(String observaciones) { this.observaciones = observaciones; }
    
    public LocalDateTime getFechaRegistro() { return fechaRegistro; }
    public void setFechaRegistro(LocalDateTime fechaRegistro) { this.fechaRegistro = fechaRegistro; }
    
    public LocalDateTime getFechaActualizacion() { return fechaActualizacion; }
    public void setFechaActualizacion(LocalDateTime fechaActualizacion) { this.fechaActualizacion = fechaActualizacion; }
    
    public Map<String, Integer> getCompetencias() { return competencias; }
    public void setCompetencias(Map<String, Integer> competencias) { this.competencias = competencias; }
    
    // Métodos de negocio
    public void agregarCompetencia(String competencia, Integer puntaje) {
        if (this.competencias == null) {
            this.competencias = new HashMap<>();
        }
        this.competencias.put(competencia, puntaje);
    }
    
    public void anularPrueba(String motivo) {
        this.estado = EstadoPrueba.ANULADA;
        this.observaciones = "PRUEBA ANULADA: " + (motivo != null ? motivo : "Sin motivo especificado");
        this.fechaActualizacion = LocalDateTime.now();
    }
    
    public void aprobarPrueba() {
        this.estado = EstadoPrueba.APROBADO;
        this.fechaActualizacion = LocalDateTime.now();
    }
    
    public void reprobarPrueba() {
        this.estado = EstadoPrueba.REPROBADO;
        this.fechaActualizacion = LocalDateTime.now();
    }
    
    public boolean debeRepetirPrueba() {
        return this.estado == EstadoPrueba.REPROBADO || this.estado == EstadoPrueba.ANULADA;
    }
    
    public boolean estaAprobada() {
        return this.estado == EstadoPrueba.APROBADO;
    }
    
    public boolean estaPendiente() {
        return this.estado == EstadoPrueba.PENDIENTE;
    }
    
    // Método para calcular puntaje total de competencias
    public Integer calcularPuntajeTotalCompetencias() {
        if (competencias == null || competencias.isEmpty()) {
            return 0;
        }
        return competencias.values().stream()
                .mapToInt(Integer::intValue)
                .sum();
    }
    
    // Método para validar consistencia de datos
    public boolean esValido() {
        return estudiante != null && 
               tipoPrueba != null && 
               puntajeGlobal != null && 
               puntajeGlobal >= 0 && 
               anioPrueba != null && 
               anioPrueba > 2000 && 
               periodo != null && 
               periodo >= 1 && 
               periodo <= 2;
    }
    
    @Override
    public String toString() {
        return "ResultadoPrueba{" +
                "id=" + id +
                ", estudiante=" + (estudiante != null ? estudiante.getNumeroDocumento() : "null") +
                ", tipoPrueba=" + tipoPrueba +
                ", puntajeGlobal=" + puntajeGlobal +
                ", estado=" + estado +
                ", anioPrueba=" + anioPrueba +
                ", periodo=" + periodo +
                '}';
    }
}