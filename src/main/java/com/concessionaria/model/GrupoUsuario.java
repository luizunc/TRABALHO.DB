package com.concessionaria.model;

import javax.persistence.*;
import lombok.Data;

@Entity
@Table(name = "grupos_usuarios")
@Data
public class GrupoUsuario {
    @Id
    private String grupoId;
    
    private String nomeGrupo;
    private String descricao;
    private Integer nivelAcesso;
}

