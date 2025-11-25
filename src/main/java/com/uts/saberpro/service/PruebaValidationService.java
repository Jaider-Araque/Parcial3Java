// service/PruebaValidationService.java
package com.uts.saberpro.service;

import com.uts.saberpro.entity.*;
import com.uts.saberpro.repository.ResultadoPruebaRepository;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class PruebaValidationService {
    
    private final ResultadoPruebaRepository resultadoPruebaRepository;
    
    // Constantes según las reglas de negocio
    private static final int PUNTAJE_MINIMO_TYT = 80;
    private static final int PUNTAJE_MINIMO_PRO = 121;
    private static final int MAX_INTENTOS = 2;
    
    public PruebaValidationService(ResultadoPruebaRepository resultadoPruebaRepository) {
        this.resultadoPruebaRepository = resultadoPruebaRepository;
    }
    
    /**
     * Valida si un estudiante puede presentar una nueva prueba
     */
    public ValidationResult validarNuevaPrueba(Estudiante estudiante, TipoPrueba tipoPrueba) {
        // 1. Verificar intentos previos
        List<ResultadoPrueba> resultadosPrevios = resultadoPruebaRepository
                .findByEstudianteAndTipoPrueba(estudiante, tipoPrueba);
        
        // 2. Contar intentos válidos (excluyendo anuladas)
        long intentosValidos = resultadosPrevios.stream()
                .filter(r -> r.getEstado() != EstadoPrueba.ANULADA)
                .count();
                
        // 3. Verificar si ya tiene una prueba aprobada
        boolean tieneAprobada = resultadosPrevios.stream()
                .anyMatch(r -> r.getEstado() == EstadoPrueba.APROBADO);
        
        // 4. Aplicar reglas
        if (tieneAprobada) {
            return new ValidationResult(false, 
                "El estudiante ya tiene una prueba " + tipoPrueba + " APROBADA. No puede presentar nuevamente.");
        }
        
        if (intentosValidos >= MAX_INTENTOS) {
            return new ValidationResult(false,
                "El estudiante ya agotó los " + MAX_INTENTOS + " intentos permitidos para " + tipoPrueba);
        }
        
        return new ValidationResult(true, "Puede presentar la prueba");
    }
    
    /**
     * Valida el puntaje y determina el estado automáticamente
     */
    public EstadoPrueba validarPuntaje(TipoPrueba tipoPrueba, Integer puntaje) {
        if (puntaje == null) {
            return EstadoPrueba.PENDIENTE;
        }
        
        switch (tipoPrueba) {
            case SABER_TYT:
                return puntaje >= PUNTAJE_MINIMO_TYT ? EstadoPrueba.APROBADO : EstadoPrueba.REPROBADO;
                
            case SABER_PRO:
                return puntaje >= PUNTAJE_MINIMO_PRO ? EstadoPrueba.APROBADO : EstadoPrueba.REPROBADO;
                
            default:
                return EstadoPrueba.PENDIENTE;
        }
    }
    
    /**
     * Genera observaciones automáticas según el resultado
     */
    public String generarObservaciones(TipoPrueba tipoPrueba, Integer puntaje, EstadoPrueba estado) {
        if (estado == EstadoPrueba.REPROBADO) {
            switch (tipoPrueba) {
                case SABER_TYT:
                    return "Puntaje " + puntaje + " inferior a " + PUNTAJE_MINIMO_TYT + ". Debe repetir la prueba.";
                    
                case SABER_PRO:
                    return "Puntaje " + puntaje + " inferior a " + PUNTAJE_MINIMO_PRO + ". Debe repetir la prueba.";
            }
        }
        
        return "Prueba " + estado.toString().toLowerCase() + " con puntaje " + puntaje;
    }
    
    /**
     * Clase para el resultado de validación
     */
    public static class ValidationResult {
        private final boolean valido;
        private final String mensaje;
        
        public ValidationResult(boolean valido, String mensaje) {
            this.valido = valido;
            this.mensaje = mensaje;
        }
        
        public boolean isValido() { return valido; }
        public String getMensaje() { return mensaje; }
    }
}