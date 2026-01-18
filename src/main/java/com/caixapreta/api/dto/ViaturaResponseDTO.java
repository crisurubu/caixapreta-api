package com.caixapreta.api.dto;

import java.time.LocalDateTime;

public record ViaturaResponseDTO(
        Long vtrId,
        String prefixo,           // Necessário para identificação no Card
        String statusOperacional, // TOMBAMENTO, ACIDENTE, PATRULHANDO, etc.
        Boolean bloqueada,        // Status da trava
        Boolean gpsValido,        // Para o ícone de satélite
        Double velocidade,
        Double latitude,
        Double longitude,
        Double nivelBateria,      // Para o sensor de bateria
        Double incX,              // DADO REAL: Inclinação para o carrinho girar
        Double gForce,            // DADO REAL: Para o gráfico de impacto
        String statusSirene,      // ON/OFF
        Double odometro,          // Kilometragem total
        LocalDateTime ultimaAtualizacao
) {
    /* --- DOCUMENTAÇÃO DO VIATURA_RESPONSE_DTO ROBUSTO ---
     * 1. O QUE ELE FAZ:
     * Agora ele NÃO simplifica demais. Ele entrega a telemetria bruta necessária
     * para o VehicleCard processar inclinação real e força G.
     * * 2. POR QUE MUDOU:
     * O React precisa do 'incX' para rotacionar o ícone do carro no MapaCard
     * e do 'gForce' para alimentar o gráfico de telemetria.
     */
}