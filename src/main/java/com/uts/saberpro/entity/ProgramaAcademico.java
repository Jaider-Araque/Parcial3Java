package com.uts.saberpro.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "programas_academicos")
public class ProgramaAcademico {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true)
    private String codigo;
    
    @Column(nullable = false)
    private String nombre;
    
    private String facultad;
    private Integer duracionSemestres;
    private Boolean activo = true;
    
    // Constructores
    public ProgramaAcademico() {}
    
    public ProgramaAcademico(String codigo, String nombre, String facultad, Integer duracionSemestres) {
        this.codigo = codigo;
        this.nombre = nombre;
        this.facultad = facultad;
        this.duracionSemestres = duracionSemestres;
    }
    
    // Getters y Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getCodigo() { return codigo; }
    public void setCodigo(String codigo) { this.codigo = codigo; }
    
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    
    public String getFacultad() { return facultad; }
    public void setFacultad(String facultad) { this.facultad = facultad; }
    
    public Integer getDuracionSemestres() { return duracionSemestres; }
    public void setDuracionSemestres(Integer duracionSemestres) { this.duracionSemestres = duracionSemestres; }
    
    public Boolean getActivo() { return activo; }
    public void setActivo(Boolean activo) { this.activo = activo; }
    
    @Override
    public String toString() {
        return "ProgramaAcademico{" +
                "codigo='" + codigo + '\'' +
                ", nombre='" + nombre + '\'' +
                ", facultad='" + facultad + '\'' +
                ", duracionSemestres=" + duracionSemestres +
                '}';
    }
}