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
import java.util.concurrent.TimeUnit;

@Service
public class ServicoLivro {

    @Autowired
    private RepositorioLivro repositorioLivro;

    @Autowired
    private RepositorioAutor repositorioAutor;

    @Autowired
    private ServicoGeradorId servicoGeradorId;

    @Autowired
    private RedisTemplate<String, Object> templateRedis;

    @Cacheable(value = "livros", key = "#id")
    public Optional<Livro> buscarPorId(String id) {
        return repositorioLivro.findById(id);
    }

    @Cacheable(value = "livros")
    public List<Livro> listarTodos() {
        return repositorioLivro.findAll();
    }

    public List<Livro> buscarPorStatus(String status) {
        String chaveCache = "livros:status:" + status;
        @SuppressWarnings("unchecked")
        List<Livro> emCache = (List<Livro>) templateRedis.opsForValue().get(chaveCache);
        if (emCache != null) {
            return emCache;
        }
        List<Livro> livros = repositorioLivro.buscarPorStatus(status);
        templateRedis.opsForValue().set(chaveCache, livros, 10, TimeUnit.MINUTES);
        return livros;
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
        templateRedis.delete("livros:status:*");
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
        
        templateRedis.delete("livros:status:*");
        return repositorioLivro.save(livro);
    }

    @CacheEvict(value = "livros", allEntries = true)
    @Transactional
    public void deletar(String id) {
        repositorioLivro.deleteById(id);
        templateRedis.delete("livros:status:*");
    }
}



