package br.com.biblioteca.controlador;

import br.com.biblioteca.entidade.Emprestimo;
import br.com.biblioteca.servico.ServicoEmprestimo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/emprestimos")
@CrossOrigin(origins = "*")
public class ControladorEmprestimo {

    @Autowired
    private ServicoEmprestimo servicoEmprestimo;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'BIBLIOTECARIO')")
    public ResponseEntity<List<Emprestimo>> listarTodos() {
        return ResponseEntity.ok(servicoEmprestimo.listarTodos());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'BIBLIOTECARIO')")
    public ResponseEntity<Emprestimo> buscarPorId(@PathVariable String id) {
        Optional<Emprestimo> emprestimo = servicoEmprestimo.buscarPorId(id);
        return emprestimo.map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/status/{status}")
    @PreAuthorize("hasAnyRole('ADMIN', 'BIBLIOTECARIO')")
    public ResponseEntity<List<Emprestimo>> buscarPorStatus(@PathVariable String status) {
        return ResponseEntity.ok(servicoEmprestimo.buscarPorStatus(status));
    }

    @GetMapping("/usuario/{idUsuario}")
    @PreAuthorize("hasAnyRole('ADMIN', 'BIBLIOTECARIO', 'USER')")
    public ResponseEntity<List<Emprestimo>> buscarPorUsuario(@PathVariable String idUsuario) {
        return ResponseEntity.ok(servicoEmprestimo.buscarPorUsuario(idUsuario));
    }

    @GetMapping("/atrasados")
    @PreAuthorize("hasAnyRole('ADMIN', 'BIBLIOTECARIO')")
    public ResponseEntity<List<Emprestimo>> buscarAtrasados() {
        return ResponseEntity.ok(servicoEmprestimo.buscarAtrasados());
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'BIBLIOTECARIO')")
    public ResponseEntity<?> realizarEmprestimo(
            @RequestParam String idUsuario,
            @RequestParam String idLivro,
            @RequestParam(defaultValue = "14") int diasEmprestimo) {
        try {
            return ResponseEntity.ok(servicoEmprestimo.realizarEmprestimo(idUsuario, idLivro, diasEmprestimo));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body("Erro: " + e.getMessage());
        }
    }

    @PutMapping("/{id}/devolver")
    @PreAuthorize("hasAnyRole('ADMIN', 'BIBLIOTECARIO')")
    public ResponseEntity<?> devolverEmprestimo(@PathVariable String id) {
        try {
            return ResponseEntity.ok(servicoEmprestimo.devolverEmprestimo(id));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body("Erro: " + e.getMessage());
        }
    }
}



