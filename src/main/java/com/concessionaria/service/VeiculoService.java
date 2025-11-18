package com.concessionaria.service;

import com.concessionaria.model.Veiculo;
import com.concessionaria.repository.VeiculoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class VeiculoService {
    @Autowired
    private VeiculoRepository repository;
    
    public List<Veiculo> listar() {
        return repository.findAll();
    }
    
    public List<Veiculo> listarDisponiveis() {
        return repository.findDisponiveis();
    }
    
    public Veiculo buscar(String id) {
        return repository.findById(id).orElseThrow();
    }
    
    public Veiculo criar(Veiculo veiculo) {
        return repository.save(veiculo);
    }
    
    public Veiculo atualizar(String id, Veiculo veiculo) {
        veiculo.setVeiculoId(id);
        return repository.save(veiculo);
    }
    
    public void deletar(String id) {
        repository.deleteById(id);
    }
}

