package com.uts.saberpro.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class AuthController {
    
    @GetMapping("/login")
    public String login() {
        return "auth/login";
    }
    
    @GetMapping("/")
    public String home() {
        return "redirect:/login";
    }
    
    // 🔄 NUEVO: Endpoint para detectar tipo de login
    @PostMapping("/procesar-login")
    public String procesarLogin(@RequestParam String username) {
        // Si el username es un número (documento), es estudiante
        // Si es texto, es admin/coordinador
        if (username.matches("\\d+")) {
            // Es número de documento - buscar usuario por estudiante_id
            return "redirect:/login-estudiante?documento=" + username;
        } else {
            // Es username normal - dejar que Spring Security maneje
            return "redirect:/login?username=" + username;
        }
    }
}