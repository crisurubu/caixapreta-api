package com.caixapreta.api.service;

import com.caixapreta.api.dto.DashboardStatsDTO;
import com.caixapreta.api.dto.TelemetriaRequestDTO;
import com.caixapreta.api.dto.ViaturaPainelDTO;
import com.caixapreta.api.model.Telemetria;
import com.caixapreta.api.model.Viatura;
import com.caixapreta.api.repository.TelemetriaRepository;
import com.caixapreta.api.repository.ViaturaRepository;
import com.caixapreta.api.service.alertas.AlertaProcessor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ViaturaService {

    private final ViaturaRepository viaturaRepository;
    private final TelemetriaRepository telemetriaRepository;
    private final List<AlertaProcessor> alertas;

    public ViaturaService(ViaturaRepository viaturaRepository,
                          TelemetriaRepository telemetriaRepository,
                          List<AlertaProcessor> alertas) {
        this.viaturaRepository = viaturaRepository;
        this.telemetriaRepository = telemetriaRepository;
        this.alertas = alertas;
    }

    @Transactional
    public void processarTelemetria(TelemetriaRequestDTO dados) {
        // 1. AUTO-PROVISIONAMENTO
        Viatura vtr = viaturaRepository.findById(dados.vtrId())
                .orElseGet(() -> {
                    Viatura nova = new Viatura();
                    nova.setId(dados.vtrId());
                    nova.setPrefixo("VTR-NOVA-" + dados.vtrId());
                    nova.setStatusOperacional("PATRULHANDO");
                    nova.setBloqueada(false);
                    return viaturaRepository.save(nova);
                });

        // 2. PROCESSAMENTO DE ALERTAS (Cérebro de Cores e Trava de Acidente)
        alertas.forEach(alerta -> alerta.processar(dados, vtr));

        // 3. REGISTRO NA CAIXA-PRETA (Log de Telemetria)
        Telemetria log = new Telemetria();
        log.setViatura(vtr);
        log.setVelocidade(dados.velocidade());
        log.setLatitude(dados.latitude());
        log.setLongitude(dados.longitude());
        log.setForcaG(dados.gForce());
        log.setIncX(dados.incX());
        log.setIncY(dados.incY());
        log.setSireneStatus(dados.statusSirene());
        log.setNivelBateria(dados.nivelBateria());
        log.setOdometro(dados.odometro());
        telemetriaRepository.save(log);

        // 4. ATUALIZAÇÃO DE HEARTBEAT (Última vez vista)
        vtr.setUltimaAtualizacao(LocalDateTime.now());
        viaturaRepository.save(vtr);
    }

    @Transactional(readOnly = true)
    public List<ViaturaPainelDTO> buscarTodasParaDashboard() {
        List<Viatura> viaturas = viaturaRepository.findAll();

        return viaturas.stream().map(vtr -> {
            Telemetria tel = telemetriaRepository.findFirstByViaturaOrderByIdDesc(vtr)
                    .orElse(new Telemetria());

            return new ViaturaPainelDTO(
                    vtr.getId(),
                    vtr.getPrefixo(), // Incluído para o Front saber o nome
                    tel.getVelocidade() != null ? tel.getVelocidade() : 0.0,
                    tel.getLatitude() != null ? tel.getLatitude() : 0.0,
                    tel.getLongitude() != null ? tel.getLongitude() : 0.0,
                    vtr.getStatusOperacional(),
                    tel.getSireneStatus(),
                    vtr.getUltimaAtualizacao(),
                    vtr.getBloqueada() // Importante para o Front mostrar o cadeado
            );
        }).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public DashboardStatsDTO obterEstatisticasGerais() {
        return new DashboardStatsDTO(
                viaturaRepository.countByStatusOperacional("EM_OCORRENCIA"),
                viaturaRepository.countByStatusOperacional("ACIDENTE"),
                viaturaRepository.countByStatusOperacional("PATRULHANDO"),
                viaturaRepository.countByStatusOperacional("ABORDAGEM"),
                viaturaRepository.countByStatusOperacional("MANUTENCAO")
        );
    }

    /* * --- DOCUMENTAÇÃO DO VIATURA_SERVICE ---
     * 1. O QUE FAZ: Orquestra todo o fluxo de vida da viatura, desde a recepção do sinal bruto até a entrega de dados processados ao Dashboard.
     * 2. AUTO-PROVISIONAMENTO: Garante que novas unidades sejam registradas automaticamente sem intervenção manual prévia.
     * 3. INTEGRIDADE DE TELEMETRIA: Salva cada batimento (ping) na tabela de logs para auditoria pericial futura.
     * 4. TRANSACIONALIDADE: Assegura que o processamento do status e a gravação do log ocorram como uma única operação atômica.
     */
}