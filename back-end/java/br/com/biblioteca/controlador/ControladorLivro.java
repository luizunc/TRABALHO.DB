package br.com.biblioteca.controlador;

import br.com.biblioteca.entidade.Livro;
import br.com.biblioteca.servico.ServicoLivro;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/livros")
@CrossOrigin(origins = "*")
public class ControladorLivro {

    @Autowired
    private ServicoLivro servicoLivro;

    @GetMapping
    public ResponseEntity<List<Livro>> listarTodos() {
        return ResponseEntity.ok(servicoLivro.listarTodos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Livro> buscarPorId(@PathVariable String id) {
        Optional<Livro> livro = servicoLivro.buscarPorId(id);
        return livro.map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<Livro>> buscarPorStatus(@PathVariable String status) {
        return ResponseEntity.ok(servicoLivro.buscarPorStatus(status));
    }

    @GetMapping("/buscar")
    public ResponseEntity<List<Livro>> buscarPorTitulo(@RequestParam String titulo) {
        return ResponseEntity.ok(servicoLivro.buscarPorTitulo(titulo));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'BIBLIOTECARIO')")
    public ResponseEntity<Livro> criar(@RequestBody Livro livro) {
        return ResponseEntity.ok(servicoLivro.criar(livro));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'BIBLIOTECARIO')")
    public ResponseEntity<Livro> atualizar(@PathVariable String id, @RequestBody Livro livro) {
        try {
            return ResponseEntity.ok(servicoLivro.atualizar(id, livro));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deletar(@PathVariable String id) {
        servicoLivro.deletar(id);
        return ResponseEntity.noContent().build();
    }
}



