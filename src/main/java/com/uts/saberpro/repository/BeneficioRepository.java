package com.uts.saberpro.repository;

import com.uts.saberpro.entity.Beneficio;
import com.uts.saberpro.entity.Estudiante;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BeneficioRepository extends JpaRepository<Beneficio, Long> {
    
    // ✅ MÉTODOS EXISTENTES
    long countByActivoTrue();
    List<Beneficio> findByEstudiante(Estudiante estudiante);
    List<Beneficio> findByEstudianteAndActivoTrue(Estudiante estudiante);
    List<Beneficio> findByTipoBeneficio(Beneficio.TipoBeneficio tipoBeneficio);
    List<Beneficio> findByEstudianteNumeroDocumento(String numeroDocumento);
    Page<Beneficio> findByEstudianteNumeroDocumento(String numeroDocumento, Pageable pageable);
    List<Beneficio> findByActivoTrue();
    Page<Beneficio> findByActivoTrue(Pageable pageable);
    List<Beneficio> findByEstudianteNumeroDocumentoAndActivoTrue(String numeroDocumento);
    Page<Beneficio> findByEstudianteNumeroDocumentoAndActivoTrue(String numeroDocumento, Pageable pageable);
    Page<Beneficio> findByTipoBeneficio(Beneficio.TipoBeneficio tipoBeneficio, Pageable pageable);
    
    // ✅ NUEVO MÉTODO - Agregar esta línea
    List<Beneficio> findByActivo(boolean activo);
    
    // Contar beneficios por tipo
    @Query("SELECT COUNT(b) FROM Beneficio b WHERE b.tipoBeneficio = :tipoBeneficio")
    Long countByTipoBeneficio(@Param("tipoBeneficio") Beneficio.TipoBeneficio tipoBeneficio);
}