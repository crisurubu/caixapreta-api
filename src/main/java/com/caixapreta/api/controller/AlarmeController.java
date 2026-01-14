package com.caixapreta.api.controller;

import com.caixapreta.api.model.Alarme;
import com.caixapreta.api.repository.AlarmeRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/alarmes")
@CrossOrigin(origins = "http://localhost:5173")
public class AlarmeController {

    private final AlarmeRepository alarmeRepository;

    public AlarmeController(AlarmeRepository alarmeRepository) {
        this.alarmeRepository = alarmeRepository;
    }

    // CORREÇÃO: O @PathVariable agora é String
    @GetMapping("/viatura/{id}")
    public ResponseEntity<List<Alarme>> listarPorViatura(@PathVariable Long id) {
        return ResponseEntity.ok(alarmeRepository.findByViaturaIdOrderByDataHoraDesc(id));
    }

    @GetMapping("/detalhe/{uuid}")
    public ResponseEntity<Alarme> buscarPorUuid(@PathVariable String uuid) {
        return alarmeRepository.findByUuid(uuid)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}