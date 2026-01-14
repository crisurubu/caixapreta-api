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
    public Viatura cadastrar(ViaturaCadastroDTO dto) {
        // LÓGICA DE ATUALIZAÇÃO OU CRIAÇÃO:
        // Se o DTO trouxer um ID que já existe (auto-provisionado), nós buscamos ela.
        // Se não, criamos uma nova do zero.
        Viatura vtr = (dto.id() != null)
                ? viaturaRepository.findById(dto.id()).orElse(new Viatura())
                : new Viatura();

        vtr.setPlaca(dto.placa());
        vtr.setChassi(dto.chassi());
        vtr.setModelo(dto.modelo());
        vtr.setPrefixo(dto.prefixo());

        // Mantemos o status que ela já tiver (se já estiver online) ou definimos OFFLINE
        if (vtr.getStatusOperacional() == null) {
            vtr.setStatusOperacional("OFFLINE");
            vtr.setBloqueada(false);
        }

        return viaturaRepository.save(vtr);
    }

    public List<Viatura> listarTodas() {
        return viaturaRepository.findAll();
    }

    /* * --- DOCUMENTAÇÃO DO VIATURA_ADMIN_SERVICE ---
     * 1. O QUE FAZ: Centraliza a gestão cadastral das viaturas da frota.
     * 2. SUPORTE AO BATISMO: Permite atualizar viaturas que foram criadas automaticamente
     * pela telemetria, preenchendo as informações civis (Placa/Chassi) de forma retroativa.
     * 3. INTEGRIDADE TRANSACIONAL: Utiliza @Transactional para garantir que a persistência
     * dos dados administrativos ocorra de forma atômica e segura.
     */
}