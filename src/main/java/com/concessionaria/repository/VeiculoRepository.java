package com.concessionaria.repository;

import com.concessionaria.model.Veiculo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;

public interface VeiculoRepository extends JpaRepository<Veiculo, String> {
    List<Veiculo> findByStatus(String status);
    
    @Query("SELECT v FROM Veiculo v WHERE v.status = 'disponivel'")
    List<Veiculo> findDisponiveis();
}

