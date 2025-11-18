package com.concessionaria.controller;

import com.concessionaria.service.AuthService;
import com.concessionaria.service.LogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {
    @Autowired
    private AuthService authService;
    
    @Autowired
    private LogService logService;
    
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> credenciais) {
        try {
            String email = credenciais.get("email");
            String senha = credenciais.get("senha");
            
            if (email == null || senha == null || email.isEmpty() || senha.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("erro", "Email e senha são obrigatórios"));
            }
            
            Map<String, Object> response = authService.login(email, senha);
            
            // Tentar registrar log, mas não falhar se MongoDB não estiver disponível
            try {
                logService.registrar(
                    (String) response.get("usuarioId"),
                    "login",
                    null
                );
            } catch (Exception e) {
                // Ignora erro de log (MongoDB pode não estar rodando)
            }
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("erro", e.getMessage()));
        }
    }
}

