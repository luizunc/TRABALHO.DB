package br.com.biblioteca.servico;

import br.com.biblioteca.entidade.GrupoUsuario;
import br.com.biblioteca.entidade.Usuario;
import br.com.biblioteca.repositorio.RepositorioUsuario;
import br.com.biblioteca.seguranca.ProvedorTokenJwt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class ServicoAutenticacao {

    @Autowired
    private RepositorioUsuario repositorioUsuario;

    @Autowired
    private PasswordEncoder codificadorSenha;

    @Autowired
    private AuthenticationManager gerenciadorAutenticacao;

    @Autowired
    private ProvedorTokenJwt provedorToken;

    @Autowired
    private ServicoGeradorId servicoGeradorId;

    @Transactional(readOnly = true)
    public String fazerLogin(String email, String senha) {
        System.out.println("🔍 Buscando usuário: " + email);
        Usuario usuario = repositorioUsuario.buscarPorEmailComGruposGrafico(email)
            .or(() -> {
                System.out.println("⚠️ EntityGraph falhou, tentando JOIN FETCH...");
                return repositorioUsuario.buscarPorEmailComGrupos(email);
            })
            .orElseThrow(() -> new RuntimeException("Email ou senha incorretos"));
        
        System.out.println("✅ Usuário encontrado: " + usuario.getEmail() + ", ID: " + usuario.getIdUsuario());
        
        Set<GrupoUsuario> grupos = usuario.getGrupos();
        if (grupos != null) {
            int tamanho = grupos.size();
            System.out.println("📊 Grupos carregados: " + tamanho);
            
            if (tamanho == 0) {
                System.out.println("  ⚠️ Grupos não foram carregados automaticamente. Tentando carregar manualmente...");
                List<GrupoUsuario> gruposManuais = repositorioUsuario.buscarGruposPorIdUsuario(usuario.getIdUsuario());
                if (gruposManuais != null && !gruposManuais.isEmpty()) {
                    System.out.println("  ✅ Grupos carregados manualmente: " + gruposManuais.size());
                    Set<GrupoUsuario> gruposSet = new HashSet<>(gruposManuais);
                    usuario.setGrupos(gruposSet);
                    grupos = gruposSet;
                    tamanho = gruposSet.size();
                } else {
                    System.out.println("  ❌ ERRO: Não foi possível carregar grupos manualmente!");
                    System.out.println("  ⚠️ Verifique se há registros na tabela usuario_grupo para o usuário " + usuario.getIdUsuario());
                }
            }
            
            if (tamanho > 0) {
                grupos.forEach(grupo -> {
                    System.out.println("  ✓ Grupo: " + grupo.getNome() + ", Nível: " + grupo.getNivelAcesso() + ", ID: " + grupo.getIdGrupo());
                });
            }
        } else {
            System.out.println("  ❌ ERRO: Set de grupos é NULL!");
        }

        if (usuario.getAtivo() == null || !usuario.getAtivo()) {
            throw new RuntimeException("Usuário inativo");
        }

        boolean senhaValida = false;
        
        if (usuario.getSenha().startsWith("$2")) {
            senhaValida = codificadorSenha.matches(senha, usuario.getSenha());
        } else {
            try {
                java.security.MessageDigest md = java.security.MessageDigest.getInstance("SHA-256");
                byte[] hashBytes = md.digest(senha.getBytes());
                StringBuilder sb = new StringBuilder();
                for (byte b : hashBytes) {
                    sb.append(String.format("%02x", b));
                }
                String sha256Hash = sb.toString();
                senhaValida = sha256Hash.equalsIgnoreCase(usuario.getSenha());
            } catch (Exception e) {
            }
        }
        
        if (!senhaValida) {
            throw new RuntimeException("Email ou senha incorretos");
        }

        Set<SimpleGrantedAuthority> autoridades = new HashSet<>();
        if (usuario.getGrupos() != null && !usuario.getGrupos().isEmpty()) {
            autoridades = usuario.getGrupos().stream()
                .map(grupo -> {
                    String role = switch (grupo.getNivelAcesso()) {
                        case 3 -> "ROLE_ADMIN";
                        case 2 -> "ROLE_BIBLIOTECARIO";
                        default -> "ROLE_USER";
                    };
                    System.out.println("Grupo: " + grupo.getNome() + ", Nivel: " + grupo.getNivelAcesso() + ", Role: " + role);
                    return new SimpleGrantedAuthority(role);
                })
                .collect(Collectors.toSet());
        } else {
            System.out.println("Usuário " + email + " não possui grupos associados. Atribuindo ROLE_USER");
            autoridades.add(new SimpleGrantedAuthority("ROLE_USER"));
        }

        System.out.println("Roles atribuídas ao usuário " + email + ": " + autoridades);

        Authentication autenticacao = new UsernamePasswordAuthenticationToken(
            usuario.getEmail(), null, autoridades);

        return provedorToken.gerarToken(autenticacao);
    }

    @Transactional
    public Usuario registrar(String nome, String email, String senha, String matricula) {
        if (repositorioUsuario.existePorEmail(email)) {
            throw new RuntimeException("Email já cadastrado");
        }
        if (repositorioUsuario.existePorMatricula(matricula)) {
            throw new RuntimeException("Matrícula já cadastrada");
        }

        Usuario usuario = new Usuario();
        usuario.setIdUsuario(servicoGeradorId.gerarIdUsuario());
        usuario.setNome(nome);
        usuario.setEmail(email);
        usuario.setSenha(codificadorSenha.encode(senha));
        usuario.setMatricula(matricula);
        usuario.setAtivo(true);

        return repositorioUsuario.save(usuario);
    }
}



