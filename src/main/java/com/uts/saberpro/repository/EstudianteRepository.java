package com.uts.saberpro.repository;

import com.uts.saberpro.entity.Estudiante;
import com.uts.saberpro.entity.TipoPrueba;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EstudianteRepository extends JpaRepository<Estudiante, String>, JpaSpecificationExecutor<Estudiante> {
    
    // Buscar por número de documento
    Optional<Estudiante> findByNumeroDocumento(String numeroDocumento);

    
    // Buscar por programa académico
    List<Estudiante> findByProgramaAcademico(String programaAcademico);
    
    // ✅ CORREGIDO: Usar el enum externo TipoPrueba
    List<Estudiante> findByTipoPrueba(TipoPrueba tipoPrueba);
    
    // Buscar por semestre
    List<Estudiante> findBySemestre(Integer semestre);
    
    // ✅ CORREGIDO: Usar el enum externo TipoPrueba
    // Buscar por programa y tipo de prueba
    @Query("SELECT e FROM Estudiante e WHERE e.programaAcademico = :programa AND e.tipoPrueba = :tipoPrueba")
    List<Estudiante> findByProgramaAndTipoPrueba(@Param("programa") String programa, 
                                                @Param("tipoPrueba") TipoPrueba tipoPrueba);
    
    // Verificar si existe por documento
    boolean existsByNumeroDocumento(String numeroDocumento);
    
    // Buscar por email
    Optional<Estudiante> findByEmail(String email);
    
    // Contar estudiantes por programa
    @Query("SELECT COUNT(e) FROM Estudiante e WHERE e.programaAcademico = :programa")
    Long countByProgramaAcademico(@Param("programa") String programa);
    
    // ✅ AGREGAR: Método para contar estudiantes por tipo de prueba
    @Query("SELECT COUNT(e) FROM Estudiante e WHERE e.tipoPrueba = :tipoPrueba")
    Long countByTipoPrueba(@Param("tipoPrueba") TipoPrueba tipoPrueba);
    
    // ✅ AGREGAR: Método para buscar estudiantes activos (con email verificado)
    @Query("SELECT e FROM Estudiante e WHERE e.email IS NOT NULL AND e.email != ''")
    List<Estudiante> findEstudiantesActivos();
    
    // ✅ AGREGAR: Método para buscar por nombre o apellido
    @Query("SELECT e FROM Estudiante e WHERE LOWER(e.nombres) LIKE LOWER(CONCAT('%', :nombre, '%')) OR LOWER(e.apellidos) LIKE LOWER(CONCAT('%', :nombre, '%'))")
    List<Estudiante> findByNombreOrApellidoContainingIgnoreCase(@Param("nombre") String nombre);
    
 // En EstudianteRepository
    @Query("SELECT DISTINCT e.programaAcademico FROM Estudiante e WHERE e.programaAcademico IS NOT NULL")
    List<String> findDistinctProgramaAcademico();
    
 // En EstudianteRepository.java - AGREGAR ESTE MÉTODO
    @Query("SELECT e FROM Estudiante e WHERE e.usuario.username = :username")
    Optional<Estudiante> findByUsuarioUsername(@Param("username") String username);


}