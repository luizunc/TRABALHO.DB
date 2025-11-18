package br.com.biblioteca.seguranca;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class ManipuladorExcecoesGlobal {

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Map<String, Object>> tratarExcecaoAcessoNegado(AccessDeniedException ex) {
        Map<String, Object> resposta = new HashMap<>();
        resposta.put("error", "Acesso negado");
        resposta.put("message", "Você não tem permissão para realizar esta ação.");
        
        Authentication autenticacao = SecurityContextHolder.getContext().getAuthentication();
        if (autenticacao != null) {
            resposta.put("authenticated", true);
            resposta.put("username", autenticacao.getName());
            resposta.put("authorities", autenticacao.getAuthorities().toString());
        } else {
            resposta.put("authenticated", false);
        }
        
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(resposta);
    }
}



