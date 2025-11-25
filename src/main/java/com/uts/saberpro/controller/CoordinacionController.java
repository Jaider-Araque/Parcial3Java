package com.uts.saberpro.controller;

import com.uts.saberpro.entity.*;
import com.uts.saberpro.repository.BeneficioRepository;
import com.uts.saberpro.repository.EstudianteRepository;
import com.uts.saberpro.repository.ResultadoPruebaRepository;
import com.uts.saberpro.service.BeneficioService;
import com.uts.saberpro.service.EstudianteService;
import com.uts.saberpro.service.PruebaService;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.persistence.criteria.Predicate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Controller
@RequestMapping("/coordinacion")
public class CoordinacionController {

    private final EstudianteService estudianteService;
    private final PruebaService pruebaService;
    private final BeneficioService beneficioService;
    private final EstudianteRepository estudianteRepository;
    private final ResultadoPruebaRepository resultadoPruebaRepository;
    private final BeneficioRepository beneficioRepository;

    public CoordinacionController(EstudianteService estudianteService, 
                                PruebaService pruebaService,
                                BeneficioService beneficioService,
                                EstudianteRepository estudianteRepository,
                                ResultadoPruebaRepository resultadoPruebaRepository,
                                BeneficioRepository beneficioRepository) {
        this.estudianteService = estudianteService;
        this.pruebaService = pruebaService;
        this.beneficioService = beneficioService;
        this.estudianteRepository = estudianteRepository;
        this.resultadoPruebaRepository = resultadoPruebaRepository;
        this.beneficioRepository = beneficioRepository;
    }

    @GetMapping("/dashboard")
    public String coordinacionDashboard(Model model) {
        // Estadísticas para el dashboard
        long totalEstudiantes = estudianteRepository.count();
        long totalResultados = resultadoPruebaRepository.count();
        
        // Contar por tipo de prueba
        long estudiantesSaberPro = estudianteRepository.countByTipoPrueba(TipoPrueba.SABER_PRO);
        long estudiantesSaberTyt = estudianteRepository.countByTipoPrueba(TipoPrueba.SABER_TYT);
        
        // Contar resultados por tipo
        long resultadosSaberPro = resultadoPruebaRepository.countByTipoPrueba(TipoPrueba.SABER_PRO);
        long resultadosSaberTyt = resultadoPruebaRepository.countByTipoPrueba(TipoPrueba.SABER_TYT);
        
        // Estadísticas de beneficios
        long totalBeneficios = beneficioService.contarTotalBeneficios();
        long beneficiosActivos = beneficioService.contarBeneficiosActivos();
        
        // Importaciones (valores temporales - puedes implementar un repositorio después)
        long totalImportaciones = 0;
        long importacionesExitosas = 0;
        long importacionesConErrores = 0;
        
        model.addAttribute("totalEstudiantes", totalEstudiantes);
        model.addAttribute("estudiantesSaberPro", estudiantesSaberPro);
        model.addAttribute("estudiantesSaberTyt", estudiantesSaberTyt);
        model.addAttribute("totalResultados", totalResultados);
        model.addAttribute("resultadosSaberPro", resultadosSaberPro);
        model.addAttribute("resultadosSaberTyt", resultadosSaberTyt);
        model.addAttribute("totalBeneficios", totalBeneficios);
        model.addAttribute("beneficiosActivos", beneficiosActivos);
        model.addAttribute("totalImportaciones", totalImportaciones);
        model.addAttribute("importacionesExitosas", importacionesExitosas);
        model.addAttribute("importacionesConErrores", importacionesConErrores);
        model.addAttribute("anioActual", LocalDateTime.now().getYear());
        
        return "coordinacion/dashboard";
    }

