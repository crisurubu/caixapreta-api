package com.caixapreta.api.controller;

import com.caixapreta.api.dto.ViaturaCadastroDTO;
import com.caixapreta.api.model.Viatura;
import com.caixapreta.api.model.ViaturaPendente;
import com.caixapreta.api.service.ViaturaAdminService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/viaturas")
@CrossOrigin(origins = "http://localhost:5173")
public class ViaturaAdminController {

    private final ViaturaAdminService adminService;

    public ViaturaAdminController(ViaturaAdminService adminService) {
        this.adminService = adminService;
    }

    @GetMapping("/pendentes")
    public ResponseEntity<List<ViaturaPendente>> listarPendentes() {
        return ResponseEntity.ok(adminService.listarPendentes());
    }

    // 1. ATUALIZAÇÃO/BATISMO COM TRATAMENTO DE ERROS (BLINDAGEM)
    @PutMapping("/{id}")
    public ResponseEntity<?> cadastrar(@PathVariable Long id, @RequestBody @Valid ViaturaCadastroDTO dto) {
        try {
            // Tenta realizar o cadastro blindado no Service
            Viatura vtr = adminService.cadastrar(id, dto);
            return ResponseEntity.ok(vtr);
        } catch (RuntimeException e) {
            // Se cair aqui, é porque o Service barrou a Placa, Chassi ou Prefixo repetido
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        }
    }

    @GetMapping
    public ResponseEntity<List<Viatura>> listar() {
        return ResponseEntity.ok(adminService.listarTodas());
    }

    /* * --- DOCUMENTAÇÃO DO VIATURA_ADMIN_CONTROLLER (BLINDADO) ---
     * 1. O QUE FAZ: Provê as ferramentas de gestão para o administrador do sistema.
     * 2. TRATAMENTO DE CONFLITOS: Implementa um bloco try-catch para interceptar exceções
     * de negócio (duplicidade) e retornar o status HTTP 409 (Conflict) ao usuário.
     * 3. FLUXO OPERACIONAL: Gerencia o batismo de ativos vindo do radar, convertendo
     * o sinal de hardware em uma unidade operacional oficial com dados únicos.
     * 4. SEGURANÇA DE DADOS: Garante que o Front-end receba mensagens claras de erro
     * caso o administrador tente cadastrar um ativo que já existe na base de dados.
     */
}