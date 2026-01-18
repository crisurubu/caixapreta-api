package com.caixapreta.api.service;

import com.caixapreta.api.dto.ViaturaCadastroDTO;
import com.caixapreta.api.model.Viatura;
import com.caixapreta.api.repository.ViaturaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ViaturaAdminService {

    private final ViaturaRepository viaturaRepository;

    public ViaturaAdminService(ViaturaRepository viaturaRepository) {
        this.viaturaRepository = viaturaRepository;
    }

    @Transactional
    public Viatura cadastrar(Long id, ViaturaCadastroDTO dto) {
        Viatura vtr = viaturaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Viatura não encontrada!"));

        vtr.setPlaca(dto.placa());
        vtr.setChassi(dto.chassi());
        vtr.setModelo(dto.modelo());
        vtr.setPrefixo(dto.prefixo());

        // LINHA ESSENCIAL: Muda o status para ela sair do "berçário" e ir para o Dashboard
        vtr.setStatusOperacional("PATRULHANDO");

        return viaturaRepository.save(vtr);
    }

    // Retorna a lista completa de viaturas cadastradas no sistema
    public List<Viatura> listarTodas() {
        // Usamos o findAll do JPA para trazer todos os registros da tabela VIATURAS
        return viaturaRepository.findAll();
    }
    // Retorna apenas as viaturas que ainda não foram "batizadas"
    public List<Viatura> listarPendentes() {
        return viaturaRepository.findByStatusOperacional("PENDENTE_CADASTRO");
    }
    /* * --- DOCUMENTAÇÃO DO VIATURA_ADMIN_SERVICE ---
     * 1. O QUE FAZ: Centraliza a gestão cadastral das viaturas da frota.
     * 2. SUPORTE AO BATISMO: Permite atualizar viaturas que foram criadas automaticamente
     * pela telemetria, preenchendo as informações civis (Placa/Chassi) de forma retroativa.
     * 3. INTEGRIDADE TRANSACIONAL: Utiliza @Transactional para garantir que a persistência
     * dos dados administrativos ocorra de forma atômica e segura.
     */
}