    // ✅ MÉTODO ACTUALIZADO: Gestión de estudiantes con filtros y paginación
    @GetMapping("/estudiantes")
    public String gestionEstudiantes(
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "10") int tamaño,
            @RequestParam(required = false) String tipoPrueba,
            @RequestParam(required = false) Integer semestre,
            @RequestParam(required = false) String documento,
            @RequestParam(required = false) String nombre,
            Model model) {

        Pageable pageable = PageRequest.of(pagina, tamaño, Sort.by("nombres").ascending());
        Page<Estudiante> paginaEstudiantes;

        // Aplicar filtros con Specification
        Specification<Estudiante> spec = construirSpecification(tipoPrueba, semestre, documento, nombre);
        
        if (spec != null) {
            paginaEstudiantes = estudianteRepository.findAll(spec, pageable);
        } else {
            paginaEstudiantes = estudianteRepository.findAll(pageable);
        }

        // Agregar datos al modelo
        model.addAttribute("estudiantes", paginaEstudiantes.getContent());
        model.addAttribute("paginaActual", pagina);
        model.addAttribute("totalPaginas", paginaEstudiantes.getTotalPages());
        model.addAttribute("totalElementos", paginaEstudiantes.getTotalElements());
        model.addAttribute("totalEstudiantes", estudianteRepository.count());
        
        // Mantener los parámetros de filtro para la vista
        model.addAttribute("tipoPruebaFiltro", tipoPrueba);
        model.addAttribute("semestreFiltro", semestre);
        model.addAttribute("documentoFiltro", documento);
        model.addAttribute("nombreFiltro", nombre);

        return "coordinacion/estudiantes";
    }

    private Specification<Estudiante> construirSpecification(String tipoPrueba, Integer semestre, String documento, String nombre) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (tipoPrueba != null && !tipoPrueba.isEmpty()) {
                try {
                    TipoPrueba tipo = TipoPrueba.valueOf(tipoPrueba);
                    predicates.add(cb.equal(root.get("tipoPrueba"), tipo));
                } catch (IllegalArgumentException e) {
                    // Tipo de prueba no válido, ignorar este filtro
                }
            }

            if (semestre != null) {
                predicates.add(cb.equal(root.get("semestre"), semestre));
            }

            if (documento != null && !documento.trim().isEmpty()) {
                predicates.add(cb.like(cb.lower(root.get("numeroDocumento")), "%" + documento.toLowerCase() + "%"));
            }

            if (nombre != null && !nombre.trim().isEmpty()) {
                String likePattern = "%" + nombre.toLowerCase() + "%";
                predicates.add(cb.or(
                    cb.like(cb.lower(root.get("nombres")), likePattern),
                    cb.like(cb.lower(root.get("apellidos")), likePattern)
                ));
            }

            return predicates.isEmpty() ? null : cb.and(predicates.toArray(new Predicate[0]));
        };
    }
    
    @PostMapping("/estudiantes/guardar")
    public String guardarEstudiante(@ModelAttribute Estudiante estudiante, RedirectAttributes redirectAttributes) {
        try {
            System.out.println("🎯 GUARDANDO ESTUDIANTE");
            System.out.println("📥 Documento recibido: '" + estudiante.getNumeroDocumento() + "'");
            
            // El documento ya viene limpio del frontend
            String documento = estudiante.getNumeroDocumento();
            
            // Validaciones básicas
            if (documento == null || documento.trim().isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "El documento no puede estar vacío");
                return "redirect:/coordinacion/estudiantes";
            }
            
            if (!documento.matches("\\d+")) {
                redirectAttributes.addFlashAttribute("error", "El documento solo puede contener números");
                return "redirect:/coordinacion/estudiantes";
            }
            
            // Verificar si existe para determinar si es edición o creación
            Optional<Estudiante> existente = estudianteRepository.findById(documento);
            
            if (existente.isPresent()) {
                // 🟡 EDICIÓN
                Estudiante estudianteExistente = existente.get();
                estudianteExistente.setNombres(estudiante.getNombres());
                estudianteExistente.setApellidos(estudiante.getApellidos());
                estudianteExistente.setEmail(estudiante.getEmail());
                estudianteExistente.setNumeroTelefono(estudiante.getNumeroTelefono());
                estudianteExistente.setProgramaAcademico(estudiante.getProgramaAcademico());
                estudianteExistente.setSemestre(estudiante.getSemestre());
                estudianteExistente.setTipoPrueba(estudiante.getTipoPrueba());
                
                estudianteRepository.save(estudianteExistente);
                redirectAttributes.addFlashAttribute("exito", "Estudiante actualizado correctamente");
                
            } else {
                // 🟢 CREACIÓN
                // Verificar email único
                if (estudianteRepository.findByEmail(estudiante.getEmail()).isPresent()) {
                    redirectAttributes.addFlashAttribute("error", "Ya existe un estudiante con este email");
                    return "redirect:/coordinacion/estudiantes";
                }
                
                estudianteRepository.save(estudiante);
                redirectAttributes.addFlashAttribute("exito", "Estudiante creado correctamente");
            }
            
        } catch (Exception e) {
            System.err.println("❌ ERROR: " + e.getMessage());
            redirectAttributes.addFlashAttribute("error", "Error: " + e.getMessage());
        }
        
        return "redirect:/coordinacion/estudiantes";
    }
    
    @DeleteMapping("/estudiantes/eliminar/{documento}")
    @ResponseBody
    public ResponseEntity<?> eliminarEstudiante(@PathVariable String documento) {
        try {
            Optional<Estudiante> estudiante = estudianteRepository.findById(documento);
            if (estudiante.isPresent()) {
                estudianteRepository.deleteById(documento);
                return ResponseEntity.ok("OK");
            } else {
                return ResponseEntity.badRequest().body("ERROR: Estudiante no encontrado");
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("ERROR: No se pudo eliminar el estudiante: " + e.getMessage());
        }
    }
    @GetMapping("/estudiantes/editar/{documento}")
    @ResponseBody
    public ResponseEntity<?> obtenerEstudiante(@PathVariable String documento) {
        try {
            // ✅ CORREGIDO: Primero buscar sin limpiar, luego limpiar si es necesario
            Optional<Estudiante> estudianteOpt = estudianteRepository.findById(documento);
            
            // Si no encuentra con el documento original, intentar limpiarlo
            if (!estudianteOpt.isPresent()) {
                String documentoLimpio = documento.trim().replaceAll("[^\\d]", "");
                estudianteOpt = estudianteRepository.findById(documentoLimpio);
            }
            
            Estudiante estudiante = estudianteOpt
                    .orElseThrow(() -> new RuntimeException("Estudiante no encontrado con documento: " + documento));
            
            // Crear un DTO para evitar recursividad
            Map<String, Object> estudianteDto = new HashMap<>();
            estudianteDto.put("numeroDocumento", estudiante.getNumeroDocumento());
            estudianteDto.put("nombres", estudiante.getNombres());
            estudianteDto.put("apellidos", estudiante.getApellidos());
            estudianteDto.put("email", estudiante.getEmail());
            estudianteDto.put("numeroTelefono", estudiante.getNumeroTelefono());
            estudianteDto.put("programaAcademico", estudiante.getProgramaAcademico());
            estudianteDto.put("semestre", estudiante.getSemestre());
            estudianteDto.put("tipoPrueba", estudiante.getTipoPrueba());
            
            return ResponseEntity.ok(estudianteDto);
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }
    
    @GetMapping("/resultados")
    public String gestionResultados(
            @RequestParam(required = false) String documento,
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "5") int tamaño,
            Model model) {
        
        try {
            System.out.println("=== CARGANDO RESULTADOS ===");
            System.out.println("Documento filtro: " + documento);
            System.out.println("Página: " + pagina + ", Tamaño: " + tamaño);
            
            Pageable pageable = PageRequest.of(pagina, tamaño, Sort.by("fechaRegistro").descending());
            Page<ResultadoPrueba> paginaResultados;

            if (documento != null && !documento.trim().isEmpty()) {
                System.out.println("Buscando resultados para documento: " + documento);
                paginaResultados = resultadoPruebaRepository.findByEstudianteNumeroDocumento(documento, pageable);
                model.addAttribute("documentoFiltro", documento);
                
                Optional<Estudiante> estudianteOpt = estudianteRepository.findById(documento);
                if (estudianteOpt.isPresent()) {
                    model.addAttribute("estudianteFiltrado", estudianteOpt.get());
                    System.out.println("Estudiante encontrado: " + estudianteOpt.get().getNombreCompleto());
                } else {
                    System.out.println("Estudiante NO encontrado con documento: " + documento);
                }
            } else {
                System.out.println("Cargando TODOS los resultados");
                paginaResultados = resultadoPruebaRepository.findAll(pageable);
            }

            System.out.println("Total de resultados encontrados: " + paginaResultados.getTotalElements());
            System.out.println("Total de páginas: " + paginaResultados.getTotalPages());

            // Procesar resultados para calcular niveles
            List<Map<String, Object>> resultadosConNiveles = new ArrayList<>();
            List<ResultadoPrueba> resultados = paginaResultados.getContent();
            
            System.out.println("Procesando " + resultados.size() + " resultados");
            
            for (ResultadoPrueba resultado : resultados) {
                try {
                    Map<String, Object> resultadoConNivel = new HashMap<>();
                    resultadoConNivel.put("resultado", resultado);
                    
                    Map<String, Integer> competencias = resultado.getCompetencias();
                    System.out.println("Resultado ID: " + resultado.getId() + ", Competencias: " + 
                                     (competencias != null ? competencias.size() : 0));
                    
                    resultadoConNivel.put("competenciasConNivel", calcularNivelesCompetencias(competencias));
                    resultadosConNiveles.add(resultadoConNivel);
                } catch (Exception e) {
                    System.err.println("Error procesando resultado ID: " + resultado.getId() + " - " + e.getMessage());
                }
            }

            model.addAttribute("resultadosConNiveles", resultadosConNiveles);
            model.addAttribute("estudiantes", estudianteRepository.findAll(Sort.by("nombres").ascending()));
            model.addAttribute("tiposPrueba", TipoPrueba.values());
            model.addAttribute("paginaActual", pagina);
            model.addAttribute("totalPaginas", paginaResultados.getTotalPages());
            model.addAttribute("totalElementos", paginaResultados.getTotalElements());
            model.addAttribute("totalResultados", resultadoPruebaRepository.count());
            
            System.out.println("=== CARGA EXITOSA ===");
            
        } catch (Exception e) {
            System.err.println("ERROR en gestión de resultados: " + e.getMessage());
            e.printStackTrace();
            model.addAttribute("error", "Error al cargar los resultados: " + e.getMessage());
        }
        
        return "coordinacion/resultados";
    }

    // Método para calcular niveles según tu fórmula
    private Map<String, Map<String, Object>> calcularNivelesCompetencias(Map<String, Integer> competencias) {
        Map<String, Map<String, Object>> competenciasConNivel = new HashMap<>();
        
        if (competencias != null) {
            for (Map.Entry<String, Integer> entry : competencias.entrySet()) {
                String competencia = entry.getKey();
                Integer puntaje = entry.getValue();
                Map<String, Object> infoCompetencia = new HashMap<>();
                
                infoCompetencia.put("puntaje", puntaje);
                infoCompetencia.put("nivel", calcularNivel(puntaje));
                infoCompetencia.put("claseNivel", obtenerClaseNivel(puntaje));
                
                competenciasConNivel.put(competencia, infoCompetencia);
            }
        }
        
        return competenciasConNivel;
    }

    private String calcularNivel(Integer puntaje) {
        if (puntaje == null) return "N/A";
        if (puntaje >= 191 && puntaje <= 300) return "Nivel 4";
        if (puntaje >= 156 && puntaje <= 190) return "Nivel 3";
        if (puntaje >= 126 && puntaje <= 155) return "Nivel 2";
        if (puntaje >= 0 && puntaje <= 125) return "Nivel 1";
        return "Error";
    }

    private String obtenerClaseNivel(Integer puntaje) {
        if (puntaje == null) return "nivel-na";
        if (puntaje >= 191) return "nivel-4";
        if (puntaje >= 156) return "nivel-3";
        if (puntaje >= 126) return "nivel-2";
        return "nivel-1";
    }

    @PostMapping("/resultados/guardar")
    public String guardarResultado(@ModelAttribute ResultadoPrueba resultadoPrueba,
                                 RedirectAttributes redirectAttributes) {
        try {
            // Verificar que el estudiante existe
            Estudiante estudiante = estudianteRepository.findById(resultadoPrueba.getEstudiante().getNumeroDocumento())
                    .orElseThrow(() -> new IllegalArgumentException("Estudiante no encontrado"));
            
            resultadoPrueba.setEstudiante(estudiante);
            resultadoPrueba.setFechaRegistro(LocalDateTime.now());
            
            // Calcular estado basado en el puntaje global
            if (resultadoPrueba.getPuntajeGlobal() >= 60) {
                resultadoPrueba.setEstado(EstadoPrueba.APROBADO);
            } else {
                resultadoPrueba.setEstado(EstadoPrueba.REPROBADO);
            }
            
            resultadoPruebaRepository.save(resultadoPrueba);
            redirectAttributes.addFlashAttribute("exito", "Resultado registrado exitosamente");
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al registrar resultado: " + e.getMessage());
        }
        
        // Mantener el filtro si existe
        String documento = resultadoPrueba.getEstudiante().getNumeroDocumento();
        if (documento != null && !documento.trim().isEmpty()) {
            return "redirect:/coordinacion/resultados?documento=" + documento;
        }
        
        return "redirect:/coordinacion/resultados";
    }

    @GetMapping("/resultados/editar/{id}")
    @ResponseBody
    public ResponseEntity<?> obtenerResultado(@PathVariable Long id) {
        try {
            ResultadoPrueba resultado = resultadoPruebaRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Resultado no encontrado"));
            
            // Crear DTO para evitar recursividad
            Map<String, Object> resultadoDto = new HashMap<>();
            resultadoDto.put("id", resultado.getId());
            resultadoDto.put("puntajeGlobal", resultado.getPuntajeGlobal());
            resultadoDto.put("tipoPrueba", resultado.getTipoPrueba());
            resultadoDto.put("anioPrueba", resultado.getAnioPrueba());
            resultadoDto.put("periodo", resultado.getPeriodo());
            resultadoDto.put("estado", resultado.getEstado());
            resultadoDto.put("observaciones", resultado.getObservaciones());
            resultadoDto.put("fechaRegistro", resultado.getFechaRegistro());
            resultadoDto.put("competencias", resultado.getCompetencias());
            resultadoDto.put("estudiante", Map.of(
                "numeroDocumento", resultado.getEstudiante().getNumeroDocumento(),
                "nombres", resultado.getEstudiante().getNombres(),
                "apellidos", resultado.getEstudiante().getApellidos()
            ));
            
            return ResponseEntity.ok(resultadoDto);
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/resultados/actualizar")
    public String actualizarResultado(@ModelAttribute ResultadoPrueba resultadoPrueba,
                                    RedirectAttributes redirectAttributes) {
        try {
            ResultadoPrueba resultadoExistente = resultadoPruebaRepository.findById(resultadoPrueba.getId())
                    .orElseThrow(() -> new RuntimeException("Resultado no encontrado"));
            
            resultadoExistente.setPuntajeGlobal(resultadoPrueba.getPuntajeGlobal());
            resultadoExistente.setTipoPrueba(resultadoPrueba.getTipoPrueba());
            resultadoExistente.setAnioPrueba(resultadoPrueba.getAnioPrueba());
            resultadoExistente.setPeriodo(resultadoPrueba.getPeriodo());
            resultadoExistente.setObservaciones(resultadoPrueba.getObservaciones());
            resultadoExistente.setCompetencias(resultadoPrueba.getCompetencias());
            
            // Recalcular estado
            if (resultadoExistente.getPuntajeGlobal() >= 60) {
                resultadoExistente.setEstado(EstadoPrueba.APROBADO);
            } else {
                resultadoExistente.setEstado(EstadoPrueba.REPROBADO);
            }
            
            resultadoExistente.setFechaActualizacion(LocalDateTime.now());
            
            resultadoPruebaRepository.save(resultadoExistente);
            redirectAttributes.addFlashAttribute("exito", "Resultado actualizado exitosamente");
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al actualizar resultado: " + e.getMessage());
        }
        
        return "redirect:/coordinacion/resultados";
    }

    @DeleteMapping("/resultados/eliminar/{id}")
    @ResponseBody
    public ResponseEntity<?> eliminarResultado(@PathVariable Long id) {
        try {
            Optional<ResultadoPrueba> resultado = resultadoPruebaRepository.findById(id);
            if (resultado.isPresent()) {
                resultadoPruebaRepository.deleteById(id);
                return ResponseEntity.ok("OK");
            } else {
                return ResponseEntity.badRequest().body("ERROR: Resultado no encontrado");
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("ERROR: No se pudo eliminar el resultado: " + e.getMessage());
        }
    }

    // ✅ MÉTODO ACTUALIZADO: Gestión de beneficios con estudiantes sin beneficios
    @GetMapping("/beneficios")
    public String gestionBeneficios(
            @RequestParam(required = false) String documento,
            @RequestParam(required = false) String tipoBeneficio,
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "10") int tamaño,
            Model model) {
        
        try {
            Pageable pageable = PageRequest.of(pagina, tamaño, Sort.by("fechaAsignacion").descending());
            Page<Beneficio> paginaBeneficios;

            // Aplicar filtros
            if (documento != null && !documento.trim().isEmpty()) {
                // Usar el nuevo método con paginación
                paginaBeneficios = beneficioRepository.findByEstudianteNumeroDocumento(documento, pageable);
                model.addAttribute("documentoFiltro", documento);
                
                Optional<Estudiante> estudianteOpt = estudianteRepository.findById(documento);
                estudianteOpt.ifPresent(estudiante -> model.addAttribute("estudianteFiltrado", estudiante));
            } else if (tipoBeneficio != null && !tipoBeneficio.isEmpty()) {
                // Filtrar por tipo de beneficio
                try {
                    Beneficio.TipoBeneficio tipo = Beneficio.TipoBeneficio.valueOf(tipoBeneficio);
                    paginaBeneficios = beneficioRepository.findByTipoBeneficio(tipo, pageable);
                    model.addAttribute("tipoBeneficioFiltro", tipoBeneficio);
                } catch (IllegalArgumentException e) {
                    // Tipo de beneficio no válido, cargar todos
                    paginaBeneficios = beneficioRepository.findAll(pageable);
                }
            } else {
                // Cargar todos los beneficios
                paginaBeneficios = beneficioRepository.findAll(pageable);
            }

            // Procesar beneficios para mostrar información detallada
            List<Map<String, Object>> beneficiosConDetalles = new ArrayList<>();
            
            for (Beneficio beneficio : paginaBeneficios.getContent()) {
                Map<String, Object> beneficioConDetalles = new HashMap<>();
                beneficioConDetalles.put("beneficio", beneficio);
                beneficioConDetalles.put("estudiante", beneficio.getEstudiante());
                
                // Obtener descripción del beneficio
                String descripcion = beneficioService.obtenerDescripcionBeneficio(
                    beneficio.getTipoBeneficio(), 
                    beneficio.getNotaAsignada(), 
                    beneficio.getPorcentajeDescuento()
                );
                beneficioConDetalles.put("descripcionBeneficio", descripcion);
                
                // Buscar el resultado que generó este beneficio (si existe)
                Optional<ResultadoPrueba> resultadoOpt = resultadoPruebaRepository
                    .findTopByEstudianteAndTipoPruebaOrderByFechaRegistroDesc(
                        beneficio.getEstudiante(), 
                        beneficio.getEstudiante().getTipoPrueba()
                    );
                
                resultadoOpt.ifPresent(resultado -> beneficioConDetalles.put("resultado", resultado));
                
                beneficiosConDetalles.add(beneficioConDetalles);
            }

            // Estadísticas
            long totalBeneficios = beneficioService.contarTotalBeneficios();
            long beneficiosActivos = beneficioService.contarBeneficiosActivos();
            long totalEstudiantes = estudianteRepository.count();
            
            // ✅ NUEVO: Calcular estudiantes sin beneficios
            long estudiantesSinBeneficios = calcularEstudiantesSinBeneficios();
            
            // Contar por tipo de beneficio
            long countExoneracion = beneficioRepository.countByTipoBeneficio(Beneficio.TipoBeneficio.EXONERACION_TRABAJO_GRADO);
            long countDescuento50 = beneficioRepository.countByTipoBeneficio(Beneficio.TipoBeneficio.DESCUENTO_50_DERECHOS_GRADO);
            long countDescuento100 = beneficioRepository.countByTipoBeneficio(Beneficio.TipoBeneficio.DESCUENTO_100_DERECHOS_GRADO);

            model.addAttribute("beneficiosConDetalles", beneficiosConDetalles);
            model.addAttribute("totalBeneficios", totalBeneficios);
            model.addAttribute("beneficiosActivos", beneficiosActivos);
            model.addAttribute("estudiantesSinBeneficios", estudiantesSinBeneficios); // ✅ NUEVO
            model.addAttribute("totalEstudiantes", totalEstudiantes);
            model.addAttribute("countExoneracion", countExoneracion);
            model.addAttribute("countDescuento50", countDescuento50);
            model.addAttribute("countDescuento100", countDescuento100);
            model.addAttribute("paginaActual", pagina);
            model.addAttribute("totalPaginas", paginaBeneficios.getTotalPages());
            model.addAttribute("totalElementos", paginaBeneficios.getTotalElements());
            
        } catch (Exception e) {
            model.addAttribute("error", "Error al cargar los beneficios: " + e.getMessage());
            e.printStackTrace(); // Para debugging
        }
        
        return "coordinacion/beneficios";
    }

    // ✅ NUEVO MÉTODO: Calcular estudiantes sin beneficios
    private long calcularEstudiantesSinBeneficios() {
        try {
            // Obtener todos los estudiantes
            List<Estudiante> todosEstudiantes = estudianteRepository.findAll();
            
            // Contar estudiantes que NO tienen beneficios activos
            long estudiantesSinBeneficios = todosEstudiantes.stream()
                    .filter(estudiante -> {
                        List<Beneficio> beneficiosActivos = beneficioRepository.findByEstudianteAndActivoTrue(estudiante);
                        return beneficiosActivos.isEmpty();
                    })
                    .count();
            
            return estudiantesSinBeneficios;
            
        } catch (Exception e) {
            System.err.println("Error al calcular estudiantes sin beneficios: " + e.getMessage());
            return 0;
        }
    }

    // ✅ NUEVO MÉTODO: Obtener beneficio para editar
    @GetMapping("/beneficios/editar/{id}")
    @ResponseBody
    public ResponseEntity<?> obtenerBeneficio(@PathVariable Long id) {
        try {
            Beneficio beneficio = beneficioRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Beneficio no encontrado"));
            
            // Crear DTO para evitar recursividad
            Map<String, Object> beneficioDto = new HashMap<>();
            beneficioDto.put("id", beneficio.getId());
            beneficioDto.put("tipoBeneficio", beneficio.getTipoBeneficio());
            beneficioDto.put("notaAsignada", beneficio.getNotaAsignada());
            beneficioDto.put("porcentajeDescuento", beneficio.getPorcentajeDescuento());
            beneficioDto.put("fechaAsignacion", beneficio.getFechaAsignacion());
            beneficioDto.put("activo", beneficio.getActivo());
            beneficioDto.put("estudiante", Map.of(
                "numeroDocumento", beneficio.getEstudiante().getNumeroDocumento(),
                "nombres", beneficio.getEstudiante().getNombres(),
                "apellidos", beneficio.getEstudiante().getApellidos()
            ));
            
            return ResponseEntity.ok(beneficioDto);
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // ✅ NUEVO MÉTODO: Guardar beneficio
    @PostMapping("/beneficios/guardar")
    public String guardarBeneficio(@ModelAttribute Beneficio beneficio,
                                 RedirectAttributes redirectAttributes) {
        try {
            // Verificar que el estudiante existe
            Estudiante estudiante = estudianteRepository.findById(beneficio.getEstudiante().getNumeroDocumento())
                    .orElseThrow(() -> new IllegalArgumentException("Estudiante no encontrado"));
            
            beneficio.setEstudiante(estudiante);
            beneficio.setFechaAsignacion(java.time.LocalDate.now());
            beneficio.setActivo(true);
            
            beneficioRepository.save(beneficio);
            redirectAttributes.addFlashAttribute("exito", "Beneficio asignado exitosamente");
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al asignar beneficio: " + e.getMessage());
        }
        
        return "redirect:/coordinacion/beneficios";
    }

    // ✅ NUEVO MÉTODO: Actualizar beneficio
    @PostMapping("/beneficios/actualizar")
    public String actualizarBeneficio(@ModelAttribute Beneficio beneficio,
                                    RedirectAttributes redirectAttributes) {
        try {
            Beneficio beneficioExistente = beneficioRepository.findById(beneficio.getId())
                    .orElseThrow(() -> new RuntimeException("Beneficio no encontrado"));
            
            beneficioExistente.setTipoBeneficio(beneficio.getTipoBeneficio());
            beneficioExistente.setNotaAsignada(beneficio.getNotaAsignada());
            beneficioExistente.setPorcentajeDescuento(beneficio.getPorcentajeDescuento());
            
            beneficioRepository.save(beneficioExistente);
            redirectAttributes.addFlashAttribute("exito", "Beneficio actualizado exitosamente");
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al actualizar beneficio: " + e.getMessage());
        }
        
        return "redirect:/coordinacion/beneficios";
    }

    // ✅ NUEVO MÉTODO: Calcular beneficios automáticamente
    @PostMapping("/beneficios/calcular-automaticos")
    @ResponseBody
    public ResponseEntity<?> calcularBeneficiosAutomaticos() {
        try {
            int beneficiosCreados = beneficioService.calcularBeneficiosAutomaticos();
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Cálculo automático completado",
                "beneficiosCreados", beneficiosCreados
            ));
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "error", e.getMessage()
            ));
        }
    }

    // ✅ NUEVO MÉTODO: Desactivar beneficio
    @PutMapping("/beneficios/desactivar/{id}")
    @ResponseBody
    public ResponseEntity<?> desactivarBeneficio(@PathVariable Long id) {
        try {
            beneficioService.desactivarBeneficio(id);
            return ResponseEntity.ok("OK");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("ERROR: No se pudo desactivar el beneficio: " + e.getMessage());
        }
    }

    // ✅ NUEVO MÉTODO: Activar beneficio
    @PutMapping("/beneficios/activar/{id}")
    @ResponseBody
    public ResponseEntity<?> activarBeneficio(@PathVariable Long id) {
        try {
            beneficioService.activarBeneficio(id);
            return ResponseEntity.ok("OK");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("ERROR: No se pudo activar el beneficio: " + e.getMessage());
        }
    }


    @GetMapping("/verificar-datos")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> verificarDatos() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            long totalEstudiantes = estudianteRepository.count();
            long totalResultados = resultadoPruebaRepository.count();
            long totalBeneficios = beneficioService.contarTotalBeneficios();
            
            response.put("success", true);
            response.put("totalEstudiantes", totalEstudiantes);
            response.put("totalResultados", totalResultados);
            response.put("totalBeneficios", totalBeneficios);
            response.put("fecha", LocalDateTime.now().toString());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error al verificar datos: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    @GetMapping("/estudiantes/detalles")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> obtenerDetallesEstudiantes() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            List<Estudiante> estudiantes = estudianteRepository.findAll();
            List<ResultadoPrueba> resultados = resultadoPruebaRepository.findAll();
            List<Beneficio> beneficios = beneficioRepository.findAll();
            
            response.put("success", true);
            response.put("totalEstudiantes", estudiantes.size());
            response.put("totalResultados", resultados.size());
            response.put("totalBeneficios", beneficios.size());
            response.put("estudiantes", estudiantes.stream().map(e -> 
                Map.of(
                    "documento", e.getNumeroDocumento(),
                    "nombre", e.getNombreCompleto(),
                    "email", e.getEmail(),
                    "programa", e.getProgramaAcademico(),
                    "tipoPrueba", e.getTipoPrueba()
                )
            ).toList());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error al obtener detalles: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
}