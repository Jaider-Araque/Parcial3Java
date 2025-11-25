package com.uts.saberpro.entity;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "estudiantes")
public class Estudiante {
    
    @Id
    private String numeroDocumento;
    
    @Column(nullable = false)
    private String nombres;
    
    @Column(nullable = false)
    private String apellidos;
    
    @Column(nullable = false, unique = true)
    private String email;
    
    // 🔥 NUEVO CAMPO: Teléfono
    @Column(name = "numero_telefono")
    private String numeroTelefono;
    
    @Column(nullable = false)
    private String programaAcademico;
    
    @Column(nullable = false)
    private Integer semestre;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoPrueba tipoPrueba;
    
    // 🔄 Relación bidireccional con Usuario
    @OneToOne(mappedBy = "estudiante", fetch = FetchType.LAZY)
    private Usuario usuario;
    
    @OneToMany(mappedBy = "estudiante", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ResultadoPrueba> resultados = new ArrayList<>();
    
    @OneToMany(mappedBy = "estudiante", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Beneficio> beneficios = new ArrayList<>();
    
    // Constructores
    public Estudiante() {}
    
    public Estudiante(String numeroDocumento, String nombres, String apellidos, 
                     String email, String numeroTelefono, String programaAcademico, 
                     Integer semestre, TipoPrueba tipoPrueba) {
        this.numeroDocumento = numeroDocumento;
        this.nombres = nombres;
        this.apellidos = apellidos;
        this.email = email;
        this.numeroTelefono = numeroTelefono;
        this.programaAcademico = programaAcademico;
        this.semestre = semestre;
        this.tipoPrueba = tipoPrueba;
    }
    
    // Getters y Setters
    public String getNumeroDocumento() { 
        return numeroDocumento; 
    }
    
    public void setNumeroDocumento(String numeroDocumento) { 
        this.numeroDocumento = numeroDocumento; 
    }
    
    public String getNombres() { 
        return nombres; 
    }
    
    public void setNombres(String nombres) { 
        this.nombres = nombres; 
    }
    
    public String getApellidos() { 
        return apellidos; 
    }
    
    public void setApellidos(String apellidos) { 
        this.apellidos = apellidos; 
    }
    
    public String getEmail() { 
        return email; 
    }
    
    public void setEmail(String email) { 
        this.email = email; 
    }
    
    // 🔥 NUEVO: Getter y Setter para teléfono
    public String getNumeroTelefono() { 
        return numeroTelefono; 
    }
    
    public void setNumeroTelefono(String numeroTelefono) { 
        this.numeroTelefono = numeroTelefono; 
    }
    
    public String getProgramaAcademico() { 
        return programaAcademico; 
    }
    
    public void setProgramaAcademico(String programaAcademico) { 
        this.programaAcademico = programaAcademico; 
    }
    
    public Integer getSemestre() { 
        return semestre; 
    }
    
    public void setSemestre(Integer semestre) { 
        this.semestre = semestre; 
    }
    
    public TipoPrueba getTipoPrueba() { 
        return tipoPrueba; 
    }
    
    public void setTipoPrueba(TipoPrueba tipoPrueba) { 
        this.tipoPrueba = tipoPrueba; 
    }
    
    public Usuario getUsuario() { 
        return usuario; 
    }
    
    public void setUsuario(Usuario usuario) { 
        this.usuario = usuario; 
    }
    
    public List<ResultadoPrueba> getResultados() { 
        return resultados; 
    }
    
    public void setResultados(List<ResultadoPrueba> resultados) { 
        this.resultados = resultados; 
    }
    
    public List<Beneficio> getBeneficios() { 
        return beneficios; 
    }
    
    public void setBeneficios(List<Beneficio> beneficios) { 
        this.beneficios = beneficios; 
    }
    
    // Métodos de conveniencia
    public String getNombreCompleto() {
        return nombres + " " + apellidos;
    }
    
    public boolean tieneUsuarioAsociado() {
        return usuario != null;
    }
    
    public long getTotalResultados() {
        return resultados != null ? resultados.size() : 0;
    }
    
    public long getTotalBeneficios() {
        return beneficios != null ? beneficios.size() : 0;
    }
    
    @Override
    public String toString() {
        return "Estudiante{" +
                "numeroDocumento='" + numeroDocumento + '\'' +
                ", nombres='" + nombres + '\'' +
                ", apellidos='" + apellidos + '\'' +
                ", email='" + email + '\'' +
                ", numeroTelefono='" + numeroTelefono + '\'' +
                ", programaAcademico='" + programaAcademico + '\'' +
                ", semestre=" + semestre +
                ", tipoPrueba=" + tipoPrueba +
                ", tieneUsuario=" + (usuario != null) +
                '}';
    }
}