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

        System.out.println(">>> [AUTH] Tentativa de login para: " + username);

        Usuario usuario = repository.findByUsername(username).orElse(null);

        if (usuario != null && passwordEncoder.matches(password, usuario.getPassword())) {

            // VERIFICAÇÃO DE SEGURANÇA:
            if (usuario.getUsername() == null || usuario.getUsername().isEmpty()) {
                System.out.println(">>> [ERRO FATAL] Usuário encontrado no banco, mas o campo USERNAME está VAZIO!");
                return ResponseEntity.status(500).body("Erro interno: Nome de usuário não carregado.");
            }

            String token = tokenService.generateToken(usuario);

            System.out.println(">>> [AUTH] Login Sucesso! Token gerado para: " + usuario.getUsername());

            return ResponseEntity.ok(Map.of(
                    "token", token,
                    "user", usuario.getUsername(),
                    "roles", usuario.getRoles()
            ));
        }

        return ResponseEntity.status(401).body("Usuário ou senha inválidos");
    }

}