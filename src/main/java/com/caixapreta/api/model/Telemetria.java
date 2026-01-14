package com.caixapreta.api.model;

import jakarta.persistence.*;
import lombok.Data;
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
    private Double odometro;
    private LocalDateTime dataHora;

    @PrePersist
    public void prePersist() {
        this.dataHora = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Viatura getViatura() {
        return viatura;
    }

    public void setViatura(Viatura viatura) {
        this.viatura = viatura;
    }

    public Double getVelocidade() {
        return velocidade;
    }

    public void setVelocidade(Double velocidade) {
        this.velocidade = velocidade;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public Double getForcaG() {
        return forcaG;
    }

    public void setForcaG(Double forcaG) {
        this.forcaG = forcaG;
    }

    public Double getIncX() {
        return incX;
    }

    public void setIncX(Double incX) {
        this.incX = incX;
    }

    public Double getIncY() {
        return incY;
    }

    public void setIncY(Double incY) {
        this.incY = incY;
    }

    public String getSireneStatus() {
        return sireneStatus;
    }

    public void setSireneStatus(String sireneStatus) {
        this.sireneStatus = sireneStatus;
    }

    public Double getNivelBateria() {
        return nivelBateria;
    }

    public void setNivelBateria(Double nivelBateria) {
        this.nivelBateria = nivelBateria;
    }

    public Double getOdometro() {
        return odometro;
    }

    public void setOdometro(Double odometro) {
        this.odometro = odometro;
    }

    public LocalDateTime getDataHora() {
        return dataHora;
    }

    public void setDataHora(LocalDateTime dataHora) {
        this.dataHora = dataHora;
    }

    /* * --- DOCUMENTAÇÃO DA CLASSE TELEMETRIA ---
     * 1. O QUE ELA FAZ:
     * Funciona como a "fita" da caixa preta. Ela guarda o histórico bruto de cada
     * segundo enviado pelo ESP32. É aqui que fazemos o rastro do mapa.
     * * 2. EXPLICAÇÃO DOS MÉTODOS E CAMPOS:
     * - viatura: Relacionamento (Chave Estrangeira). Diz a qual carro esse log pertence.
     * - incX / incY: Inclinação lateral e frontal. Crucial para detectar capotamento.
     * - forcaG: Medida de impacto ou aceleração brusca.
     * - prePersist(): Registra a data/hora do log automaticamente no momento em que
     * o dado chega no servidor, garantindo a precisão cronológica da perícia.
     */
}