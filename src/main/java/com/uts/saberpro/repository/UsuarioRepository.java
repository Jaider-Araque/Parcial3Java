package com.uts.saberpro.repository;

import com.uts.saberpro.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, String> {
    
    // Buscar por email
    Optional<Usuario> findByEmail(String email);
    
    // Buscar por rol
    List<Usuario> findByRol(Usuario.RolUsuario rol);
    
    // Buscar usuarios activos
    List<Usuario> findByActivoTrue();
    
    // Verificar si existe por email
    boolean existsByEmail(String email);
    
    // ✅ CORREGIDO: Solo un método para buscar por documento de estudiante
    Optional<Usuario> findByEstudiante_NumeroDocumento(String numeroDocumento);
    
}