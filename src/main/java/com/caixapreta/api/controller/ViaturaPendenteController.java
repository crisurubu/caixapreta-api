package com.caixapreta.api.controller;

import com.caixapreta.api.model.ViaturaPendente;
import com.caixapreta.api.repository.ViaturaPendenteRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/viaturas/pendentes")
@CrossOrigin("*") // Para o seu React conseguir acessar sem erro de CORS
public class ViaturaPendenteController {

    private final ViaturaPendenteRepository repository;

    public ViaturaPendenteController(ViaturaPendenteRepository repository) {
        this.repository = repository;
    }

    @GetMapping
    public List<ViaturaPendente> listarTodas() {
        return repository.findAll();
    }

    @DeleteMapping("/{id}")
    public void excluirPendente(@PathVariable Long id) {
        repository.deleteById(id);
    }
}