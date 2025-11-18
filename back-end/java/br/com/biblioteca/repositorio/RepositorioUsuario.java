package br.com.biblioteca.repositorio;

import br.com.biblioteca.entidade.GrupoUsuario;
import br.com.biblioteca.entidade.Usuario;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface RepositorioUsuario extends JpaRepository<Usuario, String> {
    @Query("SELECT u FROM Usuario u WHERE u.email = :email")
    Optional<Usuario> buscarPorEmail(@Param("email") String email);
    
    @Query("SELECT u FROM Usuario u WHERE u.matricula = :matricula")
    Optional<Usuario> buscarPorMatricula(@Param("matricula") String matricula);
    
    @Query("SELECT COUNT(u) > 0 FROM Usuario u WHERE u.email = :email")
    boolean existePorEmail(@Param("email") String email);
    
    @Query("SELECT COUNT(u) > 0 FROM Usuario u WHERE u.matricula = :matricula")
    boolean existePorMatricula(@Param("matricula") String matricula);
    
    @Query("SELECT DISTINCT u FROM Usuario u LEFT JOIN FETCH u.grupos WHERE u.email = :email")
    Optional<Usuario> buscarPorEmailComGrupos(@Param("email") String email);
    
    @Query("SELECT u FROM Usuario u WHERE u.email = :email")
    @EntityGraph(type = EntityGraph.EntityGraphType.FETCH, attributePaths = {"grupos"})
    Optional<Usuario> buscarPorEmailComGruposGrafico(@Param("email") String email);
    
    @Query("SELECT g FROM GrupoUsuario g " +
           "WHERE EXISTS (SELECT 1 FROM Usuario u JOIN u.grupos ug WHERE u.idUsuario = :idUsuario AND ug.idGrupo = g.idGrupo)")
    List<GrupoUsuario> buscarGruposPorIdUsuario(@Param("idUsuario") String idUsuario);
}



