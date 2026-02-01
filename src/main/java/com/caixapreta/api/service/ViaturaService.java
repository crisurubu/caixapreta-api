package com.caixapreta.api.service;

import com.caixapreta.api.dto.DashboardStatsDTO;
import com.caixapreta.api.dto.TelemetriaRequestDTO;
import com.caixapreta.api.dto.ViaturaPainelDTO;
import com.caixapreta.api.model.*;
import com.caixapreta.api.repository.*;
import com.caixapreta.api.service.alertas.AlertaProcessor;
import com.caixapreta.api.service.util.GeoUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
public class ViaturaService {

    private final ViaturaRepository viaturaRepository;
    private final TelemetriaRepository telemetriaRepository;
    private final ViaturaPendenteRepository viaturaPendenteRepository;
    private final List<AlertaProcessor> alertas;
    // Controle de flood
    private final Map<Long, LocalDateTime> controleFloodAlarmes = new ConcurrentHashMap<>();
    // No topo da classe, adicione um Map para guardar a última Força G
    private final Map<Long, Double> ultimaForcaGAlarme = new ConcurrentHashMap<>();

    @Autowired
    private GeocodingService geocodingService;

    @Autowired
    private GeoUtils geoUtils;

    @Autowired
    private SolicitacaoRepository solicitacaoRepository;

    @Autowired
    private HistoricoVidaRepository historicoVidaRepository;

    @Autowired
    private AlarmeRepository alarmeRepository;

    public ViaturaService(ViaturaRepository viaturaRepository,
                          TelemetriaRepository telemetriaRepository,
                          ViaturaPendenteRepository viaturaPendenteRepository,
                          List<AlertaProcessor> alertas) {
        this.viaturaRepository = viaturaRepository;
        this.telemetriaRepository = telemetriaRepository;
        this.viaturaPendenteRepository = viaturaPendenteRepository;
        this.alertas = alertas;
    }

