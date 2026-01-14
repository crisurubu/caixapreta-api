package com.caixapreta.api.service.alertas;

import com.caixapreta.api.dto.TelemetriaRequestDTO;
import com.caixapreta.api.model.Viatura;
import org.springframework.stereotype.Component;

@Component
public class AcidenteAlerta implements AlertaProcessor {

    @Override
    public void processar(TelemetriaRequestDTO dados, Viatura vtr) {
        // 1. Definição de Limites Críticos
        boolean tombamento = Math.abs(dados.incX()) > 45 || Math.abs(dados.incY()) > 45;
        boolean impactoGrave = dados.gForce() > 3.0; // Valor de referência para colisão

        // 2. Se detectar qualquer um dos dois, trava a viatura
        if (tombamento || impactoGrave) {
            vtr.setStatusOperacional("ACIDENTE");
            vtr.setBloqueada(true); // <--- AQUI ESTÁ A TRAVA DE SEGURANÇA

            // Log interno para sabermos o motivo exato no console
            System.out.println("[ALERTA CRÍTICO] VTR " + vtr.getId() + " - DETECTADO: " +
                    (tombamento ? "TOMBAMENTO " : "") + (impactoGrave ? "IMPACTO G-FORCE" : ""));
        }
    }

    /* * --- DOCUMENTAÇÃO DO ACIDENTE_ALERTA ---
     * 1. O QUE FAZ: Detecta capotamento ou colisão grave via sensores de inclinação e impacto.
     * 2. REGRA: Ativa o status 'ACIDENTE' e bloqueia a viatura se os limites de segurança forem ultrapassados.
     * 3. VANTAGEM: Garante que um acidente grave tenha prioridade visual absoluta no mapa, impedindo
     * que outros eventos (como a sirene) removam o alerta até que o operador intervenha.
     */
}