package com.caixapreta.api.controller;

import com.caixapreta.api.dto.SolicitacaoDestravaDTO;
import com.caixapreta.api.service.ViaturaService;
import com.caixapreta.api.model.Usuario;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/viaturas/destrava")
@CrossOrigin(origins = "http://localhost:5173")
public class DestravaController {

    @Autowired
    private ViaturaService viaturaService;

    // 1. SOLICITAR DESTRAVA (Ajustado para enviar o IP)
    @PostMapping("/solicitar")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> solicitarDestravamento(
            @RequestBody SolicitacaoDestravaDTO dto,
            Authentication authentication,
            HttpServletRequest request) { // ✅ Adicionado HttpServletRequest
        try {
            Usuario user = (Usuario) authentication.getPrincipal();

            // ✅ Captura o IP de quem está solicitando
            String ipOrigem = request.getRemoteAddr();
            if ("0:0:0:0:0:0:0:1".equals(ipOrigem)) ipOrigem = "127.0.0.1";

            viaturaService.solicitarDestravamento(
                    dto.viaturaId(),
                    dto.uuidAcidente(),
                    dto.justificativa(),
                    user.getId(),
                    user.getUsername(),
                    ipOrigem // ✅ Passando o IP para o Service rodar liso
            );

            return ResponseEntity.ok("Solicitação registrada. Responsável: " + user.getUsername());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Erro na solicitação: " + e.getMessage());
        }
    }

    // 2. APROVAR DESTRAVA
    @PostMapping("/aprovar/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> aprovarDestravamento(
            @PathVariable Long id,
            Authentication authentication,
            HttpServletRequest request) {
        try {
            Usuario admin = (Usuario) authentication.getPrincipal();

            String ipAdmin = request.getRemoteAddr();
            if ("0:0:0:0:0:0:0:1".equals(ipAdmin)) ipAdmin = "127.0.0.1";

            System.out.println(">>> [LOG SEGURANÇA] Aprovador: " + admin.getUsername() + " | Terminal IP: " + ipAdmin);

            viaturaService.aprovarDestravamento(
                    id,
                    admin.getId(),
                    admin.getUsername(),
                    ipAdmin
            );

            return ResponseEntity.ok("Viatura liberada com sucesso por: " + admin.getUsername());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(403).body("Erro na autorização ou processamento: " + e.getMessage());
        }
    }

    // 3. LISTAR PENDÊNCIAS
    @GetMapping("/pendentes")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> listarPendentes() {
        return ResponseEntity.ok(viaturaService.buscarTodasPendentes());
    }

    // 4. VERIFICAÇÃO TÉCNICA
    @GetMapping("/check-silencio/{prefixo}")
    public ResponseEntity<Boolean> checkSilencio(@PathVariable String prefixo) {
        return ResponseEntity.ok(viaturaService.deveIgnorarImpacto(prefixo));
    }
}