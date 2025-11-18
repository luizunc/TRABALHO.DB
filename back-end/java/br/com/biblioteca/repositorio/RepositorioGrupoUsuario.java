package br.com.biblioteca.repositorio;

import br.com.biblioteca.entidade.GrupoUsuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RepositorioGrupoUsuario extends JpaRepository<GrupoUsuario, String> {
    @Query("SELECT g FROM GrupoUsuario g WHERE g.nome = :nome")
    Optional<GrupoUsuario> buscarPorNome(@Param("nome") String nome);
}



