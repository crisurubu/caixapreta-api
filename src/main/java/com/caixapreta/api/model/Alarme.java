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
    private String uuid; // Identificador único e imutável para o Laudo PDF

    @ManyToOne
    @JoinColumn(name = "viatura_id")
    private Viatura viatura;

    private String tipoEvento; // ACIDENTE, EMERGENCIA, ABORDAGEM, MANUTENCAO
    private Double gForce;
    private Double velocidade;
    private Double latitude;
    private Double longitude;
    private LocalDateTime dataHora;

    @PrePersist
    protected void onCreate() {
        // Gera automaticamente o UUID e a data antes de salvar no banco
        this.uuid = UUID.randomUUID().toString();
        this.dataHora = LocalDateTime.now();
    }

    /* * --- DOCUMENTAÇÃO DO MODEL ALARME ---
     * 1. O QUE FAZ: Registra eventos críticos que exigem perícia posterior.
     * 2. SEGURANÇA: Usa UUID em vez de ID sequencial para links externos de laudos.
     * 3. INTEGRIDADE: O uso de @PrePersist garante que a marca temporal seja do servidor.
     */

    // Getters e Setters Manuais (Padrão sem Lombok)
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
}