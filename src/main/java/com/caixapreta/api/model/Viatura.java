package com.caixapreta.api.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "VIATURAS")
public class Viatura {
    @Id
    private Long id;

    // --- DADOS DE CADASTRO (Estáticos) ---
    private String placa;
    private String chassi;
    private String modelo;
    private String prefixo;

    // --- ESTADO TÁTICO (Define a COR no Mapa e Painel) ---
    private String statusOperacional; // PATRULHANDO, EM_OCORRENCIA, ACIDENTE, ABORDAGEM
    private Boolean bloqueada = false;
    private LocalDateTime ultimaAtualizacao;

    // --- ESTADO DE HARDWARE E TELEMETRIA (Cards e Diagnóstico) ---
    private Boolean gpsValido = true;
    private Double nivelBateria;

    // --- NOVOS CAMPOS PARA ODÔMETRO E POSIÇÃO (Ajuste solicitado) ---
    private Double latitude = 0.0;
    private Double longitude = 0.0;

    @Column(name = "odometro_manutencao")
    private Double odometroManutencao = 0.0; // Acumulador vitalício (Nunca zera)

    @Column(name = "km_diario_atual")
    private Double kmDiarioAtual = 0.0;    // Contador que o Service zera diariamente

    // Definimos um tamanho maior para suportar a concatenação de múltiplos alertas
    @Column(length = 500)
    private String alertaAdicional;

    @PrePersist @PreUpdate
    public void ajustarData() {
        this.ultimaAtualizacao = LocalDateTime.now();
    }

    // --- GETTERS E SETTERS ATUALIZADOS ---
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getPlaca() { return placa; }
    public void setPlaca(String placa) { this.placa = placa; }

    public String getChassi() { return chassi; }
    public void setChassi(String chassi) { this.chassi = chassi; }

    public String getModelo() { return modelo; }
    public void setModelo(String modelo) { this.modelo = modelo; }

    public String getPrefixo() { return prefixo; }
    public void setPrefixo(String prefixo) { this.prefixo = prefixo; }

    public String getStatusOperacional() { return statusOperacional; }
    public void setStatusOperacional(String statusOperacional) { this.statusOperacional = statusOperacional; }

    public Boolean getBloqueada() { return bloqueada != null && bloqueada; }
    public void setBloqueada(Boolean bloqueada) { this.bloqueada = bloqueada; }

    public LocalDateTime getUltimaAtualizacao() { return ultimaAtualizacao; }
    public void setUltimaAtualizacao(LocalDateTime ultimaAtualizacao) { this.ultimaAtualizacao = ultimaAtualizacao; }

    public Boolean getGpsValido() { return gpsValido; }
    public void setGpsValido(Boolean gpsValido) { this.gpsValido = gpsValido; }

    public Double getNivelBateria() { return nivelBateria; }
    public void setNivelBateria(Double nivelBateria) { this.nivelBateria = nivelBateria; }

    public String getAlertaAdicional() { return alertaAdicional; }
    public void setAlertaAdicional(String alertaAdicional) { this.alertaAdicional = alertaAdicional; }

    // Novos Getters e Setters para a lógica de Quilometragem
    public Double getLatitude() { return latitude != null ? latitude : 0.0; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }

    public Double getLongitude() { return longitude != null ? longitude : 0.0; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }

    public Double getOdometroManutencao() { return odometroManutencao != null ? odometroManutencao : 0.0; }
    public void setOdometroManutencao(Double odometroManutencao) { this.odometroManutencao = odometroManutencao; }

    public Double getKmDiarioAtual() { return kmDiarioAtual != null ? kmDiarioAtual : 0.0; }
    public void setKmDiarioAtual(Double kmDiarioAtual) { this.kmDiarioAtual = kmDiarioAtual; }

    /* * --- DOCUMENTAÇÃO DA ENTIDADE VIATURA (PRODUTO FINAL) ---
     * 1. O QUE ELA FAZ: Atua como o "Estado Atual" (Single Source of Truth) de cada unidade da frota no banco de dados.
     * 2. SEPARAÇÃO DE ESTADOS: Divide a informação em 'statusOperacional' (foco tático/visual) e 'alertaAdicional'
     * (foco técnico/sensores), impedindo conflitos de prioridade entre telemetria e segurança.
     * 3. INTEGRIDADE FÍSICA: Inclui campos de 'placa' e 'chassi' para vincular o dispositivo eletrônico a um veículo real.
     * 4. PROTEÇÃO DE DADOS: O campo 'bloqueada' serve como um semáforo para o sistema de escrita, garantindo que
     * evidências de acidentes não sejam sobrescritas por novos sinais de patrulha.
     * 5. RESILIÊNCIA DE TELEMETRIA: Armazena o odômetro de manutenção e o km diário diretamente, permitindo a limpeza
     * periódica da tabela de logs (Telemetria) sem perda dos acumuladores de quilometragem.
     */
}