    @Transactional
    public void processarTelemetria(TelemetriaRequestDTO dados) {
        // 🛡️ BLOQUEIO DE CONCORRÊNCIA: Cria uma fila única por ID de viatura.
        synchronized (dados.vtrId().toString().intern()) {

            Optional<Viatura> vtrOpt = viaturaRepository.findById(dados.vtrId());

            if (vtrOpt.isPresent()) {
                Viatura vtr = vtrOpt.get();
                LocalDateTime agora = LocalDateTime.now();

                try {
                    // --- PASSO 1: GPS E ODÔMETRO ---
                    processarMovimentacao(vtr, dados, agora);

                    // --- PASSO 2: TRAVA DE AUDITORIA (COM INTELIGÊNCIA ANTI-GUINCHO) ---
                    List<String> statusProtegidos = List.of("EM_ANALISE", "MANUTENCAO");
                    boolean sobAuditoria = statusProtegidos.contains(vtr.getStatusOperacional());

                    // Nova lógica: Só sai da auditoria se tiver velocidade > 50 E motor ligado (Bateria >= 13.2V)
                    boolean motorLigado = dados.nivelBateria() != null && dados.nivelBateria() >= 13.2;
                    if (sobAuditoria && dados.velocidade() > 50.0 && motorLigado) {
                        vtr.setStatusOperacional("PATRULHANDO");
                        vtr.setBloqueada(false); // Garante a destrava física
                        vtr.setAlertaAdicional(null);
                        sobAuditoria = false;

                        // Registro histórico da volta por meios próprios
                        registrarHistoricoVida(vtr, UUID.randomUUID().toString(), "RETORNO_OPERACIONAL",
                                "Motor ligado (" + dados.nivelBateria() + "V) e velocidade atingida. Retorno automático.",
                                0L, "SISTEMA", "127.0.0.1");
                    }

                    // --- PASSO 3: PROCESSAMENTO DE ALERTAS ---
                    if (!sobAuditoria && alertas != null) {
                        for (AlertaProcessor alerta : alertas) {
                            alerta.processar(dados, vtr);
                        }
                    }

                    // 🔥 SALVAMENTO OPERACIONAL: Garante cor no Front imediatamente
                    viaturaRepository.saveAndFlush(vtr);

                    // --- PASSO 4: REGISTRO NA CAIXA-PRETA E ALARMES (COM UNIFICAÇÃO DE UUID) ---
                    String status = vtr.getStatusOperacional() != null ? vtr.getStatusOperacional().toUpperCase() : "";
                    boolean isEventoCritico = List.of("TOMBAMENTO", "ACIDENTE").contains(status);

                    if (isEventoCritico && !sobAuditoria) {
                        LocalDateTime ultimaGravacao = controleFloodAlarmes.get(vtr.getId());
                        Double ultimaForcaG = ultimaForcaGAlarme.getOrDefault(vtr.getId(), 0.0);

                        boolean novoImpactoForte = Math.abs(dados.gForce() - ultimaForcaG) > 1.0;
                        // Aumentado para 30 minutos conforme conversamos para o laudo pericial
                        boolean tempoExcedido = (ultimaGravacao == null || agora.isAfter(ultimaGravacao.plusMinutes(30)));

                        if (tempoExcedido || novoImpactoForte) {
                            String enderecoReal = geocodingService.resolverEndereco(dados.latitude(), dados.longitude());

                            Alarme novoAlarme = new Alarme();
                            novoAlarme.setViatura(vtr);
                            novoAlarme.setDataHora(agora);
                            novoAlarme.setTipoEvento(status);
                            novoAlarme.setgForce(dados.gForce());
                            novoAlarme.setIncX(dados.incX());
                            novoAlarme.setIncY(dados.incY());
                            novoAlarme.setLatitude(dados.latitude());
                            novoAlarme.setLongitude(dados.longitude());
                            novoAlarme.setVelocidade(dados.velocidade() != null ? dados.velocidade() : 0.0);
                            novoAlarme.setNivelBateria(dados.nivelBateria());

                            // 🧠 UNIFICAÇÃO: Busca se já existe um alarme recente para manter o mesmo UUID/Laudo
                            Optional<Alarme> ultimoExistente = alarmeRepository.findFirstByViaturaOrderByDataHoraDesc(vtr);
                            if (ultimoExistente.isPresent() && !tempoExcedido) {
                                novoAlarme.setUuid(ultimoExistente.get().getUuid());
                            } else {
                                novoAlarme.setUuid(UUID.randomUUID().toString());
                            }

                            novoAlarme.setEndereco(enderecoReal);
                            alarmeRepository.saveAndFlush(novoAlarme);

                            telemetriaRepository.save(criarLogTelemetria(dados, vtr, agora));

                            controleFloodAlarmes.put(vtr.getId(), agora);
                            ultimaForcaGAlarme.put(vtr.getId(), dados.gForce());

                            String motivo = novoImpactoForte ? "NOVO IMPACTO SEQUENCIAL" : "EVENTO CRÍTICO";
                            System.out.println("✅ [" + motivo + "] UUID: " + novoAlarme.getUuid());
                        } else {
                            System.out.println("🚫 [BLOQUEADO] Viatura sinistrada estável. Aguardando novo impacto ou tempo.");
                        }
                    } else {
                        telemetriaRepository.save(criarLogTelemetria(dados, vtr, agora));
                    }

                } catch (Exception e) {
                    System.err.println("❌ Erro Crítico VTR " + vtr.getPrefixo() + ": " + e.getMessage());
                }
            } else {
                registrarPendente(dados);
            }
        }
    }
    private boolean isPropulsaoPropria(TelemetriaRequestDTO dados) {
        // 13.2V é o threshold padrão onde o alternador está atuando.
        // Se a VTR está a 80km/h mas com 12.4V, ela está no guincho com motor desligado.
        boolean motorLigado = dados.nivelBateria() != null && dados.nivelBateria() >= 13.2;
        boolean emVelocidade = dados.velocidade() != null && dados.velocidade() > 50.0;

        return motorLigado && emVelocidade;
    }

