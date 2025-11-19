package br.com.biblioteca.entidade;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;
import java.util.Set;

@Entity
@Table(name = "livro")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(exclude = {"autor", "emprestimos"})
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "emprestimos"})
public class Livro {
    
    @Id
    @Column(name = "id_livro", length = 20)
    private String idLivro;
    
    @Column(name = "titulo", nullable = false, length = 255)
    private String titulo;
    
    @Column(name = "ano", nullable = false)
    private Integer ano;
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_autor", nullable = false)
    private Autor autor;
    
    @Column(name = "status", nullable = false, length = 50)
    private String status = "Disponivel";
    
    @Column(name = "data_cadastro")
    private LocalDateTime dataCadastro;
    
    @OneToMany(mappedBy = "livro", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<Emprestimo> emprestimos;
    
    @PrePersist
    protected void onCreate() {
        dataCadastro = LocalDateTime.now();
        if (status == null) {
            status = "Disponivel";
        }
    }
}



