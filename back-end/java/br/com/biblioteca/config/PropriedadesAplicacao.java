package br.com.biblioteca.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "app")
public class PropriedadesAplicacao {
    private String segredoJwt = "biblioteca_secret_key_2024_muito_segura_para_producao_alterar";
    private long expiracaoJwtMs = 86400000;

    public String getSegredoJwt() {
        return segredoJwt;
    }

    public void setSegredoJwt(String segredoJwt) {
        this.segredoJwt = segredoJwt;
    }

    public long getExpiracaoJwtMs() {
        return expiracaoJwtMs;
    }

    public void setExpiracaoJwtMs(long expiracaoJwtMs) {
        this.expiracaoJwtMs = expiracaoJwtMs;
    }
}

