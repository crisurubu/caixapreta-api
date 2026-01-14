package com.caixapreta.api.config;

import com.caixapreta.api.model.Usuario;
import com.caixapreta.api.repository.UsuarioRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.util.Set;

@Configuration
public class DatabaseSeeder {

    @Bean
    CommandLineRunner initDatabase(
            UsuarioRepository userRepo,
            PasswordEncoder encoder) {
        return args -> {
            // 1. CRIAÇÃO DO USUÁRIO ADMIN (Essencial para o Login)
            if (userRepo.count() == 0) {
                Usuario admin = new Usuario();
                admin.setUsername("admin");
                admin.setPassword(encoder.encode("admin123"));
                admin.setRoles(Set.of("ADMIN"));
                userRepo.save(admin);

                System.out.println("==============================================");
                System.out.println(">>> [AUTH] SUCESSO: USUÁRIO MESTRE CRIADO!");
                System.out.println("LOGIN: admin | SENHA: admin123");
                System.out.println("==============================================");
            }

            // A Viatura não é mais criada aqui para testarmos o Auto-Provisionamento
            System.out.println(">>> [SISTEMA] PRONTO. AGUARDANDO DADOS DO ESP32 OU POSTMAN...");
        };
    }

    /* * --- DOCUMENTAÇÃO DO DATABASE_SEEDER ---
     * 1. O QUE FAZ: Realiza a "semeadura" inicial de dados vitais sempre que o banco de dados inicia zerado.
     * 2. FOCO EM SEGURANÇA: Garante a existência de um usuário administrativo com senha criptografada
     * para permitir o acesso imediato ao Dashboard.
     * 3. AUTO-PROVISIONAMENTO: Remove a criação manual de viaturas para permitir que o sistema valide
     * a lógica de cadastro automático no primeiro sinal de telemetria recebido.
     */
}