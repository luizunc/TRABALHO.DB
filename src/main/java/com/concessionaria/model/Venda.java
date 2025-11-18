package com.concessionaria.model;

import javax.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "vendas")
@Data
public class Venda {
    @Id
    private String vendaId;
    
    @ManyToOne
    @JoinColumn(name = "cliente_id")
    private Cliente cliente;
    
    @ManyToOne
    @JoinColumn(name = "veiculo_id")
    private Veiculo veiculo;
    
    @ManyToOne
    @JoinColumn(name = "vendedor_id")
    private Usuario vendedor;
    
    private BigDecimal valorVenda;
    private LocalDate dataVenda;
    private String formaPagamento;
    
    @Column(name = "criado_em")
    private LocalDateTime criadoEm;
}

