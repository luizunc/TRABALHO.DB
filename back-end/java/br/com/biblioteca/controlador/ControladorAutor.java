package br.com.biblioteca.controlador;

import br.com.biblioteca.entidade.Autor;
import br.com.biblioteca.servico.ServicoAutor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/autores")
@CrossOrigin(origins = "*")
public class ControladorAutor {

    @Autowired
    private ServicoAutor servicoAutor;

    @GetMapping
    public ResponseEntity<List<Autor>> listarTodos() {
        return ResponseEntity.ok(servicoAutor.listarTodos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Autor> buscarPorId(@PathVariable String id) {
        Optional<Autor> autor = servicoAutor.buscarPorId(id);
        return autor.map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/buscar")
    public ResponseEntity<List<Autor>> buscarPorNome(@RequestParam String nome) {
        return ResponseEntity.ok(servicoAutor.buscarPorNome(nome));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'BIBLIOTECARIO')")
    public ResponseEntity<Autor> criar(@RequestBody Autor autor) {
        return ResponseEntity.ok(servicoAutor.criar(autor));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'BIBLIOTECARIO')")
    public ResponseEntity<Autor> atualizar(@PathVariable String id, @RequestBody Autor autor) {
        try {
            return ResponseEntity.ok(servicoAutor.atualizar(id, autor));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deletar(@PathVariable String id) {
        servicoAutor.deletar(id);
        return ResponseEntity.noContent().build();
    }
}



