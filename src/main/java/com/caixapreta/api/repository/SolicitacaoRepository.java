package com.caixapreta.api.repository;

import com.caixapreta.api.model.SolicitacaoDestrava;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface SolicitacaoRepository extends JpaRepository<SolicitacaoDestrava, Long> {

    // Busca por ID da Viatura (Mais preciso que o prefixo para auditoria interna)
    List<SolicitacaoDestrava> findByViaturaId(Long viaturaId);

    // Busca pelo UUID do Evento (Para saber se um acidente específico já tem solicitação)
    Optional<SolicitacaoDestrava> findByUuidEventoOrigem(String uuid);

    // Busca todas as solicitações por status (Ex: PENDENTE, EM_MANUTENCAO)
    List<SolicitacaoDestrava> findByStatusAnalise(String status);

    // Busca solicitações de um usuário específico
    List<SolicitacaoDestrava> findByUsuarioId(Long usuarioId);
}