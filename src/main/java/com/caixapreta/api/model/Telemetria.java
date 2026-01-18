package com.caixapreta.api.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "telemetria_logs")
public class Telemetria {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private Viatura viatura;

    private Double velocidade;
    private Double latitude;
    private Double longitude;
    private Double forcaG;
    private Double incX;
    private Double incY;
    private String sireneStatus;
    private Double nivelBateria;

    // --- AJUSTE: Campos de Odômetro conforme nossa nova estratégia ---
    private Double odometroTotal; // Para auditoria de manutenção
    private Double kmDoDia;       // Para histórico de produtividade diária

    private LocalDateTime dataHora;

    @PrePersist
    public void prePersist() {
        this.dataHora = LocalDateTime.now();
    }

    // --- GETTERS E SETTERS ---
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Viatura getViatura() { return viatura; }
    public void setViatura(Viatura viatura) { this.viatura = viatura; }

    public Double getVelocidade() { return velocidade; }
    public void setVelocidade(Double velocidade) { this.velocidade = velocidade; }

    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }

    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }

    public Double getForcaG() { return forcaG; }
    public void setForcaG(Double forcaG) { this.forcaG = forcaG; }

    public Double getIncX() { return incX; }
    public void setIncX(Double incX) { this.incX = incX; }

    public Double getIncY() { return incY; }
    public void setIncY(Double incY) { this.incY = incY; }

    public String getSireneStatus() { return sireneStatus; }
    public void setSireneStatus(String sireneStatus) { this.sireneStatus = sireneStatus; }

    public Double getNivelBateria() { return nivelBateria; }
    public void setNivelBateria(Double nivelBateria) { this.nivelBateria = nivelBateria; }

    public Double getOdometroTotal() { return odometroTotal; }
    public void setOdometroTotal(Double odometroTotal) { this.odometroTotal = odometroTotal; }

    public Double getKmDoDia() { return kmDoDia; }
    public void setKmDoDia(Double kmDoDia) { this.kmDoDia = kmDoDia; }

    public LocalDateTime getDataHora() { return dataHora; }
    public void setDataHora(LocalDateTime dataHora) { this.dataHora = dataHora; }

    /* * --- DOCUMENTAÇÃO DA CLASSE TELEMETRIA (VERSÃO HISTÓRICO) ---
     * 1. O QUE ELA FAZ: Atua como o log transacional. Diferente da entidade Viatura,
     * esta classe pode ter seus registros limpos periodicamente para economizar espaço.
     * 2. REGISTRO DE ODÔMETRO: Armazena o 'odometroTotal' e 'kmDoDia' no instante do log.
     * Isso permite que, mesmo limpando o banco no futuro, o rastro de quilometragem
     * percorrida em datas específicas permaneça auditável nos logs remanescentes.
     * 3. INTEGRIDADE: O prePersist garante que o carimbo de tempo seja do servidor,
     * evitando fraudes de data enviadas pelo hardware.
     */
}