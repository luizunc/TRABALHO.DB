package br.com.biblioteca.entidade;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;
import java.util.Set;

@Entity
@Table(name = "grupos_usuarios")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GrupoUsuario {
    
    @Id
    @Column(name = "id_grupo", length = 20)
    private String idGrupo;
    
    @Column(nullable = false, unique = true, length = 100)
    private String nome;
    
    @Column(columnDefinition = "TEXT")
    private String descricao;
    
    @Column(name = "nivel_acesso", nullable = false)
    private Integer nivelAcesso;
    
    @Column(name = "data_criacao")
    private LocalDateTime dataCriacao;
    
    @ManyToMany(mappedBy = "grupos")
    private Set<Usuario> usuarios;
    
    @PrePersist
    protected void onCreate() {
        dataCriacao = LocalDateTime.now();
    }
}



