package com.caixapreta.api.repository;

import com.caixapreta.api.model.Viatura;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ViaturaRepository extends JpaRepository<Viatura, Long> {

    long countByStatusOperacional(String statusOperacional);

    /* --- DOCUMENTAÇÃO DO VIATURA_REPOSITORY ---
     * 1. O QUE ELA FAZ:
     * É a ponte de comunicação entre o Java e a tabela "viaturas" no banco H2.
     * 2. EXPLICAÇÃO:

     */
}