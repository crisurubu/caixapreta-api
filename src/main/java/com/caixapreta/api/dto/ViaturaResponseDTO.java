package com.caixapreta.api.dto;

import java.time.LocalDateTime;

public record ViaturaResponseDTO(
        Long vtrId,
        String statusOperacional,
        Boolean bloqueada,
        Double velocidade,
        Double latitude,
        Double longitude,
        LocalDateTime ultimaAtualizacao
) {
    /* --- DOCUMENTAÇÃO DO VIATURA_RESPONSE_DTO ---
     * 1. O QUE ELE FAZ:
     * Simplifica os dados para o Frontend. O React não precisa ver os eixos X e Y
     * brutos, ele só precisa saber o Status e a Posição para pintar o ícone no mapa.
     * 2. EXPLICAÇÃO:
     * - ultimaAtualizacao: Informa ao operador há quanto tempo a VTR não envia sinal.
     */
}