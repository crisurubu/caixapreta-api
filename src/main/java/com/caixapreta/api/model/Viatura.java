package com.caixapreta.api.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "VIATURAS")
public class Viatura {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // Ex: VTR-04

    // Dados de Cadastro (Obrigatórios para rodar)
    private String placa;
    private String chassi;
    private String modelo;
    private String prefixo;

    // Dados de Estado (Dinâmicos)
    private String statusOperacional; // PATRULHA, ACIDENTE, etc.
    private Boolean bloqueada;
    private LocalDateTime ultimaAtualizacao;
    private Boolean gpsValido = true; // Novo campo para o "Cérebro" usar

    @PrePersist @PreUpdate
    public void ajustarData() {
        this.ultimaAtualizacao = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getPlaca() {
        return placa;
    }

    public void setPlaca(String placa) {
        this.placa = placa;
    }

    public String getChassi() {
        return chassi;
    }

    public void setChassi(String chassi) {
        this.chassi = chassi;
    }

    public String getModelo() {
        return modelo;
    }

    public void setModelo(String modelo) {
        this.modelo = modelo;
    }

    public String getPrefixo() {
        return prefixo;
    }

    public void setPrefixo(String prefixo) {
        this.prefixo = prefixo;
    }

    public String getStatusOperacional() {
        return statusOperacional;
    }

    public void setStatusOperacional(String statusOperacional) {
        this.statusOperacional = statusOperacional;
    }

    public Boolean getBloqueada() {
        return bloqueada;
    }

    public void setBloqueada(Boolean bloqueada) {
        this.bloqueada = bloqueada;
    }

    public LocalDateTime getUltimaAtualizacao() {
        return ultimaAtualizacao;
    }

    public void setUltimaAtualizacao(LocalDateTime ultimaAtualizacao) {
        this.ultimaAtualizacao = ultimaAtualizacao;
    }

    public Boolean getGpsValido() {
        return gpsValido;
    }

    public void setGpsValido(Boolean gpsValido) {
        this.gpsValido = gpsValido;
    }
    /* --- DOCUMENTAÇÃO ---
     * 1. O QUE FAZ: Une o registro físico do veículo ao seu estado lógico atual.
     * 2. CAMPOS: 'placa' e 'chassi' são agora campos de auditoria obrigatórios.
     */
}