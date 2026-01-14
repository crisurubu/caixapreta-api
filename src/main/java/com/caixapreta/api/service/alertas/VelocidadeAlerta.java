package com.caixapreta.api.service.alertas;

import com.caixapreta.api.dto.TelemetriaRequestDTO;
import com.caixapreta.api.model.Viatura;
import org.springframework.stereotype.Component;

@Component
public class VelocidadeAlerta implements AlertaProcessor {

    @Override
    public void processar(TelemetriaRequestDTO dados, Viatura vtr) {
        // Alerta de excesso de velocidade acima de 100km/h
        if (dados.velocidade() > 100) {
            vtr.setStatusOperacional("ALTA_VELOCIDADE");
        }
    }

    /* * --- DOCUMENTAÇÃO DO VELOCIDADE_ALERTA ---
     * 1. O QUE FAZ: Monitora se o condutor está excedendo o limite de segurança.
     * 2. OBSERVAÇÃO: Este status pode ser sobrescrito se houver um ACIDENTE.
     */
}