package com.uts.saberpro.repository;

import com.uts.saberpro.entity.ImportacionLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ImportacionLogRepository extends JpaRepository<ImportacionLog, Long> {

    /**
     * Obtiene todas las importaciones ordenadas por fecha descendente
     */
    List<ImportacionLog> findAllByOrderByFechaImportacionDesc();

    /**
     * Obtiene importaciones por tipo, ordenadas por fecha descendente
     */
    List<ImportacionLog> findByTipoImportacionOrderByFechaImportacionDesc(String tipoImportacion);

    /**
     * Obtiene importaciones anteriores a una fecha específica (para mantenimiento)
     */
    List<ImportacionLog> findByFechaImportacionBefore(LocalDateTime fecha);

    /**
     * Obtiene importaciones por estado
     */
    List<ImportacionLog> findByEstado(ImportacionLog.EstadoImportacion estado);

    /**
     * Obtiene importaciones por tipo y estado
     */
    List<ImportacionLog> findByTipoImportacionAndEstado(String tipoImportacion, ImportacionLog.EstadoImportacion estado);

    /**
     * Busca importaciones por nombre de archivo (búsqueda parcial)
     */
    List<ImportacionLog> findByNombreArchivoContainingIgnoreCase(String nombreArchivo);

    /**
     * Obtiene la última importación de un tipo específico
     */
    Optional<ImportacionLog> findTopByTipoImportacionOrderByFechaImportacionDesc(String tipoImportacion);

    /**
     * Cuenta el número de importaciones por estado
     */
    long countByEstado(ImportacionLog.EstadoImportacion estado);

    /**
     * Cuenta el número de importaciones por tipo
     */
    long countByTipoImportacion(String tipoImportacion);

    /**
     * Obtiene importaciones dentro de un rango de fechas
     */
    List<ImportacionLog> findByFechaImportacionBetween(LocalDateTime fechaInicio, LocalDateTime fechaFin);

    /**
     * Obtiene importaciones con errores (estado CON_ERRORES o FALLIDO)
     */
    @Query("SELECT il FROM ImportacionLog il WHERE il.estado IN (com.uts.saberpro.entity.ImportacionLog$EstadoImportacion.CON_ERRORES, com.uts.saberpro.entity.ImportacionLog$EstadoImportacion.FALLIDO) ORDER BY il.fechaImportacion DESC")
    List<ImportacionLog> findImportacionesConErrores();

    /**
     * Obtiene estadísticas de importaciones por tipo
     */
    @Query("SELECT il.tipoImportacion, COUNT(il), SUM(il.totalRegistros), SUM(il.registrosExitosos) " +
           "FROM ImportacionLog il " +
           "GROUP BY il.tipoImportacion")
    List<Object[]> obtenerEstadisticasPorTipo();

    /**
     * Obtiene las últimas N importaciones
     */
    List<ImportacionLog> findTop10ByOrderByFechaImportacionDesc();

    /**
     * Verifica si existe una importación con el mismo nombre de archivo
     */
    boolean existsByNombreArchivo(String nombreArchivo);

    /**
     * Obtiene importaciones con más de un número específico de errores
     */
    List<ImportacionLog> findByRegistrosConErrorGreaterThan(Integer numeroErrores);

    /**
     * Obtiene importaciones exitosas (sin errores)
     */
    @Query("SELECT il FROM ImportacionLog il WHERE il.estado = com.uts.saberpro.entity.ImportacionLog$EstadoImportacion.COMPLETADO AND il.registrosConError = 0")
    List<ImportacionLog> findImportacionesExitosas();
}