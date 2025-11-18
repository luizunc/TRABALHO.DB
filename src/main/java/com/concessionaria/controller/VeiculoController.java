package com.concessionaria.controller;

import com.concessionaria.model.Veiculo;
import com.concessionaria.service.VeiculoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/veiculos")
@CrossOrigin(origins = "*")
public class VeiculoController {
    @Autowired
    private VeiculoService service;
    
    @GetMapping
    public List<Veiculo> listar() {
        return service.listar();
    }
    
    @GetMapping("/disponiveis")
    public List<Veiculo> listarDisponiveis() {
        return service.listarDisponiveis();
    }
    
    @GetMapping("/{id}")
    public Veiculo buscar(@PathVariable String id) {
        return service.buscar(id);
    }
    
    @PostMapping
    public Veiculo criar(@RequestBody Veiculo veiculo) {
        return service.criar(veiculo);
    }
    
    @PutMapping("/{id}")
    public Veiculo atualizar(@PathVariable String id, @RequestBody Veiculo veiculo) {
        return service.atualizar(id, veiculo);
    }
    
    @DeleteMapping("/{id}")
    public void deletar(@PathVariable String id) {
        service.deletar(id);
    }
}

