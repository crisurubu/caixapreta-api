package com.caixapreta.api.service.alertas;

import com.caixapreta.api.dto.TelemetriaRequestDTO;
import com.caixapreta.api.model.Viatura;
import org.springframework.stereotype.Component;

@Component
public class BateriaAlerta implements AlertaProcessor {

    @Override
    public void processar(TelemetriaRequestDTO dados, Viatura vtr) {
        // 1. Atualizamos o nível numérico
        vtr.setNivelBateria(dados.nivelBateria());

        // 2. LÓGICA DE ALERTA CALIBRADA PARA 12V
        // 12.98V é carga cheia. Vamos alertar apenas abaixo de 11.5V.
        if (dados.nivelBateria() > 0 && dados.nivelBateria() < 11.5) {
            vtr.setAlertaAdicional("BATERIA_BAIXA");
        }
        // Se a bateria estiver acima de 11.8V (margem de segurança) e tinha alerta, limpamos
        else if (dados.nivelBateria() >= 11.8 && "BATERIA_BAIXA".equals(vtr.getAlertaAdicional())) {
            vtr.setAlertaAdicional(null);
        }
    }


    /* * --- DOCUMENTAÇÃO DO BATERIA_ALERTA (REVISADO) ---
     * 1. O QUE FAZ: Monitora a tensão do hardware (ESP32) sem interferir no status tático.
     * 2. SEPARAÇÃO DE ESTADOS: O status 'BATERIA_BAIXA' agora é um 'alertaAdicional'.
     * 3. RESULTADO VISUAL: A viatura mantém sua cor de operação (Verde/Roxo/Vermelho)
     * no mapa, mas exibe um ícone de aviso no detalhamento técnico do Dashboard.
     */
}