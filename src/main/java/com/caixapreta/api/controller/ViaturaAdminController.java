package com.caixapreta.api.controller;

import com.caixapreta.api.dto.ViaturaCadastroDTO;
import com.caixapreta.api.model.Viatura;
import com.caixapreta.api.service.ViaturaAdminService;
import jakarta.validation.Valid;
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

    // 1. CADASTRO/ATUALIZAÇÃO: Para "batizar" a viatura que apareceu no mapa
    @PostMapping
    public ResponseEntity<Viatura> cadastrar(@RequestBody @Valid ViaturaCadastroDTO dto) {
        return ResponseEntity.ok(adminService.cadastrar(dto));
    }

    // 2. LISTAGEM: Para ver o inventário completo da frota
    @GetMapping
    public ResponseEntity<List<Viatura>> listar() {
        return ResponseEntity.ok(adminService.listarTodas());
    }

    /* * --- DOCUMENTAÇÃO DO VIATURA_ADMIN_CONTROLLER ---
     * 1. O QUE FAZ: Provê as ferramentas de gestão para o administrador do sistema.
     * 2. FLUXO OPERACIONAL: Permite completar o cadastro de viaturas detectadas automaticamente,
     * inserindo dados civis como placa, chassi e prefixo operacional.
     * 3. INTEGRAÇÃO: Utiliza o DTO de cadastro para validar os dados antes de persistir no banco,
     * garantindo que nenhuma viatura fique com informações incompletas na base de dados.
     */
}