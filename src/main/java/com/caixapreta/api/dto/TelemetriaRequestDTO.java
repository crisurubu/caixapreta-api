package com.caixapreta.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record TelemetriaRequestDTO(
        @NotNull Long vtrId,           // ID Numérico (Long) para compatibilidade com H2
        @NotNull Double velocidade,    // Velocidade atual em km/h
        @NotNull Double latitude,     // Coordenada GPS
        @NotNull Double longitude,    // Coordenada GPS
        @NotNull Double gForce,       // Impacto detectado
        @NotNull Double incX,         // Inclinação lateral (Eixo X)
        @NotNull Double incY,         // Inclinação frontal (Eixo Y)
        @NotBlank String statusSirene, // OFF, PATRULHA, ABORDAGEM, EMERGENCIA
        @NotNull Double nivelBateria,  // Nome alterado de 'bateria' para 'nivelBateria' para bater com o Service
        Double odometro               // Distância total (opcional)
) {
    /* * --- DOCUMENTAÇÃO DO TELEMETRIA_REQUEST_DTO ---
     * 1. O QUE FAZ: Define o contrato de entrada de dados brutos enviados pelo hardware (ESP32).
     * 2. PADRONIZAÇÃO: O campo 'nivelBateria' foi renomeado para garantir que o ViaturaService
     * consiga mapear o valor para a entidade de log sem erros de compilação.
     * 3. INTEGRIDADE: Utiliza anotações de validação (@NotNull/@NotBlank) para impedir que
     * pacotes de dados incompletos corrompam as estatísticas do sistema.
     */
}