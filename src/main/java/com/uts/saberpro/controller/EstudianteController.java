package com.uts.saberpro.controller;

import com.uts.saberpro.entity.*;
import com.uts.saberpro.repository.*;
import com.uts.saberpro.service.BeneficioService;
import com.uts.saberpro.service.ResultadoPruebaService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/estudiante")
public class EstudianteController {

    private final UsuarioRepository usuarioRepository;
    private final ResultadoPruebaRepository resultadoPruebaRepository;
    private final BeneficioRepository beneficioRepository;
    private final ResultadoPruebaService resultadoPruebaService;
    private final BeneficioService beneficioService;
    private final EstudianteRepository estudianteRepository;

    public EstudianteController(UsuarioRepository usuarioRepository,
                              ResultadoPruebaRepository resultadoPruebaRepository,
                              BeneficioRepository beneficioRepository,
                              ResultadoPruebaService resultadoPruebaService,
                              BeneficioService beneficioService,
                              EstudianteRepository estudianteRepository) {
        this.usuarioRepository = usuarioRepository;
        this.resultadoPruebaRepository = resultadoPruebaRepository;
        this.beneficioRepository = beneficioRepository;
        this.resultadoPruebaService = resultadoPruebaService;
        this.beneficioService = beneficioService;
        this.estudianteRepository = estudianteRepository;
    }

