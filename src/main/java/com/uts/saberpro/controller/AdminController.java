package com.uts.saberpro.controller;

import com.uts.saberpro.entity.Usuario;
import com.uts.saberpro.service.UsuarioService;
import com.uts.saberpro.service.EstudianteService;
import com.uts.saberpro.service.ResultadoPruebaService;
import com.uts.saberpro.service.BeneficioService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final UsuarioService usuarioService;
    private final EstudianteService estudianteService;
    private final ResultadoPruebaService resultadoPruebaService;
    private final BeneficioService beneficioService;

    public AdminController(UsuarioService usuarioService,
                         EstudianteService estudianteService,
                         ResultadoPruebaService resultadoPruebaService,
                         BeneficioService beneficioService) {
        this.usuarioService = usuarioService;
        this.estudianteService = estudianteService;
        this.resultadoPruebaService = resultadoPruebaService;
        this.beneficioService = beneficioService;
    }

    @GetMapping("/dashboard")
    public String adminDashboard(Model model) {
        try {
            // Obtener estadísticas reales de la base de datos
            long totalUsuarios = usuarioService.contarUsuarios();
            long estudiantesActivos = usuarioService.obtenerUsuariosPorRol(Usuario.RolUsuario.ESTUDIANTE).size();
            long resultadosCargados = resultadoPruebaService.contarResultados();
            long beneficiosAsignados = beneficioService.contarTotalBeneficios();
            
            // Agregar estadísticas al modelo
            model.addAttribute("totalUsuarios", totalUsuarios);
            model.addAttribute("estudiantesActivos", estudiantesActivos);
            model.addAttribute("resultadosCargados", resultadosCargados);
            model.addAttribute("beneficiosAsignados", beneficiosAsignados);
            
        } catch (Exception e) {
            // En caso de error, establecer valores por defecto
            model.addAttribute("totalUsuarios", 0);
            model.addAttribute("estudiantesActivos", 0);
            model.addAttribute("resultadosCargados", 0);
            model.addAttribute("beneficiosAsignados", 0);
        }
        
        return "admin/dashboard";
    }

    // ... (el resto de tus métodos permanecen igual)
    @GetMapping("/usuarios")
    public String gestionUsuarios(Model model) {
        List<Usuario> usuarios = usuarioService.obtenerTodosUsuarios();
        model.addAttribute("usuarios", usuarios);
        return "admin/usuarios";
    }

    @GetMapping("/usuarios/nuevo")
    public String mostrarFormularioNuevoUsuario(Model model) {
        model.addAttribute("usuario", new Usuario());
        model.addAttribute("roles", Usuario.RolUsuario.values());
        return "admin/form-usuario";
    }

    @GetMapping("/usuarios/editar/{username}")
    public String mostrarFormularioEditarUsuario(@PathVariable String username, Model model) {
        Optional<Usuario> usuarioOpt = usuarioService.obtenerUsuarioPorUsername(username);
        if (usuarioOpt.isPresent()) {
            model.addAttribute("usuario", usuarioOpt.get());
            model.addAttribute("roles", Usuario.RolUsuario.values());
            return "admin/form-usuario";
        } else {
            return "redirect:/admin/usuarios?error=Usuario no encontrado";
        }
    }

    @PostMapping("/usuarios/guardar")
    public String guardarUsuario(@ModelAttribute Usuario usuario, 
                               @RequestParam(required = false) String confirmarPassword,
                               RedirectAttributes redirectAttributes) {
        try {
            // 🔄 NUEVO: Validar email como campo principal (en lugar de username)
            if (usuario.getEmail() == null || usuario.getEmail().trim().isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "El email es requerido");
                return usuario.getUsername() != null ? 
                    "redirect:/admin/usuarios/editar/" + usuario.getUsername() : 
                    "redirect:/admin/usuarios/nuevo";
            }

            // 🔄 NUEVO: Determinar si es usuario nuevo (username vacío o null)
            boolean esNuevo = (usuario.getUsername() == null || usuario.getUsername().trim().isEmpty());
            
            if (esNuevo) {
                // 🔄 NUEVO: Para usuario nuevo, validaciones específicas
                
                // Validar password para usuario nuevo
                if (usuario.getPassword() == null || usuario.getPassword().trim().isEmpty()) {
                    redirectAttributes.addFlashAttribute("error", "La contraseña es requerida para nuevos usuarios");
                    return "redirect:/admin/usuarios/nuevo";
                }
                
                // Validar confirmación de password
                if (!usuario.getPassword().equals(confirmarPassword)) {
                    redirectAttributes.addFlashAttribute("error", "Las contraseñas no coinciden");
                    return "redirect:/admin/usuarios/nuevo";
                }
                
                // 🔄 NUEVO: Verificar si el email ya existe
                if (usuarioService.existeEmail(usuario.getEmail())) {
                    redirectAttributes.addFlashAttribute("error", "El email ya está registrado");
                    return "redirect:/admin/usuarios/nuevo";
                }
                
                // 🔄 NUEVO: El username se generará automáticamente en el servicio
                // No necesitamos validar username aquí porque se genera desde el email
                
            } else {
                // 🔄 NUEVO: Para edición, validar que el email no esté usado por otro usuario
                if (!usuarioService.emailDisponible(usuario.getEmail(), usuario.getUsername())) {
                    redirectAttributes.addFlashAttribute("error", "El email ya está registrado por otro usuario");
                    return "redirect:/admin/usuarios/editar/" + usuario.getUsername();
                }
                
                // Para edición: si el password está vacío, mantener el actual
                if (usuario.getPassword() == null || usuario.getPassword().trim().isEmpty()) {
                    // Obtener el usuario actual para mantener el password
                    Optional<Usuario> usuarioExistente = usuarioService.obtenerUsuarioPorUsername(usuario.getUsername());
                    if (usuarioExistente.isPresent()) {
                        usuario.setPassword(usuarioExistente.get().getPassword());
                    }
                } else {
                    // Si se está cambiando el password, validar confirmación
                    if (!usuario.getPassword().equals(confirmarPassword)) {
                        redirectAttributes.addFlashAttribute("error", "Las contraseñas no coinciden");
                        return "redirect:/admin/usuarios/editar/" + usuario.getUsername();
                    }
                }
            }

            // Establecer activo por defecto si es null
            if (usuario.getActivo() == null) {
                usuario.setActivo(true);
            }

            usuarioService.guardarUsuario(usuario);
            redirectAttributes.addFlashAttribute("success", 
                esNuevo ? "Usuario creado exitosamente" : "Usuario actualizado exitosamente");
                
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al guardar el usuario: " + e.getMessage());
            return "redirect:/admin/usuarios";
        }
        return "redirect:/admin/usuarios";
    }

    @PostMapping("/usuarios/{username}/activar")
    public String activarUsuario(@PathVariable String username, RedirectAttributes redirectAttributes) {
        try {
            usuarioService.activarUsuario(username);
            redirectAttributes.addFlashAttribute("success", "Usuario activado correctamente");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al activar el usuario: " + e.getMessage());
        }
        return "redirect:/admin/usuarios";
    }

    @PostMapping("/usuarios/{username}/desactivar")
    public String desactivarUsuario(@PathVariable String username, RedirectAttributes redirectAttributes) {
        try {
            usuarioService.desactivarUsuario(username);
            redirectAttributes.addFlashAttribute("warning", "Usuario desactivado correctamente");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al desactivar el usuario: " + e.getMessage());
        }
        return "redirect:/admin/usuarios";
    }
    @PostMapping("/usuarios/{username}/eliminar")
    public String eliminarUsuario(@PathVariable String username, RedirectAttributes redirectAttributes) {
        try {
            // No permitir eliminar al usuario admin actual
            if ("admin".equals(username)) {
                redirectAttributes.addFlashAttribute("error", "No se puede eliminar el usuario admin principal");
                return "redirect:/admin/usuarios";
            }
            
            // 🔄 NUEVO: Verificar si es estudiante ANTES de intentar eliminar
            Optional<Usuario> usuarioOpt = usuarioService.obtenerUsuarioPorUsername(username);
            if (usuarioOpt.isPresent()) {
                Usuario usuario = usuarioOpt.get();
                
                // Si es estudiante, NO PERMITIR eliminación
                if (usuario.getRol() == Usuario.RolUsuario.ESTUDIANTE) {
                    redirectAttributes.addFlashAttribute("error", 
                        "🚫 <strong>No se puede eliminar usuarios con rol ESTUDIANTE</strong><br><br>" +
                        "📋 Los estudiantes tienen datos académicos asociados que deben conservarse.<br>" +
                        "💡 <strong>Solución:</strong> En lugar de eliminar, puedes <strong>desactivar</strong> el usuario.");
                    return "redirect:/admin/usuarios";
                }
            }
            
            usuarioService.eliminarUsuario(username);
            redirectAttributes.addFlashAttribute("success", "✅ Usuario eliminado correctamente");
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", 
                "❌ Error inesperado al eliminar el usuario: " + e.getMessage());
        }
        
        return "redirect:/admin/usuarios";
    }
/*
    @GetMapping("/reportes")
    public String reportesSistema() {
        return "admin/reportes";
    }
*/
    @GetMapping("/configuracion")
    public String configuracionSistema() {
        return "admin/configuracion";
    }

    @GetMapping("/seguridad")
    public String seguridadSistema() {
        return "admin/seguridad";
    }

    @GetMapping("/auditoria")
    public String auditoriaSistema() {
        return "admin/auditoria";
    }

    @GetMapping("/backup")
    public String backupSistema() {
        return "admin/backup";
    }
}