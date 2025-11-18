package com.concessionaria.service;

import com.concessionaria.model.LogAtividade;
import com.concessionaria.repository.LogAtividadeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class LogService {
    @Autowired
    private LogAtividadeRepository repository;
    
    public void registrar(String usuarioId, String acao, Map<String, Object> detalhes) {
        LogAtividade log = new LogAtividade();
        log.setUsuarioId(usuarioId);
        log.setAcao(acao);
        log.setTimestamp(LocalDateTime.now());
        log.setDetalhes(detalhes != null ? detalhes : new HashMap<>());
        repository.save(log);
    }
    
    public List<LogAtividade> listarPorUsuario(String usuarioId) {
        return repository.findByUsuarioId(usuarioId);
    }
}

