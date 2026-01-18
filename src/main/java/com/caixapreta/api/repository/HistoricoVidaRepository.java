package com.caixapreta.api.repository;

import com.caixapreta.api.model.HistoricoVidaViatura;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface HistoricoVidaRepository extends JpaRepository<HistoricoVidaViatura, Long> {
    // Busca todo o histórico de uma viatura específica (Cronologia)
    List<HistoricoVidaViatura> findByViaturaIdOrderByDataOcorrenciaDesc(Long viaturaId);

    // Busca por tipo de evento (Ex: Quantas vezes essa VTR teve SINISTRO?)
    List<HistoricoVidaViatura> findByVtrPrefixoAndTipoEvento(String prefixo, String tipo);
}