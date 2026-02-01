package com.caixapreta.api.repository;

import com.caixapreta.api.model.Alarme;
import com.caixapreta.api.model.Viatura;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface AlarmeRepository extends JpaRepository<Alarme, Long> {

    // --- CONSULTAS OPERACIONAIS ---
    Optional<Alarme> findTopByViaturaAndTipoEventoOrderByDataHoraDesc(Viatura viatura, String tipoEvento);

    Optional<Alarme> findTopByViaturaAndTipoEventoInOrderByDataHoraDesc(Viatura viatura, List<String> tipos);

    List<Alarme> findAllByViaturaIdAndDataHoraBetweenOrderByDataHoraAsc(Long viaturaId, LocalDateTime inicio, LocalDateTime fim);

    @Query("SELECT a FROM Alarme a JOIN FETCH a.viatura v WHERE v.id = :id ORDER BY a.dataHora DESC")
    List<Alarme> findByViaturaIdOrderByDataHoraDesc(@Param("id") Long id);

    // --- LÓGICA DE UNIFICAÇÃO (CAIXA-PRETA) ---
    Optional<Alarme> findFirstByViaturaOrderByDataHoraDesc(Viatura viatura);

    Optional<Alarme> findTopByViaturaAndDataHoraAfterOrderByDataHoraDesc(Viatura viatura, LocalDateTime dataLimite);

    // --- EXPORTAÇÃO E AUDITORIA (LAUDO) ---

    // ✅ BUSCA PARA O PDF (Múltiplos registros)
    List<Alarme> findAllByUuidOrderByDataHoraAsc(String uuid);

    // ✅ BUSCA PARA O SERVICE (Registro Único)
    // Alteramos o nome para 'findFirstByUuid' para o Spring não confundir com o 'findByUuid' que retorna lista.
    Optional<Alarme> findFirstByUuid(String uuid);

    // ✅ BUSCA GENÉRICA (Retorna Lista para manter compatibilidade com outras partes do sistema)
    List<Alarme> findByUuid(String uuid);
}

// --- RESUMO DO FUNCIONAMENTO DA CLASSE (O QUE ELA FAZ) ---
/* 1. GESTÃO DE CRONOLOGIA PERICIAL: Esta interface atua como o indexador principal da "Caixa-Preta".
   Através de métodos 'findTop' e 'OrderByDataHoraDesc', ela garante que o sistema sempre recupere
   o estado mais recente da viatura, essencial para reconstruções de acidentes.

2. UNIFICAÇÃO VIA UUID (DNA): Implementa a lógica de continuidade de eventos. O método
   'findFirstByViaturaOrderByDataHoraDesc' permite que múltiplos pacotes de telemetria enviados
   pelo hardware sejam agrupados sob o mesmo UUID de incidente durante a janela de impacto.

3. COMPATIBILIDADE DE DADOS: Resolve o conflito entre processos de Auditoria e Exportação.
   Oferece tanto o retorno em 'Optional' (através do findFirstByUuid para o ViaturaService)
   quanto em 'List' (através do findByUuid para o gerador de laudos), evitando erros de
   NonUniqueResultException.

4. RASTREABILIDADE JURÍDICA: Os métodos de busca por período e ordenação ascendente por UUID
   asseguram que os dados exportados para o checklist de auditoria mantenham a integridade
   cronológica, fundamental para a validade de provas em perícias táticas.
*/