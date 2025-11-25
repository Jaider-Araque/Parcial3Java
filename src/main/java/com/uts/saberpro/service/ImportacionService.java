package com.uts.saberpro.service;

import com.uts.saberpro.entity.ImportacionLog;
import com.uts.saberpro.entity.ResultadoPrueba;
import com.uts.saberpro.entity.TipoPrueba;
import com.uts.saberpro.repository.ImportacionLogRepository;
import com.uts.saberpro.repository.ResultadoPruebaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ImportacionService {

    private static final Logger logger = LoggerFactory.getLogger(ImportacionService.class);
    
    private final ExcelService excelService;
    private final ResultadoPruebaRepository resultadoPruebaRepository;
    private final ImportacionLogRepository importacionLogRepository;

    public ImportacionService(ExcelService excelService,
                            ResultadoPruebaRepository resultadoPruebaRepository,
                            ImportacionLogRepository importacionLogRepository) {
        this.excelService = excelService;
        this.resultadoPruebaRepository = resultadoPruebaRepository;
        this.importacionLogRepository = importacionLogRepository;
    }

    @Transactional
    public ImportacionLog importarResultados(MultipartFile archivo) {
        ImportacionLog log = new ImportacionLog(archivo.getOriginalFilename(), "RESULTADOS");
        
        try {
            logger.info("Iniciando importación de resultados desde archivo: {}", archivo.getOriginalFilename());
            
            // Validar archivo
            if (archivo.isEmpty()) {
                throw new IllegalArgumentException("El archivo está vacío");
            }

            if (!archivo.getOriginalFilename().toLowerCase().endsWith(".xlsx") && 
                !archivo.getOriginalFilename().toLowerCase().endsWith(".xls")) {
                throw new IllegalArgumentException("Solo se permiten archivos Excel (.xlsx, .xls)");
            }

            // ✅ CORREGIDO: Ahora procesarArchivoResultados retorna Map<String, Object>
            Map<String, Object> resultadoProcesamiento = excelService.procesarArchivoResultados(archivo, null);
            
            // Extraer los resultados y estadísticas del Map
            @SuppressWarnings("unchecked")
            List<ResultadoPrueba> resultados = (List<ResultadoPrueba>) resultadoProcesamiento.get("resultados");
            @SuppressWarnings("unchecked")
            Map<String, Object> estadisticas = (Map<String, Object>) resultadoProcesamiento.get("estadisticas");
            @SuppressWarnings("unchecked")
            List<String> errores = (List<String>) resultadoProcesamiento.get("errores");
            
            log.setTotalRegistros(resultados.size());
            
            logger.info("Archivo procesado. {} registros encontrados", resultados.size());
            
            int exitosos = 0;
            int procesados = 0;
            
            // Los resultados ya están guardados en la base de datos por ExcelService
            // Solo contamos los exitosos
            for (ResultadoPrueba resultado : resultados) {
                procesados++;
                if (resultado != null && resultado.esValido()) {
                    exitosos++;
                    
                    // Log cada 50 registros para seguimiento
                    if (exitosos % 50 == 0) {
                        logger.info("{} registros guardados exitosamente", exitosos);
                    }
                } else {
                    String mensajeError = resultado == null ? "Resultado nulo" : "Resultado inválido";
                    log.agregarError(procesados, mensajeError);
                    logger.warn("Registro {} descartado: {}", procesados, mensajeError);
                }
            }
            
            // Agregar errores del procesamiento al log
            if (errores != null && !errores.isEmpty()) {
                for (String error : errores) {
                    log.agregarError(0, error); // Usamos 0 para errores generales
                }
            }
            
            log.setRegistrosExitosos(exitosos);
            log.calcularEstadisticas();
            
            // Agregar estadísticas adicionales al log
            if (estadisticas != null) {
                log.setObservaciones("Estadísticas: " + estadisticas.toString());
            }
            
            logger.info("Importación completada. Exitosos: {}, Errores: {}, Total: {}", 
                       exitosos, log.getRegistrosConError(), resultados.size());
            
        } catch (Exception e) {
            logger.error("Error general en importación: {}", e.getMessage(), e);
            log.setEstado(ImportacionLog.EstadoImportacion.FALLIDO);
            log.setObservaciones("Error general: " + e.getMessage());
        }
        
        ImportacionLog logGuardado = importacionLogRepository.save(log);
        logger.info("Log de importación guardado con ID: {}", logGuardado.getId());
        
        return logGuardado;
    }

    // Método simplificado que usa directamente el ExcelService
    @Transactional
    public Map<String, Object> importarResultadosDirecto(MultipartFile archivo) {
        Map<String, Object> resultado = new HashMap<>();
        
        try {
            logger.info("Importación directa desde archivo: {}", archivo.getOriginalFilename());
            
            // Validar archivo
            if (archivo.isEmpty()) {
                throw new IllegalArgumentException("El archivo está vacío");
            }

            if (!archivo.getOriginalFilename().toLowerCase().endsWith(".xlsx") && 
                !archivo.getOriginalFilename().toLowerCase().endsWith(".xls")) {
                throw new IllegalArgumentException("Solo se permiten archivos Excel (.xlsx, .xls)");
            }

            // Procesar directamente con ExcelService
            Map<String, Object> resultadoProcesamiento = excelService.procesarArchivoResultados(archivo, TipoPrueba.SABER_PRO);
            
            // Crear log de importación
            ImportacionLog log = new ImportacionLog(archivo.getOriginalFilename(), "RESULTADOS_DIRECTO");
            log.setTotalRegistros((Integer) resultadoProcesamiento.get("totalProcesado"));
            log.setRegistrosExitosos((Integer) resultadoProcesamiento.get("totalProcesado"));
            log.calcularEstadisticas();
            log.setObservaciones("Importación directa completada");
            
            importacionLogRepository.save(log);
            
            resultado.put("success", true);
            resultado.put("message", "Importación completada exitosamente");
            resultado.put("log", log);
            resultado.put("procesamiento", resultadoProcesamiento);
            
            logger.info("Importación directa completada. Log ID: {}", log.getId());
            
        } catch (Exception e) {
            logger.error("Error en importación directa: {}", e.getMessage(), e);
            
            // Crear log de error
            ImportacionLog logError = new ImportacionLog(archivo.getOriginalFilename(), "RESULTADOS_DIRECTO");
            logError.setEstado(ImportacionLog.EstadoImportacion.FALLIDO);
            logError.setObservaciones("Error: " + e.getMessage());
            importacionLogRepository.save(logError);
            
            resultado.put("success", false);
            resultado.put("message", "Error en importación: " + e.getMessage());
            resultado.put("log", logError);
        }
        
        return resultado;
    }

    public List<ImportacionLog> obtenerHistorialImportaciones() {
        return importacionLogRepository.findAllByOrderByFechaImportacionDesc();
    }

    public ImportacionLog obtenerImportacionPorId(Long id) {
        return importacionLogRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Importación no encontrada con ID: " + id));
    }

    public List<ImportacionLog> obtenerImportacionesPorTipo(String tipoImportacion) {
        return importacionLogRepository.findByTipoImportacionOrderByFechaImportacionDesc(tipoImportacion);
    }

    /**
     * Método para reintentar una importación fallida
     */
    @Transactional
    public ImportacionLog reintentarImportacion(Long importacionId) {
        ImportacionLog importacionOriginal = obtenerImportacionPorId(importacionId);
        
        if (importacionOriginal.getEstado() != ImportacionLog.EstadoImportacion.FALLIDO && 
            importacionOriginal.getEstado() != ImportacionLog.EstadoImportacion.CON_ERRORES) {
            throw new IllegalArgumentException("Solo se pueden reintentar importaciones fallidas o con errores");
        }

        // Crear un nuevo log para el reintento
        ImportacionLog reintentoLog = new ImportacionLog(
            importacionOriginal.getNombreArchivo() + "_reintento", 
            importacionOriginal.getTipoImportacion()
        );
        reintentoLog.setObservaciones("Reintento de importación ID: " + importacionId);

        // Aquí podrías implementar la lógica para reprocesar el archivo original
        // Por ahora, simplemente guardamos el log de reintento
        reintentoLog.setEstado(ImportacionLog.EstadoImportacion.PROCESANDO);
        
        return importacionLogRepository.save(reintentoLog);
    }

    /**
     * Método para obtener estadísticas generales de importaciones
     */
    public Map<String, Object> obtenerEstadisticasImportaciones() {
        List<ImportacionLog> todasImportaciones = importacionLogRepository.findAll();
        
        long totalImportaciones = todasImportaciones.size();
        long completadas = todasImportaciones.stream()
                .filter(log -> log.getEstado() == ImportacionLog.EstadoImportacion.COMPLETADO)
                .count();
        long conErrores = todasImportaciones.stream()
                .filter(log -> log.getEstado() == ImportacionLog.EstadoImportacion.CON_ERRORES)
                .count();
        long fallidas = todasImportaciones.stream()
                .filter(log -> log.getEstado() == ImportacionLog.EstadoImportacion.FALLIDO)
                .count();
        
        long totalRegistros = todasImportaciones.stream()
                .mapToLong(log -> log.getTotalRegistros() != null ? log.getTotalRegistros() : 0)
                .sum();
        long totalExitosos = todasImportaciones.stream()
                .mapToLong(log -> log.getRegistrosExitosos() != null ? log.getRegistrosExitosos() : 0)
                .sum();
        
        Map<String, Object> estadisticas = new HashMap<>();
        estadisticas.put("totalImportaciones", totalImportaciones);
        estadisticas.put("completadas", completadas);
        estadisticas.put("conErrores", conErrores);
        estadisticas.put("fallidas", fallidas);
        estadisticas.put("totalRegistros", totalRegistros);
        estadisticas.put("totalExitosos", totalExitosos);
        estadisticas.put("tasaExito", totalRegistros > 0 ? (double) totalExitosos / totalRegistros * 100 : 0);
        
        return estadisticas;
    }

    /**
     * Método para eliminar importaciones antiguas (mantenimiento)
     */
    @Transactional
    public void eliminarImportacionesAntiguas(int dias) {
        LocalDateTime fechaLimite = LocalDateTime.now().minusDays(dias);
        List<ImportacionLog> importacionesAntiguas = importacionLogRepository
                .findByFechaImportacionBefore(fechaLimite);
        
        if (!importacionesAntiguas.isEmpty()) {
            logger.info("Eliminando {} importaciones antiguas anteriores a {}", 
                       importacionesAntiguas.size(), fechaLimite);
            importacionLogRepository.deleteAll(importacionesAntiguas);
        }
    }
}