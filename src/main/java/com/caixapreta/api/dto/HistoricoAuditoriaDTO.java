package com.caixapreta.api.dto;

import java.time.LocalDateTime;

/**
 * DTO Pericial: Representa a verdade imutável da viatura.
 * Inclui o elo de ligação (uuidEventoOrigem) para reconstrução da cadeia de custódia.
 */
public record HistoricoAuditoriaDTO(
        // Identificação e Tempo
        String uuid,
        String uuidEventoOrigem, // 🔗 O elo que amarra Solicitação -> Autorização -> Alarme
        String prefixo,
        LocalDateTime dataOcorrencia,
        String tipoEvento,

        // Telemetria (A "Caixa-Preta")
        Double velocidade,
        Double forcaG,
        Double incX,
        Double incY,
        Double nivelBateria,
        String statusSirene,
        Double latitude,
        Double longitude,

        // Operacional
        String nomeResponsavel,
        String descricao,

        // Camada de Segurança e Custódia
        String hashIntegridade,
        String ipOrigem
) {}

// --- RESUMO DO FUNCIONAMENTO DA CLASSE (O QUE ELA FAZ) ---
/* 1. CONTRATO DE EXPOSIÇÃO: Este Record define como os dados de auditoria serão enviados para o Frontend (React/Vue). Ele é otimizado para que o Dashboard consiga montar tabelas de perícia detalhadas sem precisar de múltiplas chamadas à API.

2. RECONSTRUÇÃO DE EVENTOS: Através do campo 'uuidEventoOrigem', o sistema permite rastrear a "árvore genealógica" de um incidente, ligando um log de download de hoje ao acidente que aconteceu semana passada.

3. DADOS FORENSES: Carrega todas as métricas físicas (G, Inclinação, Velocidade) e geográficas (GPS), servindo como um Dossiê completo para investigações administrativas ou judiciais.

4. CAMADA DE CONFIANÇA: Transporta o 'hashIntegridade' e o 'ipOrigem', garantindo que o operador do sistema veja que a informação é autêntica e saiba exatamente de qual terminal ela foi gerada ou acessada.

5. IMUTABILIDADE: Por ser um Record, o objeto é seguro para transporte entre camadas do sistema (Controller -> Service -> Frontend), garantindo que os dados não sejam alterados acidentalmente durante o processamento.
*/