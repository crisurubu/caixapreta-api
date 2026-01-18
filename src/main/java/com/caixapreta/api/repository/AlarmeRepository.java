package com.caixapreta.api.repository;

import com.caixapreta.api.model.Alarme;
import com.caixapreta.api.model.Viatura;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface AlarmeRepository extends JpaRepository<Alarme, Long> {

    // Mantém a busca por tipo específico (usado em lógicas de negócio pontuais)
    Optional<Alarme> findTopByViaturaAndTipoEventoOrderByDataHoraDesc(Viatura viatura, String tipoEvento);

    // NOVO: Busca o último alarme crítico (ACIDENTE ou TOMBAMENTO)
    // Essencial para comparar Deltas de Força G e Inclinação em múltiplos impactos
    Optional<Alarme> findTopByViaturaAndTipoEventoInOrderByDataHoraDesc(Viatura viatura, List<String> tipos);

    // Essencial para carregar a tabela de histórico na Web
    List<Alarme> findByViaturaIdOrderByDataHoraDesc(Long viaturaId);

    // Chave mestra para a geração do Laudo PDF individual
    Optional<Alarme> findByUuid(String uuid);


}

/**
 * --- DOCUMENTAÇÃO DO ALARME_REPOSITORY (VERSÃO FINAL) ---
 * 1. O QUE FAZ: Interface de abstração de dados (DAL) para a "Caixa-Preta" do sistema.
 * 2. INTEGRIDADE: Suporta a persistência completa dos novos campos de auditoria (incX, bateria, endereço)
 * sem necessidade de queries manuais.
 * 3. DESEMPENHO: O método 'findTopBy...' é crucial para o mecanismo de debounce, impedindo
 * o flooding (excesso) de registros idênticos no banco de dados.
 * 4. RASTREABILIDADE: Garante a recuperação de registros via UUID, isolando dados de perícia
 * de IDs sequenciais previsíveis.
 */