package com.concessionaria.repository;

import com.concessionaria.model.LogAtividade;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;

public interface LogAtividadeRepository extends MongoRepository<LogAtividade, String> {
    List<LogAtividade> findByUsuarioId(String usuarioId);
}

