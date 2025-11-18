package br.com.biblioteca.seguranca;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class FiltroAutenticacaoJwt extends OncePerRequestFilter {

    @Autowired
    private ProvedorTokenJwt provedorToken;

    @Override
    protected void doFilterInternal(HttpServletRequest requisicao, HttpServletResponse resposta, FilterChain cadeiaFiltros)
            throws ServletException, IOException {
        try {
            String jwt = obterJwtDaRequisicao(requisicao);

            if (StringUtils.hasText(jwt)) {
                try {
                    if (provedorToken.validarToken(jwt)) {
                        String nomeUsuario = provedorToken.obterUsuarioDoToken(jwt);
                        
                        io.jsonwebtoken.Claims claims = provedorToken.obterClaimsDoToken(jwt);
                        String rolesStr = claims.get("roles", String.class);
                        List<SimpleGrantedAuthority> autoridades = rolesStr != null 
                            ? Arrays.stream(rolesStr.split(","))
                                .map(String::trim)
                                .map(role -> {
                                    String roleFinal = role.startsWith("ROLE_") ? role : "ROLE_" + role;
                                    logger.info("Processando role: " + role + " -> " + roleFinal);
                                    return new SimpleGrantedAuthority(roleFinal);
                                })
                                .collect(Collectors.toList())
                            : List.of();

                        logger.info("JWT Authentication - Username: " + nomeUsuario + ", Roles extraídas: " + rolesStr + ", Authorities: " + autoridades);

                        UsernamePasswordAuthenticationToken autenticacao = 
                            new UsernamePasswordAuthenticationToken(nomeUsuario, null, autoridades);
                        autenticacao.setDetails(new WebAuthenticationDetailsSource().buildDetails(requisicao));

                        SecurityContextHolder.getContext().setAuthentication(autenticacao);
                    }
                } catch (Exception ex) {
                    logger.error("Invalid JWT token: " + ex.getMessage(), ex);
                }
            } else {
                logger.debug("No JWT token found in request");
            }
            
            if (SecurityContextHolder.getContext().getAuthentication() != null) {
                logger.info("Security Context - Username: " + 
                    SecurityContextHolder.getContext().getAuthentication().getName() + 
                    ", Authorities: " + 
                    SecurityContextHolder.getContext().getAuthentication().getAuthorities());
            }
        } catch (Exception ex) {
            logger.error("Could not set user authentication in security context", ex);
        }

        cadeiaFiltros.doFilter(requisicao, resposta);
    }

    private String obterJwtDaRequisicao(HttpServletRequest requisicao) {
        String bearerToken = requisicao.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}



