package br.com.biblioteca.servico;

import br.com.biblioteca.entidade.Autor;
import br.com.biblioteca.repositorio.RepositorioAutor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class ServicoAutor {

    @Autowired
    private RepositorioAutor repositorioAutor;

    @Autowired
    private ServicoGeradorId servicoGeradorId;

    @Autowired
    private RedisTemplate<String, Object> templateRedis;

    @Cacheable(value = "autores", key = "#id")
    public Optional<Autor> buscarPorId(String id) {
        return repositorioAutor.findById(id);
    }

    @Cacheable(value = "autores")
    public List<Autor> listarTodos() {
        return repositorioAutor.findAll();
    }

    public List<Autor> buscarPorNome(String nome) {
        return repositorioAutor.buscarPorNomeContendo(nome);
    }

    @CacheEvict(value = "autores", allEntries = true)
    @Transactional
    public Autor criar(Autor autor) {
        if (autor.getIdAutor() == null) {
            autor.setIdAutor(servicoGeradorId.gerarIdAutor());
        }
        return repositorioAutor.save(autor);
    }

    @CacheEvict(value = "autores", allEntries = true)
    @Transactional
    public Autor atualizar(String id, Autor autorAtualizado) {
        Autor autor = repositorioAutor.findById(id)
            .orElseThrow(() -> new RuntimeException("Autor não encontrado"));
        
        autor.setNome(autorAtualizado.getNome());
        return repositorioAutor.save(autor);
    }

    @CacheEvict(value = "autores", allEntries = true)
    @Transactional
    public void deletar(String id) {
        repositorioAutor.deleteById(id);
    }
}



