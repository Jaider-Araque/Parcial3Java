package com.uts.saberpro.controller;

import com.uts.saberpro.service.EstudianteService;
import com.uts.saberpro.service.ResultadoPruebaService;
import com.uts.saberpro.service.BeneficioService;
import com.uts.saberpro.entity.TipoPrueba;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.http.ResponseEntity;

import java.util.Arrays;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

@Controller
public class ReporteController {

    private final EstudianteService estudianteService;
    private final ResultadoPruebaService resultadoPruebaService;
    private final BeneficioService beneficioService;

    public ReporteController(EstudianteService estudianteService,
                           ResultadoPruebaService resultadoPruebaService,
                           BeneficioService beneficioService) {
        this.estudianteService = estudianteService;
        this.resultadoPruebaService = resultadoPruebaService;
        this.beneficioService = beneficioService;
    }

    // ✅ REPORTES PARA COORDINACIÓN
    @GetMapping("/coordinacion/reportes")
    public String reportesCoordinacion(Model model) {
        return cargarReportesComunes(model, "coordinacion/reportes");
    }

    // ✅ REPORTES PARA ADMIN
    @GetMapping("/admin/reportes")
    public String reportesAdmin(Model model) {
        return cargarReportesComunes(model, "admin/reportes");
    }

    // ✅ MÉTODO COMÚN PARA AMBOS ROLES
    private String cargarReportesComunes(Model model, String vista) {
        try {
            // Obtener estadísticas para la página de reportes
            long totalEstudiantes = estudianteService.contarEstudiantesActivos();
            long totalResultados = resultadoPruebaService.contarResultados();
            long totalBeneficios = beneficioService.contarTotalBeneficios();
            double promedioGlobal = resultadoPruebaService.obtenerPromedioGlobal();
            
            // Contar resultados por tipo de prueba
            long resultadosSaberPro = resultadoPruebaService.contarResultadosPorTipo(TipoPrueba.SABER_PRO);
            long resultadosSaberTyt = resultadoPruebaService.contarResultadosPorTipo(TipoPrueba.SABER_TYT);
            
            // Obtener programas académicos únicos
            List<String> programas = Arrays.asList(
                "Ingeniería de Software", 
                "Tecnología en Sistemas", 
                "Administración de Empresas",
                "Contaduría Pública"
            );
            
            // Agregar datos al modelo
            model.addAttribute("totalEstudiantes", totalEstudiantes);
            model.addAttribute("totalResultados", totalResultados);
            model.addAttribute("totalBeneficios", totalBeneficios);
            model.addAttribute("promedioGlobal", String.format("%.1f", promedioGlobal));
            model.addAttribute("totalReportes", 15);
            
            // Datos para gráficos
            model.addAttribute("resultadosSaberPro", (int) resultadosSaberPro);
            model.addAttribute("resultadosSaberTyt", (int) resultadosSaberTyt);
            model.addAttribute("programas", programas);
            
            // Datos para estadísticas de beneficios
            long beneficiosActivos = beneficioService.contarBeneficiosActivos();
            model.addAttribute("beneficiosActivos", (int) beneficiosActivos);
            
        } catch (Exception e) {
            // En caso de error, establecer valores por defecto
            cargarValoresPorDefecto(model);
        }
        
        return vista;
    }

    // ✅ ENDPOINTS COMUNES PARA AMBOS ROLES
    @GetMapping({"/coordinacion/reportes/opciones-filtro", "/admin/reportes/opciones-filtro"})
    @ResponseBody
    public ResponseEntity<?> obtenerOpcionesFiltro(@RequestParam String tipo) {
        return generarRespuestaOpcionesFiltro();
    }

    @GetMapping({"/coordinacion/reportes/estadisticas", "/admin/reportes/estadisticas"})
    @ResponseBody
    public ResponseEntity<?> obtenerEstadisticas() {
        return generarRespuestaEstadisticas();
    }

    @GetMapping({"/coordinacion/reportes/estadisticas-beneficios", "/admin/reportes/estadisticas-beneficios"})
    @ResponseBody
    public ResponseEntity<?> obtenerEstadisticasBeneficios() {
        return generarRespuestaEstadisticasBeneficios();
    }

