package com.caixapreta.api.repository;

import com.caixapreta.api.model.Viatura;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ViaturaRepository extends JpaRepository<Viatura, Long> {

    // --- BUSCAS (Mova para cá, longe dos @Modifying) ---
    Optional<Viatura> findByPrefixo(String prefixo);

    List<Viatura> findByStatusOperacionalNot(String statusOperacional);
    List<Viatura> findByStatusOperacional(String statusOperacional);
    long countByStatusOperacional(String statusOperacional);
    Optional<Viatura> findByPlaca(String placa);
    boolean existsByPrefixo(String prefixo);

    // --- OPERAÇÕES ATÔMICAS ---

    @Modifying
    @Transactional
    @Query("UPDATE Viatura v SET v.ultimaAtualizacao = :agora WHERE v.id = :id")
    void atualizarHeartbeat(@Param("id") Long id, @Param("agora") LocalDateTime agora);

    @Modifying
    @Transactional
    @Query(value = "INSERT INTO viaturas (id, prefixo, status_operacional) " +
            "SELECT :id, 'NOVA_VTR', 'PENDENTE_CADASTRO' " +
            "WHERE NOT EXISTS (SELECT 1 FROM viaturas WHERE id = :id)",
            nativeQuery = true)
    void garantirViaturaNoBanco(@Param("id") Long id); // Adicione o nome do método aqui se ele sumiu
}
    /* --- DOCUMENTAÇÃO DO VIATURA_REPOSITORY ---
     * 1. O QUE FAZ:
     * Gerencia a persistência das viaturas, separando unidades operacionais de unidades recém-detectadas.
     * * 2. ESTRATÉGIA DE SEGREGAÇÃO:
     * - findByStatusOperacionalNot("PENDENTE_CADASTRO"): Alimenta o mapa principal apenas com viaturas validadas.
     * - findByStatusOperacional("PENDENTE_CADASTRO"): Alimenta a "Fila de Ativação" para o Admin batizar os novos IDs.
     * * 3. PERFORMANCE E CONCORRÊNCIA:
     * - O método atualizarHeartbeat utiliza SQL direto (@Modifying) para evitar que o fluxo intenso de
     * telemetria cause erros de versão (StaleObjectStateException) quando o Admin tentar editar a viatura.
     */
