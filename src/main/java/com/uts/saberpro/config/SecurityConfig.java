package com.uts.saberpro.config;

import com.uts.saberpro.repository.UsuarioRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final UsuarioRepository usuarioRepository;

    public SecurityConfig(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
        System.out.println("=== 🔧 SECURITY CONFIG INICIALIZADO ===");
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return NoOpPasswordEncoder.getInstance();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        System.out.println("=== 🔧 CONFIGURANDO FILTRO DE SEGURIDAD ===");
        
        http
            .authorizeHttpRequests(authz -> authz
                .requestMatchers("/css/**", "/js/**", "/images/**").permitAll()
                .requestMatchers("/login").permitAll()
                .requestMatchers("/admin/**").hasAuthority("ROLE_ADMIN")
                .requestMatchers("/coordinacion/**").hasAuthority("ROLE_COORDINACION")
                .requestMatchers("/estudiante/**").hasAuthority("ROLE_ESTUDIANTE")
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login")
                .loginProcessingUrl("/login")
                .successHandler(authenticationSuccessHandler())
                .failureUrl("/login?error=true")
                .permitAll()
            )
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login?logout")
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID")
                .permitAll()
            )
            .csrf(csrf -> csrf.disable());
        
        return http.build();
    }

    @Bean
    public AuthenticationSuccessHandler authenticationSuccessHandler() {
        return (request, response, authentication) -> {
            System.out.println("=== ✅ LOGIN EXITOSO ===");
            System.out.println("Usuario autenticado: " + authentication.getName());
            
            java.util.Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
            String redirectUrl = "/login?error";
            
            for (GrantedAuthority authority : authorities) {
                System.out.println("Rol del usuario: " + authority.getAuthority());
                if (authority.getAuthority().equals("ROLE_ADMIN")) {
                    redirectUrl = "/admin/dashboard";
                    break;
                } else if (authority.getAuthority().equals("ROLE_COORDINACION")) {
                    redirectUrl = "/coordinacion/dashboard";
                    break;
                } else if (authority.getAuthority().equals("ROLE_ESTUDIANTE")) {
                    redirectUrl = "/estudiante/dashboard";
                    break;
                }
            }
            System.out.println("Redirigiendo a: " + redirectUrl);
            response.sendRedirect(redirectUrl);
        };
    }

    // 🔥 ESTE ES EL UserDetailsService - DEBE ESTAR DENTRO DE SecurityConfig
    @Bean
    public UserDetailsService userDetailsService() {
        System.out.println("=== 🔧 REGISTRANDO UserDetailsService BEAN ===");
        
        return username -> {
            System.out.println("=== 🔍 UserDetailsService LLAMADO ===");
            System.out.println("Username recibido: '" + username + "'");
            
            // 1. Buscar por username normal
            Optional<com.uts.saberpro.entity.Usuario> usuarioOpt = usuarioRepository.findById(username);
            
            if (usuarioOpt.isPresent()) {
                System.out.println("✅ Usuario encontrado por username: " + username);
                return crearUserDetails(usuarioOpt.get());
            }
            
            // 2. Si es número, buscar por documento de estudiante
            if (username.matches("\\d+")) {
                System.out.println("🔍 Buscando como documento de estudiante: " + username);
                usuarioOpt = usuarioRepository.findByEstudiante_NumeroDocumento(username);
                
                if (usuarioOpt.isPresent()) {
                    System.out.println("✅ Estudiante encontrado por documento: " + username);
                    return crearUserDetails(usuarioOpt.get());
                }
            }
            
            System.out.println("❌ Usuario NO encontrado: " + username);
            throw new UsernameNotFoundException("Usuario no encontrado: " + username);
        };
    }
    
    private UserDetails crearUserDetails(com.uts.saberpro.entity.Usuario usuario) {
        System.out.println("=== 🎯 CREANDO USERDETAILS ===");
        System.out.println("Username: " + usuario.getUsername());
        System.out.println("Rol: " + usuario.getRol());
        System.out.println("Activo: " + usuario.getActivo());
        
        String rol = usuario.getRol().name();
        GrantedAuthority authority = new SimpleGrantedAuthority("ROLE_" + rol);
        
        return new User(
            usuario.getUsername(),
            usuario.getPassword(),
            usuario.getActivo(),
            true, true, true,
            Collections.singletonList(authority)
        );
    }
}