package com.concessionaria.service;

import com.concessionaria.model.Usuario;
import com.concessionaria.repository.UsuarioRepository;
import com.concessionaria.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
public class AuthService {
    @Autowired
    private UsuarioRepository usuarioRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Autowired
    private JwtUtil jwtUtil;
    
    public Map<String, Object> login(String email, String senha) {
        Optional<Usuario> usuarioOpt = usuarioRepository.findByEmail(email);
        
        if (usuarioOpt.isEmpty()) {
            throw new RuntimeException("Email não encontrado");
        }
        
        Usuario usuario = usuarioOpt.get();
        
        if (!passwordEncoder.matches(senha, usuario.getSenha())) {
            throw new RuntimeException("Senha incorreta");
        }
        
        if (usuario.getAtivo() == null || !usuario.getAtivo()) {
            throw new RuntimeException("Usuário inativo");
        }
        
        if (usuario.getGrupo() == null) {
            throw new RuntimeException("Usuário sem grupo associado");
        }
        
        String token = jwtUtil.generateToken(usuario.getEmail(), usuario.getGrupo().getGrupoId());
        
        Map<String, Object> response = new HashMap<>();
        response.put("token", token);
        response.put("usuario", usuario);
        response.put("usuarioId", usuario.getUsuarioId());
        response.put("grupo", usuario.getGrupo());
        
        return response;
    }
}

