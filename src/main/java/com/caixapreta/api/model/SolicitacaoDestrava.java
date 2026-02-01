package com.caixapreta.api.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "solicitacoes_destrava")
public class SolicitacaoDestrava {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "uuid_evento_origem", unique = true, nullable = false, length = 36)
    private String uuidEventoOrigem;

    @Column(name = "viatura_id", nullable = false)
    private Long viaturaId;

    @Column(name = "vtr_prefixo", nullable = false)
    private String vtrPrefixo;

    @Column(name = "usuario_id", nullable = false)
    private Long usuarioId;

    @Column(name = "usuario_nome", nullable = false)
    private String usuarioNome;

    @Column(name = "ip_admin", length = 45)
    private String ipAdmin;

    @Column(name = "justificativa_operador", columnDefinition = "TEXT")
    private String justificativaOperador;

    @Column(name = "status_analise")
    private String statusAnalise; // PENDENTE, APROVADO, REJEITADO

    @Column(name = "data_solicitacao")
    private LocalDateTime dataSolicitacao;

    // --- CAMPOS DE AUDITORIA PÓS-ANÁLISE ---
    @Column(name = "data_analise")
    private LocalDateTime dataAnalise;

    @Column(name = "admin_id")
    private Long adminId;

    @Column(name = "admin_nome")
    private String adminNome;

    public SolicitacaoDestrava() {
        this.dataSolicitacao = LocalDateTime.now();
        this.statusAnalise = "PENDENTE";
    }

    @PrePersist
    public void prePersist() {
        if (this.dataSolicitacao == null) {
            this.dataSolicitacao = LocalDateTime.now();
        }
        if (this.statusAnalise == null) {
            this.statusAnalise = "PENDENTE";
        }
    }

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

    public String getIpAdmin() { return ipAdmin; }
    public void setIpAdmin(String ipAdmin) { this.ipAdmin = ipAdmin; }

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

// --- RESUMO DO FUNCIONAMENTO DA CLASSE (O QUE ELA FAZ) ---
/* 1. REGISTRO DE CUSTÓDIA: Esta classe funciona como um formulário eletrônico de responsabilidade. Ela captura o pedido de destrava de uma viatura que foi bloqueada por um evento crítico (Acidente/Tombamento).

2. RASTREABILIDADE POR UUID: Utiliza o 'uuidEventoOrigem' como uma chave única para vincular a solicitação ao evento de alarme original. Isso impede que múltiplos pedidos sejam feitos para o mesmo incidente, garantindo a integridade da auditoria.

3. AUDITORIA DE ACESSO: Armazena o IP do administrador e o nome dos envolvidos (operador e aprovador), permitindo identificar exatamente de onde partiu a ordem de liberação do veículo, essencial para perícias administrativas.

4. SEGREGAÇÃO DE FUNÇÕES: O modelo separa os dados da solicitação (quem pediu e por que) dos dados da análise (quem aprovou e quando), garantindo que o fluxo de trabalho siga o protocolo de segurança definido.

5. PERSISTÊNCIA AUTOMÁTICA: Através do @PrePersist, a classe garante que a data de criação e o status inicial sejam preenchidos pelo servidor, evitando falhas de preenchimento manual e garantindo a ordem cronológica dos fatos.
*/