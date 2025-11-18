package br.com.biblioteca.repositorio;

import br.com.biblioteca.entidade.Emprestimo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface RepositorioEmprestimo extends JpaRepository<Emprestimo, String> {
    @Query("SELECT e FROM Emprestimo e WHERE e.status = :status")
    List<Emprestimo> buscarPorStatus(@Param("status") String status);
    
    @Query("SELECT e FROM Emprestimo e WHERE e.usuario.idUsuario = :idUsuario")
    List<Emprestimo> buscarPorIdUsuario(@Param("idUsuario") String idUsuario);
    
    @Query("SELECT e FROM Emprestimo e WHERE e.livro.idLivro = :idLivro")
    List<Emprestimo> buscarPorIdLivro(@Param("idLivro") String idLivro);
    
    @Query("SELECT e FROM Emprestimo e WHERE e.usuario.idUsuario = :idUsuario AND e.status = :status")
    List<Emprestimo> buscarPorIdUsuarioEStatus(@Param("idUsuario") String idUsuario, @Param("status") String status);
    
    @Query("SELECT e FROM Emprestimo e WHERE e.dataDevolucaoPrevista < :data AND e.status = 'Ativo'")
    List<Emprestimo> buscarEmprestimosAtrasados(@Param("data") LocalDate data);
}



