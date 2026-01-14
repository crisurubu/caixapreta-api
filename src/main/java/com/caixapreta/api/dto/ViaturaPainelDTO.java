package com.caixapreta.api.dto;

import java.time.LocalDateTime;

/**
 * Este DTO une os dados físicos (Telemetria) com a inteligência (Status)
 * para que o Frontend não precise fazer cálculos, apenas exibir.
 */
public record ViaturaPainelDTO(
        Long id,
        String prefixo,         // Adicionado para identificar a VTR no mapa
        Double velocidade,
        Double latitude,
        Double longitude,
        String statusOperacional, // Cor do Card/Ícone
        String statusSirene,     // Texto do hardware
        LocalDateTime ultimaAtualizacao,
        Boolean bloqueada        // Adicionado para a trava de acidente
) {
    /* --- DOCUMENTAÇÃO DO VIATURA_PAINEL_DTO ---
     * 1. O QUE ELE FAZ: Unifica o Hardware (Telemetria) e o Software (Status) em um único JSON.
     * 2. SINCRONISMO: Os nomes dos campos devem bater exatamente com o 'new ViaturaPainelDTO'
     * chamado no ViaturaService para evitar erros de compilação.
     * 3. FINALIDADE: Entrega para o Dashboard tudo o que é necessário para renderizar o estado tático.
     */
}