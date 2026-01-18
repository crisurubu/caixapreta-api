package com.caixapreta.api.service.alertas;

import com.caixapreta.api.dto.TelemetriaRequestDTO;
import com.caixapreta.api.model.Viatura;
import org.springframework.stereotype.Component;

@Component
public class BateriaAlerta implements AlertaProcessor {

    @Override
    public void processar(TelemetriaRequestDTO dados, Viatura vtr) {
        // 1. Atualizamos o nível numérico para o gráfico do Front-end
        vtr.setNivelBateria(dados.nivelBateria());

        // 2. LÓGICA DE ALERTA (Sem mudar a cor do mapa)
        if (dados.nivelBateria() < 15) {
            // Alimentamos o campo de alertas secundários
            vtr.setAlertaAdicional("BATERIA_BAIXA");
        }
        // Se a bateria subiu (carregou), limpamos o alerta adicional
        else if ("BATERIA_BAIXA".equals(vtr.getAlertaAdicional())) {
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