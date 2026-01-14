package com.caixapreta.api.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Record para transporte de dados de cadastro inicial ou atualização (Batismo) da viatura.
 */
public record ViaturaCadastroDTO(
        Long id,                  // ID gerado pelo sistema no auto-provisionamento (Opcional no cadastro manual)
        @NotBlank String placa,   // Placa oficial do veículo
        @NotBlank String chassi,  // Número do chassi para perícia
        @NotBlank String modelo,  // Marca/Modelo (ex: Toyota Hilux)
        @NotBlank String prefixo  // Prefixo operacional (ex: 15º BPM - 02)
) {

    /* * --- DOCUMENTAÇÃO DO VIATURA_CADASTRO_DTO ---
     * 1. O QUE FAZ: Atua como o contrato de dados para o registro formal da viatura no sistema.
     * 2. SUPORTE AO BATISMO: A inclusão do campo 'id' permite que o administrador selecione
     * uma viatura detectada automaticamente e preencha seus dados civis (Placa/Chassi).
     * 3. VALIDAÇÃO: Utiliza Bean Validation (@NotBlank) para garantir a integridade dos
     * dados obrigatórios de identificação veicular.
     */
}