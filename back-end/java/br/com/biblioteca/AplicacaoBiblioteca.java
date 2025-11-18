package br.com.biblioteca;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class AplicacaoBiblioteca {
    public static void main(String[] args) {
        SpringApplication.run(AplicacaoBiblioteca.class, args);
    }
}



