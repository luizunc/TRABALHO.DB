package br.com.biblioteca.entidade;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "usuario")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(exclude = {"grupos", "emprestimos"})
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "senha", "emprestimos", "grupos"})
public class Usuario {
    
    @Id
    @Column(name = "id_usuario", length = 20)
    private String idUsuario;
    
    @Column(name = "nome", nullable = false, length = 255)
    private String nome;
    
    @Column(name = "email", nullable = false, unique = true, length = 255)
    private String email;
    
    @Column(name = "senha", nullable = false, length = 255)
    private String senha;
    
    @Column(name = "matricula", nullable = false, unique = true, length = 50)
    private String matricula;
    
    @Column(name = "ativo", nullable = false)
    private Boolean ativo = true;
    
    @Column(name = "data_cadastro")
    private LocalDateTime dataCadastro;
    
    @Column(name = "ultimo_acesso")
    private LocalDateTime ultimoAcesso;
    
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "usuario_grupo",
        joinColumns = @JoinColumn(name = "id_usuario"),
        inverseJoinColumns = @JoinColumn(name = "id_grupo")
    )
    private Set<GrupoUsuario> grupos = new HashSet<>();
    
    @OneToMany(mappedBy = "usuario", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<Emprestimo> emprestimos = new HashSet<>();
    
    @PrePersist
    protected void onCreate() {
        dataCadastro = LocalDateTime.now();
        if (ativo == null) {
            ativo = true;
        }
    }
}



