package com.caixapreta.api.service;

import com.caixapreta.api.model.Usuario;
import com.caixapreta.api.repository.UsuarioRepository;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;

    // Construtor Manual (Garante funcionamento sem Lombok)
    public CustomUserDetailsService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // 1. Busca o usuário no banco
        Usuario usuario = usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado: " + username));

        // 2. Converte nosso 'Usuario' para o 'UserDetails' que o Spring Security entende
        return User.builder()
                .username(usuario.getUsername())
                .password(usuario.getPassword())
                .roles(usuario.getRoles().toArray(new String[0]))
                .build();
    }

    /* * --- DOCUMENTAÇÃO DO CUSTOM_USER_DETAILS_SERVICE ---
     * 1. O QUE ELA FAZ:
     * Ensina o Spring Security a buscar as credenciais dentro da nossa tabela 'usuarios'.
     * 2. EXPLICAÇÃO:
     * - loadUserByUsername: É o método mestre da segurança. Ele pega o login digitado,
     * vai ao banco, recupera a senha criptografada e as permissões (Roles).
     */
}