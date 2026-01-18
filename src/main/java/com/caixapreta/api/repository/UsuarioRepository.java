package com.caixapreta.api.repository;

import com.caixapreta.api.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    // Busca um usuário pelo nome para conferir a senha no login
    Optional<Usuario> findByUsername(String username);



    /* * --- DOCUMENTAÇÃO DO USUARIO_REPOSITORY ---
     * 1. O QUE ELA FAZ:
     * É a ponte entre o código Java e a tabela de usuários no banco de dados H2.
     * 2. EXPLICAÇÃO:
     * - findByUsername: Método essencial para o Spring Security localizar o
     * usuário no banco durante o processo de autenticação.
     */
}