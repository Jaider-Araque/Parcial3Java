package com.uts.saberpro.repository;

import com.uts.saberpro.entity.ProgramaAcademico;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProgramaRepository extends JpaRepository<ProgramaAcademico, Long> {
    
    Optional<ProgramaAcademico> findByCodigo(String codigo);
    
    List<ProgramaAcademico> findByFacultad(String facultad);
    
    List<ProgramaAcademico> findByActivoTrue();
}