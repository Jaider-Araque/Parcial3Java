package com.uts.saberpro.repository;

import com.uts.saberpro.entity.EstadoPrueba;
import com.uts.saberpro.entity.Estudiante;
import com.uts.saberpro.entity.ResultadoPrueba;
import com.uts.saberpro.entity.TipoPrueba;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface ResultadoPruebaRepository extends JpaRepository<ResultadoPrueba, Long> {
    
    // ✅ MÉTODOS EXISTENTES
    List<ResultadoPrueba> findByEstudiante(Estudiante estudiante);
    List<ResultadoPrueba> findByEstudianteAndTipoPrueba(Estudiante estudiante, TipoPrueba tipoPrueba);
    Optional<ResultadoPrueba> findTopByEstudianteAndTipoPruebaOrderByFechaRegistroDesc(Estudiante estudiante, TipoPrueba tipoPrueba);
    List<ResultadoPrueba> findByEstado(EstadoPrueba estado);
    List<ResultadoPrueba> findByAnioPruebaAndPeriodo(Integer anioPrueba, Integer periodo);
    long countByEstudianteAndTipoPrueba(Estudiante estudiante, TipoPrueba tipoPrueba);
    List<ResultadoPrueba> findByTipoPrueba(TipoPrueba tipoPrueba);
    
    @Query("SELECT DISTINCT r.estudiante.numeroDocumento FROM ResultadoPrueba r")
    List<String> findDistinctEstudianteIds();
    
    @Query("SELECT r FROM ResultadoPrueba r WHERE r.estudiante.numeroDocumento = :estudianteId")
    List<ResultadoPrueba> findByEstudianteId(String estudianteId);

    // ✅ NUEVO MÉTODO - Agrega esta línea
    Optional<ResultadoPrueba> findByEstudianteAndTipoPruebaAndAnioPrueba(
        Estudiante estudiante, 
        TipoPrueba tipoPrueba, 
        Integer anioPrueba);

    // ✅ NUEVO MÉTODO PARA EL DASHBOARD - Agrega esta línea
    @Query("SELECT COUNT(r) FROM ResultadoPrueba r WHERE r.tipoPrueba = :tipoPrueba")
    Long countByTipoPrueba(@Param("tipoPrueba") TipoPrueba tipoPrueba);

    // ✅ NUEVO MÉTODO PARA PAGINACIÓN - Agrega estas líneas
    @Query("SELECT r FROM ResultadoPrueba r WHERE r.estudiante.numeroDocumento = :documento")
    Page<ResultadoPrueba> findByEstudianteNumeroDocumento(@Param("documento") String documento, Pageable pageable);

    // Método para obtener todos los resultados con paginación
    Page<ResultadoPrueba> findAll(Pageable pageable);
    
    // En ResultadoPruebaRepository  
    @Query("SELECT DISTINCT r.anioPrueba FROM ResultadoPrueba r ORDER BY r.anioPrueba DESC")
    List<Integer> findDistinctAnioPrueba();
}