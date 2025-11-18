package com.concessionaria.model;

import javax.persistence.*;
import lombok.Data;

@Entity
@Table(name = "usuarios")
@Data
public class Usuario {
    @Id
    private String usuarioId;
    
    private String nome;
    private String email;
    private String senha;
    private Boolean ativo;
    
    @ManyToOne
    @JoinColumn(name = "grupo_id")
    private GrupoUsuario grupo;
}

