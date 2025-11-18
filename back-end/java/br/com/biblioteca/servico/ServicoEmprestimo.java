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
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

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

    @Autowired
    private RedisTemplate<String, Object> templateRedis;

    @Cacheable(value = "emprestimos", key = "#id")
    public Optional<Emprestimo> buscarPorId(String id) {
        return repositorioEmprestimo.findById(id);
    }

    @Cacheable(value = "emprestimos")
    public List<Emprestimo> listarTodos() {
        List<Emprestimo> emprestimos = repositorioEmprestimo.findAll();
        return emprestimos != null ? emprestimos : java.util.Collections.emptyList();
    }

    public List<Emprestimo> buscarPorStatus(String status) {
        String chaveCache = "emprestimos:status:" + status;
        @SuppressWarnings("unchecked")
        List<Emprestimo> emCache = (List<Emprestimo>) templateRedis.opsForValue().get(chaveCache);
        if (emCache != null && !emCache.isEmpty()) {
            List<Emprestimo> validos = emCache.stream()
                .filter(emp -> emp != null && emp.getIdEmprestimo() != null)
                .collect(java.util.stream.Collectors.toList());
            if (!validos.isEmpty()) {
                return validos;
            }
        }
        List<Emprestimo> emprestimos = repositorioEmprestimo.buscarPorStatus(status);
        if (emprestimos == null) {
            emprestimos = java.util.Collections.emptyList();
        }
        if (emprestimos.isEmpty()) {
            templateRedis.delete(chaveCache);
        } else {
            templateRedis.opsForValue().set(chaveCache, emprestimos, 5, TimeUnit.MINUTES);
        }
        return emprestimos;
    }

    public List<Emprestimo> buscarPorUsuario(String idUsuario) {
        return repositorioEmprestimo.buscarPorIdUsuario(idUsuario);
    }

    public List<Emprestimo> buscarAtrasados() {
        return repositorioEmprestimo.buscarEmprestimosAtrasados(LocalDate.now());
    }

    @CacheEvict(value = "emprestimos", allEntries = true)
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

        templateRedis.delete("emprestimos:status:*");
        return repositorioEmprestimo.findById(idEmprestimo)
            .orElseThrow(() -> new RuntimeException("Erro ao recuperar empréstimo criado"));
    }

    @CacheEvict(value = "emprestimos", allEntries = true)
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

        templateRedis.delete("emprestimos:status:*");
        return repositorioEmprestimo.findById(idEmprestimo)
            .orElseThrow(() -> new RuntimeException("Erro ao recuperar empréstimo atualizado"));
    }
}



