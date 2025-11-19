package br.com.biblioteca.servico;

import br.com.biblioteca.entidade.Usuario;
import br.com.biblioteca.repositorio.RepositorioUsuario;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Service
public class ServicoUsuario {

    @Autowired
    private RepositorioUsuario repositorioUsuario;

    @Autowired
    private ServicoGeradorId servicoGeradorId;

    @Autowired(required = false)
    private RedisTemplate<String, Object> templateRedis;

    // Não cachear Optional - pode causar problemas de deserialização
    public Optional<Usuario> buscarPorId(String id) {
        return repositorioUsuario.findById(id);
    }

    @Cacheable(value = "usuarios")
    public List<Usuario> listarTodos() {
        return repositorioUsuario.findAll();
    }

    @CacheEvict(value = "usuarios", allEntries = true)
    @Transactional
    public Usuario criar(Usuario usuario) {
        if (usuario.getIdUsuario() == null) {
            usuario.setIdUsuario(servicoGeradorId.gerarIdUsuario());
        }
        if (templateRedis != null) {
            templateRedis.opsForValue().set("usuario:" + usuario.getIdUsuario(), usuario, 10, TimeUnit.MINUTES);
        }
        return repositorioUsuario.save(usuario);
    }

    @CacheEvict(value = "usuarios", allEntries = true)
    @Transactional
    public Usuario atualizar(String id, Usuario usuarioAtualizado) {
        Usuario usuario = repositorioUsuario.findById(id)
            .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));
        
        usuario.setNome(usuarioAtualizado.getNome());
        usuario.setEmail(usuarioAtualizado.getEmail());
        usuario.setMatricula(usuarioAtualizado.getMatricula());
        usuario.setAtivo(usuarioAtualizado.getAtivo());
        
        if (templateRedis != null) {
            templateRedis.delete("usuario:" + id);
        }
        return repositorioUsuario.save(usuario);
    }

    @CacheEvict(value = "usuarios", allEntries = true)
    @Transactional
    public void deletar(String id) {
        repositorioUsuario.deleteById(id);
        if (templateRedis != null) {
            templateRedis.delete("usuario:" + id);
        }
    }

    public Optional<Usuario> buscarPorEmail(String email) {
        return repositorioUsuario.buscarPorEmail(email);
    }
}



