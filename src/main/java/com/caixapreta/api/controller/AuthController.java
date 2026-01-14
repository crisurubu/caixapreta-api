package com.caixapreta.api.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "http://localhost:5173") // Permite que o React acesse
public class AuthController {

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> credentials) {
        String usuario = credentials.get("username");
        String senha = credentials.get("password");

        // Validação Simples (Substitua por busca no banco depois)
        if ("admin".equals(usuario) && "admin123".equals(senha)) {
            // Simulando um Token de acesso
            return ResponseEntity.ok(Map.of(
                    "token", "token-gerado-caixa-preta-2026",
                    "user", usuario
            ));
        }

        return ResponseEntity.status(401).body("Usuário ou senha inválidos");
    }
}