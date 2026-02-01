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

    // --- BUSCAS DE NEGÓCIO E IDENTIFICAÇÃO ÚNICA ---
    Optional<Viatura> findByPrefixo(String prefixo);
    Optional<Viatura> findByPlaca(String placa);
    Optional<Viatura> findByChassi(String chassi);

    List<Viatura> findByStatusOperacional(String statusOperacional);
    List<Viatura> findByStatusOperacionalNot(String statusOperacional);
    long countByStatusOperacional(String statusOperacional);

    // --- MÉTODOS DE BLINDAGEM (VERIFICAÇÃO DE DUPLICIDADE) ---
    boolean existsByPrefixo(String prefixo);
    boolean existsByPlaca(String placa);
    boolean existsByChassi(String chassi);

    // --- OPERAÇÕES ATÔMICAS E GARANTIA DE INTEGRIDADE ---

    @Modifying
    @Transactional
    @Query("UPDATE Viatura v SET v.ultimaAtualizacao = :agora WHERE v.id = :id")
    void atualizarHeartbeat(@Param("id") Long id, @Param("agora") LocalDateTime agora);

    /**
     * Ajuste da Nova Regra: Garante a inserção sem violar as restrições de Unicidade.
     * Usa o ID para gerar valores temporários únicos de Prefixo, Placa e Chassi.
     */
    @Modifying
    @Transactional
    @Query(value = "INSERT INTO viaturas (id, prefixo, placa, chassi, status_operacional) " +
            "SELECT :id, CONCAT('VTR-', :id), CONCAT('TEMP-', :id), CONCAT('CH-TEMP-', :id), 'PENDENTE_CADASTRO' " +
            "WHERE NOT EXISTS (SELECT 1 FROM viaturas WHERE id = :id)",
            nativeQuery = true)
    void garantirViaturaNoBanco(@Param("id") Long id);

    // --- MÉTODOS PARA O WATCHDOG (MONITORAMENTO DE STATUS) ---

    List<Viatura> findByUltimaAtualizacaoBeforeAndStatusOperacionalNot(LocalDateTime limite, String status);
    List<Viatura> findByUltimaAtualizacaoIsNullAndStatusOperacionalNot(String status);

    @Modifying
    @Transactional
    @Query(value = "UPDATE viaturas SET status_operacional = 'OFF', alerta_adicional = 'TIMEOUT_SINAL' " +
            "WHERE (ultima_atualizacao < :limite OR ultima_atualizacao IS NULL) " +
            "AND status_operacional <> 'OFF'", nativeQuery = true)
    int matarViaturasInativas(@Param("limite") LocalDateTime limite);
}

// --- RESUMO DO FUNCIONAMENTO DA CLASSE (O QUE ELA FAZ) ---
/* * 1. BLINDAGEM DE UNICIDADE: Implementa a nova regra de negócio que impede a existência de viaturas com
 * mesmo Prefixo, Placa ou Chassi através de métodos 'existsBy', servindo de barreira para o Controller.
 * * 2. AUTO-CADASTRO RESILIENTE: O método 'garantirViaturaNoBanco' utiliza concatenação dinâmica para criar
 * identificadores temporários únicos. Isso evita erros de SQL Constraint quando múltiplos hardwares
 * novos tentam se registrar simultaneamente antes do cadastro oficial pelo Admin.
 * * 3. GESTÃO DE PERSISTÊNCIA: Abstrai as operações complexas de banco de dados, permitindo buscas rápidas
 * por identificadores naturais (Placa/Chassi) essenciais para a vinculação do Histórico de Vida.
 * * 4. WATCHDOG DE STATUS: Mantém a saúde visual do sistema através de queries nativas de alta performance
 * que detectam e desligam (Status OFF) ativos que perderam comunicação, prevenindo dados obsoletos.
 * * 5. INTEGRIDADE REFERENCIAL: Ao travar a duplicidade no nível de acesso a dados, garante que cada
 * perícia ou laudo gerado seja indubitavelmente atrelado a um único veículo físico.
 */