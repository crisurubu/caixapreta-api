package com.caixapreta.api.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private com.caixapreta.api.config.SecurityFilter securityFilter;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                // Necessário para visualizar o banco de dados H2 no navegador
                .headers(headers -> headers.frameOptions(frame -> frame.disable()))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // 1. ACESSO AO BANCO E AUTH
                        .requestMatchers("/h2-console/**").permitAll()
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers("/api/historico-vida/**").permitAll()


                        // 2. DISPOSITIVOS E SIMULAÇÃO (POSTMAN)
                        // Permitimos alarmes e telemetria para que os dispositivos enviem dados sem Token (Simulando IoT)
                        .requestMatchers("/api/alarmes/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/telemetria/**").permitAll()

                        // 3. CONSULTA PÚBLICA/PAINEL (SEM DADOS SENSÍVEIS)
                        .requestMatchers("/api/viaturas/painel").permitAll()
                        .requestMatchers("/api/viaturas/stats").permitAll()

                        // 4. GESTÃO E DESTRAVA (AUDITORIA HUMANA)
                        // Apenas ADMIN pode ver auditoria e aprovar destravas
                        // No SecurityConfig.java, mude para:

                        //.requestMatchers("/api/historico-vida/**").hasAnyAuthority("ADMIN", "ROLE_ADMIN", "HARDWARE")
                        .requestMatchers("/api/auditoria/**").hasAnyAuthority("ADMIN", "ROLE_ADMIN")
                        .requestMatchers("/api/viaturas/destrava/aprovar/**").hasAnyAuthority("ADMIN", "ROLE_ADMIN")
                        .requestMatchers("/api/viaturas/destrava/pendentes").hasAnyAuthority("ADMIN", "ROLE_ADMIN")
                        .requestMatchers("/api/viaturas/destrava/solicitar").authenticated()

                        // ✅ REGRAS DE CADASTRO E HOMOLOGAÇÃO (Ajustadas para maior abrangência)
                        .requestMatchers("/api/viaturas/pendentes").hasAnyAuthority("ADMIN", "ROLE_ADMIN")
                        .requestMatchers("/api/viaturas/pendentes/**").hasAnyAuthority("ADMIN", "ROLE_ADMIN")
                        .requestMatchers("/api/admin/viaturas/**").hasAnyAuthority("ADMIN", "ROLE_ADMIN")
                        // 5. SOLICITAÇÕES (OPERADOR AUTENTICADO)

                        .anyRequest().authenticated()
                )
                .addFilterBefore(securityFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // ✅ ADICIONE O SEU IP NA LISTA DE ORIGENS PERMITIDAS
        configuration.setAllowedOrigins(Arrays.asList(
                "http://localhost:5173",
                "http://127.0.0.1:5173",
                "http://192.168.0.109:5173" // Adicione o IP que o Front está usando
        ));

        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type", "X-Requested-With"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}