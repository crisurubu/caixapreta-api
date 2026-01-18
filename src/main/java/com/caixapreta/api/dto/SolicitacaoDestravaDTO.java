package com.caixapreta.api.dto;

/**
 * DTO (Data Transfer Object) para Solicitação de Destravamento.
 * Agora inclui o Bloco de Auditoria (Quem e Qual).
 */
public record SolicitacaoDestravaDTO(
        Long viaturaId,          // ID único da VTR no banco
        String prefixo,          // Prefixo visual (ex: ROTA-01)
        String uuidAcidente,     // UUID do laudo de impacto
        String justificativa,    // Por que está sendo destravada?

        // BLOCO DE USUÁRIO (O que estava faltando!)
        Long usuarioId,          // ID do operador logado
        String usuarioNome       // Nome do operador para o histórico
) {}