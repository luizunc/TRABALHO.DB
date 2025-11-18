package com.concessionaria.service;

import com.concessionaria.model.Cliente;
import com.concessionaria.repository.ClienteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class ClienteService {
    @Autowired
    private ClienteRepository repository;
    
    public List<Cliente> listar() {
        return repository.findAll();
    }
    
    public Cliente buscar(String id) {
        return repository.findById(id).orElseThrow();
    }
    
    public Cliente criar(Cliente cliente) {
        return repository.save(cliente);
    }
    
    public Cliente atualizar(String id, Cliente cliente) {
        cliente.setClienteId(id);
        return repository.save(cliente);
    }
    
    public void deletar(String id) {
        repository.deleteById(id);
    }
}

