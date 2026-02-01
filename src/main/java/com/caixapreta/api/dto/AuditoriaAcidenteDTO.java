package com.caixapreta.api.dto;

import com.caixapreta.api.model.Alarme;

import java.time.LocalDateTime;
import java.util.List;

// AuditoriaAcidenteDTO.java
public record AuditoriaAcidenteDTO(
        String uuidReferencia,
        String prefixoViatura,
        LocalDateTime dataImpacto,
        List<Alarme> registrosContexto, // Aqui virão todos os logs da janela de tempo
        Double picoGForce,
        Double picoInclinacaoY,
        String enderecoPrincipal
) {}