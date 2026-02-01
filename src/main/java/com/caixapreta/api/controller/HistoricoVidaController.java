package com.caixapreta.api.controller;

import com.caixapreta.api.model.HistoricoVidaViatura;
import com.caixapreta.api.repository.HistoricoVidaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/historico-vida")
@CrossOrigin(origins = "*") // Libera para o Front
// Mudamos de hasRole para hasAnyAuthority para aceitar o "ADMIN" que vem do seu banco
@PreAuthorize("hasAnyAuthority('ADMIN', 'ROLE_ADMIN')")
public class HistoricoVidaController {

    @Autowired
    private HistoricoVidaRepository repository;

    // ✅ Adicionamos um método genérico para caso o Front chame a rota raiz
    @GetMapping
    public ResponseEntity<List<HistoricoVidaViatura>> getAll() {
        return ResponseEntity.ok(repository.findAll());
    }

    @GetMapping("/viatura/{id}")
    public ResponseEntity<List<HistoricoVidaViatura>> getHistoricoByViatura(@PathVariable Long id) {
        List<HistoricoVidaViatura> historico = repository.findByViaturaId(id);
        return ResponseEntity.ok(historico);
    }
}