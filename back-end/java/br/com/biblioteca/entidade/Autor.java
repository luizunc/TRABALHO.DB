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
@Table(name = "autor")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(exclude = {"livros"})
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "livros"})
public class Autor {
    
    @Id
    @Column(name = "id_autor", length = 20)
    private String idAutor;
    
    @Column(name = "nome", nullable = false, unique = true, length = 255)
    private String nome;
    
    @Column(name = "data_cadastro")
    private LocalDateTime dataCadastro;
    
    @OneToMany(mappedBy = "autor", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<Livro> livros;
    
    @PrePersist
    protected void onCreate() {
        dataCadastro = LocalDateTime.now();
    }
}