    private Estudiante obtenerEstudianteActual(Authentication authentication) {
        try {
            String username = authentication.getName();
            System.out.println("🔍 [DEBUG] Buscando usuario autenticado: " + username);
            
            // Buscar usuario por username
            Optional<Usuario> usuarioOpt = usuarioRepository.findById(username);
            
            if (usuarioOpt.isPresent()) {
                Usuario usuario = usuarioOpt.get();
                System.out.println("✅ [DEBUG] Usuario encontrado: " + usuario.getUsername());
                System.out.println("🎯 [DEBUG] Rol del usuario: " + usuario.getRol());
                
                // Obtener el estudiante asociado
                Estudiante estudiante = usuario.getEstudiante();
                
                if (estudiante != null) {
                    System.out.println("✅ [DEBUG] Estudiante asociado encontrado: " + estudiante.getNombreCompleto());
                    System.out.println("📊 [DEBUG] Programa: " + estudiante.getProgramaAcademico());
                    return estudiante;
                } else {
                    System.out.println("⚠️ [DEBUG] Usuario no tiene estudiante asociado directamente");
                    
                    // Intentar buscar estudiante por documento si el username es numérico
                    if (username.matches("\\d+")) {
                        System.out.println("🔍 [DEBUG] Buscando estudiante por documento: " + username);
                        Optional<Estudiante> estudianteOpt = estudianteRepository.findById(username);
                        if (estudianteOpt.isPresent()) {
                            System.out.println("✅ [DEBUG] Estudiante encontrado por documento: " + estudianteOpt.get().getNombreCompleto());
                            return estudianteOpt.get();
                        }
                    }
                    
                    // Buscar por relación inversa
                    System.out.println("🔍 [DEBUG] Buscando estudiante por username de usuario...");
                    Optional<Estudiante> estudianteOpt = estudianteRepository.findByUsuarioUsername(username);
                    if (estudianteOpt.isPresent()) {
                        System.out.println("✅ [DEBUG] Estudiante encontrado por relación inversa: " + estudianteOpt.get().getNombreCompleto());
                        return estudianteOpt.get();
                    }
                }
            } else {
                System.out.println("❌ [DEBUG] Usuario no encontrado en la base de datos: " + username);
            }
            
            System.out.println("❌ [ERROR] No se pudo obtener el estudiante para el usuario: " + username);
            return null;
            
        } catch (Exception e) {
            System.err.println("❌ [ERROR] Excepción en obtenerEstudianteActual: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    @GetMapping("/dashboard")
    public String estudianteDashboard(Authentication authentication, Model model) {
        try {
            System.out.println("=== 🚀 INICIANDO DASHBOARD ESTUDIANTE ===");
            
            Estudiante estudiante = obtenerEstudianteActual(authentication);
            
            if (estudiante == null) {
                System.out.println("❌ [ERROR] No se pudo obtener el estudiante - Estableciendo valores por defecto");
                model.addAttribute("error", "No se pudo cargar la información del estudiante");
                establecerValoresPorDefecto(model);
                return "estudiante/dashboard";
            }
            
            System.out.println("✅ [DEBUG] Estudiante cargado: " + estudiante.getNombreCompleto());
            
            // Obtener todos los resultados del estudiante
            List<ResultadoPrueba> resultados = resultadoPruebaRepository.findByEstudiante(estudiante);
            System.out.println("📈 [DEBUG] Resultados encontrados: " + resultados.size());
            
            // Obtener beneficios del estudiante
            List<Beneficio> beneficios = beneficioRepository.findByEstudiante(estudiante);
            System.out.println("💰 [DEBUG] Beneficios encontrados: " + beneficios.size());
            
            // Obtener últimos resultados específicos
            Optional<ResultadoPrueba> ultimoSaberPro = resultadoPruebaRepository
                    .findTopByEstudianteAndTipoPruebaOrderByFechaRegistroDesc(estudiante, TipoPrueba.SABER_PRO);
            
            Optional<ResultadoPrueba> ultimoSaberTT = resultadoPruebaRepository
                    .findTopByEstudianteAndTipoPruebaOrderByFechaRegistroDesc(estudiante, TipoPrueba.SABER_TYT);

            // Calcular estadísticas
            long totalPruebas = resultados.size();
            long totalBeneficios = beneficios.size();
            double promedioEstudiante = calcularPromedioEstudianteSeguro(resultados);
            int posicionPrograma = calcularPosicionEnProgramaSeguro(estudiante, resultados);

            // DEBUG: Mostrar estadísticas calculadas
            System.out.println("=== 📊 ESTADÍSTICAS CALCULADAS ===");
            System.out.println("📊 Total Pruebas: " + totalPruebas);
            System.out.println("💰 Total Beneficios: " + totalBeneficios);
            System.out.println("⭐ Promedio: " + promedioEstudiante);
            System.out.println("📈 Posición: " + posicionPrograma);
            System.out.println("🎯 Último Saber Pro: " + (ultimoSaberPro.isPresent() ? "Sí" : "No"));
            System.out.println("🎯 Último Saber T&T: " + (ultimoSaberTT.isPresent() ? "Sí" : "No"));

            // Agregar datos al modelo
            model.addAttribute("estudiante", estudiante);
            model.addAttribute("ultimoSaberPro", ultimoSaberPro.orElse(null));
            model.addAttribute("ultimoSaberTT", ultimoSaberTT.orElse(null));
            model.addAttribute("resultados", resultados);
            model.addAttribute("beneficios", beneficios);
            model.addAttribute("totalPruebas", totalPruebas);
            model.addAttribute("totalBeneficios", totalBeneficios);
            model.addAttribute("promedioEstudiante", promedioEstudiante);
            model.addAttribute("posicionPrograma", posicionPrograma);
            
            System.out.println("✅ [SUCCESS] Dashboard cargado exitosamente");
            
        } catch (Exception e) {
            System.err.println("❌ [ERROR] Excepción en dashboard: " + e.getMessage());
            e.printStackTrace();
            establecerValoresPorDefecto(model);
            model.addAttribute("error", "Error al cargar los datos: " + e.getMessage());
        }
        
        return "estudiante/dashboard";
    }

    @GetMapping("/resultados")
    public String misResultados(Authentication authentication, Model model) {
        try {
            System.out.println("=== 📋 CARGANDO PÁGINA DE RESULTADOS ===");
            
            Estudiante estudiante = obtenerEstudianteActual(authentication);
            
            if (estudiante == null) {
                System.out.println("❌ [ERROR] No se pudo obtener el estudiante para resultados");
                establecerValoresPorDefectoResultados(model);
                return "estudiante/resultados";
            }
            
            // Obtener todas las pruebas del estudiante
            List<ResultadoPrueba> todasLasPruebas = resultadoPruebaRepository.findByEstudiante(estudiante);
            System.out.println("📊 [DEBUG] Total pruebas del estudiante: " + todasLasPruebas.size());
            
            // Separar por tipo de prueba
            List<ResultadoPrueba> pruebasSaberPro = todasLasPruebas.stream()
                    .filter(prueba -> prueba.getTipoPrueba() == TipoPrueba.SABER_PRO)
                    .collect(Collectors.toList());
                    
            List<ResultadoPrueba> pruebasSaberTT = todasLasPruebas.stream()
                    .filter(prueba -> prueba.getTipoPrueba() == TipoPrueba.SABER_TYT)
                    .collect(Collectors.toList());
            
            // Calcular estadísticas para la vista
            long totalPruebas = todasLasPruebas.size();
            double promedioGeneral = calcularPromedioGeneralSeguro(todasLasPruebas);
            double mejoria = calcularMejoriaSeguro(todasLasPruebas);
            int posicionPrograma = calcularPosicionEnProgramaSeguro(estudiante, todasLasPruebas);
            
            // Obtener evolución de puntajes para gráficas
            List<Object[]> evolucionPuntajes = obtenerEvolucionPuntajes(todasLasPruebas);

            // Agregar datos al modelo
            model.addAttribute("estudiante", estudiante);
            model.addAttribute("pruebasSaberPro", pruebasSaberPro);
            model.addAttribute("pruebasSaberTT", pruebasSaberTT);
            model.addAttribute("totalPruebas", totalPruebas);
            model.addAttribute("promedioGeneral", promedioGeneral);
            model.addAttribute("mejoria", mejoria);
            model.addAttribute("posicionPrograma", posicionPrograma);
            model.addAttribute("evolucionPuntajes", evolucionPuntajes);
            
            System.out.println("✅ [SUCCESS] Página de resultados cargada exitosamente");
            
        } catch (Exception e) {
            System.err.println("❌ [ERROR] Excepción en resultados: " + e.getMessage());
            e.printStackTrace();
            establecerValoresPorDefectoResultados(model);
        }
        
        return "estudiante/resultados";
    }

    @GetMapping("/beneficios")
    public String misBeneficios(Authentication authentication, Model model) {
        try {
            System.out.println("=== 💰 CARGANDO PÁGINA DE BENEFICIOS ===");
            
            Estudiante estudiante = obtenerEstudianteActual(authentication);
            
            if (estudiante == null) {
                System.out.println("❌ [ERROR] No se pudo obtener el estudiante para beneficios");
                establecerValoresPorDefectoBeneficios(model);
                return "estudiante/beneficios";
            }
            
            List<Beneficio> todosLosBeneficios = beneficioRepository.findByEstudiante(estudiante);
            List<ResultadoPrueba> pruebasEstudiante = resultadoPruebaRepository.findByEstudiante(estudiante);
            
            System.out.println("📊 [DEBUG] Total beneficios: " + todosLosBeneficios.size());
            System.out.println("📊 [DEBUG] Total pruebas: " + pruebasEstudiante.size());
            
            // Separar beneficios por estado (activo)
            List<Beneficio> beneficiosActivos = todosLosBeneficios.stream()
                    .filter(b -> b.getActivo())
                    .collect(Collectors.toList());
                    
            List<Beneficio> beneficiosFinalizados = todosLosBeneficios.stream()
                    .filter(b -> !b.getActivo())
                    .collect(Collectors.toList());
            
            // Obtener pruebas aprobadas
            List<ResultadoPrueba> pruebasAprobadasSinBeneficios = pruebasEstudiante.stream()
                    .filter(p -> p.getEstado() == EstadoPrueba.APROBADO)
                    .filter(p -> beneficiosActivos.stream()
                            .noneMatch(b -> b.getFechaAsignacion().isAfter(p.getFechaRegistro().toLocalDate())))
                    .collect(Collectors.toList());
            
            // Calcular estadísticas
            long beneficiosActivosCount = beneficiosActivos.size();
            long beneficiosFinalizadosCount = beneficiosFinalizados.size();
            double totalValorBeneficios = calcularTotalValorBeneficiosSeguro(todosLosBeneficios);
            
            // Agregar datos al modelo
            model.addAttribute("estudiante", estudiante);
            model.addAttribute("beneficiosActivos", beneficiosActivosCount);
            model.addAttribute("beneficiosPendientes", 0);
            model.addAttribute("beneficiosFinalizados", beneficiosFinalizadosCount);
            model.addAttribute("totalValorBeneficios", totalValorBeneficios);
            model.addAttribute("beneficiosActivosLista", beneficiosActivos);
            model.addAttribute("beneficiosFinalizadosLista", beneficiosFinalizados);
            model.addAttribute("pruebasAprobadasSinBeneficios", pruebasAprobadasSinBeneficios);
            
            System.out.println("✅ [SUCCESS] Página de beneficios cargada exitosamente");
            
        } catch (Exception e) {
            System.err.println("❌ [ERROR] Excepción en beneficios: " + e.getMessage());
            e.printStackTrace();
            establecerValoresPorDefectoBeneficios(model);
        }
        
        return "estudiante/beneficios";
    }

    @GetMapping("/estadisticas")
    public String misEstadisticas(Authentication authentication, Model model) {
        try {
            System.out.println("=== 📈 CARGANDO PÁGINA DE ESTADÍSTICAS ===");
            
            Estudiante estudiante = obtenerEstudianteActual(authentication);
            
            if (estudiante == null) {
                System.out.println("❌ [ERROR] No se pudo obtener el estudiante para estadísticas");
                model.addAttribute("promedio", 0.0);
                model.addAttribute("pruebasAprobadas", 0);
                model.addAttribute("totalPruebas", 0);
                model.addAttribute("resultados", List.of());
                return "estudiante/estadisticas";
            }
            
            List<ResultadoPrueba> resultados = resultadoPruebaRepository.findByEstudiante(estudiante);
            
            // Calcular estadísticas detalladas
            double promedio = calcularPromedioEstudianteSeguro(resultados);
            long pruebasAprobadas = resultados.stream()
                    .filter(r -> r.getEstado() == EstadoPrueba.APROBADO)
                    .count();
            
            model.addAttribute("estudiante", estudiante);
            model.addAttribute("resultados", resultados);
            model.addAttribute("promedio", promedio);
            model.addAttribute("pruebasAprobadas", pruebasAprobadas);
            model.addAttribute("totalPruebas", resultados.size());
            
            System.out.println("✅ [SUCCESS] Página de estadísticas cargada exitosamente");
            
        } catch (Exception e) {
            System.err.println("❌ [ERROR] Excepción en estadísticas: " + e.getMessage());
            e.printStackTrace();
            model.addAttribute("promedio", 0.0);
            model.addAttribute("pruebasAprobadas", 0);
            model.addAttribute("totalPruebas", 0);
            model.addAttribute("resultados", List.of());
        }
        
        return "estudiante/estadisticas";
    }

    @GetMapping("/perfil")
    public String miPerfil(Authentication authentication, Model model) {
        try {
            System.out.println("=== 👤 CARGANDO PÁGINA DE PERFIL ===");
            
            Estudiante estudiante = obtenerEstudianteActual(authentication);
            
            if (estudiante == null) {
                System.out.println("❌ [ERROR] No se pudo obtener el estudiante para perfil");
                model.addAttribute("totalPruebas", 0);
                model.addAttribute("promedioGeneral", 0.0);
                model.addAttribute("pruebasAprobadas", 0);
                model.addAttribute("totalBeneficios", 0);
                return "estudiante/perfil";
            }
            
            List<ResultadoPrueba> resultados = resultadoPruebaRepository.findByEstudiante(estudiante);
            List<Beneficio> beneficios = beneficioRepository.findByEstudiante(estudiante);
            
            // Calcular estadísticas para el perfil
            long totalPruebas = resultados.size();
            double promedioGeneral = calcularPromedioGeneralSeguro(resultados);
            long pruebasAprobadas = resultados.stream()
                    .filter(r -> r.getEstado() == EstadoPrueba.APROBADO)
                    .count();
            long totalBeneficios = beneficios.size();
            
            model.addAttribute("estudiante", estudiante);
            model.addAttribute("totalPruebas", totalPruebas);
            model.addAttribute("promedioGeneral", promedioGeneral);
            model.addAttribute("pruebasAprobadas", pruebasAprobadas);
            model.addAttribute("totalBeneficios", totalBeneficios);
            
            System.out.println("✅ [SUCCESS] Página de perfil cargada exitosamente");
            
        } catch (Exception e) {
            System.err.println("❌ [ERROR] Excepción en perfil: " + e.getMessage());
            e.printStackTrace();
            model.addAttribute("totalPruebas", 0);
            model.addAttribute("promedioGeneral", 0.0);
            model.addAttribute("pruebasAprobadas", 0);
            model.addAttribute("totalBeneficios", 0);
        }
        
        return "estudiante/perfil";
    }

    // ========== MÉTODOS AUXILIARES ==========

    private void establecerValoresPorDefecto(Model model) {
        model.addAttribute("totalPruebas", 0);
        model.addAttribute("totalBeneficios", 0);
        model.addAttribute("promedioEstudiante", 0.0);
        model.addAttribute("posicionPrograma", 0);
        model.addAttribute("beneficios", List.of());
        model.addAttribute("resultados", List.of());
    }
    
    private void establecerValoresPorDefectoResultados(Model model) {
        model.addAttribute("totalPruebas", 0);
        model.addAttribute("promedioGeneral", 0.0);
        model.addAttribute("mejoria", 0.0);
        model.addAttribute("posicionPrograma", 0);
        model.addAttribute("pruebasSaberPro", List.of());
        model.addAttribute("pruebasSaberTT", List.of());
        model.addAttribute("evolucionPuntajes", List.of());
    }
    
    private void establecerValoresPorDefectoBeneficios(Model model) {
        model.addAttribute("beneficiosActivos", 0);
        model.addAttribute("beneficiosPendientes", 0);
        model.addAttribute("beneficiosFinalizados", 0);
        model.addAttribute("totalValorBeneficios", 0.0);
        model.addAttribute("beneficiosActivosLista", List.of());
        model.addAttribute("beneficiosFinalizadosLista", List.of());
        model.addAttribute("pruebasAprobadasSinBeneficios", List.of());
    }

    // ========== MÉTODOS AUXILIARES SEGUROS (NUNCA RETORNAN NULL) ==========

    private double calcularPromedioEstudianteSeguro(List<ResultadoPrueba> resultados) {
        if (resultados == null || resultados.isEmpty()) return 0.0;
        
        return resultados.stream()
                .mapToInt(ResultadoPrueba::getPuntajeGlobal)
                .average()
                .orElse(0.0);
    }
    
    private double calcularPromedioGeneralSeguro(List<ResultadoPrueba> resultados) {
        if (resultados == null || resultados.isEmpty()) {
            return 0.0;
        }
        
        double promedio = resultados.stream()
                .mapToInt(ResultadoPrueba::getPuntajeGlobal)
                .average()
                .orElse(0.0);
        
        // Convertir a porcentaje (asumiendo que 500 es el máximo)
        return (promedio / 500) * 100;
    }
    
    private double calcularMejoriaSeguro(List<ResultadoPrueba> resultados) {
        if (resultados == null || resultados.size() < 2) {
            return 0.0;
        }
        
        // Ordenar por fecha (más reciente primero)
        List<ResultadoPrueba> resultadosOrdenados = resultados.stream()
                .sorted((r1, r2) -> r2.getFechaRegistro().compareTo(r1.getFechaRegistro()))
                .collect(Collectors.toList());
        
        // Obtener los dos más recientes
        int ultimoPuntaje = resultadosOrdenados.get(0).getPuntajeGlobal();
        int penultimoPuntaje = resultadosOrdenados.get(1).getPuntajeGlobal();
        
        if (penultimoPuntaje == 0) {
            return 0.0;
        }
        
        // Calcular porcentaje de mejora
        double mejoria = ((double) (ultimoPuntaje - penultimoPuntaje) / penultimoPuntaje) * 100;
        return Math.round(mejoria * 10.0) / 10.0;
    }
    
    private int calcularPosicionEnProgramaSeguro(Estudiante estudiante, List<ResultadoPrueba> resultados) {
        if (resultados == null || resultados.isEmpty()) return 0;
        
        double promedio = calcularPromedioEstudianteSeguro(resultados);
        // Simulación de posición basada en el promedio
        return Math.max(1, (int) (100 - (promedio / 3))); // Asegurar que sea al menos 1
    }
    
    private List<Object[]> obtenerEvolucionPuntajes(List<ResultadoPrueba> resultados) {
        if (resultados == null) return List.of();
        
        return resultados.stream()
                .sorted((r1, r2) -> r1.getFechaRegistro().compareTo(r2.getFechaRegistro()))
                .map(r -> new Object[]{
                    r.getPeriodo(),
                    r.getPuntajeGlobal(),
                    r.getTipoPrueba().toString()
                })
                .collect(Collectors.toList());
    }
    
    private double calcularTotalValorBeneficiosSeguro(List<Beneficio> beneficios) {
        if (beneficios == null || beneficios.isEmpty()) return 0.0;
        
        return beneficios.stream()
                .mapToDouble(b -> {
                    double valor = 0.0;
                    if (b.getPorcentajeDescuento() != null && b.getPorcentajeDescuento() > 0) {
                        valor += (b.getPorcentajeDescuento() / 100.0) * 1000000;
                    }
                    if (b.getNotaAsignada() != null && b.getNotaAsignada() >= 4.5) {
                        valor += 500000;
                    }
                    return valor;
                })
                .sum();
    }
}