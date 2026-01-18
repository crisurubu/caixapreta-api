package com.caixapreta.api.service.alertas;

import com.caixapreta.api.dto.TelemetriaRequestDTO;
import com.caixapreta.api.model.Alarme;
import com.caixapreta.api.model.Viatura;
import com.caixapreta.api.repository.AlarmeRepository;
import com.caixapreta.api.service.GeocodingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;
import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class StatusAlerta implements AlertaProcessor {

    @Autowired
    private AlarmeRepository alarmeRepository;

    @Autowired
    private GeocodingService geocodingService;

    @Override
    public void processar(TelemetriaRequestDTO dados, Viatura vtr) {
        // 1. MONITORAMENTO DE SAÚDE (Mantido original)
        vtr.setUltimaAtualizacao(LocalDateTime.now());
        vtr.setNivelBateria(dados.nivelBateria());
        boolean gpsComDefeito = (dados.latitude() == 0.0 && dados.longitude() == 0.0);
        vtr.setGpsValido(!gpsComDefeito);

        // 2. ALERTAS ADICIONAIS (Tooltip) (Mantido original)
        StringBuilder alertasStr = new StringBuilder();
        if (dados.velocidade() > 100) alertasStr.append("ALTA_VELOCIDADE;");
        if (dados.nivelBateria() < 15) alertasStr.append("BATERIA_BAIXA;");
        if (gpsComDefeito) alertasStr.append("ERRO_GPS;");
        vtr.setAlertaAdicional(alertasStr.length() > 0 ? alertasStr.toString() : null);

        // 3. REGRA DE OURO: Evolução de Sinistro e Múltiplos Impactos
        if (vtr.getBloqueada() != null && vtr.getBloqueada()) {
            // Se for um tombamento novo, evoluímos o status e gravamos
            if (!"TOMBAMENTO".equals(vtr.getStatusOperacional()) && (Math.abs(dados.incX()) > 45)) {
                vtr.setStatusOperacional("TOMBAMENTO");
                processarGravacaoAlarme(vtr, "TOMBAMENTO", dados);
            }
            // NOVO: Se for um novo impacto forte de ACIDENTE, gravamos também!
            else if (dados.gForce() > 4.0) {
                processarGravacaoAlarme(vtr, "ACIDENTE", dados);
            }
            return; // Agora o return só para o que não for impacto
        }

        // 4. LÓGICA DE STATUS OPERACIONAL (Mantido original)
        String statusCalculado = "PATRULHANDO";
        boolean deveGravarAlarme = false;

        if (Math.abs(dados.incX()) > 45) {
            statusCalculado = "TOMBAMENTO";
            deveGravarAlarme = true;
        } else if (dados.gForce() > 4.0) {
            statusCalculado = "ACIDENTE";
            deveGravarAlarme = true;
        } else {
            String sireneBruta = (dados.statusSirene() != null) ? dados.statusSirene().toUpperCase() : "OFF";
            switch (sireneBruta) {
                case "EMERGENCIA": statusCalculado = "EM_OCORRENCIA"; break;
                case "ABORDAGEM":  statusCalculado = "ABORDAGEM"; break;
                default:           statusCalculado = "PATRULHANDO"; break;
            }
        }

        vtr.setStatusOperacional(statusCalculado);

        if (deveGravarAlarme) {
            vtr.setBloqueada(true);
            processarGravacaoAlarme(vtr, statusCalculado, dados);
        }
    }

    private void processarGravacaoAlarme(Viatura vtr, String status, TelemetriaRequestDTO dados) {
        List<String> tiposCriticos = List.of("ACIDENTE", "TOMBAMENTO");
        Optional<Alarme> ultimo = alarmeRepository.findTopByViaturaAndTipoEventoInOrderByDataHoraDesc(vtr, tiposCriticos);

        boolean salvarNovo = ultimo.map(alarme -> {
            long segundosDesdeUltimo = Duration.between(alarme.getDataHora(), LocalDateTime.now()).toSeconds();
            double deltaG = Math.abs(dados.gForce() - alarme.getgForce());
            double deltaInc = Math.abs(dados.incX() - alarme.getIncX());

            // Critérios para salvar novo registro no mesmo acidente:
            // 1. Mudança física brusca (Delta G > 0.5 ou Delta Inclinacao > 10)
            // 2. Mudança de tipo (ACIDENTE -> TOMBAMENTO) após 2 segundos
            boolean mudancaFisicaSignificativa = deltaG > 0.5 || deltaInc > 10.0;
            boolean mudancaStatus = !status.equals(alarme.getTipoEvento());

            if (mudancaFisicaSignificativa) return true;
            if (mudancaStatus && segundosDesdeUltimo >= 2) return true;

            // Proteção contra inundação de dados idênticos
            return segundosDesdeUltimo >= 60;
        }).orElse(true);

        if (salvarNovo) {
            Alarme alarme = new Alarme();
            alarme.setViatura(vtr);
            alarme.setTipoEvento(status);
            alarme.setgForce(dados.gForce());
            alarme.setVelocidade(dados.velocidade());
            alarme.setLatitude(dados.latitude());
            alarme.setLongitude(dados.longitude());
            alarme.setIncX(dados.incX());
            alarme.setNivelBateria(dados.nivelBateria());

            try {
                String rua = geocodingService.resolverEndereco(dados.latitude(), dados.longitude());
                alarme.setEndereco(rua);
            } catch (Exception e) {
                alarme.setEndereco("Coord: " + dados.latitude() + ", " + dados.longitude());
            }

            alarme.setUuid(UUID.randomUUID().toString());
            alarme.setDataHora(LocalDateTime.now());

            alarmeRepository.saveAndFlush(alarme);
            System.out.println("🚨 [AUDITORIA] Evento " + status + " registrado. G:" + dados.gForce());
        }
    }
}