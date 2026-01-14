package com.caixapreta.api.repository;

import com.caixapreta.api.model.Alarme;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface AlarmeRepository extends JpaRepository<Alarme, Long> {

    // Busca o histórico de alertas de uma viatura específica ordenado pelo mais recente
    List<Alarme> findByViaturaIdOrderByDataHoraDesc(Long viaturaId);

    // Busca um alerta específico pelo seu identificador universal (UUID)
    Optional<Alarme> findByUuid(String uuid);

    /* --- DOCUMENTAÇÃO DO ALARME_REPOSITORY ---
     * 1. O QUE FAZ: Gerencia o acesso aos registros de eventos críticos capturados pelo StatusAlerta.
     * 2. SEGURANÇA: Utiliza UUID para consultas externas, permitindo a geração de laudos periciais
     * seguros e protegidos contra ataques de enumeração de IDs.
     * 3. PERFORMANCE: Otimizado para buscar alertas por ID de viatura de forma indexada e cronológica.
     */
}