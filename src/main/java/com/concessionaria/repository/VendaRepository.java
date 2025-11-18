package com.concessionaria.repository;

import com.concessionaria.model.Venda;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VendaRepository extends JpaRepository<Venda, String> {
}

