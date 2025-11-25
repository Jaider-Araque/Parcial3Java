// service/PruebaService.java
package com.uts.saberpro.service;

import com.uts.saberpro.entity.*;
import com.uts.saberpro.repository.ResultadoPruebaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
@Transactional
public class PruebaService {
    
    private final ResultadoPruebaRepository resultadoPruebaRepository;
    private final PruebaValidationService validationService;
    private final BeneficioService beneficioService; // ✅ AGREGADO
    
    public PruebaService(ResultadoPruebaRepository resultadoPruebaRepository,
                        PruebaValidationService validationService,
                        BeneficioService beneficioService) { // ✅ AGREGADO
        this.resultadoPruebaRepository = resultadoPruebaRepository;
        this.validationService = validationService;
        this.beneficioService = beneficioService; // ✅ AGREGADO
    }
    
    /**
     * Registra un nuevo resultado con validación automática
     */
    public ResultadoPrueba registrarResultado(Estudiante estudiante, TipoPrueba tipoPrueba, 
                                            Integer puntajeGlobal, Integer anio, Integer periodo) {
        
        // 1. Validar si puede presentar la prueba
        PruebaValidationService.ValidationResult validacion = 
            validationService.validarNuevaPrueba(estudiante, tipoPrueba);
        
        if (!validacion.isValido()) {
            throw new IllegalStateException(validacion.getMensaje());
        }
        
        // 2. Determinar estado automáticamente
        EstadoPrueba estado = validationService.validarPuntaje(tipoPrueba, puntajeGlobal);
        String observaciones = validationService.generarObservaciones(tipoPrueba, puntajeGlobal, estado);
        
        // 3. Crear resultado
        ResultadoPrueba resultado = new ResultadoPrueba();
        resultado.setEstudiante(estudiante);
        resultado.setTipoPrueba(tipoPrueba);
        resultado.setPuntajeGlobal(puntajeGlobal);
        resultado.setAnioPrueba(anio);
        resultado.setPeriodo(periodo);
        resultado.setEstado(estado);
        resultado.setObservaciones(observaciones);
        resultado.setFechaRegistro(LocalDateTime.now());
        
        // 4. Guardar resultado
        ResultadoPrueba resultadoGuardado = resultadoPruebaRepository.save(resultado);
        
        // ✅ 5. NUEVO: Calcular beneficios automáticamente si está APROBADO
        if (resultadoGuardado.getEstado() == EstadoPrueba.APROBADO) {
            beneficioService.calcularBeneficio(estudiante, resultadoGuardado);
        }
        
        return resultadoGuardado;
    }
    
    /**
     * Anular una prueba existente
     */
    public ResultadoPrueba anularPrueba(Long resultadoId, String motivo) {
        ResultadoPrueba resultado = resultadoPruebaRepository.findById(resultadoId)
                .orElseThrow(() -> new IllegalArgumentException("Resultado no encontrado"));
        
        resultado.setEstado(EstadoPrueba.ANULADA);
        resultado.setObservaciones("PRUEBA ANULADA: " + motivo);
        resultado.setFechaActualizacion(LocalDateTime.now());
        
        return resultadoPruebaRepository.save(resultado);
    }
    
    /**
     * Verificar si un estudiante debe repetir prueba
     */
    public boolean debeRepetirPrueba(Estudiante estudiante, TipoPrueba tipoPrueba) {
        // Buscar el último resultado no anulado
        Optional<ResultadoPrueba> ultimoResultado = resultadoPruebaRepository
                .findTopByEstudianteAndTipoPruebaOrderByFechaRegistroDesc(estudiante, tipoPrueba);
        
        if (ultimoResultado.isEmpty()) {
            return false; // No ha presentado la prueba
        }
        
        ResultadoPrueba resultado = ultimoResultado.get();
        return resultado.getEstado() == EstadoPrueba.REPROBADO || 
               resultado.getEstado() == EstadoPrueba.ANULADA;
    }
    
    /**
     * Obtener el último resultado de un estudiante
     */
    public Optional<ResultadoPrueba> obtenerUltimoResultado(Estudiante estudiante, TipoPrueba tipoPrueba) {
        return resultadoPruebaRepository
                .findTopByEstudianteAndTipoPruebaOrderByFechaRegistroDesc(estudiante, tipoPrueba);
    }
}