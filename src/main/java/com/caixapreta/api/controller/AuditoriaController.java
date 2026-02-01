package com.caixapreta.api.controller;

import com.caixapreta.api.dto.HistoricoAuditoriaDTO;
import com.caixapreta.api.model.Alarme;
import com.caixapreta.api.model.Usuario;
import com.caixapreta.api.service.AuditoriaService;
import com.caixapreta.api.service.HistoricoVidaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/auditoria")
// ✅ AJUSTE PARA TOKEN: Removido allowCredentials pois agora usamos localStorage/Bearer Token
// ✅ Sincronizado com a porta do seu AuthController (Vite usa 5173 por padrão)
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:3000"})
@PreAuthorize("hasRole('ADMIN')")
public class AuditoriaController {

    @Autowired
    private HistoricoVidaService historicoService;

    @Autowired
    private AuditoriaService auditoriaService;

    @GetMapping("/fluxo-completo")
    public ResponseEntity<List<HistoricoAuditoriaDTO>> obterAuditoriaGeral() {
        List<HistoricoAuditoriaDTO> dados = historicoService.buscarAuditoriaCompleta();
        return ResponseEntity.ok(dados);
    }

    @PostMapping("/registrar-download")
    public ResponseEntity<Map<String, String>> registrarDownload(
            @RequestBody Map<String, Object> payload,
            Authentication authentication,
            HttpServletRequest request) {

        // ✅ Com o Interceptor de Token no Front, o Spring Security preenche este objeto automaticamente
        Usuario usuarioLogado = (Usuario) authentication.getPrincipal();

        String prefixo = (String) payload.get("prefixo");
        Long viaturaId = Long.valueOf(payload.get("viaturaId").toString());
        String uuidEventoOrigem = (String) payload.get("uuidOrigem");

        String ipTerminal = request.getRemoteAddr();
        if ("0:0:0:0:0:0:0:1".equals(ipTerminal)) ipTerminal = "127.0.0.1";

        String hashGerado = historicoService.registrarLogDownload(
                prefixo,
                usuarioLogado.getUsername(),
                ipTerminal,
                viaturaId,
                uuidEventoOrigem,
                usuarioLogado.getId()
        );

        return ResponseEntity.ok(Map.of(
                "status", "LAUDO_LACRADO",
                "hash", hashGerado,
                "data", java.time.LocalDateTime.now().toString()
        ));
    }

    @GetMapping("/incidente/{uuid}")
    public ResponseEntity<List<Alarme>> obterSessaoIncidente(@PathVariable String uuid) {
        List<Alarme> sessao = auditoriaService.obterSessaoCompletaAcidente(uuid);
        return ResponseEntity.ok(sessao);
    }
}