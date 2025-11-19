package br.com.biblioteca.entidade;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "emprestimo")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(exclude = {"usuario", "livro"})
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Emprestimo {
    
    @Id
    @Column(name = "id_emprestimo", length = 20)
    private String idEmprestimo;
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_usuario", nullable = false)
    private Usuario usuario;
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_livro", nullable = false)
    private Livro livro;
    
    @Column(name = "data_emprestimo", nullable = false)
    private LocalDate dataEmprestimo;
    
    @Column(name = "data_devolucao_prevista", nullable = false)
    private LocalDate dataDevolucaoPrevista;
    
    @Column(name = "data_devolucao_real")
    private LocalDate dataDevolucaoReal;
    
    @Column(name = "status", nullable = false, length = 50)
    private String status = "Ativo";
    
    @Column(name = "dias_atraso")
    private Integer diasAtraso = 0;
    
    @Column(name = "multa", precision = 10, scale = 2)
    private BigDecimal multa = BigDecimal.ZERO;
    
    @Column(name = "data_criacao")
    private LocalDateTime dataCriacao;
    
    @PrePersist
    protected void onCreate() {
        dataCriacao = LocalDateTime.now();
        if (status == null) {
            status = "Ativo";
        }
        if (diasAtraso == null) {
            diasAtraso = 0;
        }
        if (multa == null) {
            multa = BigDecimal.ZERO;
        }
    }
}



