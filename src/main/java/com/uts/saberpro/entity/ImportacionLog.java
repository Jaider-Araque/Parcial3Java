package com.uts.saberpro.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "importaciones_log")
public class ImportacionLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nombreArchivo;

    @Column(nullable = false)
    private String tipoImportacion; // RESULTADOS, ESTUDIANTES, BENEFICIOS

    @Column(nullable = false)
    private LocalDateTime fechaImportacion;

    private Integer totalRegistros;
    private Integer registrosExitosos;
    private Integer registrosConError;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoImportacion estado;

    @Column(length = 1000)
    private String observaciones;

    @ElementCollection
    @CollectionTable(name = "importacion_errores", 
                    joinColumns = @JoinColumn(name = "importacion_id"))
    private List<ErrorImportacion> errores = new ArrayList<>();

    // Constructores
    public ImportacionLog() {
        this.fechaImportacion = LocalDateTime.now();
        this.estado = EstadoImportacion.PROCESANDO;
    }

    public ImportacionLog(String nombreArchivo, String tipoImportacion) {
        this();
        this.nombreArchivo = nombreArchivo;
        this.tipoImportacion = tipoImportacion;
    }

    // Getters y Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNombreArchivo() { return nombreArchivo; }
    public void setNombreArchivo(String nombreArchivo) { this.nombreArchivo = nombreArchivo; }

    public String getTipoImportacion() { return tipoImportacion; }
    public void setTipoImportacion(String tipoImportacion) { this.tipoImportacion = tipoImportacion; }

    public LocalDateTime getFechaImportacion() { return fechaImportacion; }
    public void setFechaImportacion(LocalDateTime fechaImportacion) { this.fechaImportacion = fechaImportacion; }

    public Integer getTotalRegistros() { return totalRegistros; }
    public void setTotalRegistros(Integer totalRegistros) { this.totalRegistros = totalRegistros; }

    public Integer getRegistrosExitosos() { return registrosExitosos; }
    public void setRegistrosExitosos(Integer registrosExitosos) { this.registrosExitosos = registrosExitosos; }

    public Integer getRegistrosConError() { return registrosConError; }
    public void setRegistrosConError(Integer registrosConError) { this.registrosConError = registrosConError; }

    public EstadoImportacion getEstado() { return estado; }
    public void setEstado(EstadoImportacion estado) { this.estado = estado; }

    public String getObservaciones() { return observaciones; }
    public void setObservaciones(String observaciones) { this.observaciones = observaciones; }

    public List<ErrorImportacion> getErrores() { return errores; }
    public void setErrores(List<ErrorImportacion> errores) { this.errores = errores; }

    // 🔄 MÉTODOS DE NEGOCIO MEJORADOS
    public void agregarError(int fila, String mensaje) {
        this.errores.add(new ErrorImportacion(fila, mensaje));
    }

    public void agregarError(int fila, String mensaje, String datoProblema) {
        String errorCompleto = String.format("Fila %d: %s | Dato: %s", fila, mensaje, 
                datoProblema != null ? datoProblema : "N/A");
        this.errores.add(new ErrorImportacion(fila, errorCompleto));
    }

    public void calcularEstadisticas() {
        this.totalRegistros = (this.totalRegistros != null) ? this.totalRegistros : 0;
        this.registrosExitosos = (this.registrosExitosos != null) ? this.registrosExitosos : 0;
        this.registrosConError = this.errores.size();
        
        // Determinar estado final
        if (this.totalRegistros == 0) {
            this.estado = EstadoImportacion.FALLIDO;
        } else if (this.registrosConError == 0) {
            this.estado = EstadoImportacion.COMPLETADO;
        } else if (this.registrosExitosos > 0) {
            this.estado = EstadoImportacion.CON_ERRORES;
        } else {
            this.estado = EstadoImportacion.FALLIDO;
        }
    }

    // 🔄 MÉTODOS DE CONVENIENCIA
    public double getPorcentajeExito() {
        if (totalRegistros == null || totalRegistros == 0) {
            return 0.0;
        }
        return (double) (registrosExitosos != null ? registrosExitosos : 0) / totalRegistros * 100;
    }

    public double getPorcentajeError() {
        if (totalRegistros == null || totalRegistros == 0) {
            return 0.0;
        }
        return (double) (registrosConError != null ? registrosConError : 0) / totalRegistros * 100;
    }

    public boolean tieneErrores() {
        return registrosConError != null && registrosConError > 0;
    }

    public boolean esCompletamenteExitosa() {
        return estado == EstadoImportacion.COMPLETADO && 
               (registrosConError == null || registrosConError == 0);
    }

    public boolean esFallidaCompletamente() {
        return estado == EstadoImportacion.FALLIDO || 
               (registrosExitosos != null && registrosExitosos == 0);
    }

    public String getResumen() {
        return String.format("Archivo: %s | Estado: %s | Exitosos: %d/%d (%.1f%%)", 
                nombreArchivo, 
                estado, 
                registrosExitosos != null ? registrosExitosos : 0,
                totalRegistros != null ? totalRegistros : 0,
                getPorcentajeExito());
    }

    public String getFechaImportacionFormateada() {
        if (fechaImportacion != null) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
            return fechaImportacion.format(formatter);
        }
        return "N/A";
    }

    public String getEstadoColor() {
        switch (estado) {
            case COMPLETADO:
                return "success";
            case CON_ERRORES:
                return "warning";
            case FALLIDO:
                return "danger";
            case PROCESANDO:
                return "info";
            default:
                return "secondary";
        }
    }

    public String getEstadoIcono() {
        switch (estado) {
            case COMPLETADO:
                return "✓";
            case CON_ERRORES:
                return "⚠";
            case FALLIDO:
                return "✗";
            case PROCESANDO:
                return "⏳";
            default:
                return "?";
        }
    }

    public void marcarComoCompletado() {
        this.estado = EstadoImportacion.COMPLETADO;
    }

    public void marcarComoFallido(String motivo) {
        this.estado = EstadoImportacion.FALLIDO;
        if (motivo != null && !motivo.trim().isEmpty()) {
            this.observaciones = motivo;
        }
    }

    public void actualizarProgreso(int registrosProcesados, int exitosos) {
        this.registrosExitosos = exitosos;
        this.registrosConError = registrosProcesados - exitosos;
        
        if (registrosProcesados > 0 && registrosProcesados == exitosos) {
            this.estado = EstadoImportacion.COMPLETADO;
        } else if (exitosos > 0) {
            this.estado = EstadoImportacion.CON_ERRORES;
        }
    }

    // Enum para estado de importación
    public enum EstadoImportacion {
        PROCESANDO("Procesando"),
        COMPLETADO("Completado"),
        CON_ERRORES("Con Errores"),
        FALLIDO("Fallido");

        private final String descripcion;

        EstadoImportacion(String descripcion) {
            this.descripcion = descripcion;
        }

        public String getDescripcion() {
            return descripcion;
        }
    }

    // Clase embebida para errores
    @Embeddable
    public static class ErrorImportacion {
        private Integer fila;
        
        @Column(length = 500)
        private String mensaje;

        public ErrorImportacion() {}

        public ErrorImportacion(Integer fila, String mensaje) {
            this.fila = fila;
            this.mensaje = mensaje;
        }

        // Getters y Setters
        public Integer getFila() { return fila; }
        public void setFila(Integer fila) { this.fila = fila; }

        public String getMensaje() { return mensaje; }
        public void setMensaje(String mensaje) { this.mensaje = mensaje; }

        @Override
        public String toString() {
            return String.format("Fila %d: %s", fila, mensaje);
        }
    }

    @Override
    public String toString() {
        return "ImportacionLog{" +
                "id=" + id +
                ", nombreArchivo='" + nombreArchivo + '\'' +
                ", tipoImportacion='" + tipoImportacion + '\'' +
                ", fechaImportacion=" + fechaImportacion +
                ", totalRegistros=" + totalRegistros +
                ", registrosExitosos=" + registrosExitosos +
                ", registrosConError=" + registrosConError +
                ", estado=" + estado +
                ", porcentajeExito=" + String.format("%.1f%%", getPorcentajeExito()) +
                '}';
    }
}