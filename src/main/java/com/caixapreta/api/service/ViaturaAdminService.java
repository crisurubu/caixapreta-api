package com.caixapreta.api.service;

import com.caixapreta.api.dto.ViaturaCadastroDTO;
import com.caixapreta.api.model.Viatura;
import com.caixapreta.api.model.ViaturaPendente;
import com.caixapreta.api.repository.ViaturaRepository;
import com.caixapreta.api.repository.ViaturaPendenteRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ViaturaAdminService {

    private final ViaturaRepository viaturaRepository;
    private final ViaturaPendenteRepository viaturaPendenteRepository;

    public ViaturaAdminService(ViaturaRepository viaturaRepository, ViaturaPendenteRepository viaturaPendenteRepository) {
        this.viaturaRepository = viaturaRepository;
        this.viaturaPendenteRepository = viaturaPendenteRepository;
    }

    @Transactional
    public Viatura cadastrar(Long id, ViaturaCadastroDTO dto) {
        // --- NOVA REGRA: BLINDAGEM DE UNICIDADE ---
        // Verificamos na tabela oficial se esses dados já existem antes de migrar
        if (viaturaRepository.existsByPlaca(dto.placa())) {
            throw new RuntimeException("CONFLITO: A placa " + dto.placa() + " já está cadastrada!");
        }
        if (viaturaRepository.existsByChassi(dto.chassi())) {
            throw new RuntimeException("CONFLITO: Este CHASSI já pertence a outra viatura!");
        }
        if (viaturaRepository.existsByPrefixo(dto.prefixo())) {
            throw new RuntimeException("CONFLITO: O prefixo " + dto.prefixo() + " já está em uso!");
        }

        // 1. Localizamos o hardware na tabela de pendentes
        ViaturaPendente pendente = viaturaPendenteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Hardware ID " + id + " não encontrado no radar!"));

        // 2. Criamos a nova entidade oficial (Transferindo os dados)
        Viatura novaVtr = new Viatura();
        novaVtr.setId(id); // O ID 888 agora será o ID oficial na tabela VIATURA
        novaVtr.setPlaca(dto.placa());
        novaVtr.setChassi(dto.chassi());
        novaVtr.setModelo(dto.modelo());
        novaVtr.setPrefixo(dto.prefixo());

        // 3. Aproveitamos a última localização conhecida do radar
        novaVtr.setNivelBateria(pendente.getNivelBateria());
        novaVtr.setLatitude(pendente.getLatitude());
        novaVtr.setLongitude(pendente.getLongitude());
        novaVtr.setUltimaAtualizacao(LocalDateTime.now());

        // 4. Status inicial: PATRULHANDO (para ela "nascer" ativa no mapa)
        novaVtr.setStatusOperacional("PATRULHANDO");
        novaVtr.setBloqueada(false);
        novaVtr.setKmDiarioAtual(0.0);
        novaVtr.setOdometroManutencao(0.0);

        // 5. SALVAMOS na tabela definitiva
        Viatura vtrSalva = viaturaRepository.save(novaVtr);

        // 6. APAGAMOS da tabela de pendentes (Saindo do limbo)
        viaturaPendenteRepository.delete(pendente);

        return vtrSalva;
    }

    // Retorna a lista completa de viaturas cadastradas no sistema
    public List<Viatura> listarTodas() {
        return viaturaRepository.findAll();
    }

    // Retorna as viaturas detectadas pelo radar que aguardam batismo
    public List<ViaturaPendente> listarPendentes() {
        return viaturaPendenteRepository.findAll();
    }

    /* * --- DOCUMENTAÇÃO DO VIATURA_ADMIN_SERVICE (ATUALIZADO) ---
     * 1. O QUE FAZ: Centraliza a gestão cadastral das viaturas da frota.
     * 2. SUPORTE AO BATISMO: Migra o hardware da tabela 'viaturas_pendentes'
     * para 'viaturas', aplicando as informações civis fornecidas pelo ADMIN.
     * 3. INTEGRIDADE TRANSACIONAL: O @Transactional garante que se o salvamento
     * falhar, o hardware não seja apagado dos pendentes (rollback).
     * 4. BLINDAGEM DE DADOS: Implementa verificações de 'existsBy' para Placa,
     * Chassi e Prefixo, impedindo duplicidade na tabela oficial durante o cadastro.
     */
}