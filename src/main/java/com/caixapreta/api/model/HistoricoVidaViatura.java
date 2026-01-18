package com.caixapreta.api.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "historico_vida_viatura")
public class HistoricoVidaViatura {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long viaturaId;

    @Column(nullable = false)
    private String vtrPrefixo;

    // Tipo de Evento: ENTRADA_MANUTENCAO, SAIDA_MANUTENCAO, SINISTRO, REVISAO, ALERTA_CRITICO
    @Column(nullable = false)
    private String tipoEvento;

    @Column(columnDefinition = "TEXT")
    private String descricao;

    private LocalDateTime dataOcorrencia;

    // Auditoria: Quem era o responsável no momento?
    private String nomeResponsavel;
    private Long usuarioId;

    // Dados de Telemetria no momento do registro
    private Double velocidadeNoMomento;
    private Double latitude;
    private Double longitude;

    public HistoricoVidaViatura() {
        this.dataOcorrencia = LocalDateTime.now();
    }

    // Getters e Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getViaturaId() { return viaturaId; }
    public void setViaturaId(Long viaturaId) { this.viaturaId = viaturaId; }
    public String getVtrPrefixo() { return vtrPrefixo; }
    public void setVtrPrefixo(String vtrPrefixo) { this.vtrPrefixo = vtrPrefixo; }
    public String getTipoEvento() { return tipoEvento; }
    public void setTipoEvento(String tipoEvento) { this.tipoEvento = tipoEvento; }
    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }
    public LocalDateTime getDataOcorrencia() { return dataOcorrencia; }
    public void setDataOcorrencia(LocalDateTime dataOcorrencia) { this.dataOcorrencia = dataOcorrencia; }
    public String getNomeResponsavel() { return nomeResponsavel; }
    public void setNomeResponsavel(String nomeResponsavel) { this.nomeResponsavel = nomeResponsavel; }
    public Long getUsuarioId() { return usuarioId; }
    public void setUsuarioId(Long usuarioId) { this.usuarioId = usuarioId; }
    public Double getVelocidadeNoMomento() { return velocidadeNoMomento; }
    public void setVelocidadeNoMomento(Double velocidadeNoMomento) { this.velocidadeNoMomento = velocidadeNoMomento; }
    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }
    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }
}