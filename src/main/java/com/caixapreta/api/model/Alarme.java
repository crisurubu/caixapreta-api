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

    // REMOVIDO o unique=true para permitir que múltiplos logs (Tombamento + Solicitação)
    // compartilhem o mesmo UUID de Incidente.
    @Column(nullable = false, updatable = false)
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

    private Double incX;
    private Double incY;
    private Double nivelBateria;
    private String endereco;

    @PrePersist
    protected void onCreate() {
        // Agora o Service tentará setar o UUID antes.
        // Se chegar aqui sem UUID, ele gera um novo (início de um novo incidente).
        if (this.uuid == null) {
            this.uuid = UUID.randomUUID().toString();
        }
        if (this.dataHora == null) {
            this.dataHora = LocalDateTime.now();
        }
    }

    // --- Getters e Setters mantidos conforme seu código ---
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
    public Double getIncX() { return incX; }
    public void setIncX(Double incX) { this.incX = incX; }
    public Double getIncY() { return incY; }
    public void setIncY(Double incY) { this.incY = incY; }
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