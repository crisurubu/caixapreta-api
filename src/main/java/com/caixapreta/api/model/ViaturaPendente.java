package com.caixapreta.api.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "viaturas_pendentes")
public class ViaturaPendente {

    @Id
    private Long vtrId; // O ID que o ESP32/Postman enviou e não encontramos

    private LocalDateTime ultimaTentativa;
    private Double latitude;
    private Double longitude;
    private String observacao; // "Aguardando aprovação do ADM"

    // Construtor padrão exigido pelo JPA
    public ViaturaPendente() {}

    // Getters e Setters
    public Long getVtrId() { return vtrId; }
    public void setVtrId(Long vtrId) { this.vtrId = vtrId; }

    public LocalDateTime getUltimaTentativa() { return ultimaTentativa; }
    public void setUltimaTentativa(LocalDateTime ultimaTentativa) { this.ultimaTentativa = ultimaTentativa; }

    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }

    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }

    public String getObservacao() { return observacao; }
    public void setObservacao(String observacao) { this.observacao = observacao; }
}