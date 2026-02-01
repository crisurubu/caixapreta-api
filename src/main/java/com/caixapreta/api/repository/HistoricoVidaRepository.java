package com.caixapreta.api.repository;

import com.caixapreta.api.model.HistoricoVidaViatura;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface HistoricoVidaRepository extends JpaRepository<HistoricoVidaViatura, Long> {

    // --- MÉTODOS ORIGINAIS ---
    List<HistoricoVidaViatura> findByViaturaIdOrderByDataOcorrenciaDesc(Long viaturaId);

    List<HistoricoVidaViatura> findByVtrPrefixoAndTipoEvento(String prefixo, String tipo);

    List<HistoricoVidaViatura> findAllByOrderByDataOcorrenciaDesc();

    // Busca o último registro de entrada para herança de telemetria no Service
    Optional<HistoricoVidaViatura> findFirstByViaturaIdAndTipoEventoOrderByDataOcorrenciaDesc(
            Long viaturaId,
            String tipoEvento
    );
    // Busca todos os registros de histórico de uma viatura específica
    @Query(value = "SELECT * FROM HISTORICO_VIDA_VIATURA WHERE VIATURA_ID = :viaturaId", nativeQuery = true)
    List<HistoricoVidaViatura> findByViaturaId(@Param("viaturaId") Long viaturaId);

    // --- MÉTODOS PARA A NOVA ESTRATÉGIA DE UUID ÚNICO ---

    /**
     * ✅ A CHAVE DA AUDITORIA:
     * Retorna toda a sequência de eventos (Entrada, Aprovação, Destrava)
     * que compartilham o mesmo UUID de Incidente.
     */
    List<HistoricoVidaViatura> findAllByUuidOrderByDataOcorrenciaAsc(String uuid);

    /**
     * Busca um evento específico dentro de um incidente.
     * Útil para validar se uma 'ENTRADA_EM_ANALISE' já existe para este UUID.
     */
    Optional<HistoricoVidaViatura> findByUuidAndTipoEvento(String uuid, String tipoEvento);
}