        // Métodos auxiliares para organizar o código
        private void processarMovimentacao(Viatura vtr, TelemetriaRequestDTO dados, LocalDateTime agora) {
            double distancia = 0.0;
            if (vtr.getLatitude() != null && vtr.getLatitude() != 0) {
                distancia = geoUtils.calcularDistanciaHaversine(vtr.getLatitude(), vtr.getLongitude(), dados.latitude(), dados.longitude());
            }
            vtr.setLatitude(dados.latitude());
            vtr.setLongitude(dados.longitude());
            // --- COLE O CÓDIGO AQUI ---
            double bateriaRecebida = dados.nivelBateria() != null ? dados.nivelBateria() : 0.0;

            // Se o sensor "enlouquecer" (ruído ou Postman), a gente trava no limite técnico
            if (bateriaRecebida > 16.0) {
                vtr.setNivelBateria(14.4); // Força o valor de alternador carregando para não quebrar a lógica
            } else {
                vtr.setNivelBateria(bateriaRecebida);
            }
            // --- FIM DO BLOCO ---
            vtr.setUltimaAtualizacao(agora);
            if (distancia > 0.005) {
                vtr.setOdometroManutencao((vtr.getOdometroManutencao() != null ? vtr.getOdometroManutencao() : 0.0) + distancia);
                vtr.setKmDiarioAtual((vtr.getKmDiarioAtual() != null ? vtr.getKmDiarioAtual() : 0.0) + distancia);
            }
        }


        private void registrarPendente(TelemetriaRequestDTO dados) {
            ViaturaPendente pendente = viaturaPendenteRepository.findById(dados.vtrId()).orElse(new ViaturaPendente());
            pendente.setVtrId(dados.vtrId());
            pendente.setUltimaTentativa(LocalDateTime.now());
            pendente.setObservacao("Hardware não cadastrado.");
            viaturaPendenteRepository.saveAndFlush(pendente);
        }

    // Método auxiliar apenas para limpar o código principal
    private Telemetria criarLogTelemetria(TelemetriaRequestDTO dados, Viatura vtr, LocalDateTime agora) {
        Telemetria log = new Telemetria();
        log.setViatura(vtr);
        log.setDataHora(agora);
        log.setLatitude(dados.latitude());
        log.setLongitude(dados.longitude());
        log.setVelocidade(dados.velocidade());
        log.setForcaG(dados.gForce());
        log.setIncX(dados.incX());
        log.setIncY(dados.incY());
        log.setSireneStatus(dados.statusSirene());
        log.setNivelBateria(dados.nivelBateria());
        log.setKmDoDia(vtr.getKmDiarioAtual());
        log.setOdometroTotal(vtr.getOdometroManutencao());
        return log;
    }
    // ✅ MÉTODO RECUPERADO: LISTAR PENDÊNCIAS
    @Transactional(readOnly = true)
    public List<SolicitacaoDestrava> buscarTodasPendentes() {
        return solicitacaoRepository.findByStatusAnalise("PENDENTE");
    }

    // ✅ MÉTODO RECUPERADO: VERIFICAÇÃO TÉCNICA
    @Transactional(readOnly = true)
    public boolean deveIgnorarImpacto(String prefixo) {
        return viaturaRepository.findByPrefixo(prefixo)
                .map(v -> List.of("EM_ANALISE", "MANUTENCAO")
                        .contains(v.getStatusOperacional()))
                .orElse(false);
    }

