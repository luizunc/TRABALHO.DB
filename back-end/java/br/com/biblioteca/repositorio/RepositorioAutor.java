package br.com.biblioteca.repositorio;

import br.com.biblioteca.entidade.Autor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RepositorioAutor extends JpaRepository<Autor, String> {
    @Query("SELECT a FROM Autor a WHERE a.nome = :nome")
    Optional<Autor> buscarPorNome(@Param("nome") String nome);
    
    @Query("SELECT a FROM Autor a WHERE LOWER(a.nome) LIKE LOWER(CONCAT('%', :nome, '%'))")
    List<Autor> buscarPorNomeContendo(@Param("nome") String nome);
}



