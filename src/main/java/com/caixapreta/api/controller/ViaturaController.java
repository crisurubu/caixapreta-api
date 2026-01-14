package com.caixapreta.api.controller;

import com.caixapreta.api.dto.DashboardStatsDTO;
import com.caixapreta.api.dto.TelemetriaRequestDTO;
import com.caixapreta.api.dto.ViaturaPainelDTO;
import com.caixapreta.api.service.ViaturaService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/viaturas")
@CrossOrigin(origins = "http://localhost:5173") // Porta padrão do Vite/React
public class ViaturaController {

    private final ViaturaService viaturaService;

    public ViaturaController(ViaturaService viaturaService) {
        this.viaturaService = viaturaService;
    }

    // 1. RECEBIMENTO (Entrada): O ESP32 envia os dados via POST
    @PostMapping("/telemetria")
    public ResponseEntity<Void> receberTelemetria(@RequestBody TelemetriaRequestDTO dados) {
        viaturaService.processarTelemetria(dados);
        return ResponseEntity.ok().build();
    }

    // 2. PAINEL (Saída): O Front-end busca a lista para o mapa e cards
    @GetMapping("/painel")
    public ResponseEntity<List<ViaturaPainelDTO>> obterPainelCompleto() {
        return ResponseEntity.ok(viaturaService.buscarTodasParaDashboard());
    }

    // 3. ESTATÍSTICAS (Saída): O Front-end busca números para os gráficos
    @GetMapping("/stats")
    public ResponseEntity<DashboardStatsDTO> obterEstatisticas() {
        return ResponseEntity.ok(viaturaService.obterEstatisticasGerais());
    }

    /* * --- DOCUMENTAÇÃO DO VIATURA_CONTROLLER ---
     * 1. O QUE FAZ: Atua como a porta de entrada e saída (Gateway) da API Caixa Preta.
     * 2. COMUNICAÇÃO DUAL: Gerencia o recebimento de dados brutos do hardware (ESP32)
     * e a entrega de dados processados para a interface de usuário (React).
     * 3. SEGURANÇA E ACESSO: Implementa a regra de @CrossOrigin para permitir que o
     * Frontend consuma os dados sem bloqueios de política de segurança do navegador.
     */
}