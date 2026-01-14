package com.caixapreta.api.model;

import jakarta.persistence.*;
import java.util.Set;

@Entity
@Table(name = "usuarios")
public class Usuario {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(nullable = false)
    private String password;

    @ElementCollection(fetch = FetchType.EAGER)
    private Set<String> roles; // ADMIN, OPERADOR

    // --- GETTERS E SETTERS MANUAIS ---
    public Long getId() { return id; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public Set<String> getRoles() { return roles; }
    public void setRoles(Set<String> roles) { this.roles = roles; }

    /* * --- DOCUMENTAÇÃO ---
     * 1. O QUE FAZ: Armazena as credenciais de quem acessa o Dashboard.
     * 2. SEGURANÇA: O campo 'password' guardará o HASH da senha, nunca a senha real.
     */
}