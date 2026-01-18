package com.caixapreta.api.config;

import com.caixapreta.api.model.Usuario;
import com.caixapreta.api.model.Viatura;
import com.caixapreta.api.repository.UsuarioRepository;
import com.caixapreta.api.repository.ViaturaRepository;
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
            ViaturaRepository viaturaRepo,
            PasswordEncoder encoder) {
        return args -> {

            // 1. CRIAÇÃO DO USUÁRIO ADMIN
            if (userRepo.count() == 0) {
                Usuario admin = new Usuario();
                admin.setUsername("admin");
                admin.setPassword(encoder.encode("admin123"));
                admin.setRoles(Set.of("ADMIN"));
                userRepo.save(admin);

                System.out.println("==============================================");
                System.out.println(">>> [AUTH] SUCESSO: USUÁRIO MESTRE CRIADO!");
                System.out.println(">>> LOGIN: admin | SENHA: admin123");
            }

            // 2. CRIAÇÃO DE VIATURA PARA TESTE (Ajustada para novos campos)
            if (viaturaRepo.count() == 0) {
                Viatura vtrTeste = new Viatura();
                vtrTeste.setId(1L);
                vtrTeste.setPrefixo("ROTA-01");
                vtrTeste.setPlaca("BRA2E26");
                vtrTeste.setModelo("Toyota Hilux");
                vtrTeste.setStatusOperacional("PATRULHANDO");
                vtrTeste.setBloqueada(false);
                vtrTeste.setNivelBateria(100.0);

                // --- INICIALIZAÇÃO DOS NOVOS CAMPOS (Evita NullPointerException) ---
                vtrTeste.setOdometroManutencao(0.0);
                vtrTeste.setKmDiarioAtual(0.0);
                vtrTeste.setLatitude(0.0);
                vtrTeste.setLongitude(0.0);

                viaturaRepo.save(vtrTeste);
                System.out.println(">>> [FROTA] SUCESSO: VIATURA ID 1 (OFICIAL) CADASTRADA COM ODÔMETRO ZERADO!");
            }

            System.out.println("==============================================");
            System.out.println(">>> [SISTEMA] STATUS: PRONTO.");
            System.out.println("==============================================");
        };
    }

    /* * --- DOCUMENTAÇÃO DO DATABASE_SEEDER (VERSÃO DE TESTE) ---
     * 1. PROVISIONAMENTO HÍBRIDO: Garante tanto o acesso administrativo quanto o operacional para validação imediata.
     * 2. ESTADO ZERO: Inicializa explicitamente os campos de odômetro e coordenadas com 0.0, prevenindo falhas de
     * cálculo matemático (Null Safety) no processamento do primeiro sinal de telemetria.
     * 3. PERSISTÊNCIA DE IDENTIDADE: Fixa o ID 1 para sincronia direta com dispositivos de hardware (ESP32).
     */
}