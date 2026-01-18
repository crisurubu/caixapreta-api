package com.caixapreta.api.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "solicitacoes_destrava")
public class SolicitacaoDestrava {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String uuidEventoOrigem;

    @Column(nullable = false)
    private Long viaturaId;

    @Column(nullable = false)
    private String vtrPrefixo;

    @Column(nullable = false)
    private Long usuarioId;

    @Column(nullable = false)
    private String usuarioNome;

    @Column(columnDefinition = "TEXT")
    private String justificativaOperador;

    private String statusAnalise; // PENDENTE, APROVADO, REJEITADO

    private LocalDateTime dataSolicitacao;

    // --- CAMPOS DE AUDITORIA (NULLABLE POR PADRÃO) ---
    // Deixamos sem 'nullable = false' pois são preenchidos apenas na aprovação
    private LocalDateTime dataAnalise;
    private Long adminId;
    private String adminNome;

    public SolicitacaoDestrava() {}

    // --- GETTERS E SETTERS ---

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUuidEventoOrigem() { return uuidEventoOrigem; }
    public void setUuidEventoOrigem(String uuidEventoOrigem) { this.uuidEventoOrigem = uuidEventoOrigem; }

    public Long getViaturaId() { return viaturaId; }
    public void setViaturaId(Long viaturaId) { this.viaturaId = viaturaId; }

    public String getVtrPrefixo() { return vtrPrefixo; }
    public void setVtrPrefixo(String vtrPrefixo) { this.vtrPrefixo = vtrPrefixo; }

    public Long getUsuarioId() { return usuarioId; }
    public void setUsuarioId(Long usuarioId) { this.usuarioId = usuarioId; }

    public String getUsuarioNome() { return usuarioNome; }
    public void setUsuarioNome(String usuarioNome) { this.usuarioNome = usuarioNome; }

    public String getJustificativaOperador() { return justificativaOperador; }
    public void setJustificativaOperador(String justificativaOperador) { this.justificativaOperador = justificativaOperador; }

    public String getStatusAnalise() { return statusAnalise; }
    public void setStatusAnalise(String statusAnalise) { this.statusAnalise = statusAnalise; }

    public LocalDateTime getDataSolicitacao() { return dataSolicitacao; }
    public void setDataSolicitacao(LocalDateTime dataSolicitacao) { this.dataSolicitacao = dataSolicitacao; }

    public LocalDateTime getDataAnalise() { return dataAnalise; }
    public void setDataAnalise(LocalDateTime dataAnalise) { this.dataAnalise = dataAnalise; }

    public Long getAdminId() { return adminId; }
    public void setAdminId(Long adminId) { this.adminId = adminId; }

    public String getAdminNome() { return adminNome; }
    public void setAdminNome(String adminNome) { this.adminNome = adminNome; }
}