    @Transactional
    public void solicitarDestravamento(Long viaturaId, String uuidAcidente, String justificativa, Long usuarioId, String usuarioNome, String ipOrigem) {
        Viatura viatura = viaturaRepository.findById(viaturaId)
                .orElseThrow(() -> new RuntimeException("Viatura não encontrada"));

        viatura.setStatusOperacional("EM_ANALISE");
        String alertaPrevio = viatura.getAlertaAdicional() != null ? viatura.getAlertaAdicional() : "";
        if (!alertaPrevio.contains("EM ANALISE")) {
            viatura.setAlertaAdicional(alertaPrevio + " [EM ANALISE]");
        }

        viatura = viaturaRepository.saveAndFlush(viatura);

        SolicitacaoDestrava sol = new SolicitacaoDestrava();
        sol.setViaturaId(viatura.getId());
        sol.setVtrPrefixo(viatura.getPrefixo());
        sol.setUuidEventoOrigem(uuidAcidente);
        sol.setJustificativaOperador(justificativa);
        sol.setUsuarioId(usuarioId);
        sol.setUsuarioNome(usuarioNome);
        sol.setStatusAnalise("PENDENTE");
        sol.setDataSolicitacao(LocalDateTime.now());
        sol.setIpAdmin(ipOrigem);
        solicitacaoRepository.save(sol);

        registrarHistoricoVida(viatura, uuidAcidente, "ENTRADA_EM_ANALISE",
                "Solicitação de destrava iniciada. Justificativa: " + justificativa,
                usuarioId, usuarioNome, ipOrigem);
    }

    @Transactional
    public void aprovarDestravamento(Long solicitacaoId, Long adminId, String adminNome, String ipAdmin) {
        SolicitacaoDestrava sol = solicitacaoRepository.findById(solicitacaoId)
                .orElseThrow(() -> new RuntimeException("Solicitação não encontrada"));

        Viatura viatura = viaturaRepository.findById(sol.getViaturaId())
                .orElseThrow(() -> new RuntimeException("Viatura vinculada não encontrada"));

        sol.setStatusAnalise("APROVADO");
        sol.setDataAnalise(LocalDateTime.now());
        sol.setAdminId(adminId);
        sol.setAdminNome(adminNome);
        solicitacaoRepository.save(sol);

        viatura.setBloqueada(false);
        viatura.setStatusOperacional("MANUTENCAO");
        viatura.setAlertaAdicional("[LIBERADO PARA SAIDA]");
        viaturaRepository.saveAndFlush(viatura);

        registrarHistoricoVida(viatura, sol.getUuidEventoOrigem(), "AUTORIZACAO_MANUTENCAO",
                "Desbloqueio autorizado e efetivado pelo Administrador: " + adminNome,
                adminId, adminNome, ipAdmin);
    }

    private void registrarHistoricoVida(Viatura vtr, String uuidOrigem, String tipo, String desc, Long userId, String nome, String ip) {
        HistoricoVidaViatura hist = new HistoricoVidaViatura();
        LocalDateTime agora = LocalDateTime.now();

        Optional<Alarme> alarmeOpt = alarmeRepository.findFirstByUuid(uuidOrigem);

        hist.setUuid(UUID.randomUUID().toString());
        hist.setUuidEventoOrigem(uuidOrigem);
        hist.setViaturaId(vtr.getId());
        hist.setVtrPrefixo(vtr.getPrefixo());
        hist.setTipoEvento(tipo);
        hist.setDescricao(desc);
        hist.setUsuarioId(userId);
        hist.setNomeResponsavel(nome);
        hist.setDataOcorrencia(agora);
        hist.setIpOrigem(ip);
        hist.setLatitude(vtr.getLatitude());
        hist.setLongitude(vtr.getLongitude());
        hist.setNivelBateria(vtr.getNivelBateria() != null ? vtr.getNivelBateria() : 0.0);
        hist.setStatusSirene("OFF");

        if (alarmeOpt.isPresent()) {
            Alarme alarme = alarmeOpt.get();
            hist.setForcaG(alarme.getgForce() != null ? alarme.getgForce() : 0.0);
            hist.setVelocidade(alarme.getVelocidade() != null ? alarme.getVelocidade() : 0.0);
            hist.setIncX(alarme.getIncX() != null ? alarme.getIncX() : 0.0);
            hist.setIncY(alarme.getIncY() != null ? alarme.getIncY() : 0.0);
        } else {
            hist.setForcaG(0.0);
            hist.setVelocidade(0.0);
            hist.setIncX(0.0);
            hist.setIncY(0.0);
        }

        hist.setHashIntegridade(gerarHashManual(uuidOrigem, vtr.getPrefixo(), nome, agora));
        historicoVidaRepository.save(hist);
    }

