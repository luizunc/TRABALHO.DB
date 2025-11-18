package br.com.biblioteca.seguranca;

import br.com.biblioteca.config.PropriedadesAplicacao;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.stream.Collectors;

@Component
public class ProvedorTokenJwt {

    @Autowired
    private PropriedadesAplicacao propriedadesApp;

    private SecretKey obterChaveAssinatura() {
        return Keys.hmacShaKeyFor(propriedadesApp.getSegredoJwt().getBytes(StandardCharsets.UTF_8));
    }

    public String gerarToken(Authentication autenticacao) {
        String nomeUsuario = autenticacao.getName();
        String roles = autenticacao.getAuthorities().stream()
            .map(GrantedAuthority::getAuthority)
            .collect(Collectors.joining(","));

        System.out.println("ProvedorTokenJwt - Gerando token para usuário: " + nomeUsuario + ", Roles: " + roles);

        Date agora = new Date();
        Date dataExpiracao = new Date(agora.getTime() + propriedadesApp.getExpiracaoJwtMs());

        return Jwts.builder()
            .subject(nomeUsuario)
            .claim("roles", roles)
            .issuedAt(agora)
            .expiration(dataExpiracao)
            .signWith(obterChaveAssinatura())
            .compact();
    }

    public String obterUsuarioDoToken(String token) {
        Claims claims = Jwts.parser()
            .verifyWith(obterChaveAssinatura())
            .build()
            .parseSignedClaims(token)
            .getPayload();
        return claims.getSubject();
    }

    public boolean validarToken(String token) {
        try {
            Jwts.parser()
                .verifyWith(obterChaveAssinatura())
                .build()
                .parseSignedClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    public Claims obterClaimsDoToken(String token) {
        return Jwts.parser()
            .verifyWith(obterChaveAssinatura())
            .build()
            .parseSignedClaims(token)
            .getPayload();
    }
}

