package com.caixapreta.api.controller;

import com.caixapreta.api.dto.TelemetriaRequestDTO;
import com.caixapreta.api.service.ViaturaService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.caixapreta.api.dto.ViaturaPainelDTO;

import java.util.List;

@CrossOrigin(origins = "http://localhost:5173")
@RestController
@RequestMapping("/api/telemetria")
public class TelemetriaController {

    private final ViaturaService viaturaService;

    public TelemetriaController(ViaturaService viaturaService) {
        this.viaturaService = viaturaService;
    }

    @PostMapping
    public ResponseEntity<String> receberDados(@RequestBody @Valid TelemetriaRequestDTO dados) {
        // Log de monitoramento em tempo real no console do desenvolvedor
        System.out.println("\n[SISTEMA CAIXA-PRETA] 📥 NOVA TELEMETRIA");
        System.out.printf("VTR ID: %d | SIRENE/EVENTO: %s\n", dados.vtrId(), dados.statusSirene());
        System.out.printf("METRICAS: %.1f km/h | %.2f G | Bat: %.1fV\n", dados.velocidade(), dados.gForce(), dados.nivelBateria());
        System.out.printf("ANGULOS: Lateral(X): %.1f° | Frontal(Y): %.1f°\n", dados.incX(), dados.incY());
        System.out.printf("GPS: Lat %.6f | Lng %.6f\n", dados.latitude(), dados.longitude());

        // Envia para o Service processar a inteligência de estados e travas
        viaturaService.processarTelemetria(dados);

        return ResponseEntity.ok("Dados processados com sucesso!");
    }

    @GetMapping("/painel")
    public ResponseEntity<List<ViaturaPainelDTO>> obterPainelCompleto() {
        return ResponseEntity.ok(viaturaService.buscarTodasParaDashboard());
    }
}

// --- RESUMO DO FUNCIONAMENTO DA CLASSE (O QUE ELA FAZ) ---
/* 1. PORTA DE ENTRADA (ENDPOINT): Esta classe atua como a interface de comunicação entre o hardware (ESP32) e o servidor. Ela expõe rotas REST para recebimento de dados via POST e consulta de status via GET.

2. VALIDAÇÃO DE CONTRATO: Utiliza a anotação @Valid para garantir que nenhum dado chegue incompleto ou corrompido ao sistema. Se o hardware enviar um pacote sem ID ou coordenadas, o Controller rejeita a requisição antes mesmo de gastar processamento no Service.

3. MONITORAMENTO DE DEPURAÇÃO: Implementa logs formatados no console que permitem ao técnico visualizar instantaneamente as forças G, inclinação e status da sirene sem precisar acessar o banco de dados, facilitando testes de bancada.

4. ALIMENTAÇÃO DO DASHBOARD: Através do endpoint /painel, ela fornece a lista completa de viaturas com seus estados processados (cores), permitindo que o Frontend (React/Vue) renderize o mapa em tempo real.

5. DESACOPLAMENTO: O Controller não contém nenhuma regra de negócio (como decidir se houve um acidente). Sua única função é garantir que a mensagem chegou corretamente e entregá-la ao ViaturaService.
*/