package com.caixapreta.api.controller;

import com.caixapreta.api.dto.SolicitacaoDestravaDTO;
import com.caixapreta.api.service.ViaturaService;
import com.caixapreta.api.model.Usuario;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/viaturas/destrava")
@CrossOrigin(origins = "http://localhost:5173") // Ajustado para sua porta do React
public class DestravaController {

    @Autowired
    private ViaturaService viaturaService;

    // 1. SOLICITAR (Operador Logado)
    @PostMapping("/solicitar")
    public ResponseEntity<String> solicitarDestravamento(
            @RequestBody SolicitacaoDestravaDTO dto,
            Authentication authentication) {
        try {
            // Se o filtro estiver certo, o Principal será o seu Model Usuario
            Usuario user = (Usuario) authentication.getPrincipal();

            // Log para conferência no console do Java
            System.out.println(">>> [SOLICITAÇÃO] Usuário: " + user.getUsername() + " pedindo destrava para VTR: " + dto.viaturaId());

            viaturaService.solicitarDestravamento(
                    dto.viaturaId(),
                    dto.uuidAcidente(),
                    dto.justificativa(),
                    user.getId(),
                    user.getUsername()
            );

            return ResponseEntity.ok("Solicitação registrada. Responsável: " + user.getUsername());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Erro na solicitação: " + e.getMessage());
        }
    }

    // 2. APROVAR (Admin Logado)
    @PostMapping("/aprovar/{id}")
    public ResponseEntity<String> aprovarDestravamento(
            @PathVariable Long id,
            Authentication authentication) {
        try {
            // Recupera o Admin que está aprovando
            Usuario admin = (Usuario) authentication.getPrincipal();

            // Log de Auditoria
            System.out.println(">>> [APROVAÇÃO] Admin: " + admin.getUsername() + " autorizando ID: " + id);

            viaturaService.aprovarDestravamento(
                    id,
                    admin.getId(),
                    admin.getUsername()
            );

            return ResponseEntity.ok("Viatura liberada com sucesso por: " + admin.getUsername());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Erro ao aprovar liberação: " + e.getMessage());
        }
    }

    @GetMapping("/pendentes")
    public ResponseEntity<?> listarPendentes() {
        try {
            return ResponseEntity.ok(viaturaService.buscarTodasPendentes());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Erro ao carregar lista: " + e.getMessage());
        }
    }

    @GetMapping("/check-silencio/{prefixo}")
    public ResponseEntity<Boolean> checkSilencio(@PathVariable String prefixo) {
        return ResponseEntity.ok(viaturaService.deveIgnorarImpacto(prefixo));
    }
}