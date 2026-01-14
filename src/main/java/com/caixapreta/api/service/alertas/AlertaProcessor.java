package com.caixapreta.api.service.alertas;

import com.caixapreta.api.dto.TelemetriaRequestDTO;
import com.caixapreta.api.model.Viatura;

public interface AlertaProcessor {
    /**
     * Contrato para processamento de regras de negócio sobre os dados recebidos.
     */
    void processar(TelemetriaRequestDTO dados, Viatura vtr);

    /* * --- DOCUMENTAÇÃO DA INTERFACE ALERTA_PROCESSOR ---
     * 1. O QUE FAZ: Define o padrão para qualquer nova regra de monitoramento.
     * 2. VANTAGEM: Permite adicionar novos alarmes (ex: SOS, Saída de Perímetro)
     * sem precisar alterar o código principal do Service.
     */
}