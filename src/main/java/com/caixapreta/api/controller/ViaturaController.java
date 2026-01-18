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

   

    /* * --- DOCUMENTAÇÃO DO VIATURA_CONTROLLER (GATEWAY) ---
     * 1. O QUE FAZ: Atua como a porta de entrada (Hardware) e saída (Frontend) da API.
     * 2. INTERFACE DE COMANDO: O novo endpoint 'desbloquear' permite que o operador humano
     * retome o controle da viatura após a resolução de um sinistro (Acidente).
     * 3. INTEGRAÇÃO REACT: Facilita a criação de botões de 'Reset' no Dashboard para
     * limpar estados de erro ou bloqueios táticos de forma remota.
     */
}