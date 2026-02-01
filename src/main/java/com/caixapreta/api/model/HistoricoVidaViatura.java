package com.caixapreta.api.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "historico_vida_viatura")
public class HistoricoVidaViatura {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // AMARRAÇÃO: Removido 'unique = true' para permitir que múltiplos eventos
    // (Entrada, Aprovação, Retorno) compartilhem o mesmo UUID do Incidente original.
    @Column(nullable = false, length = 36)
    private String uuid;

    // Campo de Referência: Pode guardar o UUID específico da ação se necessário
    @Column(name = "uuid_evento_origem", length = 36)
    private String uuidEventoOrigem;

    @Column(nullable = false)
    private Long viaturaId;

    @Column(nullable = false)
    private String vtrPrefixo;

    @Column(nullable = false)
    private String tipoEvento;

    @Column(columnDefinition = "TEXT")
    private String descricao;

    private LocalDateTime dataOcorrencia;

    private String nomeResponsavel;
    private Long usuarioId;

    // --- TELEMETRIA COM VALORES DEFAULT (GARANTIA ANTI-NULL) ---
    private Double velocidade = 0.0;
    private Double forcaG = 0.0;
    private Double incX = 0.0;
    private Double incY = 0.0;
    private Double latitude = 0.0;
    private Double longitude = 0.0;
    private Double nivelBateria = 0.0;
    private String statusSirene = "OFF";

    @Column(length = 64)
    private String hashIntegridade;

    @Column(length = 45)
    private String ipOrigem;

    public HistoricoVidaViatura() {
        this.dataOcorrencia = LocalDateTime.now();
    }

    @PrePersist
    public void prePersist() {
        // Só gera novo UUID se o Service não tiver injetado o UUID do incidente pai
        if (this.uuid == null) {
            this.uuid = UUID.randomUUID().toString();
        }
        if (this.dataOcorrencia == null) {
            this.dataOcorrencia = LocalDateTime.now();
        }

        // Blindagem final contra campos nulos de sensores ou hardware
        this.velocidade = (this.velocidade == null) ? 0.0 : this.velocidade;
        this.forcaG = (this.forcaG == null) ? 0.0 : this.forcaG;
        this.incX = (this.incX == null) ? 0.0 : this.incX;
        this.incY = (this.incY == null) ? 0.0 : this.incY;
        this.latitude = (this.latitude == null) ? 0.0 : this.latitude;
        this.longitude = (this.longitude == null) ? 0.0 : this.longitude;
        this.nivelBateria = (this.nivelBateria == null) ? 0.0 : this.nivelBateria;
        this.statusSirene = (this.statusSirene == null) ? "OFF" : this.statusSirene;
    }

    // --- GETTERS E SETTERS ---
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getUuid() { return uuid; }
    public void setUuid(String uuid) { this.uuid = uuid; }
    public String getUuidEventoOrigem() { return uuidEventoOrigem; }
    public void setUuidEventoOrigem(String uuidEventoOrigem) { this.uuidEventoOrigem = uuidEventoOrigem; }
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
    public Double getVelocidade() { return velocidade; }
    public void setVelocidade(Double velocidade) { this.velocidade = velocidade; }
    public Double getForcaG() { return forcaG; }
    public void setForcaG(Double forcaG) { this.forcaG = forcaG; }
    public Double getIncX() { return incX; }
    public void setIncX(Double incX) { this.incX = incX; }
    public Double getIncY() { return incY; }
    public void setIncY(Double incY) { this.incY = incY; }
    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }
    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }
    public Double getNivelBateria() { return nivelBateria; }
    public void setNivelBateria(Double nivelBateria) { this.nivelBateria = nivelBateria; }
    public String getStatusSirene() { return statusSirene; }
    public void setStatusSirene(String statusSirene) { this.statusSirene = statusSirene; }
    public String getHashIntegridade() { return hashIntegridade; }
    public void setHashIntegridade(String hashIntegridade) { this.hashIntegridade = hashIntegridade; }
    public String getIpOrigem() { return ipOrigem; }
    public void setIpOrigem(String ipOrigem) { this.ipOrigem = ipOrigem; }
}

/**
 * --- DOCUMENTAÇÃO DO MODEL HISTORICO_VIDA (VERSÃO ESTRATÉGICA) ---
 * 1. CONSOLIDAR: O campo 'uuid' agora atua como Identificador do Dossiê, permitindo
 * que múltiplos registros (Entrada/Aprovação) pertençam ao mesmo incidente.
 * 2. INTEGRIDADE: O método @PrePersist garante que nenhum campo de telemetria seja
 * persistido como NULL, preenchendo com 0.0 ou valores default.
 * 3. RASTREABILIDADE: Mantém o vínculo com 'uuid_evento_origem' para auditorias
 * que exigem saber a causa raiz de cada movimentação administrativa.
 * 4. SEGURANÇA: Estrutura preparada para assinatura digital via 'hashIntegridade',
 * assegurando que os dados periciais não foram alterados.
 */