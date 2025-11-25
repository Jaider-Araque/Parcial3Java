package com.uts.saberpro.controller;

import com.uts.saberpro.entity.TipoPrueba;
import com.uts.saberpro.service.ExcelService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/coordinacion/importar")
public class ImportacionController {

    private final ExcelService excelService;

    public ImportacionController(ExcelService excelService) {
        this.excelService = excelService;
    }

    @GetMapping
    public String mostrarPaginaImportacion() {
        return "coordinacion/importar";
    }

    @PostMapping("/resultados/saberpro")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> importarSaberPro(@RequestParam("archivo") MultipartFile archivo) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            if (archivo.isEmpty()) {
                response.put("success", false);
                response.put("message", "Por favor selecciona un archivo");
                return ResponseEntity.badRequest().body(response);
            }

            if (!esArchivoExcel(archivo)) {
                response.put("success", false);
                response.put("message", "Solo se permiten archivos Excel (.xlsx, .xls)");
                return ResponseEntity.badRequest().body(response);
            }

            System.out.println("🔄 Iniciando procesamiento SABER PRO: " + archivo.getOriginalFilename());
            
            // 🔥 MODIFICADO: Pasar TipoPrueba.SABER_PRO explícitamente
            Map<String, Object> resultado = excelService.procesarArchivoResultados(archivo, TipoPrueba.SABER_PRO);
            
            @SuppressWarnings("unchecked")
            Map<String, Object> estadisticas = (Map<String, Object>) resultado.get("estadisticas");
            Integer totalProcesado = (Integer) resultado.get("totalProcesado");
            
            response.put("success", true);
            response.put("message", "Archivo Saber Pro procesado exitosamente");
            response.put("registrosProcesados", totalProcesado);
            response.put("estadisticas", estadisticas);
            response.put("tipo", "Saber Pro");
            
            System.out.println("✅ Procesamiento SABER PRO completado: " + archivo.getOriginalFilename());
            System.out.println("📊 Resultados: " + totalProcesado + " registros procesados");
            
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            System.err.println("❌ Error al procesar archivo Saber Pro: " + e.getMessage());
            e.printStackTrace();
            response.put("success", false);
            response.put("message", "Error al procesar el archivo Saber Pro: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    @PostMapping("/resultados/tyt")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> importarTyT(@RequestParam("archivo") MultipartFile archivo) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            if (archivo.isEmpty()) {
                response.put("success", false);
                response.put("message", "Por favor selecciona un archivo");
                return ResponseEntity.badRequest().body(response);
            }

            if (!esArchivoExcel(archivo)) {
                response.put("success", false);
                response.put("message", "Solo se permiten archivos Excel (.xlsx, .xls)");
                return ResponseEntity.badRequest().body(response);
            }

            System.out.println("🔄 Iniciando procesamiento T&T: " + archivo.getOriginalFilename());
            
            // 🔥 MODIFICADO: Pasar TipoPrueba.TYT explícitamente
            Map<String, Object> resultado = excelService.procesarArchivoResultados(archivo, TipoPrueba.SABER_TYT);
            Integer totalProcesado = (Integer) resultado.get("totalProcesado");
            
            response.put("success", true);
            response.put("message", "Archivo T&T procesado exitosamente");
            response.put("registrosProcesados", totalProcesado);
            response.put("tipo", "T&T");
            
            System.out.println("✅ Procesamiento T&T completado: " + archivo.getOriginalFilename());
            System.out.println("📊 Resultados: " + totalProcesado + " registros procesados");
            
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            System.err.println("❌ Error al procesar archivo T&T: " + e.getMessage());
            response.put("success", false);
            response.put("message", "Error al procesar el archivo T&T: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    @PostMapping("/estudiantes")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> importarEstudiantes(@RequestParam("archivo") MultipartFile archivo) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            if (archivo.isEmpty()) {
                response.put("success", false);
                response.put("message", "Por favor selecciona un archivo");
                return ResponseEntity.badRequest().body(response);
            }

            if (!esArchivoExcel(archivo)) {
                response.put("success", false);
                response.put("message", "Solo se permiten archivos Excel (.xlsx, .xls)");
                return ResponseEntity.badRequest().body(response);
            }

            // TODO: Implementar método específico para estudiantes
            response.put("success", true);
            response.put("message", "Importación de estudiantes en desarrollo");
            response.put("tipo", "Estudiantes");
            
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error al procesar el archivo: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    @PostMapping("/beneficios")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> importarBeneficios(@RequestParam("archivo") MultipartFile archivo) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            if (archivo.isEmpty()) {
                response.put("success", false);
                response.put("message", "Por favor selecciona un archivo");
                return ResponseEntity.badRequest().body(response);
            }

            if (!esArchivoExcel(archivo)) {
                response.put("success", false);
                response.put("message", "Solo se permiten archivos Excel (.xlsx, .xls)");
                return ResponseEntity.badRequest().body(response);
            }

