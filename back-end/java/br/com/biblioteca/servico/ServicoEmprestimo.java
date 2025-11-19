package br.com.biblioteca.servico;

import br.com.biblioteca.entidade.Emprestimo;
import br.com.biblioteca.entidade.Livro;
import br.com.biblioteca.entidade.Usuario;
import br.com.biblioteca.repositorio.RepositorioEmprestimo;
import br.com.biblioteca.repositorio.RepositorioLivro;
import br.com.biblioteca.repositorio.RepositorioUsuario;
import jakarta.persistence.EntityManager;
import jakarta.persistence.ParameterMode;
import jakarta.persistence.StoredProcedureQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class ServicoEmprestimo {

    @Autowired
    private RepositorioEmprestimo repositorioEmprestimo;

    @Autowired
    private RepositorioUsuario repositorioUsuario;

    @Autowired
    private RepositorioLivro repositorioLivro;

    @Autowired
    private ServicoGeradorId servicoGeradorId;

    @Autowired
    private EntityManager gerenciadorEntidade;

    // Não cachear Optional - pode causar problemas de deserialização
    public Optional<Emprestimo> buscarPorId(String id) {
        return repositorioEmprestimo.findById(id);
    }

    // Não usar cache - sempre buscar dados atualizados do banco
    public List<Emprestimo> listarTodos() {
        List<Emprestimo> emprestimos = repositorioEmprestimo.findAll();
        return emprestimos != null ? emprestimos : java.util.Collections.emptyList();
    }

    // Não usar cache - sempre buscar dados atualizados do banco
    public List<Emprestimo> buscarPorStatus(String status) {
        List<Emprestimo> emprestimos = repositorioEmprestimo.buscarPorStatus(status);
        return emprestimos != null ? emprestimos : java.util.Collections.emptyList();
    }

    public List<Emprestimo> buscarPorUsuario(String idUsuario) {
        return repositorioEmprestimo.buscarPorIdUsuario(idUsuario);
    }

    public List<Emprestimo> buscarAtrasados() {
        return repositorioEmprestimo.buscarEmprestimosAtrasados(LocalDate.now());
    }

    @CacheEvict(value = {"emprestimos", "livros"}, allEntries = true)
    @Transactional
    public Emprestimo realizarEmprestimo(String idUsuario, String idLivro, int diasEmprestimo) {
        Usuario usuario = repositorioUsuario.findById(idUsuario)
            .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));
        
        if (!usuario.getAtivo()) {
            throw new RuntimeException("Usuário inativo");
        }

        Livro livro = repositorioLivro.findById(idLivro)
            .orElseThrow(() -> new RuntimeException("Livro não encontrado"));

        if (!"Disponivel".equals(livro.getStatus())) {
            throw new RuntimeException("Livro não está disponível. Status: " + livro.getStatus());
        }

        StoredProcedureQuery consulta = gerenciadorEntidade.createStoredProcedureQuery("sp_realizar_emprestimo");
        consulta.registerStoredProcedureParameter(1, String.class, ParameterMode.IN);
        consulta.registerStoredProcedureParameter(2, String.class, ParameterMode.IN);
        consulta.registerStoredProcedureParameter(3, Integer.class, ParameterMode.IN);
        consulta.registerStoredProcedureParameter(4, String.class, ParameterMode.OUT);
        consulta.registerStoredProcedureParameter(5, String.class, ParameterMode.OUT);
        
        consulta.setParameter(1, idUsuario);
        consulta.setParameter(2, idLivro);
        consulta.setParameter(3, diasEmprestimo);
        
        consulta.execute();
        
        String resultado = (String) consulta.getOutputParameterValue(4);
        String idEmprestimo = (String) consulta.getOutputParameterValue(5);
        
        if (!resultado.startsWith("SUCESSO")) {
            throw new RuntimeException(resultado);
        }

        return repositorioEmprestimo.findById(idEmprestimo)
            .orElseThrow(() -> new RuntimeException("Erro ao recuperar empréstimo criado"));
    }

    @CacheEvict(value = {"emprestimos", "livros"}, allEntries = true)
    @Transactional
    public Emprestimo devolverEmprestimo(String idEmprestimo) {
        Emprestimo emprestimo = repositorioEmprestimo.findById(idEmprestimo)
            .orElseThrow(() -> new RuntimeException("Empréstimo não encontrado"));

        if ("Devolvido".equals(emprestimo.getStatus())) {
            throw new RuntimeException("Empréstimo já foi devolvido");
        }

        StoredProcedureQuery consulta = gerenciadorEntidade.createStoredProcedureQuery("sp_devolver_emprestimo");
        consulta.registerStoredProcedureParameter(1, String.class, ParameterMode.IN);
        consulta.registerStoredProcedureParameter(2, String.class, ParameterMode.OUT);
        consulta.registerStoredProcedureParameter(3, BigDecimal.class, ParameterMode.OUT);
        
        consulta.setParameter(1, idEmprestimo);
        consulta.execute();
        
        String resultado = (String) consulta.getOutputParameterValue(2);
        BigDecimal multa = (BigDecimal) consulta.getOutputParameterValue(3);
        
        if (!resultado.startsWith("SUCESSO")) {
            throw new RuntimeException(resultado);
        }
        return repositorioEmprestimo.findById(idEmprestimo)
            .orElseThrow(() -> new RuntimeException("Erro ao recuperar empréstimo atualizado"));
    }
}



