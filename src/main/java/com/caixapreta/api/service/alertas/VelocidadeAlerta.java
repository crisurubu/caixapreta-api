package com.caixapreta.api.service.alertas;

import com.caixapreta.api.dto.TelemetriaRequestDTO;
import com.caixapreta.api.model.Viatura;
import org.springframework.stereotype.Component;

@Component
public class VelocidadeAlerta implements AlertaProcessor {

    @Override
    public void processar(TelemetriaRequestDTO dados, Viatura vtr) {
        // 1. LÓGICA DE INFRAÇÃO (Sem mudar a cor do mapa)
        if (dados.velocidade() > 100) {
            // Alimentamos o campo de alertas secundários para o Front exibir um "Badge"
            vtr.setAlertaAdicional("ALTA_VELOCIDADE");
        }
        // 2. AUTO-LIMPEZA: Se a velocidade normalizou e o alerta era de velocidade, limpamos
        else if ("ALTA_VELOCIDADE".equals(vtr.getAlertaAdicional())) {
            vtr.setAlertaAdicional(null);
        }
    }

    /* * --- DOCUMENTAÇÃO DO VELOCIDADE_ALERTA (REVISADO) ---
     * 1. O QUE FAZ: Monitora infrações de trânsito em tempo real.
     * 2. HIERARQUIA: Não interfere no statusOperacional, permitindo que a cor da viatura
     * no mapa represente apenas o estado tático ou de segurança (Missão).
     * 3. FEEDBACK VISUAL: No Dashboard, o operador verá a cor da viatura e uma
     * notificação de 'Excesso de Velocidade' simultaneamente.
     */
}