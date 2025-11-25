package com.uts.saberpro.service;

import com.uts.saberpro.entity.Usuario;
import com.uts.saberpro.repository.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;

    public UsuarioService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    public List<Usuario> obtenerTodosUsuarios() {
        return usuarioRepository.findAll();
    }

    public Optional<Usuario> obtenerUsuarioPorUsername(String username) {
        return usuarioRepository.findById(username);
    }

    public Optional<Usuario> obtenerUsuarioPorEmail(String email) {
        return usuarioRepository.findByEmail(email);
    }

    public Usuario guardarUsuario(Usuario usuario) {
        // 🔄 Asegurar que el username se genere desde el email si no existe
        if ((usuario.getUsername() == null || usuario.getUsername().trim().isEmpty()) 
            && usuario.getEmail() != null) {
            usuario.generarUsernameDesdeEmail();
        }
        
        // 🔄 Si el username generado ya existe, generar uno único
        if (usuario.getUsername() != null && existeUsername(usuario.getUsername())) {
            // Verificar si estamos editando un usuario existente o creando uno nuevo
            Optional<Usuario> usuarioExistente = obtenerUsuarioPorUsername(usuario.getUsername());
            if (usuarioExistente.isEmpty() || 
                !usuarioExistente.get().getEmail().equals(usuario.getEmail())) {
                // Es un usuario nuevo o el email cambió, generar username único
                String usernameUnico = generarUsernameUnicoDesdeEmail(usuario.getEmail());
                usuario.setUsername(usernameUnico);
            }
        }

        // 🔐 ACTUALIZADO: NO agregar {noop} - Spring Security usará el PasswordEncoder
        // El password se guarda tal como viene, sin modificar
        // Spring Security se encargará del encoding con BCryptPasswordEncoder
        
        // Si es usuario nuevo y no tiene estado, establecer como activo
        if (usuario.getActivo() == null) {
            usuario.setActivo(true);
        }
        
        return usuarioRepository.save(usuario);
    }

    public Usuario guardarUsuarioSinModificarPassword(Usuario usuario) {
        // Para cuando solo queremos actualizar otros campos pero mantener el password actual
        if (usuario.getActivo() == null) {
            usuario.setActivo(true);
        }
        return usuarioRepository.save(usuario);
    }

    public void eliminarUsuario(String username) {
        usuarioRepository.deleteById(username);
    }

    public long contarUsuarios() {
        return usuarioRepository.count();
    }

    public List<Usuario> obtenerUsuariosPorRol(Usuario.RolUsuario rol) {
        return usuarioRepository.findByRol(rol);
    }

    public List<Usuario> obtenerUsuariosActivos() {
        return usuarioRepository.findByActivoTrue();
    }

    public boolean existeEmail(String email) {
        return usuarioRepository.existsByEmail(email);
    }

    // ✅ CORREGIDO: Usar el método correcto del repository
    public Optional<Usuario> obtenerUsuarioPorEstudiante(String numeroDocumento) {
        return usuarioRepository.findByEstudiante_NumeroDocumento(numeroDocumento);
    }

    public void activarUsuario(String username) {
        usuarioRepository.findById(username).ifPresent(usuario -> {
            usuario.setActivo(true);
            usuarioRepository.save(usuario);
        });
    }

    public void desactivarUsuario(String username) {
        usuarioRepository.findById(username).ifPresent(usuario -> {
            usuario.setActivo(false);
            usuarioRepository.save(usuario);
        });
    }

    // 🔧 ACTUALIZADO: Método específico para actualizar solo el password (sin {noop})
    public void actualizarPassword(String username, String nuevoPassword) {
        usuarioRepository.findById(username).ifPresent(usuario -> {
            // Guardar el password tal como viene, sin {noop}
            usuario.setPassword(nuevoPassword);
            usuarioRepository.save(usuario);
        });
    }

    // 🔍 Método para verificar si un username ya existe
    public boolean existeUsername(String username) {
        return usuarioRepository.existsById(username);
    }

    // 🔍 Método para buscar usuarios por nombre o apellido
    public List<Usuario> buscarUsuariosPorNombre(String texto) {
        return usuarioRepository.findAll().stream()
                .filter(usuario -> 
                    usuario.getNombres().toLowerCase().contains(texto.toLowerCase()) ||
                    usuario.getApellidos().toLowerCase().contains(texto.toLowerCase()))
                .toList();
    }

    // 🔄 Método para generar un username único desde un email
    public String generarUsernameUnicoDesdeEmail(String email) {
        if (email == null || !email.contains("@")) {
            throw new IllegalArgumentException("Email no válido: " + email);
        }
        
        String baseUsername = email.substring(0, email.indexOf('@'));
        String username = baseUsername;
        int counter = 1;
        
        // Si el username base ya existe, agregar números hasta encontrar uno disponible
        while (existeUsername(username)) {
            username = baseUsername + counter;
            counter++;
            
            // Prevención de bucle infinito (por si acaso)
            if (counter > 1000) {
                throw new IllegalStateException("No se pudo generar un username único después de 1000 intentos");
            }
        }
        
        return username;
    }

    // 🔄 Método para crear usuario directamente desde email (más simple)
    public Usuario crearUsuarioDesdeEmail(String email, String password, String nombres, 
                                        String apellidos, Usuario.RolUsuario rol) {
        Usuario usuario = new Usuario();
        usuario.setEmail(email); // Esto generará el username automáticamente
        usuario.setPassword(password);
        usuario.setNombres(nombres);
        usuario.setApellidos(apellidos);
        usuario.setRol(rol);
        usuario.setActivo(true);
        
        return guardarUsuario(usuario);
    }

    // 🔄 Método para verificar si se puede usar un email (no existe o pertenece al mismo usuario)
    public boolean emailDisponible(String email, String usernameActual) {
        Optional<Usuario> usuarioConEmail = usuarioRepository.findByEmail(email);
        
        // Si no existe usuario con ese email, está disponible
        if (usuarioConEmail.isEmpty()) {
            return true;
        }
        
        // Si existe, verificar si pertenece al usuario actual (para edición)
        return usuarioConEmail.get().getUsername().equals(usernameActual);
    }
}