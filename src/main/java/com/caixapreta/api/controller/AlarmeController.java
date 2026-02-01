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

    @GetMapping("/viatura/{id}")
    public ResponseEntity<List<Alarme>> listarPorViatura(@PathVariable Long id) {
        return ResponseEntity.ok(alarmeRepository.findByViaturaIdOrderByDataHoraDesc(id));
    }

    // ALTERADO: Agora retorna uma LISTA de alarmes para o mesmo UUID
    // Isso permite que o Frontend mostre todos os logs (impactos) do mesmo laudo
    @GetMapping("/dossie/{uuid}")
    public ResponseEntity<List<Alarme>> buscarDossieCompleto(@PathVariable String uuid) {
        // O Repository agora retorna uma List, não mais um Optional
        List<Alarme> logs = alarmeRepository.findAllByUuidOrderByDataHoraAsc(uuid);

        if (logs == null || logs.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(logs);
    }

    @DeleteMapping("/reset-pericial")
    public ResponseEntity<String> resetarSimulacao() {
        alarmeRepository.deleteAll();
        System.out.println(">>> SISTEMA REINICIADO: Aguardando nova simulação...");
        return ResponseEntity.ok("MEMÓRIA ZERADA");
    }
}