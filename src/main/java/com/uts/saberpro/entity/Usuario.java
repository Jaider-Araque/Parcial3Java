package com.uts.saberpro.entity;

import jakarta.persistence.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import java.util.Collection;
import java.util.Collections;

@Entity
@Table(name = "usuarios")
public class Usuario implements UserDetails {
    
    @Id
    private String username; // Ahora se generará automáticamente desde el email
    
    @Column(nullable = false)
    private String password;
    
    @Column(nullable = false)
    private String nombres;
    
    @Column(nullable = false)
    private String apellidos;
    
    @Column(nullable = false, unique = true)
    private String email;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RolUsuario rol;
    
    @Column(nullable = false)
    private Boolean activo = true;
    
    @Column(name = "password_temporal")
    private Boolean passwordTemporal = true;

    
    // Relación opcional con Estudiante (solo para usuarios con rol ESTUDIANTE)
    @OneToOne
    @JoinColumn(name = "estudiante_id")
    private Estudiante estudiante;
    
    // ENUM para roles del sistema
    public enum RolUsuario {
        ADMIN,
        COORDINACION,
        ESTUDIANTE
    }
    
    // Constructores
    public Usuario() {}
    
    public Usuario(String username, String password, String nombres, 
                  String apellidos, String email, RolUsuario rol) {
        this.username = username;
        this.password = password;
        this.nombres = nombres;
        this.apellidos = apellidos;
        this.email = email;
        this.rol = rol;
    }
    
    // 🔄 NUEVO: Constructor que genera username automáticamente desde email
    public Usuario(String password, String nombres, String apellidos, 
                  String email, RolUsuario rol) {
        this.password = password;
        this.nombres = nombres;
        this.apellidos = apellidos;
        this.setEmail(email); // Usar setter para generar username automáticamente
        this.rol = rol;
    }
    
    // 🔄 NUEVO: Constructor que incluye passwordTemporal
    public Usuario(String password, String nombres, String apellidos, 
                  String email, RolUsuario rol, Boolean passwordTemporal) {
        this.password = password;
        this.nombres = nombres;
        this.apellidos = apellidos;
        this.setEmail(email);
        this.rol = rol;
        this.passwordTemporal = passwordTemporal;
    }
    
    // 🔄 NUEVO: Método para generar username desde email
    public void generarUsernameDesdeEmail() {
        if (this.email != null && this.email.contains("@")) {
            this.username = this.email.substring(0, this.email.indexOf('@'));
        }
    }
    
    // 🔄 NUEVO: Método para establecer email y generar username automáticamente
    public void setEmailYUsername(String email) {
        this.email = email;
        this.generarUsernameDesdeEmail();
    }
    
    // 🔄 ACTUALIZADO: Setter de email que genera username automáticamente si está vacío
    public void setEmail(String email) {
        this.email = email;
        // Si el username está vacío o es null, generarlo desde el email
        if (this.username == null || this.username.trim().isEmpty()) {
            this.generarUsernameDesdeEmail();
        }
    }
    
    // Getters y Setters para passwordTemporal
    public Boolean getPasswordTemporal() { 
        return passwordTemporal; 
    }
    
    public void setPasswordTemporal(Boolean passwordTemporal) { 
        this.passwordTemporal = passwordTemporal; 
    }
    
    // Implementación de UserDetails para Spring Security
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + rol.name()));
    }
    
    @Override
    public String getPassword() {
        return password;
    }
    
    @Override
    public String getUsername() {
        return username;
    }
    
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }
    
    @Override
    public boolean isAccountNonLocked() {
        return true;
    }
    
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }
    
    @Override
    public boolean isEnabled() {
        return activo;
    }
    
    // Getters y Setters
    public void setUsername(String username) { 
        this.username = username; 
    }
    
    public void setPassword(String password) { 
        this.password = password; 
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
    
    // 🔄 El setter de email ya está actualizado arriba
    
    public RolUsuario getRol() { 
        return rol; 
    }
    
    public void setRol(RolUsuario rol) { 
        this.rol = rol; 
    }
    
    public Boolean getActivo() { 
        return activo; 
    }
    
    public void setActivo(Boolean activo) { 
        this.activo = activo; 
    }
    
    public Estudiante getEstudiante() { 
        return estudiante; 
    }
    
    public void setEstudiante(Estudiante estudiante) { 
        this.estudiante = estudiante; 
    }
    
    @Override
    public String toString() {
        return "Usuario{" +
                "username='" + username + '\'' +
                ", nombres='" + nombres + '\'' +
                ", apellidos='" + apellidos + '\'' +
                ", email='" + email + '\'' +
                ", rol=" + rol +
                ", activo=" + activo +
                ", passwordTemporal=" + passwordTemporal +
                '}';
    }
}