package br.com.biblioteca.controlador;

import br.com.biblioteca.dto.RequisicaoLogin;
import br.com.biblioteca.dto.RespostaLogin;
import br.com.biblioteca.servico.ServicoAutenticacao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class ControladorAutenticacao {

    @Autowired
    private ServicoAutenticacao servicoAutenticacao;

    @PostMapping("/login")
    public ResponseEntity<?> fazerLogin(@RequestBody RequisicaoLogin requisicao) {
        try {
            String token = servicoAutenticacao.fazerLogin(requisicao.getEmail(), requisicao.getSenha());
            return ResponseEntity.ok(new RespostaLogin(token, "Bearer", requisicao.getEmail()));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of("error", "Erro ao fazer login: " + e.getMessage()));
        }
    }

    @PostMapping("/registrar")
    public ResponseEntity<?> registrar(@RequestBody br.com.biblioteca.entidade.Usuario usuario, 
                                       @RequestParam String senha) {
        try {
            br.com.biblioteca.entidade.Usuario novoUsuario = servicoAutenticacao.registrar(
                usuario.getNome(), usuario.getEmail(), senha, usuario.getMatricula());
            return ResponseEntity.ok(novoUsuario);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Erro ao registrar: " + e.getMessage());
        }
    }
}