    private String gerarHashManual(String uuid, String prefixo, String usuario, LocalDateTime data) {
        try {
            String rawData = uuid + "|" + prefixo + "|" + usuario + "|" + data.toString();
            java.security.MessageDigest digest = java.security.MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(rawData.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            return java.util.HexFormat.of().formatHex(hashBytes).toUpperCase();
        } catch (Exception e) { return "HASH_ERROR"; }
    }

    @Transactional(readOnly = true)
    public DashboardStatsDTO obterEstatisticasGerais() {
        return new DashboardStatsDTO(
                viaturaRepository.countByStatusOperacional("EM OCORRENCIA"),
                viaturaRepository.countByStatusOperacional("ACIDENTE") + viaturaRepository.countByStatusOperacional("TOMBAMENTO"),
                viaturaRepository.countByStatusOperacional("PATRULHANDO"),
                viaturaRepository.countByStatusOperacional("ABORDAGEM"),
                viaturaRepository.countByStatusOperacional("MANUTENCAO"),
                viaturaRepository.countByStatusOperacional("EM_ANALISE")
        );
    }

    @Transactional(readOnly = true)
    public List<ViaturaPainelDTO> buscarTodasParaDashboard() {
        return viaturaRepository.findAll().stream().map(vtr -> {
            Telemetria tel = telemetriaRepository.findFirstByViaturaOrderByIdDesc(vtr).orElse(new Telemetria());
            // 🛡️ TRAVA DE SAÍDA: Garante que o Painel nunca exiba lixo do Postman
            double bateriaExibicao = (vtr.getNivelBateria() != null && vtr.getNivelBateria() < 17.0)
                    ? vtr.getNivelBateria() : 13.8;
            return new ViaturaPainelDTO(
                    vtr.getId(), vtr.getPrefixo(), vtr.getPlaca(),
                    vtr.getModelo(),
                    vtr.getChassi(), tel.getVelocidade() != null ? tel.getVelocidade() : 0.0,
                    vtr.getLatitude(), vtr.getLongitude(), vtr.getStatusOperacional(),
                    tel.getSireneStatus(), vtr.getUltimaAtualizacao(), vtr.getBloqueada(),
                    vtr.getAlertaAdicional(), bateriaExibicao, vtr.getGpsValido(),
                    tel.getIncX(), tel.getForcaG(),
                    vtr.getKmDiarioAtual() != null ? vtr.getKmDiarioAtual() : 0.0,
                    vtr.getOdometroManutencao() != null ? vtr.getOdometroManutencao() : 0.0,
                    "Coordenadas: " + vtr.getLatitude() + ", " + vtr.getLongitude()
            );
        }).collect(Collectors.toList());
    }

    @Scheduled(fixedRate = 5000)
    @Transactional
    public void verificarViaturasOffline() {
        LocalDateTime limite = LocalDateTime.now().minusSeconds(60);
        viaturaRepository.matarViaturasInativas(limite);
    }
} // 👈 Esta chave fecha a classe corretamente

// --- RESUMO DO FUNCIONAMENTO DA CLASSE ---
/* 1. ORQUESTRADOR DE TELEMETRIA: Centraliza o fluxo de dados e garante a atualização de odômetro e alertas.
2. COMPATIBILIDADE: Agora os métodos 'buscarTodasPendentes' e 'deveIgnorarImpacto' estão dentro da classe para atender o Controller.
3. CADEIA DE CUSTÓDIA: O registro de histórico vida busca dados reais do alarme via UUID.
4. WATCHDOG: Monitora inatividade a cada 5 segundos.
*/