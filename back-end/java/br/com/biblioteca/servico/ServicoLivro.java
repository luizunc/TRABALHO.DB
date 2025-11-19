package br.com.biblioteca.servico;

import br.com.biblioteca.entidade.Autor;
import br.com.biblioteca.entidade.Livro;
import br.com.biblioteca.repositorio.RepositorioAutor;
import br.com.biblioteca.repositorio.RepositorioLivro;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class ServicoLivro {

    @Autowired
    private RepositorioLivro repositorioLivro;

    @Autowired
    private RepositorioAutor repositorioAutor;

    @Autowired
    private ServicoGeradorId servicoGeradorId;

    @Autowired(required = false)
    private RedisTemplate<String, Object> templateRedis;

    // Não cachear Optional - pode causar problemas de deserialização
    public Optional<Livro> buscarPorId(String id) {
        return repositorioLivro.findById(id);
    }

    // Não usar cache - sempre buscar dados atualizados do banco
    public List<Livro> listarTodos() {
        return repositorioLivro.findAll();
    }

    // Não usar cache - sempre buscar dados atualizados do banco
    public List<Livro> buscarPorStatus(String status) {
        List<Livro> livros = repositorioLivro.buscarPorStatus(status);
        return livros != null ? livros : java.util.Collections.emptyList();
    }

    public List<Livro> buscarPorTitulo(String titulo) {
        return repositorioLivro.buscarPorTituloContendo(titulo);
    }

    @CacheEvict(value = "livros", allEntries = true)
    @Transactional
    public Livro criar(Livro livro) {
        if (livro.getIdLivro() == null) {
            livro.setIdLivro(servicoGeradorId.gerarIdLivro());
        }
        if (livro.getStatus() == null) {
            livro.setStatus("Disponivel");
        }
        if (templateRedis != null) {
            templateRedis.delete("livros:status:*");
        }
        return repositorioLivro.save(livro);
    }

    @CacheEvict(value = "livros", allEntries = true)
    @Transactional
    public Livro atualizar(String id, Livro livroAtualizado) {
        Livro livro = repositorioLivro.findById(id)
            .orElseThrow(() -> new RuntimeException("Livro não encontrado"));
        
        livro.setTitulo(livroAtualizado.getTitulo());
        livro.setAno(livroAtualizado.getAno());
        livro.setStatus(livroAtualizado.getStatus());
        if (livroAtualizado.getAutor() != null) {
            livro.setAutor(livroAtualizado.getAutor());
        }
        
        if (templateRedis != null) {
            templateRedis.delete("livros:status:*");
        }
        return repositorioLivro.save(livro);
    }

    @CacheEvict(value = "livros", allEntries = true)
    @Transactional
    public void deletar(String id) {
        repositorioLivro.deleteById(id);
        if (templateRedis != null) {
            templateRedis.delete("livros:status:*");
        }
    }
}



