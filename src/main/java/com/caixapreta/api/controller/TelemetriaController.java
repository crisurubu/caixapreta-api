package com.caixapreta.api.controller;

import com.caixapreta.api.dto.TelemetriaRequestDTO;
import com.caixapreta.api.service.ViaturaService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.caixapreta.api.dto.ViaturaPainelDTO;

import java.util.List;

// No seu Controller de Telemetria:
@CrossOrigin(origins = "http://localhost:5173")
@RestController
@RequestMapping("/api/telemetria")
public class TelemetriaController {

    private final ViaturaService viaturaService;

    // Construtor Manual (Resolve o erro se o Lombok travar)
    public TelemetriaController(ViaturaService viaturaService) {
        this.viaturaService = viaturaService;
    }

    @PostMapping
    public ResponseEntity<String> receberDados(@RequestBody @Valid TelemetriaRequestDTO dados) {
        // Envia os dados para o "Cérebro" (Service) processar a lógica das cores/acidente
        // --- SYSTEM LOG PARA VISUALIZAÇÃO NO CONSOLE ---
        // --- MONITORAMENTO COMPLETO (Sincronizado com o DTO) ---
        System.out.println("\n[SISTEMA CAIXA-PRETA] 📥 NOVA TELEMETRIA");
        System.out.printf("VTR ID: %d | SIRENE/EVENTO: %s\n", dados.vtrId(), dados.statusSirene());
        System.out.printf("METRICAS: %.1f km/h | %.2f G | Bat: %.1fV\n", dados.velocidade(), dados.gForce(), dados.nivelBateria());
        System.out.printf("ANGULOS: Lateral(X): %.1f° | Frontal(Y): %.1f°\n", dados.incX(), dados.incY());
        System.out.printf("GPS: Lat %.6f | Lng %.6f\n", dados.latitude(), dados.longitude());
        viaturaService.processarTelemetria(dados);

        // Retorna Sucesso para o hardware (ESP32)
        return ResponseEntity.ok("Dados processados com sucesso!");
    }

    // Adicione o import da List se necessário: import java.util.List;

    @GetMapping("/painel") // Mudamos o nome da rota para fazer mais sentido com a lista
    public ResponseEntity<List<ViaturaPainelDTO>> obterPainelCompleto() {
        // O erro some aqui porque agora os tipos (List) batem entre Controller e Service
        return ResponseEntity.ok(viaturaService.buscarTodasParaDashboard());
    }
    /* --- DOCUMENTAÇÃO DO TELEMETRIA_CONTROLLER ---
     * 1. O QUE ELA FAZ:
     * Expõe o "Endpoint" (URL) para que o mundo externo (ESP32 / Postman) envie dados.
     * 2. EXPLICAÇÃO DOS MÉTODOS E ANOTAÇÕES:
     * - @RestController: Define que esta classe responde via JSON.
     * - @RequestMapping("/api/telemetria"): Define a rota. O ESP32 enviará para: localhost:8080/api/telemetria
     * - @Valid: ESSENCIAL! Esta anotação ativa o @NotNull e @NotBlank que colocamos no DTO.
     * Se o dado for inválido, ela barra aqui mesmo.
     * - @RequestBody: Converte o JSON enviado pelo hardware automaticamente em um objeto Java (Record).
     */
}