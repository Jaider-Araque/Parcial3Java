package com.uts.saberpro.service;

import com.uts.saberpro.entity.*;
import com.uts.saberpro.repository.BeneficioRepository;
import com.uts.saberpro.repository.EstudianteRepository;
import com.uts.saberpro.repository.ResultadoPruebaRepository;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class BeneficioService {
    
    private final BeneficioRepository beneficioRepository;
    private final ResultadoPruebaRepository resultadoPruebaRepository;
    private final EstudianteRepository estudianteRepository;
    
    public BeneficioService(BeneficioRepository beneficioRepository, 
                          ResultadoPruebaRepository resultadoPruebaRepository,
                          EstudianteRepository estudianteRepository) {
        this.beneficioRepository = beneficioRepository;
        this.resultadoPruebaRepository = resultadoPruebaRepository;
        this.estudianteRepository = estudianteRepository;
    }
    
    // ✅ Método para contar total de beneficios
    public long contarTotalBeneficios() {
        return beneficioRepository.count();
    }
    
    // ✅ Método para contar beneficios activos
    public long contarBeneficiosActivos() {
        return beneficioRepository.countByActivoTrue();
    }
    
    // ✅ Método para obtener todos los beneficios
    public List<Beneficio> obtenerTodosBeneficios() {
        return beneficioRepository.findAll();
    }
    
    // ✅ Método para obtener beneficios por estudiante
    public List<Beneficio> obtenerBeneficiosPorEstudiante(Estudiante estudiante) {
        return beneficioRepository.findByEstudiante(estudiante);
    }
    
    // ✅ Método para obtener beneficio por ID
    public Optional<Beneficio> obtenerBeneficioPorId(Long id) {
        return beneficioRepository.findById(id);
    }
    
    // ✅ Método para guardar beneficio
    public Beneficio guardarBeneficio(Beneficio beneficio) {
        return beneficioRepository.save(beneficio);
    }
    
    // ✅ Método para eliminar beneficio
    public void eliminarBeneficio(Long id) {
        beneficioRepository.deleteById(id);
    }
    
    /**
     * Calcula y asigna beneficios según el Acuerdo 01-009
     * Solo para pruebas APROBADAS
     */
    public Beneficio calcularBeneficio(Estudiante estudiante, ResultadoPrueba resultado) {
        // ✅ CONDICIÓN: Solo calcular beneficios para pruebas APROBADAS
        if (resultado.getEstado() != EstadoPrueba.APROBADO) {
            System.out.println("❌ No se calcula beneficio - Prueba no está APROBADA. Estado: " + resultado.getEstado());
            return null;
        }
        
        Beneficio.TipoBeneficio tipoBeneficio = null;
        Double notaAsignada = 0.0;
        Double porcentajeDescuento = 0.0;
        
        System.out.println("🔍 Calculando beneficio para: " + estudiante.getNombreCompleto());
        System.out.println("📊 Tipo Prueba: " + resultado.getTipoPrueba() + ", Puntaje: " + resultado.getPuntajeGlobal());
        
        if (resultado.getTipoPrueba() == TipoPrueba.SABER_TYT) {
            // ✅ Beneficios para Saber T&T (0-200 puntos)
            if (resultado.getPuntajeGlobal() >= 120 && resultado.getPuntajeGlobal() <= 150) {
                tipoBeneficio = Beneficio.TipoBeneficio.EXONERACION_TRABAJO_GRADO;
                notaAsignada = 4.5;
                porcentajeDescuento = 0.0;
                System.out.println("🎯 Beneficio asignado: EXONERACION_TRABAJO_GRADO - Nota 4.5");
            } else if (resultado.getPuntajeGlobal() >= 151 && resultado.getPuntajeGlobal() <= 170) {
                tipoBeneficio = Beneficio.TipoBeneficio.DESCUENTO_50_DERECHOS_GRADO;
                notaAsignada = 4.7;
                porcentajeDescuento = 50.0;
                System.out.println("🎯 Beneficio asignado: DESCUENTO_50_DERECHOS_GRADO - Nota 4.7 - 50% descuento");
            } else if (resultado.getPuntajeGlobal() > 170 && resultado.getPuntajeGlobal() <= 200) {
                tipoBeneficio = Beneficio.TipoBeneficio.DESCUENTO_100_DERECHOS_GRADO;
                notaAsignada = 5.0;
                porcentajeDescuento = 100.0;
                System.out.println("🎯 Beneficio asignado: DESCUENTO_100_DERECHOS_GRADO - Nota 5.0 - 100% descuento");
            } else if (resultado.getPuntajeGlobal() < 80) {
                System.out.println("⚠️ Debe repetir prueba Saber T&T - Puntaje insuficiente: " + resultado.getPuntajeGlobal());
                return null;
            } else {
                System.out.println("ℹ️ No califica para beneficio Saber T&T - Puntaje: " + resultado.getPuntajeGlobal());
                return null;
            }
        } else if (resultado.getTipoPrueba() == TipoPrueba.SABER_PRO) {
            // ✅ Beneficios para Saber Pro (0-300 puntos)
            if (resultado.getPuntajeGlobal() >= 180 && resultado.getPuntajeGlobal() <= 210) {
                tipoBeneficio = Beneficio.TipoBeneficio.EXONERACION_TRABAJO_GRADO;
                notaAsignada = 4.5;
                porcentajeDescuento = 0.0;
                System.out.println("🎯 Beneficio asignado: EXONERACION_TRABAJO_GRADO - Nota 4.5");
            } else if (resultado.getPuntajeGlobal() >= 211 && resultado.getPuntajeGlobal() <= 240) {
                tipoBeneficio = Beneficio.TipoBeneficio.DESCUENTO_50_DERECHOS_GRADO;
                notaAsignada = 4.7;
                porcentajeDescuento = 50.0;
                System.out.println("🎯 Beneficio asignado: DESCUENTO_50_DERECHOS_GRADO - Nota 4.7 - 50% descuento");
            } else if (resultado.getPuntajeGlobal() > 240 && resultado.getPuntajeGlobal() <= 300) {
                tipoBeneficio = Beneficio.TipoBeneficio.DESCUENTO_100_DERECHOS_GRADO;
                notaAsignada = 5.0;
                porcentajeDescuento = 100.0;
                System.out.println("🎯 Beneficio asignado: DESCUENTO_100_DERECHOS_GRADO - Nota 5.0 - 100% descuento");
            } else if (resultado.getPuntajeGlobal() <= 120) {
                System.out.println("⚠️ Debe repetir prueba Saber PRO - Puntaje insuficiente: " + resultado.getPuntajeGlobal());
                return null;
            } else {
                System.out.println("ℹ️ No califica para beneficio Saber PRO - Puntaje: " + resultado.getPuntajeGlobal());
                return null;
            }
        }
        
        // ✅ Si no califica para beneficio
        if (tipoBeneficio == null) {
            System.out.println("❌ No se asignó beneficio - No cumple criterios");
            return null;
        }
        
        // ✅ Verificar si ya existe un beneficio activo para este estudiante y tipo
        List<Beneficio> beneficiosExistentes = beneficioRepository.findByEstudianteAndActivoTrue(estudiante);
        if (!beneficiosExistentes.isEmpty()) {
            System.out.println("⚠️ El estudiante ya tiene beneficios activos. No se creará uno nuevo.");
            return null;
        }
        
        // ✅ Crear y guardar beneficio
        Beneficio beneficio = new Beneficio();
        beneficio.setEstudiante(estudiante);
        beneficio.setTipoBeneficio(tipoBeneficio);
        beneficio.setNotaAsignada(notaAsignada);
        beneficio.setPorcentajeDescuento(porcentajeDescuento);
        beneficio.setFechaAsignacion(LocalDate.now());
        beneficio.setActivo(true);
        
        Beneficio beneficioGuardado = beneficioRepository.save(beneficio);
        System.out.println("✅ Beneficio guardado exitosamente: " + tipoBeneficio);
        
        return beneficioGuardado;
    }
    
    /**
     * Obtiene la descripción legible del beneficio
     */
    public String obtenerDescripcionBeneficio(Beneficio.TipoBeneficio tipoBeneficio, Double nota, Double descuento) {
        switch (tipoBeneficio) {
            case EXONERACION_TRABAJO_GRADO:
                return "Exoneración trabajo de grado + Nota " + nota;
            case DESCUENTO_50_DERECHOS_GRADO:
                return "Exoneración + " + descuento + "% derechos de grado + Nota " + nota;
            case DESCUENTO_100_DERECHOS_GRADO:
                return "Exoneración + " + descuento + "% derechos de grado + Nota " + nota;
            default:
                return "Beneficio no especificado";
        }
    }
    
    /**
     * Obtiene el estado del beneficio según el resultado de la prueba
     */
    public String obtenerEstadoBeneficio(ResultadoPrueba resultado) {
        if (resultado.getEstado() != EstadoPrueba.APROBADO) {
            return "NO_APLICA - Prueba " + resultado.getEstado().toString();
        }
        
        if (resultado.getTipoPrueba() == TipoPrueba.SABER_TYT) {
            if (resultado.getPuntajeGlobal() < 80) {
                return "DEBE_REPETIR_PRUEBA";
            } else if (resultado.getPuntajeGlobal() < 120) {
                return "NO_CALIFICA";
            }
        } else if (resultado.getTipoPrueba() == TipoPrueba.SABER_PRO) {
            if (resultado.getPuntajeGlobal() <= 120) {
                return "DEBE_REPETIR_PRUEBA";
            } else if (resultado.getPuntajeGlobal() < 180) {
                return "NO_CALIFICA";
            }
        }
        
        return "CALIFICA_PARA_BENEFICIO";
    }
    
    /**
     * ✅ CORREGIDO: Calcula automáticamente beneficios para todos los estudiantes con pruebas aprobadas
     */
    public int calcularBeneficiosAutomaticos() {
        System.out.println("🚀 Iniciando cálculo automático de beneficios...");
        
        // ✅ Obtener TODOS los estudiantes (no solo los que ya tienen beneficios)
        List<Estudiante> estudiantes = estudianteRepository.findAll();
        System.out.println("📊 Total estudiantes en sistema: " + estudiantes.size());
        
        int beneficiosCreados = 0;
        int estudiantesProcesados = 0;
        int estudiantesConResultadosAprobados = 0;
        
        for (Estudiante estudiante : estudiantes) {
            try {
                estudiantesProcesados++;
                
                // Buscar el último resultado aprobado del estudiante
                Optional<ResultadoPrueba> resultadoOpt = obtenerUltimoResultadoAprobado(estudiante);
                
                if (resultadoOpt.isPresent()) {
                    estudiantesConResultadosAprobados++;
                    ResultadoPrueba resultado = resultadoOpt.get();
                    
                    // Verificar si ya tiene un beneficio activo
                    List<Beneficio> beneficiosActivos = beneficioRepository.findByEstudianteAndActivoTrue(estudiante);
                    
                    if (beneficiosActivos.isEmpty()) {
                        System.out.println("🎯 [" + estudiantesProcesados + "/" + estudiantes.size() + 
                                         "] Procesando: " + estudiante.getNombreCompleto() + 
                                         " - Puntaje: " + resultado.getPuntajeGlobal() +
                                         " - Tipo: " + resultado.getTipoPrueba());
                        
                        // Calcular y asignar beneficio
                        Beneficio beneficio = calcularBeneficio(estudiante, resultado);
                        if (beneficio != null) {
                            beneficiosCreados++;
                            System.out.println("✅ Beneficio creado: " + beneficio.getTipoBeneficio());
                        } else {
                            System.out.println("ℹ️  No califica para beneficio");
                        }
                    } else {
                        System.out.println("⏭️  Ya tiene beneficio activo: " + estudiante.getNombreCompleto());
                    }
                } else {
                    System.out.println("📭 Sin resultados aprobados: " + estudiante.getNombreCompleto());
                }
            } catch (Exception e) {
                System.err.println("❌ Error procesando " + estudiante.getNumeroDocumento() + ": " + e.getMessage());
                e.printStackTrace();
            }
        }
        
        System.out.println("✅ Cálculo completado:");
        System.out.println("   - Estudiantes procesados: " + estudiantesProcesados);
        System.out.println("   - Estudiantes con resultados aprobados: " + estudiantesConResultadosAprobados);
        System.out.println("   - Beneficios creados: " + beneficiosCreados);
        
        return beneficiosCreados;
    }
    
    /**
     * Obtiene el último resultado aprobado de un estudiante
     */
    private Optional<ResultadoPrueba> obtenerUltimoResultadoAprobado(Estudiante estudiante) {
        // Buscar todos los resultados del estudiante ordenados por fecha descendente
        List<ResultadoPrueba> resultados = resultadoPruebaRepository.findByEstudiante(estudiante);
        
        return resultados.stream()
                .filter(r -> r.getEstado() == EstadoPrueba.APROBADO)
                .sorted((r1, r2) -> r2.getFechaRegistro().compareTo(r1.getFechaRegistro()))
                .findFirst();
    }
    
    // ✅ Método para desactivar beneficio
    public void desactivarBeneficio(Long id) {
        Optional<Beneficio> beneficioOpt = beneficioRepository.findById(id);
        if (beneficioOpt.isPresent()) {
            Beneficio beneficio = beneficioOpt.get();
            beneficio.setActivo(false);
            beneficioRepository.save(beneficio);
            System.out.println("🔴 Beneficio desactivado: " + id);
        }
    }
    
    // ✅ Método para activar beneficio
    public void activarBeneficio(Long id) {
        Optional<Beneficio> beneficioOpt = beneficioRepository.findById(id);
        if (beneficioOpt.isPresent()) {
            Beneficio beneficio = beneficioOpt.get();
            beneficio.setActivo(true);
            beneficioRepository.save(beneficio);
            System.out.println("🟢 Beneficio activado: " + id);
        }
    }
    
}