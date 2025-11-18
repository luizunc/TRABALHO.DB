package br.com.biblioteca.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class RespostaLogin {
    private String token;
    private String tipo = "Bearer";
    private String email;
}



