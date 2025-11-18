package com.concessionaria.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;

@Document(collection = "logs_atividades")
@Data
public class LogAtividade {
    @Id
    private String id;
    private String usuarioId;
    private String acao;
    private LocalDateTime timestamp;
    private Object detalhes;
}

