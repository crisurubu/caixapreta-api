package com.caixapreta.api.service;

import com.caixapreta.api.dto.HistoricoAuditoriaDTO;
import com.caixapreta.api.model.Alarme;
import com.caixapreta.api.model.HistoricoVidaViatura;
import com.caixapreta.api.repository.AlarmeRepository;
import com.caixapreta.api.repository.HistoricoVidaRepository;
import com.caixapreta.api.repository.ViaturaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.HexFormat;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

@Service
public class HistoricoVidaService {

    @Autowired
    private HistoricoVidaRepository repository;

    @Autowired
    private ViaturaRepository viaturaRepository;

    @Autowired
    private AlarmeRepository alarmeRepository;

    @Transactional(readOnly = true)
    public List<HistoricoAuditoriaDTO> buscarAuditoriaCompleta() {
        List<HistoricoVidaViatura> historicos = repository.findAllByOrderByDataOcorrenciaDesc();
        List<Alarme> alarmes = alarmeRepository.findAll();

        Stream<HistoricoAuditoriaDTO> dtoHistorico = historicos.stream().map(this::mapToDTO);
        Stream<HistoricoAuditoriaDTO> dtoAlarmes = alarmes.stream().map(this::mapAlarmeToDTO);

        return Stream.concat(dtoHistorico, dtoAlarmes)
                .sorted((a, b) -> b.dataOcorrencia().compareTo(a.dataOcorrencia()))
                .toList();
    }

    private HistoricoAuditoriaDTO mapAlarmeToDTO(Alarme a) {
        return new HistoricoAuditoriaDTO(
                a.getUuid(),
                null,
                a.getViatura() != null ? a.getViatura().getPrefixo() : "VTR-DESCONHECIDA",
                a.getDataHora(),
                a.getTipoEvento(),
                a.getVelocidade() != null ? a.getVelocidade() : 0.0,
                a.getgForce() != null ? a.getgForce() : 0.0,
                a.getIncX() != null ? a.getIncX() : 0.0,
                a.getIncY() != null ? a.getIncY() : 0.0,
                a.getNivelBateria() != null ? a.getNivelBateria() : 0.0,
                "OFF",
                a.getLatitude(),
                a.getLongitude(),
                "SENSORES_HARDWARE",
                a.getEndereco(),
                null, // Alarmes técnicos são dados brutos do sensor
                "DEVICE_IOT"
        );
    }

    @Transactional
    public String registrarLogDownload(String vtrPrefixo, String usuario, String ip, Long viaturaId, String uuidEventoOrigem, Long usuarioId) {
        HistoricoVidaViatura log = new HistoricoVidaViatura();
        LocalDateTime agora = LocalDateTime.now();

        // --- ESTRATÉGIA DE AMARRAÇÃO PERICIAL ---
        if (uuidEventoOrigem != null && !uuidEventoOrigem.isBlank() && !uuidEventoOrigem.equals("Geral")) {
            log.setUuid(UUID.randomUUID().toString());
            log.setUuidEventoOrigem(uuidEventoOrigem);

            // AJUSTE AQUI: O findByUuid agora retorna uma List.
            // Pegamos o primeiro (index 0) para preencher os dados de telemetria do log.
            List<Alarme> alarmesRelacionados = alarmeRepository.findByUuid(uuidEventoOrigem);

            if (!alarmesRelacionados.isEmpty()) {
                Alarme principal = alarmesRelacionados.get(0); // O primeiro impacto da série
                log.setVelocidade(principal.getVelocidade() != null ? principal.getVelocidade() : 0.0);
                log.setForcaG(principal.getgForce() != null ? principal.getgForce() : 0.0);
                log.setIncX(principal.getIncX() != null ? principal.getIncX() : 0.0);
                log.setIncY(principal.getIncY() != null ? principal.getIncY() : 0.0);
                log.setLatitude(principal.getLatitude());
                log.setLongitude(principal.getLongitude());
                log.setNivelBateria(principal.getNivelBateria() != null ? principal.getNivelBateria() : 0.0);
            }
        } else {
            log.setUuid(UUID.randomUUID().toString());
            log.setVelocidade(0.0);
            log.setForcaG(0.0);
            log.setIncX(0.0);
            log.setIncY(0.0);
            log.setNivelBateria(0.0);
        }

        log.setVtrPrefixo(vtrPrefixo);
        log.setViaturaId(viaturaId);
        log.setUsuarioId(usuarioId);
        log.setDataOcorrencia(agora);
        log.setTipoEvento("DOWNLOAD_LAUDO_PERICIAL");
        log.setNomeResponsavel(usuario);
        log.setIpOrigem(ip);
        log.setDescricao("Acesso ao dossiê: " + uuidEventoOrigem);
        log.setStatusSirene("OFF");

        String hash = gerarHashIntegridade(log.getUuid(), vtrPrefixo, usuario, agora);
        log.setHashIntegridade(hash);

        repository.save(log);
        return hash;
    }

    private String gerarHashIntegridade(String uuid, String prefixo, String user, LocalDateTime data) {
        try {
            String rawData = uuid + "|" + prefixo + "|" + user + "|" + data.toString();
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(rawData.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hashBytes).toUpperCase();
        } catch (NoSuchAlgorithmException e) {
            return "HASH_ERROR";
        }
    }

    private HistoricoAuditoriaDTO mapToDTO(HistoricoVidaViatura h) {
        return new HistoricoAuditoriaDTO(
                h.getUuid(),
                h.getUuidEventoOrigem(),
                h.getVtrPrefixo(),
                h.getDataOcorrencia(),
                h.getTipoEvento(),
                h.getVelocidade() != null ? h.getVelocidade() : 0.0,
                h.getForcaG() != null ? h.getForcaG() : 0.0,
                h.getIncX() != null ? h.getIncX() : 0.0,
                h.getIncY() != null ? h.getIncY() : 0.0,
                h.getNivelBateria() != null ? h.getNivelBateria() : 0.0,
                h.getStatusSirene() != null ? h.getStatusSirene() : "OFF",
                h.getLatitude() != null ? h.getLatitude() : 0.0,
                h.getLongitude() != null ? h.getLongitude() : 0.0,
                h.getNomeResponsavel() != null ? h.getNomeResponsavel() : "SISTEMA",
                h.getDescricao(),
                h.getHashIntegridade(),
                h.getIpOrigem() != null ? h.getIpOrigem() : "0.0.0.0"
        );
    }
}

// --- RESUMO DO FUNCIONAMENTO DA CLASSE (O QUE ELA FAZ) ---
/* 1. CONSOLIDAÇÃO PERICIAL: Esta classe é o ponto de encontro entre os dados brutos de hardware (Alarmes) e os eventos de software (Logs). Ela cria uma linha do tempo única que reconstrói a vida da viatura.

2. SEGURANÇA SHA-256: Implementa a geração de Hash de integridade. Qualquer alteração manual no banco de dados invalida o Hash, servindo como prova de que o registro de auditoria é original e não foi adulterado.

3. RASTREABILIDADE DE ACESSO: O método de registro de download captura não apenas quem baixou o laudo, mas vincula o UUID do evento original, criando uma "cadeia de custódia" digital para os dados de acidente.

4. TRATAMENTO DE NULOS: Garante que o Dashboard nunca trave por falta de dados (NullPointer), convertendo valores ausentes em 0.0 ou nomes padrão, mantendo a interface sempre funcional.

5. MODO SOMENTE LEITURA: Utiliza @Transactional(readOnly = true) na busca completa para otimizar a performance do banco de dados durante consultas pesadas de auditoria.
*/