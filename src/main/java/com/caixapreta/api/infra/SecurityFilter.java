package com.caixapreta.api.config;

import com.caixapreta.api.model.Usuario;
import com.caixapreta.api.repository.UsuarioRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;

@Component
public class SecurityFilter extends OncePerRequestFilter {

    @Autowired
    private TokenService tokenService;

    @Autowired
    private UsuarioRepository usuarioRepository;
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        var token = this.recoverToken(request);
        // LOG ZERO: Verificação de chegada
        System.out.println(">>> [FILTRO] Rota acessada: " + request.getRequestURI());
        System.out.println(">>> [FILTRO] Token recebido: " + (token != null ? "SIM" : "NÃO"));

        if(token != null){
            var subject = tokenService.validateToken(token);
            // LOG 1: Verificar o que o Token diz
            System.out.println(">>> [FILTRO] Token validado. Subject extraído: " + subject);

            if (subject != null) {
                Usuario user = usuarioRepository.findByUsername(subject).orElse(null);

                if(user != null) {
                    // LOG 2: Usuário encontrado
                    System.out.println(">>> [FILTRO] Usuário encontrado no Banco: " + user.getUsername());
                    System.out.println(">>> [FILTRO] Autoridades do Usuário: " + user.getAuthorities());

                    var authentication = new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                } else {
                    // LOG 3: O erro provável está aqui
                    System.out.println(">>> [FILTRO] ERRO: Usuário '" + subject + "' não existe na tabela USUARIOS!");
                }
            }
        }
        filterChain.doFilter(request, response);
    }

    private String recoverToken(HttpServletRequest request){
        var authHeader = request.getHeader("Authorization");
        if(authHeader == null) return null;
        return authHeader.replace("Bearer ", "");
    }
}