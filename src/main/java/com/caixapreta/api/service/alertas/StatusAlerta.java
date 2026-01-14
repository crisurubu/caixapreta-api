package com.caixapreta.api.service.alertas;

import com.caixapreta.api.dto.TelemetriaRequestDTO;
import com.caixapreta.api.model.Alarme;
import com.caixapreta.api.model.Viatura;
import com.caixapreta.api.repository.AlarmeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;

@Component
public class StatusAlerta implements AlertaProcessor {

    @Autowired
    private AlarmeRepository alarmeRepository;

    @Override
    public void processar(TelemetriaRequestDTO dados, Viatura vtr) {

        // 1. MONITORAMENTO DE CONECTIVIDADE (REDE E GPS)
        // Se este método está rodando, a REDE está OK. Atualizamos o 'batimento cardíaco'.
        vtr.setUltimaAtualizacao(LocalDateTime.now());

        // Verifica se o GPS enviou coordenadas válidas (0.0 significa perda de sinal do NEO-6M)
        boolean gpsComDefeito = (dados.latitude() == 0.0 && dados.longitude() == 0.0);
        vtr.setGpsValido(!gpsComDefeito);

        // 2. REGRA DE OURO: Se a VTR está BLOQUEADA (Acidente/Manutenção Manual),
        // a telemetria não sobrescreve o status visual para não apagar alertas críticos.
        if (vtr.getBloqueada()) {
            return;
        }

        String statusAnterior = vtr.getStatusOperacional();
        String statusCalculado = "PATRULHANDO";

        // --- FUNIL DE HIERARQUIA DE CORES ---

        // NÍVEL 1: ACIDENTE (Vermelho) - Baseado em Física (Impacto ou Tombamento)
        if (Math.abs(dados.incX()) > 45 || Math.abs(dados.incY()) > 45 || dados.gForce() > 3.0) {
            statusCalculado = "ACIDENTE";
            vtr.setBloqueada(true); // Ativa a trava de segurança imediatamente
        }
        // NÍVEL 2: EMERGÊNCIA (Roxo) - Comando de Sirene
        else if ("EMERGENCIA".equals(dados.statusSirene())) {
            statusCalculado = "EM_OCORRENCIA";
        }
        // NÍVEL 3: ABORDAGEM (Laranja) - Comando de Sirene
        else if ("ABORDAGEM".equals(dados.statusSirene())) {
            statusCalculado = "ABORDAGEM";
        }

        // --- GESTÃO DE ALARMES E AUDITORIA ---
        // Só grava no banco se houve mudança de status (Evita duplicidade de logs)
        if (!statusCalculado.equals(statusAnterior) && !statusCalculado.equals("PATRULHANDO")) {
            Alarme novoAlarme = new Alarme();
            novoAlarme.setViatura(vtr);
            novoAlarme.setTipoEvento(statusCalculado);
            novoAlarme.setgForce(dados.gForce());
            novoAlarme.setVelocidade(dados.velocidade());
            novoAlarme.setLatitude(dados.latitude());
            novoAlarme.setLongitude(dados.longitude());

            alarmeRepository.save(novoAlarme);
        }

        vtr.setStatusOperacional(statusCalculado);
    }

    /* * --- DOCUMENTAÇÃO DO STATUS_ALERTA (CÉREBRO ÚNICO) ---
     * 1. O QUE FAZ: Centraliza toda a inteligência de estados (Cores) e diagnóstico de hardware em um único ponto.
     * 2. DIAGNÓSTICO DUAL: Monitora falhas de GPS (dados zerados) e atualiza o timestamp de Rede para detecção de 'Offline'.
     * 3. HIERARQUIA OPERACIONAL: Garante que Acidentes (Vermelho) bloqueiem o sistema, enquanto estados de Emergência (Roxo)
     * e Abordagem (Laranja) seguem os comandos diretos da sirene.
     * 4. INTEGRIDADE DE DADOS: Utiliza travas de bloqueio para impedir que oscilações de sensores limpem alertas críticos.
     */
}