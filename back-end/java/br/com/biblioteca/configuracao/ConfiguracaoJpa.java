package br.com.biblioteca.configuracao;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@EnableJpaRepositories(basePackages = "br.com.biblioteca.repositorio")
@EntityScan(basePackages = "br.com.biblioteca.entidade")
public class ConfiguracaoJpa {
}



