package com.concessionaria.model;

import javax.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "veiculos")
@Data
public class Veiculo {
    @Id
    private String veiculoId;
    
    private String modelo;
    private String marca;
    private Integer ano;
    private String cor;
    private String placa;
    private BigDecimal preco;
    private String status;
    private Integer quilometragem;
    
    @Column(name = "criado_em")
    private LocalDateTime criadoEm;
}

