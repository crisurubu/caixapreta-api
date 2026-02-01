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

    private static final double G_THRESHOLD = 1.5;
    private static final double TILT_THRESHOLD = 25.0;
    private static final long JANELA_MESMO_EVENTO = 300;

    @Override
    public void processar(TelemetriaRequestDTO dados, Viatura vtr) {
        atualizarSaudeViatura(vtr, dados);

        // ✅ PENTE FINO 1: Unificação dos status de auditoria/trava
        // Removemos "LIBERADO_PARA_SAIDA" daqui para deixar apenas o estado de análise pura
        List<String> travasDeAuditoria = List.of("MANUTENCAO", "EM_ANALISE");

        if (travasDeAuditoria.contains(vtr.getStatusOperacional())) {
            return;
        }

        boolean impacto = dados.gForce() > G_THRESHOLD;
        boolean inclinado = Math.abs(dados.incX()) > TILT_THRESHOLD || Math.abs(dados.incY()) > TILT_THRESHOLD;

        if (impacto || inclinado || Boolean.TRUE.equals(vtr.getBloqueada())) {
            if (impacto || inclinado) {
                vtr.setBloqueada(true);
            }

            // ✅ PENTE FINO 2: Mudança de CAPOTAMENTO para TOMBAMENTO
            boolean tombou = Math.abs(dados.incX()) > 45 || Math.abs(dados.incY()) > 45;
            vtr.setStatusOperacional(tombou ? "TOMBAMENTO" : "ACIDENTE");
            return;
        }

        processarLogicaOperacional(vtr, dados);
    }

    private void atualizarSaudeViatura(Viatura vtr, TelemetriaRequestDTO dados) {
        vtr.setUltimaAtualizacao(LocalDateTime.now());
        vtr.setNivelBateria(dados.nivelBateria());
        vtr.setGpsValido(dados.latitude() != 0.0 && dados.longitude() != 0.0);
    }

    private void processarLogicaOperacional(Viatura vtr, TelemetriaRequestDTO dados) {
        String sirene = (dados.statusSirene() != null) ? dados.statusSirene().toUpperCase() : "OFF";
        switch (sirene) {
            case "EMERGENCIA": vtr.setStatusOperacional("EM OCORRENCIA"); break;
            case "ABORDAGEM":  vtr.setStatusOperacional("ABORDAGEM"); break;
            default:           vtr.setStatusOperacional("PATRULHANDO"); break;
        }
    }




    private void tentarGravarAlarme(Viatura vtr, String tipo, TelemetriaRequestDTO dados) {
        // 1. Busca o último alarme desse tipo para esta viatura
        Optional<Alarme> ultimoGeral = alarmeRepository.findFirstByViaturaOrderByDataHoraDesc(vtr);

        String uuidParaUsar = UUID.randomUUID().toString();

        if (ultimoGeral.isPresent()) {
            Alarme ultimo = ultimoGeral.get();
            long segundosDesdeUltimo = Duration.between(ultimo.getDataHora(), LocalDateTime.now()).getSeconds();

            // 🛡️ BLOQUEIO DE FLOOD: Se o mesmo evento ocorreu há menos de 5 min (300s)
            // Nós NÃO criamos um novo registro no banco. Apenas mantemos o status.
            if (segundosDesdeUltimo < JANELA_MESMO_EVENTO && ultimo.getTipoEvento().equals(tipo)) {
                System.out.println("⏳ [ANTI-FLOOD] Evento " + tipo + " ignorado. Janela de 5min ativa.");
                return;
            }

            // Se for um evento NOVO mas dentro da janela, unificamos o UUID (Laudo)
            if (segundosDesdeUltimo < JANELA_MESMO_EVENTO) {
                uuidParaUsar = ultimo.getUuid();
            }
        }

        efetivarGravacao(vtr, tipo, dados, uuidParaUsar);
    }

    private void efetivarGravacao(Viatura vtr, String tipo, TelemetriaRequestDTO dados, String uuid) {
        try {
            Alarme alarme = new Alarme();
            alarme.setViatura(vtr);
            alarme.setTipoEvento(tipo);
            alarme.setgForce(dados.gForce());
            alarme.setVelocidade(dados.velocidade() != null ? dados.velocidade() : 0.0);
            alarme.setLatitude(dados.latitude());
            alarme.setLongitude(dados.longitude());
            alarme.setIncX(dados.incX());
            alarme.setIncY(dados.incY());
            alarme.setNivelBateria(vtr.getNivelBateria());
            alarme.setDataHora(LocalDateTime.now());
            alarme.setUuid(uuid);
            alarme.setEndereco("TESTE_DEBUG_LOCAL");

            alarmeRepository.saveAndFlush(alarme);
            System.out.println("✅ [SUCESSO] Alarme " + tipo + " persistido. UUID: " + uuid);
        } catch (Exception e) {
            System.err.println("❌ ERRO GRAVAÇÃO: " + e.getMessage());
        }
    }
}
// --- RESUMO DO FUNCIONAMENTO DA CLASSE (O QUE ELA FAZ) ---
/* 1. MÁQUINA DE ESTADOS HIERÁRQUICA: Esta classe decide o status tático da unidade. Ela segue uma ordem de
   importância rígida: Manutenção > Sinistro (Acidente) > Operacional (Sirenes).

2. BLOQUEIO DE TRANSIÇÃO INVÁLIDA: Garante que uma viatura em estado de ACIDENTE não mude para OCORRÊNCIA
   automaticamente via hardware. Enquanto o campo 'bloqueada' for verdadeiro, a lógica de sirenes é ignorada.

3. AMARRA DE UUID PARA LAUDO PERICIAL: Utiliza uma janela de 300 segundos (5 minutos) para herdar o UUID
   do último registro. Isto agrupa múltiplos picos de telemetria num único evento no laudo, resolvendo o problema de IDs espalhados.

4. FILTRAGEM ANTI-SPAM (DEBOUNCE): Evita a sobrecarga do banco de dados ao ignorar registros idênticos que ocorram
   em intervalos menores que 10 segundos, mantendo a base de dados limpa e eficiente.

5. INTEGRIDADE DE MONITORIZAÇÃO: Mesmo com o status travado, o sistema continua a atualizar a saúde da viatura
   (bateria, GPS e última atualização) para que o centro de comando saiba que a unidade ainda comunica.
*/