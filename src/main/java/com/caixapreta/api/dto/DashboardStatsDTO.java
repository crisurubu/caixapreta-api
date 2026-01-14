package com.caixapreta.api.dto;

public record DashboardStatsDTO(
        long emergencia,
        long acidente,
        long patrulha,
        long abordagem,
        long manutencao
) {
    /* O QUE FAZ: Entrega os totais prontos para os gráficos do Frontend. */
}