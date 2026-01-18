package com.caixapreta.api.service;

import com.caixapreta.api.dto.DashboardStatsDTO;
import com.caixapreta.api.dto.TelemetriaRequestDTO;
import com.caixapreta.api.dto.ViaturaPainelDTO;
import com.caixapreta.api.model.*;
import com.caixapreta.api.repository.*;
import com.caixapreta.api.service.alertas.AlertaProcessor;
import com.caixapreta.api.service.util.GeoUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ViaturaService {

    private final ViaturaRepository viaturaRepository;
    private final TelemetriaRepository telemetriaRepository;
    private final ViaturaPendenteRepository viaturaPendenteRepository;
    private final List<AlertaProcessor> alertas;

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
        Optional<Viatura> vtrOpt = viaturaRepository.findById(dados.vtrId());

        if (vtrOpt.isPresent()) {
            Viatura vtr = vtrOpt.get();
            LocalDateTime agora = LocalDateTime.now();

            try {
                double distanciaPercorrida = 0.0;
                if (vtr.getLatitude() != 0 && vtr.getLongitude() != 0) {
                    distanciaPercorrida = geoUtils.calcularDistanciaHaversine(
                            vtr.getLatitude(), vtr.getLongitude(),
                            dados.latitude(), dados.longitude()
                    );
                }

                // REGRA DOS 50 KM/H: Retorno automático ao Patrulhamento
                boolean statusBloqueado = List.of("EM_ANALISE", "MANUTENCAO", "LIBERADO_PARA_SAIDA").contains(vtr.getStatusOperacional());

                if (statusBloqueado && dados.velocidade() >= 50.0 && distanciaPercorrida > 0.01) {
                    vtr.setStatusOperacional("PATRULHANDO");
                    vtr.setBloqueada(false);
                    vtr.setAlertaAdicional(null);

                    HistoricoVidaViatura histSaida = new HistoricoVidaViatura();
                    histSaida.setViaturaId(vtr.getId());
                    histSaida.setVtrPrefixo(vtr.getPrefixo());
                    histSaida.setTipoEvento("DESTRAVA_AUTOMATICA");
                    histSaida.setDescricao("Viatura liberada automaticamente por atingir 50km/h.");
                    histSaida.setVelocidadeNoMomento(dados.velocidade());
                    histSaida.setLatitude(dados.latitude());
                    histSaida.setLongitude(dados.longitude());
                    histSaida.setDataOcorrencia(agora);
                    histSaida.setNomeResponsavel("SISTEMA_AUTO_GEOPROCESSAMENTO");
                    historicoVidaRepository.save(histSaida);
                }

                // Atualização de Odômetro e GPS
                double kmDiarioAnterior = (vtr.getKmDiarioAtual() != null) ? vtr.getKmDiarioAtual() : 0.0;
                if (vtr.getUltimaAtualizacao() != null && vtr.getUltimaAtualizacao().getDayOfYear() != agora.getDayOfYear()) {
                    kmDiarioAnterior = 0.0;
                }

                double novoTotal = (vtr.getOdometroManutencao() != null ? vtr.getOdometroManutencao() : 0.0) + (distanciaPercorrida > 0.005 ? distanciaPercorrida : 0);
                double novoDiario = kmDiarioAnterior + (distanciaPercorrida > 0.005 ? distanciaPercorrida : 0);

                vtr.setOdometroManutencao(novoTotal);
                vtr.setKmDiarioAtual(novoDiario);
                vtr.setLatitude(dados.latitude());
                vtr.setLongitude(dados.longitude());
                vtr.setNivelBateria(dados.nivelBateria());
                vtr.setUltimaAtualizacao(agora);
                viaturaRepository.save(vtr);

                // Log de Telemetria
                Telemetria log = new Telemetria();
                log.setViatura(vtr);
                log.setDataHora(agora);
                log.setLatitude(dados.latitude());
                log.setLongitude(dados.longitude());
                log.setVelocidade(dados.velocidade());
                log.setForcaG(dados.gForce());
                log.setNivelBateria(dados.nivelBateria());
                log.setOdometroTotal(novoTotal);
                log.setKmDoDia(novoDiario);
                telemetriaRepository.save(log);

                if (alertas != null) {
                    for (AlertaProcessor alerta : alertas) {
                        alerta.processar(dados, vtr);
                    }
                }

            } catch (Exception e) {
                System.err.println("❌ Erro ao processar VTR " + vtr.getId() + ": " + e.getMessage());
            }
        }
    }

    @Transactional
    public void solicitarDestravamento(Long viaturaId, String uuidAcidente, String justificativa, Long usuarioId, String usuarioNome) {
        Viatura viatura = viaturaRepository.findById(viaturaId)
                .orElseThrow(() -> new RuntimeException("Viatura não encontrada"));

        viatura.setStatusOperacional("EM_ANALISE");
        viaturaRepository.save(viatura);

        // 1. BUSCA A VELOCIDADE DO ACIDENTE PARA O HISTÓRICO INICIAL
        Double velocidadeAcidente = alarmeRepository.findByUuid(uuidAcidente)
                .map(Alarme::getVelocidade)
                .orElse(0.0);

        // Registro da Solicitação
        SolicitacaoDestrava sol = new SolicitacaoDestrava();
        sol.setViaturaId(viatura.getId());
        sol.setVtrPrefixo(viatura.getPrefixo());
        sol.setUuidEventoOrigem(uuidAcidente);
        sol.setJustificativaOperador(justificativa);
        sol.setUsuarioId(usuarioId);
        sol.setUsuarioNome(usuarioNome);
        sol.setStatusAnalise("PENDENTE");
        sol.setDataSolicitacao(LocalDateTime.now());
        solicitacaoRepository.save(sol);

        // Registro no Histórico (SEM NULLS)
        HistoricoVidaViatura hist = new HistoricoVidaViatura();
        hist.setViaturaId(viatura.getId());
        hist.setVtrPrefixo(viatura.getPrefixo());
        hist.setTipoEvento("ENTRADA_EM_ANALISE");
        hist.setDescricao("Solicitação iniciada. Justificativa: " + justificativa);
        hist.setUsuarioId(usuarioId);
        hist.setNomeResponsavel(usuarioNome);
        hist.setVelocidadeNoMomento(velocidadeAcidente);
        hist.setLatitude(viatura.getLatitude());
        hist.setLongitude(viatura.getLongitude());
        hist.setDataOcorrencia(LocalDateTime.now());
        historicoVidaRepository.save(hist);
    }

    @Transactional
    public void aprovarDestravamento(Long solicitacaoId, Long adminId, String adminNome) {
        SolicitacaoDestrava sol = solicitacaoRepository.findById(solicitacaoId)
                .orElseThrow(() -> new RuntimeException("Solicitação não encontrada"));

        // 2. BUSCA A VELOCIDADE DO ACIDENTE USANDO O UUID SALVO NA SOLICITAÇÃO
        Double velocidadeImpacto = alarmeRepository.findByUuid(sol.getUuidEventoOrigem())
                .map(Alarme::getVelocidade)
                .orElse(0.0);

        Viatura viatura = viaturaRepository.findById(sol.getViaturaId())
                .orElseThrow(() -> new RuntimeException("Viatura não encontrada"));

        sol.setStatusAnalise("APROVADO");
        sol.setDataAnalise(LocalDateTime.now());
        sol.setAdminId(adminId);
        sol.setAdminNome(adminNome);
        solicitacaoRepository.save(sol);

        viatura.setStatusOperacional("MANUTENCAO");
        viatura.setBloqueada(false);
        viatura.setAlertaAdicional("AUTORIZADO: AGUARDANDO 50KM/H");
        viaturaRepository.save(viatura);

        // Registro no Histórico (SEM NULLS)
        HistoricoVidaViatura hist = new HistoricoVidaViatura();
        hist.setViaturaId(viatura.getId());
        hist.setVtrPrefixo(viatura.getPrefixo());
        hist.setLatitude(viatura.getLatitude());
        hist.setLongitude(viatura.getLongitude());
        hist.setUsuarioId(adminId);
        hist.setNomeResponsavel(adminNome);
        hist.setTipoEvento("AUTORIZACAO_MANUTENCAO");
        hist.setVelocidadeNoMomento(velocidadeImpacto);
        hist.setDescricao("Saída autorizada pelo Administrador: " + adminNome);
        hist.setDataOcorrencia(LocalDateTime.now());
        historicoVidaRepository.save(hist);
    }

    @Transactional(readOnly = true)
    public DashboardStatsDTO obterEstatisticasGerais() { // <--- Verifique este nome
        return new DashboardStatsDTO(
                viaturaRepository.countByStatusOperacional("EM_OCORRENCIA"),
                viaturaRepository.countByStatusOperacional("ACIDENTE") + viaturaRepository.countByStatusOperacional("TOMBAMENTO"),
                viaturaRepository.countByStatusOperacional("PATRULHANDO"),
                viaturaRepository.countByStatusOperacional("ABORDAGEM"),
                viaturaRepository.countByStatusOperacional("MANUTENCAO"),
                viaturaRepository.countByStatusOperacional("EM_ANALISE")
        );
    }


    @Transactional(readOnly = true)
    public boolean deveIgnorarImpacto(String prefixo) {
        return viaturaRepository.findByPrefixo(prefixo)
                .map(v -> List.of("EM_ANALISE", "MANUTENCAO").contains(v.getStatusOperacional()))
                .orElse(false);
    }

    @Transactional(readOnly = true)
    public List<SolicitacaoDestrava> buscarTodasPendentes() {
        return solicitacaoRepository.findByStatusAnalise("PENDENTE");
    }

    @Transactional(readOnly = true)
    public List<ViaturaPainelDTO> buscarTodasParaDashboard() {
        return viaturaRepository.findAll().stream().map(vtr -> {
            Telemetria tel = telemetriaRepository.findFirstByViaturaOrderByIdDesc(vtr).orElse(new Telemetria());
            String ruaReal = geocodingService.resolverEndereco(vtr.getLatitude(), vtr.getLongitude());
            return new ViaturaPainelDTO(vtr.getId(), vtr.getPrefixo(), tel.getVelocidade() != null ? tel.getVelocidade() : 0.0, vtr.getLatitude(), vtr.getLongitude(), vtr.getStatusOperacional(), tel.getSireneStatus(), vtr.getUltimaAtualizacao(), vtr.getBloqueada(), vtr.getAlertaAdicional(), vtr.getNivelBateria(), vtr.getGpsValido(), tel.getIncX(), tel.getForcaG(), vtr.getKmDiarioAtual(), vtr.getOdometroManutencao(), ruaReal);
        }).collect(Collectors.toList());
    }

    public List<ViaturaPendente> listarAguardandoHomologacao() {
        return viaturaPendenteRepository.findAll();
    }
}