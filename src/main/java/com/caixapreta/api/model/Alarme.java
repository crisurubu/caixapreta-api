package com.caixapreta.api.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "alarmes")
public class Alarme {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, updatable = false)
    private String uuid;

    @ManyToOne
    @JoinColumn(name = "viatura_id")
    private Viatura viatura;

    private String tipoEvento;
    private Double gForce;
    private Double velocidade;
    private Double latitude;
    private Double longitude;
    private LocalDateTime dataHora;

    // --- NOVOS CAMPOS IMPLEMENTADOS PARA O LAUDO ---
    private Double incX;           // Inclinação lateral no momento do alarme
    private Double nivelBateria;   // Voltagem da bateria capturada no impacto
    private String endereco;       // Endereço textual resolvido via GPS

    @PrePersist
    protected void onCreate() {
        // Garante a geração do UUID e data se ainda não foram setados manualmente
        if (this.uuid == null) {
            this.uuid = UUID.randomUUID().toString();
        }
        if (this.dataHora == null) {
            this.dataHora = LocalDateTime.now();
        }
    }

    // Getters e Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getUuid() { return uuid; }
    public void setUuid(String uuid) { this.uuid = uuid; }
    public Viatura getViatura() { return viatura; }
    public void setViatura(Viatura viatura) { this.viatura = viatura; }
    public String getTipoEvento() { return tipoEvento; }
    public void setTipoEvento(String tipoEvento) { this.tipoEvento = tipoEvento; }
    public Double getgForce() { return gForce; }
    public void setgForce(Double gForce) { this.gForce = gForce; }
    public Double getVelocidade() { return velocidade; }
    public void setVelocidade(Double velocidade) { this.velocidade = velocidade; }
    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }
    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }
    public LocalDateTime getDataHora() { return dataHora; }
    public void setDataHora(LocalDateTime dataHora) { this.dataHora = dataHora; }

    // Implementação dos novos Getters e Setters
    public Double getIncX() { return incX; }
    public void setIncX(Double incX) { this.incX = incX; }
    public Double getNivelBateria() { return nivelBateria; }
    public void setNivelBateria(Double nivelBateria) { this.nivelBateria = nivelBateria; }
    public String getEndereco() { return endereco; }
    public void setEndereco(String endereco) { this.endereco = endereco; }

}

/**
 * --- DOCUMENTAÇÃO DO MODEL ALARME (ATUALIZADA) ---
 * 1. O QUE FAZ: Funciona como a "Caixa-Preta" do veículo, persistindo eventos críticos.
 * 2. NOVOS CAMPOS: Adicionado 'incX', 'nivelBateria' e 'endereco' para garantir que o laudo
 * pericial tenha todos os dados técnicos sem depender da tabela volátil de Viaturas.
 * 3. IDENTIDADE: O UUID é o elo de ligação entre o banco de dados e o PDF de auditoria.
 * 4. PERSISTÊNCIA: O uso de @PrePersist automatiza a segurança temporal e a unicidade do registro.
 */