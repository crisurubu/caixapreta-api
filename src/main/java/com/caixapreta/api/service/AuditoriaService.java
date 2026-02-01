package com.caixapreta.api.service;

import com.caixapreta.api.model.Alarme;
import com.caixapreta.api.repository.AlarmeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class AuditoriaService {

    @Autowired
    private AlarmeRepository alarmeRepository;

    /**
     * Recupera a "Sessão Pericial".
     * Pega o primeiro impacto do dossiê e abre uma janela de 4 minutos ao redor dele.
     */
    public List<Alarme> obterSessaoCompletaAcidente(String uuidReferencia) {
        // 1. Busca todos os alarmes que compartilham esse UUID (Dossiê)
        List<Alarme> dossie = alarmeRepository.findAllByUuidOrderByDataHoraAsc(uuidReferencia);

        if (dossie.isEmpty()) {
            System.err.println(">>> [AUDITORIA] Aviso: Dossiê não encontrado para UUID: " + uuidReferencia);
            return new ArrayList<>();
        }

        // 2. Usamos o PRIMEIRO evento do dossiê como o "Ponto Zero" do acidente
        Alarme pontoZero = dossie.get(0);

        // 3. Define a Janela Pericial (2 min antes e 2 min depois do impacto inicial)
        // Isso garante que se houveram 3 impactos em 30 segundos, todos apareçam na mesma janela.
        LocalDateTime inicio = pontoZero.getDataHora().minusMinutes(2);
        LocalDateTime fim = pontoZero.getDataHora().plusMinutes(2);

        // 4. Busca toda a telemetria técnica da viatura nesse período
        return alarmeRepository.findAllByViaturaIdAndDataHoraBetweenOrderByDataHoraAsc(
                pontoZero.getViatura().getId(),
                inicio,
                fim
        );
    }
}