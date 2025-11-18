package com.concessionaria.model;

import javax.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "clientes")
@Data
public class Cliente {
    @Id
    private String clienteId;
    
    private String nome;
    private String cpf;
    private String telefone;
    private String email;
    private String endereco;
    
    @Column(name = "criado_em")
    private LocalDateTime criadoEm;
}