            // TODO: Implementar método específico para beneficios
            response.put("success", true);
            response.put("message", "Importación de beneficios en desarrollo");
            response.put("tipo", "Beneficios");
            
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error al procesar el archivo: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    @GetMapping("/plantilla/{tipo}")
    public ResponseEntity<byte[]> descargarPlantilla(@PathVariable String tipo) {
        // TODO: Implementar descarga de plantillas Excel
        return ResponseEntity.notFound().build();
    }

    private boolean esArchivoExcel(MultipartFile archivo) {
        String nombreArchivo = archivo.getOriginalFilename();
        return nombreArchivo != null && 
               (nombreArchivo.endsWith(".xlsx") || 
                nombreArchivo.endsWith(".xls") || 
                nombreArchivo.endsWith(".csv"));
    }
    
    @PostMapping("/debug-archivo")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> debugArchivo(@RequestParam("archivo") MultipartFile archivo) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            System.out.println("🔍 === INICIANDO DEBUG MANUAL ===");
            
            // 🔥 MODIFICADO: Pasar TipoPrueba.SABER_PRO por defecto
            Map<String, Object> resultado = excelService.procesarArchivoResultados(archivo, TipoPrueba.SABER_PRO);
            
            @SuppressWarnings("unchecked")
            List<String> debugLogs = (List<String>) resultado.get("debugLogs");
            @SuppressWarnings("unchecked")
            List<String> errores = (List<String>) resultado.get("errores");
            
            response.put("success", true);
            response.put("debugLogs", debugLogs);
            response.put("errores", errores);
            response.put("totalProcesado", resultado.get("totalProcesado"));
            response.put("estadisticasBD", resultado.get("estadisticasBD"));
            
            // Mostrar logs en consola
            System.out.println("\n📋 LOGS DE DEBUG:");
            debugLogs.forEach(System.out::println);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error en debug: " + e.getMessage());
            e.printStackTrace();
        }
        
        return ResponseEntity.ok(response);
    }
    
    // ✅ ENDPOINT DE DIAGNÓSTICO - Agrégalo a tu ImportacionController
    @PostMapping("/debug-upload")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> debugUpload(@RequestParam("archivo") MultipartFile archivo) {
        Map<String, Object> response = new HashMap<>();
        
        System.out.println("🎯 ===== DEBUG UPLOAD INICIADO =====");
        System.out.println("📁 Archivo recibido: " + archivo.getOriginalFilename());
        System.out.println("📏 Tamaño: " + archivo.getSize() + " bytes");
        System.out.println("📋 Content Type: " + archivo.getContentType());
        System.out.println("🔍 ¿Está vacío?: " + archivo.isEmpty());
        
        try {
            // Verificar si es archivo Excel
            boolean esExcel = archivo.getOriginalFilename().toLowerCase().endsWith(".xlsx") || 
                             archivo.getOriginalFilename().toLowerCase().endsWith(".xls");
            
            System.out.println("✅ ¿Es archivo Excel?: " + esExcel);
            
            if (archivo.isEmpty()) {
                response.put("success", false);
                response.put("message", "El archivo está vacío");
                return ResponseEntity.badRequest().body(response);
            }
            
            if (!esExcel) {
                response.put("success", false);
                response.put("message", "No es un archivo Excel válido");
                return ResponseEntity.badRequest().body(response);
            }
            
            // Probar procesamiento
            System.out.println("🔄 Intentando procesar archivo...");
            
            // 🔥 MODIFICADO: Pasar TipoPrueba.SABER_PRO por defecto
            Map<String, Object> resultado = excelService.procesarArchivoResultados(archivo, TipoPrueba.SABER_PRO);
            
            response.put("success", true);
            response.put("message", "Debug completado");
            response.put("archivo", archivo.getOriginalFilename());
            response.put("tamaño", archivo.getSize());
            response.put("resultado", resultado);
            
            System.out.println("✅ DEBUG UPLOAD COMPLETADO");
            
        } catch (Exception e) {
            System.err.println("❌ ERROR EN DEBUG: " + e.getMessage());
            e.printStackTrace();
            response.put("success", false);
            response.put("message", "Error: " + e.getMessage());
        }
        
        return ResponseEntity.ok(response);
    }
    
    // En tu ImportacionController - Agrega este endpoint simple
    @GetMapping("/test")
    @ResponseBody
    public String testEndpoint() {
        System.out.println("✅ TEST ENDPOINT LLAMADO - " + new java.util.Date());
        return "✅ El controlador está funcionando - " + new java.util.Date();
    }
    
    // Agrega este método a tu ImportacionController
    @GetMapping("/simple")
    public String mostrarPaginaImportacionSimple() {
        return "coordinacion/importar-simple";
    }
}