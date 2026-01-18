package com.caixapreta.api.repository;

import com.caixapreta.api.model.ViaturaPendente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ViaturaPendenteRepository extends JpaRepository<ViaturaPendente, Long> {
    // JpaRepository já nos dá o save, findById e findAll
}