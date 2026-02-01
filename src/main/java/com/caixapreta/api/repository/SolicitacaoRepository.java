package com.caixapreta.api.repository;

import com.caixapreta.api.model.SolicitacaoDestrava;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface SolicitacaoRepository extends JpaRepository<SolicitacaoDestrava, Long> {

    // Busca por ID da Viatura (Histórico de solicitações de uma unidade específica)
    List<SolicitacaoDestrava> findByViaturaId(Long viaturaId);

    // ✅ A AMARRA: Busca pelo UUID do Evento para validar se o incidente já foi processado
    Optional<SolicitacaoDestrava> findByUuidEventoOrigem(String uuid);

    // Busca todas as solicitações por status (Crucial para o Dashboard de Pendências)
    List<SolicitacaoDestrava> findByStatusAnalise(String status);

    // Busca solicitações de um usuário específico para auditoria de produtividade
    List<SolicitacaoDestrava> findByUsuarioId(Long usuarioId);

    // ✅ ADIÇÃO PARA AUDITORIA: Busca solicitações por administrador
    List<SolicitacaoDestrava> findByAdminId(Long adminId);
    // ✅ Adicione esta linha para o erro 1 sumir

}