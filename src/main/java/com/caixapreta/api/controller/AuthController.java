package com.caixapreta.api.controller;

import com.caixapreta.api.config.TokenService;
import com.caixapreta.api.model.Usuario;
import com.caixapreta.api.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "http://localhost:5173")
public class AuthController {

    @Autowired
    private UsuarioRepository repository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private TokenService tokenService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> credentials) {
        String username = credentials.get("username");
        String password = credentials.get("password");

        // 1. Busca real no banco de dados (que o Seeder criou)
        Usuario usuario = repository.findByUsername(username)
                .orElse(null);

        // 2. Valida a senha usando o sistema de criptografia
        if (usuario != null && passwordEncoder.matches(password, usuario.getPassword())) {

            // 3. GERA O TOKEN REAL (JWT)
            String token = tokenService.generateToken(usuario);

            return ResponseEntity.ok(Map.of(
                    "token", token,
                    "user", usuario.getUsername(),
                    "roles", usuario.getRoles()
            ));
        }

        return ResponseEntity.status(401).body("Usuário ou senha inválidos");
    }
}