    @GetMapping({"/coordinacion/reportes/estadisticas-resultados", "/admin/reportes/estadisticas-resultados"})
    @ResponseBody
    public ResponseEntity<?> obtenerEstadisticasResultados() {
        return generarRespuestaEstadisticasResultados();
    }

    // ✅ MÉTODOS PRIVADOS PARA REUTILIZACIÓN
    private void cargarValoresPorDefecto(Model model) {
        model.addAttribute("totalEstudiantes", 0);
        model.addAttribute("totalResultados", 0);
        model.addAttribute("totalBeneficios", 0);
        model.addAttribute("promedioGlobal", "0.0");
        model.addAttribute("totalReportes", 0);
        model.addAttribute("resultadosSaberPro", 0);
        model.addAttribute("resultadosSaberTyt", 0);
        model.addAttribute("beneficiosActivos", 0);
        model.addAttribute("programas", Arrays.asList("No hay datos"));
    }

    private ResponseEntity<?> generarRespuestaOpcionesFiltro() {
        Map<String, Object> response = new HashMap<>();
        try {
            List<String> programas = Arrays.asList(
                "Ingeniería de Software", 
                "Tecnología en Sistemas", 
                "Administración de Empresas",
                "Contaduría Pública"
            );
            List<Integer> anios = Arrays.asList(2024, 2023, 2022);
            
            response.put("success", true);
            response.put("programas", programas);
            response.put("anios", anios);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    private ResponseEntity<?> generarRespuestaEstadisticas() {
        Map<String, Object> response = new HashMap<>();
        try {
            long totalEstudiantes = estudianteService.contarEstudiantesActivos();
            long totalResultados = resultadoPruebaService.contarResultados();
            long totalBeneficios = beneficioService.contarTotalBeneficios();
            long beneficiosActivos = beneficioService.contarBeneficiosActivos();
            
            response.put("success", true);
            response.put("estadisticas", Map.of(
                "totalEstudiantes", totalEstudiantes,
                "totalResultados", totalResultados,
                "totalBeneficios", totalBeneficios,
                "beneficiosActivos", beneficiosActivos
            ));
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    private ResponseEntity<?> generarRespuestaEstadisticasBeneficios() {
        Map<String, Object> response = new HashMap<>();
        try {
            // Valores de ejemplo - implementa según tu lógica
            response.put("success", true);
            response.put("exoneracion", 15);
            response.put("descuento50", 8);
            response.put("descuento100", 5);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    private ResponseEntity<?> generarRespuestaEstadisticasResultados() {
        Map<String, Object> response = new HashMap<>();
        try {
            // Valores de ejemplo - implementa según tu lógica
            response.put("success", true);
            response.put("saberProAprobados", 45);
            response.put("saberProReprobados", 12);
            response.put("saberTytAprobados", 38);
            response.put("saberTytReprobados", 8);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    // ✅ REPORTES ESPECÍFICOS PARA AMBOS ROLES
    @GetMapping({"/coordinacion/reportes/estudiantes", "/admin/reportes/estudiantes"})
    public String reporteEstudiantes(Model model) {
        model.addAttribute("estudiantes", estudianteService.obtenerTodosEstudiantes());
        model.addAttribute("tituloReporte", "Reporte de Estudiantes Activos");
        return determinarVista("reportes/estudiantes");
    }

    @GetMapping({"/coordinacion/reportes/resultados", "/admin/reportes/resultados"})
    public String reporteResultados(Model model) {
        model.addAttribute("resultados", resultadoPruebaService.obtenerTodosResultados());
        model.addAttribute("tituloReporte", "Reporte de Resultados de Pruebas");
        return determinarVista("reportes/resultados");
    }

    @GetMapping({"/coordinacion/reportes/beneficios", "/admin/reportes/beneficios"})
    public String reporteBeneficios(Model model) {
        model.addAttribute("beneficios", beneficioService.obtenerTodosBeneficios());
        model.addAttribute("tituloReporte", "Reporte de Beneficios Asignados");
        return determinarVista("reportes/beneficios");
    }

    // ✅ MÉTODO PARA DETERMINAR LA VISTA CORRECTA SEGÚN LA URL
    private String determinarVista(String vistaBase) {
        // Puedes implementar lógica para determinar si es admin o coordinación
        // Por ahora, asumimos que viene de coordinación
        return "coordinacion/" + vistaBase;
    }
}