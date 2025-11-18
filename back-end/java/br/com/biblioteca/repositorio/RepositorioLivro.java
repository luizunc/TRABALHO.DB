package br.com.biblioteca.repositorio;

import br.com.biblioteca.entidade.Livro;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RepositorioLivro extends JpaRepository<Livro, String> {
    @Query("SELECT l FROM Livro l WHERE l.status = :status")
    List<Livro> buscarPorStatus(@Param("status") String status);
    
    @Query("SELECT l FROM Livro l WHERE LOWER(l.titulo) LIKE LOWER(CONCAT('%', :titulo, '%'))")
    List<Livro> buscarPorTituloContendo(@Param("titulo") String titulo);
    
    @Query("SELECT l FROM Livro l WHERE l.autor.idAutor = :idAutor")
    List<Livro> buscarPorIdAutor(@Param("idAutor") String idAutor);
    
    @Query("SELECT l FROM Livro l WHERE l.autor.nome LIKE CONCAT('%', :nomeAutor, '%')")
    List<Livro> buscarPorNomeAutor(@Param("nomeAutor") String nomeAutor);
}



