package com.caixapreta.api.service.alertas;

import com.caixapreta.api.dto.TelemetriaRequestDTO;
import com.caixapreta.api.model.Viatura;
import org.springframework.stereotype.Component;

@Component
public class BateriaAlerta implements AlertaProcessor {

    @Override
    public void processar(TelemetriaRequestDTO dados, Viatura vtr) {
        // Alerta se a bateria do módulo estiver abaixo de 15%
        if (dados.bateria() < 15) {
            vtr.setStatusOperacional("BATERIA_BAIXA");
        }
    }

    /* * --- DOCUMENTAÇÃO DO BATERIA_ALERTA ---
     * 1. O QUE FAZ: Monitora a saúde energética do hardware.
     * 2. IMPACTO: Evita que a viatura fique "invisível" no mapa por falta de energia.
     */
}