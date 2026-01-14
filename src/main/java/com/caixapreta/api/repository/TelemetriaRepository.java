package com.caixapreta.api.repository;

import com.caixapreta.api.model.Telemetria;
import com.caixapreta.api.model.Viatura;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TelemetriaRepository extends JpaRepository<Telemetria, Long> {

    // Busca o último registro inserido baseado no ID (ou data)
    Optional<Telemetria> findFirstByOrderByIdDesc();

    Optional<Telemetria> findFirstByViaturaOrderByIdDesc(Viatura viatura);

    /* --- DOCUMENTAÇÃO DO TELEMETRIA_REPOSITORY ---
     * 1. O QUE ELA FAZ:
     * Gerencia a gravação do histórico de sensores na tabela "telemetria_logs".
     * 2. EXPLICAÇÃO:
     * - JpaRepository<Telemetria, Long>: Indica que gerencia a Telemetria
     * e o ID é do tipo Long (numérico autoincremento).
     */
}