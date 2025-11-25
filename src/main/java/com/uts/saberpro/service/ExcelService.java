package com.uts.saberpro.service;

import com.uts.saberpro.entity.Estudiante;
import com.uts.saberpro.entity.ResultadoPrueba;
import com.uts.saberpro.entity.TipoPrueba;
import com.uts.saberpro.entity.Usuario;
import com.uts.saberpro.entity.EstadoPrueba;
import com.uts.saberpro.repository.EstudianteRepository;
import com.uts.saberpro.repository.ResultadoPruebaRepository;
import com.uts.saberpro.repository.UsuarioRepository;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class ExcelService {

    private static final Logger logger = LoggerFactory.getLogger(ExcelService.class);
    
    private final EstudianteRepository estudianteRepository;
    private final UsuarioRepository usuarioRepository;
    private final ResultadoPruebaRepository resultadoPruebaRepository;
    private final PasswordEncoder passwordEncoder;

    public ExcelService(EstudianteRepository estudianteRepository, 
                       UsuarioRepository usuarioRepository,
                       ResultadoPruebaRepository resultadoPruebaRepository,
                       PasswordEncoder passwordEncoder) {
        this.estudianteRepository = estudianteRepository;
        this.usuarioRepository = usuarioRepository;
        this.resultadoPruebaRepository = resultadoPruebaRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // 🔥 MODIFICADO: Quitar @Transactional del método principal y manejar transacciones por fila
    public Map<String, Object> procesarArchivoResultados(MultipartFile archivo, TipoPrueba tipoPrueba) throws IOException {
        logger.info("🚀 ===== INICIANDO IMPORTACIÓN {} =====", tipoPrueba);
        logger.info("📁 Archivo recibido: {}", archivo.getOriginalFilename());
        logger.info("🎯 Tipo de prueba: {}", tipoPrueba);
        logger.info("📏 Tamaño: {} bytes", archivo.getSize());
        
        Map<String, Object> resultado = new HashMap<>();
        List<ResultadoPrueba> resultadosProcesados = new ArrayList<>();
        List<String> erroresDetallados = new ArrayList<>();
        List<String> debugLogs = new ArrayList<>();
        
        try (InputStream inputStream = archivo.getInputStream();
             Workbook workbook = new XSSFWorkbook(inputStream)) {
            
            Sheet sheet = workbook.getSheetAt(0);
            logger.info("📊 Hoja encontrada: {}", sheet.getSheetName());
            logger.info("📈 Total de filas: {}", (sheet.getLastRowNum() + 1));
            
            // === DEBUG 1: ANALIZAR ENCABEZADOS ===
            debugLogs.add("=== ANÁLISIS DE ENCABEZADOS ===");
            Row headerRow = sheet.getRow(0);
            if (headerRow == null) {
                throw new RuntimeException("No se encontró fila de encabezados");
            }
            
            logger.info("🔍 ANALIZANDO ENCABEZADOS:");
            for (int i = 0; i < headerRow.getLastCellNum(); i++) {
                String header = obtenerValorCelda(headerRow, i);
                debugLogs.add("Columna " + i + ": '" + header + "'");
                logger.info("   Col {}: '{}'", i, header);
            }
            
            // === DEBUG 2: ANALIZAR PRIMERAS FILAS DE DATOS ===
            debugLogs.add("\n=== ANÁLISIS DE DATOS ===");
            int filasConDatos = 0;
            
            for (int i = 1; i <= Math.min(sheet.getLastRowNum(), 5); i++) { // Solo primeras 5 filas para debug
                Row row = sheet.getRow(i);
                if (row != null && !esFilaVacia(row)) {
                    filasConDatos++;
                    debugLogs.add("--- Fila " + (i + 1) + " ---");
                    logger.info("\n📝 ANALIZANDO FILA {}:", (i + 1));
                    
                    for (int j = 0; j < Math.min(row.getLastCellNum(), 15); j++) { // Solo primeras 15 columnas
                        String valor = obtenerValorCelda(row, j);
                        debugLogs.add("  Col " + j + ": '" + valor + "'");
                        logger.info("   Col {}: '{}'", j, valor);
                    }
                    
                    // Procesar esta fila para debug
                    try {
                        // 🔥 MODIFICADO: Pasar tipoPrueba
                        ResultadoPrueba resultadoFila = procesarFilaResultadoConDebug(row, i + 1, debugLogs, tipoPrueba);
                        if (resultadoFila != null) {
                            resultadosProcesados.add(resultadoFila);
                        }
                    } catch (Exception e) {
                        String error = "ERROR en fila " + (i + 1) + ": " + e.getMessage();
                        erroresDetallados.add(error);
                        debugLogs.add("❌ " + error);
                        logger.error("❌ {}", error);
                    }
                }
            }
            
            logger.info("\n📊 RESUMEN PARCIAL:");
            logger.info("   Filas con datos encontradas: {}", filasConDatos);
            logger.info("   Resultados procesados: {}", resultadosProcesados.size());
            logger.info("   Errores: {}", erroresDetallados.size());
            
            // === DEBUG 3: PROCESAR TODAS LAS FILAS ===
            if (filasConDatos > 0) {
                debugLogs.add("\n=== PROCESAMIENTO COMPLETO ===");
                logger.info("\n🔄 PROCESANDO TODAS LAS FILAS...");
                
                for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                    Row row = sheet.getRow(i);
                    if (row != null && !esFilaVacia(row)) {
                        try {
                            // 🔥 MODIFICADO: Usar nuevo método con manejo de transacción individual
                            ResultadoPrueba resultadoFila = procesarFilaResultadoConTransaccion(row, i + 1, tipoPrueba);
                            if (resultadoFila != null) {
                                resultadosProcesados.add(resultadoFila);
                                debugLogs.add("✅ Fila " + (i + 1) + ": Guardado exitoso - " + 
                                            resultadoFila.getEstudiante().getNumeroDocumento());
                                logger.info("✅ Fila {}: Guardado - {}", (i + 1), 
                                                resultadoFila.getEstudiante().getNumeroDocumento());
                            }
                        } catch (Exception e) {
                            String error = "ERROR en fila " + (i + 1) + ": " + e.getMessage();
                            erroresDetallados.add(error);
                            debugLogs.add("❌ " + error);
                            logger.error("❌ Error en fila {}: {}", (i + 1), e.getMessage());
                            
                            // 🔥 NUEVO: Continuar con la siguiente fila en lugar de abortar toda la importación
                            logger.warn("Continuando con siguiente fila después del error...");
                        }
                    }
                }
            }
            
        } catch (Exception e) {
            String error = "ERROR GENERAL: " + e.getMessage();
            debugLogs.add("❌ " + error);
            logger.error("❌ {}", error);
            e.printStackTrace();
        }
        
        // === DEBUG FINAL ===
        logger.info("\n🎯 ===== RESUMEN FINAL {} =====", tipoPrueba);
        logger.info("✅ Resultados procesados: {}", resultadosProcesados.size());
        logger.info("❌ Errores: {}", erroresDetallados.size());
        logger.info("📝 Logs de debug: {}", debugLogs.size());
        
        // Verificar base de datos
        long totalEstudiantes = estudianteRepository.count();
        long totalResultados = resultadoPruebaRepository.count();
        logger.info("🗄️  Estudiantes en BD: {}", totalEstudiantes);
        logger.info("🗄️  Resultados en BD: {}", totalResultados);
        
        resultado.put("resultados", resultadosProcesados);
        resultado.put("errores", erroresDetallados);
        resultado.put("debugLogs", debugLogs);
        resultado.put("totalProcesado", resultadosProcesados.size());
        resultado.put("estadisticasBD", Map.of(
            "estudiantes", totalEstudiantes,
            "resultados", totalResultados
        ));
        
        return resultado;
    }

    // 🔥 NUEVO MÉTODO: Procesar fila con transacción individual para evitar aborto general
    @Transactional
    protected ResultadoPrueba procesarFilaResultadoConTransaccion(Row row, int numeroFila, TipoPrueba tipoPrueba) {
        logger.debug("🔍 Procesando fila {} con transacción individual - Tipo: {}", numeroFila, tipoPrueba);
        
        try {
            // Extraer datos
            String numeroDocumento = obtenerValorCelda(row, 1);
            String primerApellido = obtenerValorCelda(row, 2);
            String segundoApellido = obtenerValorCelda(row, 3);
            String primerNombre = obtenerValorCelda(row, 4);
            String segundoNombre = obtenerValorCelda(row, 5);
            String correoElectronico = obtenerValorCelda(row, 6);
            String numeroTelefono = obtenerValorCelda(row, 7);
            String puntajeStr = obtenerValorCelda(row, 9);
            
            // Validaciones básicas
            if (numeroDocumento == null || numeroDocumento.trim().isEmpty()) {
                throw new RuntimeException("Número de documento vacío");
            }
            
            if ("ANULADO".equalsIgnoreCase(puntajeStr)) {
                logger.debug("  ⏭️ Resultado ANULADO, omitiendo fila {}", numeroFila);
                return null;
            }
            
            Integer puntajeGlobal = obtenerValorNumerico(row, 9);
            logger.debug("  🔢 Puntaje convertido: {}", puntajeGlobal);
            
            if (puntajeGlobal == null) {
                throw new RuntimeException("Puntaje global inválido");
            }
            
            // Obtener o crear estudiante
            Estudiante estudiante = obtenerOCrearEstudiante(
                numeroDocumento.trim(), primerApellido, segundoApellido, 
                primerNombre, segundoNombre, correoElectronico, numeroTelefono, tipoPrueba
            );
            
            // Verificar duplicados
            Integer anioActual = LocalDateTime.now().getYear();
            boolean existeResultado = resultadoPruebaRepository
                .findByEstudianteAndTipoPrueba(estudiante, tipoPrueba)
                .stream()
                .anyMatch(r -> r.getAnioPrueba().equals(anioActual));
            
            if (existeResultado) {
                logger.debug("  🔄 Ya existe resultado para este año, omitiendo fila {}", numeroFila);
                return null;
            }
            
            // Crear resultado
            ResultadoPrueba resultado = new ResultadoPrueba(
                estudiante, tipoPrueba, puntajeGlobal, anioActual, 1
            );
            
            // Procesar competencias
            procesarCompetencias(resultado, row);
            
            // Determinar estado
            if (puntajeGlobal >= 150) {
                resultado.aprobarPrueba();
            } else {
                resultado.reprobarPrueba();
            }
            
            // Guardar resultado
            ResultadoPrueba resultadoGuardado = resultadoPruebaRepository.save(resultado);
            logger.debug("  ✅ Resultado creado - Estado: {} - Tipo: {}", resultado.getEstado(), tipoPrueba);
            
            return resultadoGuardado;
            
        } catch (Exception e) {
            logger.error("  ❌ Error en fila {}: {}", numeroFila, e.getMessage());
            // 🔥 IMPORTANTE: Relanzar la excepción para que Spring haga rollback de ESTA transacción individual
            throw new RuntimeException("Error procesando fila " + numeroFila + ": " + e.getMessage(), e);
        }
    }

    // 🔥 MODIFICADO: Agregar parámetro tipoPrueba
    private ResultadoPrueba procesarFilaResultadoConDebug(Row row, int numeroFila, List<String> debugLogs, TipoPrueba tipoPrueba) {
        debugLogs.add("🔍 Procesando fila " + numeroFila + " - Tipo: " + tipoPrueba);
        logger.debug("🔍 Procesando fila {} - Tipo: {}", numeroFila, tipoPrueba);
        
        try {
            // Extraer datos con debug - 🔥 COLUMNA 7 ES EL TELÉFONO
            String numeroDocumento = obtenerValorCelda(row, 1);
            String primerApellido = obtenerValorCelda(row, 2);
            String segundoApellido = obtenerValorCelda(row, 3);
            String primerNombre = obtenerValorCelda(row, 4);
            String segundoNombre = obtenerValorCelda(row, 5);
            String correoElectronico = obtenerValorCelda(row, 6);
            String numeroTelefono = obtenerValorCelda(row, 7); // 🔥 NUEVO: Extraer teléfono
            String puntajeStr = obtenerValorCelda(row, 9);
            
            debugLogs.add("  📋 Datos extraídos:");
            debugLogs.add("    - Documento: '" + numeroDocumento + "'");
            debugLogs.add("    - Nombre: '" + primerNombre + " " + primerApellido + "'");
            debugLogs.add("    - Correo: '" + correoElectronico + "'");
            debugLogs.add("    - Teléfono: '" + numeroTelefono + "'"); // 🔥 NUEVO
            debugLogs.add("    - Puntaje: '" + puntajeStr + "'");
            debugLogs.add("    - Tipo Prueba: '" + tipoPrueba + "'"); // 🔥 NUEVO
            
            logger.debug("  📋 Datos extraídos - Documento: '{}', Nombre: '{} {}', Correo: '{}', Teléfono: '{}', Puntaje: '{}', Tipo: '{}'", 
                        numeroDocumento, primerNombre, primerApellido, correoElectronico, numeroTelefono, puntajeStr, tipoPrueba);
            
            // Validaciones
            if (numeroDocumento == null || numeroDocumento.trim().isEmpty()) {
                throw new RuntimeException("Número de documento vacío");
            }
            
            if ("ANULADO".equalsIgnoreCase(puntajeStr)) {
                debugLogs.add("  ⏭️ Resultado ANULADO, omitiendo");
                logger.debug("  ⏭️ Resultado ANULADO, omitiendo fila {}", numeroFila);
                return null;
            }
            
            Integer puntajeGlobal = obtenerValorNumerico(row, 9);
            debugLogs.add("  🔢 Puntaje convertido: " + puntajeGlobal);
            logger.debug("  🔢 Puntaje convertido: {}", puntajeGlobal);
            
            if (puntajeGlobal == null) {
                throw new RuntimeException("Puntaje global inválido");
            }
            
            // Obtener o crear estudiante - 🔥 PASAR TELÉFONO Y TIPO PRUEBA
            Estudiante estudiante = obtenerOCrearEstudianteConDebug(
                row, numeroDocumento.trim(), primerApellido, segundoApellido, 
                primerNombre, segundoNombre, correoElectronico, numeroTelefono, tipoPrueba, debugLogs
            );
            
            // Verificar duplicados CON EL TIPO ESPECÍFICO
            Integer anioActual = LocalDateTime.now().getYear();
            boolean existeResultado = resultadoPruebaRepository
                .findByEstudianteAndTipoPrueba(estudiante, tipoPrueba) // 🔥 USAR TIPO ESPECÍFICO
                .stream()
                .anyMatch(r -> r.getAnioPrueba().equals(anioActual));
            
            if (existeResultado) {
                debugLogs.add("  🔄 Ya existe resultado para este año, omitiendo");
                logger.debug("  🔄 Ya existe resultado para este año, omitiendo fila {}", numeroFila);
                return null;
            }
            
            // Crear resultado CON TIPO DE PRUEBA
            ResultadoPrueba resultado = new ResultadoPrueba(
                estudiante, tipoPrueba, puntajeGlobal, anioActual, 1 // 🔥 USAR TIPO RECIBIDO
            );
            
            // Procesar competencias
            procesarCompetenciasConDebug(resultado, row, debugLogs);
            
            // Determinar estado
            if (puntajeGlobal >= 150) {
                resultado.aprobarPrueba();
            } else {
                resultado.reprobarPrueba();
            }
            
            debugLogs.add("  ✅ Resultado creado - Estado: " + resultado.getEstado() + " - Tipo: " + tipoPrueba);
            logger.debug("  ✅ Resultado creado - Estado: {} - Tipo: {}", resultado.getEstado(), tipoPrueba);
            return resultado;
            
        } catch (Exception e) {
            debugLogs.add("  ❌ Error: " + e.getMessage());
            logger.error("  ❌ Error en fila {}: {}", numeroFila, e.getMessage());
            throw e;
        }
    }

    // 🔥 MODIFICADO: Agregar parámetro tipoPrueba
    private ResultadoPrueba procesarFilaResultado(Row row, int numeroFila, TipoPrueba tipoPrueba) {
        return procesarFilaResultadoConDebug(row, numeroFila, new ArrayList<>(), tipoPrueba);
    }

    // 🔥 NUEVO MÉTODO: Versión simplificada sin debug para transacciones individuales
    @Transactional
    protected Estudiante obtenerOCrearEstudiante(String numeroDocumento, 
                                                String primerApellido, String segundoApellido,
                                                String primerNombre, String segundoNombre,
                                                String correoElectronico, String numeroTelefono,
                                                TipoPrueba tipoPrueba) {
        
        Optional<Estudiante> estudianteExistente = estudianteRepository.findByNumeroDocumento(numeroDocumento);
        
        if (estudianteExistente.isPresent()) {
            Estudiante estudiante = estudianteExistente.get();
            
            // Actualizar teléfono si existe
            if (numeroTelefono != null && !numeroTelefono.trim().isEmpty()) {
                estudiante.setNumeroTelefono(numeroTelefono.trim());
            }
            
            // Actualizar tipo de prueba si es diferente
            if (!estudiante.getTipoPrueba().equals(tipoPrueba)) {
                estudiante.setTipoPrueba(tipoPrueba);
                
                // 🔥 NUEVO: Actualizar semestre si cambia el tipo de prueba
                if (tipoPrueba == TipoPrueba.SABER_TYT) {
                    estudiante.setSemestre(6);
                    logger.info("🔄 Actualizando semestre a 6 para estudiante Saber T&T: {}", numeroDocumento);
                } else {
                    estudiante.setSemestre(10);
                    logger.info("🔄 Actualizando semestre a 10 para estudiante Saber PRO: {}", numeroDocumento);
                }
            }
            
            return estudianteRepository.save(estudiante);
        } else {
            return crearNuevoEstudianteYUsuario(
                numeroDocumento, primerApellido, segundoApellido, 
                primerNombre, segundoNombre, correoElectronico, numeroTelefono, tipoPrueba
            );
        }
    }

    // 🔥 MODIFICADO: Agregar parámetro tipoPrueba
    @Transactional
    protected Estudiante obtenerOCrearEstudianteConDebug(Row row, String numeroDocumento, 
                                                        String primerApellido, String segundoApellido,
                                                        String primerNombre, String segundoNombre,
                                                        String correoElectronico, String numeroTelefono,
                                                        TipoPrueba tipoPrueba, // 🔥 NUEVO PARÁMETRO
                                                        List<String> debugLogs) {
        debugLogs.add("  👤 Buscando/creando estudiante: " + numeroDocumento + " - Tipo: " + tipoPrueba);
        logger.debug("  👤 Buscando/creando estudiante: {} - Tipo: {}", numeroDocumento, tipoPrueba);
        
        Optional<Estudiante> estudianteExistente = estudianteRepository.findByNumeroDocumento(numeroDocumento);
        
        if (estudianteExistente.isPresent()) {
            debugLogs.add("  🔄 Estudiante ya existe en BD");
            logger.debug("  🔄 Estudiante ya existe en BD: {}", numeroDocumento);
            
            Estudiante estudiante = estudianteExistente.get();
            
            // 🔥 ACTUALIZAR TELÉFONO SI EL ESTUDIANTE YA EXISTE
            if (numeroTelefono != null && !numeroTelefono.trim().isEmpty()) {
                estudiante.setNumeroTelefono(numeroTelefono.trim());
                debugLogs.add("  📞 Teléfono actualizado: " + numeroTelefono);
                logger.debug("  📞 Teléfono actualizado para estudiante {}: {}", numeroDocumento, numeroTelefono);
            }
            
            // 🔥 ACTUALIZAR TIPO DE PRUEBA SI ES DIFERENTE
            if (!estudiante.getTipoPrueba().equals(tipoPrueba)) {
                estudiante.setTipoPrueba(tipoPrueba);
                
                // 🔥 NUEVO: Actualizar semestre si cambia el tipo de prueba
                if (tipoPrueba == TipoPrueba.SABER_TYT) {
                    estudiante.setSemestre(6);
                    debugLogs.add("  🔄 Semestre actualizado a 6 para Saber T&T");
                    logger.debug("  🔄 Semestre actualizado a 6 para estudiante Saber T&T: {}", numeroDocumento);
                } else {
                    estudiante.setSemestre(10);
                    debugLogs.add("  🔄 Semestre actualizado a 10 para Saber PRO");
                    logger.debug("  🔄 Semestre actualizado a 10 para estudiante Saber PRO: {}", numeroDocumento);
                }
            }
            
            estudianteRepository.save(estudiante);
            return estudiante;
        } else {
            debugLogs.add("  ✅ Creando nuevo estudiante");
            logger.debug("  ✅ Creando nuevo estudiante: {}", numeroDocumento);
            return crearNuevoEstudianteYUsuario(
                numeroDocumento, primerApellido, segundoApellido, 
                primerNombre, segundoNombre, correoElectronico, numeroTelefono, tipoPrueba // 🔥 PASAR TIPO PRUEBA
            );
        }
    }

    // 🔥 MODIFICADO: Agregar parámetro tipoPrueba y asignar semestre según el tipo
    private Estudiante crearNuevoEstudianteYUsuario(String numeroDocumento, String primerApellido, 
                                                   String segundoApellido, String primerNombre, 
                                                   String segundoNombre, String correoElectronico,
                                                   String numeroTelefono, TipoPrueba tipoPrueba) {
        
        Estudiante estudiante = new Estudiante();
        estudiante.setNumeroDocumento(numeroDocumento);
        estudiante.setNombres(construirNombreCompleto(primerNombre, segundoNombre));
        estudiante.setApellidos(construirNombreCompleto(primerApellido, segundoApellido));
        estudiante.setEmail(correoElectronico != null ? correoElectronico : generarEmailTemporal(numeroDocumento));
        estudiante.setNumeroTelefono(numeroTelefono); // 🔥 GUARDAR TELÉFONO
        estudiante.setProgramaAcademico("Ingeniería de Software");
        
        // 🔥 NUEVO: Asignar semestre según el tipo de prueba
        if (tipoPrueba == TipoPrueba.SABER_TYT) {
            estudiante.setSemestre(6); // Semestre 6 para Saber T&T
            logger.info("🎯 Asignando semestre 6 para estudiante Saber T&T: {}", numeroDocumento);
        } else {
            estudiante.setSemestre(10); // Semestre 10 para Saber PRO (valor por defecto)
            logger.info("🎓 Asignando semestre 10 para estudiante Saber PRO: {}", numeroDocumento);
        }
        
        estudiante.setTipoPrueba(tipoPrueba); // 🔥 USAR TIPO RECIBIDO

        logger.info("💾 Guardando estudiante: {} - Tipo: {} - Semestre: {}", 
                    estudiante.getNumeroDocumento(), tipoPrueba, estudiante.getSemestre());
        logger.info("📞 Teléfono guardado: {}", estudiante.getNumeroTelefono());
        
        Estudiante estudianteGuardado = estudianteRepository.save(estudiante);
        logger.info("✅ Estudiante guardado con ID: {} - Tipo: {} - Semestre: {}", 
                    estudianteGuardado.getNumeroDocumento(), tipoPrueba, estudianteGuardado.getSemestre());
        
        crearUsuarioParaEstudiante(estudianteGuardado, primerApellido);
        
        return estudianteGuardado;
    }

    private void crearUsuarioParaEstudiante(Estudiante estudiante, String primerApellido) {
        String email = estudiante.getEmail();
        
        if (usuarioRepository.findByEmail(email).isPresent()) {
            logger.info("ℹ️ Usuario ya existe: {}", email);
            return;
        }

        String passwordTemporal = generarPasswordTemporal(primerApellido, estudiante.getNumeroDocumento());

        Usuario usuario = new Usuario();
        usuario.setPassword(passwordEncoder.encode(passwordTemporal));
        usuario.setNombres(estudiante.getNombres());
        usuario.setApellidos(estudiante.getApellidos());
        usuario.setEmail(email);
        usuario.setRol(Usuario.RolUsuario.ESTUDIANTE);
        usuario.setActivo(true);
        usuario.setEstudiante(estudiante);
        usuario.setPasswordTemporal(true);

        logger.info("💾 Guardando usuario: {}", email);
        usuarioRepository.save(usuario);
        logger.info("✅ Usuario guardado: {}", email);
    }

    private void procesarCompetenciasConDebug(ResultadoPrueba resultado, Row row, List<String> debugLogs) {
        debugLogs.add("  📊 Procesando competencias:");
        logger.debug("  📊 Procesando competencias para estudiante: {}", resultado.getEstudiante().getNumeroDocumento());
        
        String[] competenciasNombres = {
            "Comunicación Escrita", "Razonamiento Cuantitativo", "Lectura Crítica",
            "Competencias Ciudadanas", "Inglés", "Formulación de Proyectos",
            "Pensamiento Científico", "Diseño de Software"
        };
        
        int[] columnasCompetencias = {11, 13, 15, 17, 19, 21, 23, 25};
        
        for (int i = 0; i < competenciasNombres.length; i++) {
            Integer puntaje = obtenerValorNumerico(row, columnasCompetencias[i]);
            if (puntaje != null) {
                resultado.agregarCompetencia(competenciasNombres[i], puntaje);
                debugLogs.add("    - " + competenciasNombres[i] + ": " + puntaje);
                logger.debug("    - {}: {}", competenciasNombres[i], puntaje);
            } else {
                debugLogs.add("    - " + competenciasNombres[i] + ": NULL (columna " + columnasCompetencias[i] + ")");
                logger.debug("    - {}: NULL (columna {})", competenciasNombres[i], columnasCompetencias[i]);
            }
        }
    }

    private void procesarCompetencias(ResultadoPrueba resultado, Row row) {
        String[] competenciasNombres = {
            "Comunicación Escrita", "Razonamiento Cuantitativo", "Lectura Crítica",
            "Competencias Ciudadanas", "Inglés", "Formulación de Proyectos",
            "Pensamiento Científico", "Diseño de Software"
        };
        
        int[] columnasCompetencias = {11, 13, 15, 17, 19, 21, 23, 25};
        
        for (int i = 0; i < competenciasNombres.length; i++) {
            Integer puntaje = obtenerValorNumerico(row, columnasCompetencias[i]);
            if (puntaje != null) {
                resultado.agregarCompetencia(competenciasNombres[i], puntaje);
            }
        }
    }

    // ========== MÉTODOS AUXILIARES (sin cambios) ==========

    private String construirNombreCompleto(String parte1, String parte2) {
        StringBuilder nombreCompleto = new StringBuilder();
        if (parte1 != null && !parte1.trim().isEmpty()) {
            nombreCompleto.append(parte1.trim());
        }
        if (parte2 != null && !parte2.trim().isEmpty()) {
            if (nombreCompleto.length() > 0) {
                nombreCompleto.append(" ");
            }
            nombreCompleto.append(parte2.trim());
        }
        return nombreCompleto.toString();
    }

    private String generarEmailTemporal(String numeroDocumento) {
        return numeroDocumento.trim() + "@uts-temporal.edu.co";
    }

    private String generarPasswordTemporal(String primerApellido, String numeroDocumento) {
        String apellido = (primerApellido != null && !primerApellido.trim().isEmpty()) 
            ? primerApellido.trim().toUpperCase() 
            : "ESTUDIANTE";
        return apellido + numeroDocumento.trim();
    }

    private String obtenerValorCelda(Row row, int colIndex) {
        if (row == null || colIndex < 0) {
            return null;
        }
        
        Cell cell = row.getCell(colIndex);
        if (cell == null) {
            return null;
        }
        
        try {
            switch (cell.getCellType()) {
                case STRING:
                    return cell.getStringCellValue().trim();
                case NUMERIC:
                    if (DateUtil.isCellDateFormatted(cell)) {
                        return cell.getLocalDateTimeCellValue().toString();
                    } else {
                        double value = cell.getNumericCellValue();
                        if (value == Math.floor(value)) {
                            return String.valueOf((int) value);
                        } else {
                            return String.valueOf(value);
                        }
                    }
                case BOOLEAN:
                    return String.valueOf(cell.getBooleanCellValue());
                case FORMULA:
                    return obtenerValorFormula(cell);
                default:
                    return null;
            }
        } catch (Exception e) {
            return null;
        }
    }

    private String obtenerValorFormula(Cell cell) {
        try {
            switch (cell.getCachedFormulaResultType()) {
                case STRING:
                    return cell.getStringCellValue().trim();
                case NUMERIC:
                    double value = cell.getNumericCellValue();
                    if (value == Math.floor(value)) {
                        return String.valueOf((int) value);
                    } else {
                        return String.valueOf(value);
                    }
                case BOOLEAN:
                    return String.valueOf(cell.getBooleanCellValue());
                default:
                    return null;
            }
        } catch (Exception e) {
            return null;
        }
    }

    private Integer obtenerValorNumerico(Row row, int colIndex) {
        try {
            String valor = obtenerValorCelda(row, colIndex);
            if (valor == null || valor.trim().isEmpty()) {
                return null;
            }
            valor = valor.replaceAll("[^0-9.]", "");
            if (valor.isEmpty()) {
                return null;
            }
            double valorDouble = Double.parseDouble(valor);
            return (int) Math.round(valorDouble);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private boolean esFilaVacia(Row row) {
        if (row == null) return true;
        for (int i = 0; i < row.getLastCellNum(); i++) {
            Cell cell = row.getCell(i);
            if (cell != null && cell.getCellType() != CellType.BLANK) {
                String valor = obtenerValorCelda(row, i);
                if (valor != null && !valor.trim().isEmpty()) {
                    return false;
                }
            }
        }
        return true;
    }
}