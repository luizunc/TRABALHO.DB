package br.com.biblioteca.servico;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ServicoGeradorId {

    @Autowired
    private EntityManager gerenciadorEntidade;

    @Transactional
    public String gerarIdAutor() {
        Query consulta = gerenciadorEntidade.createNativeQuery("SELECT gerar_id_autor()");
        return (String) consulta.getSingleResult();
    }

    @Transactional
    public String gerarIdUsuario() {
        Query consulta = gerenciadorEntidade.createNativeQuery("SELECT gerar_id_usuario()");
        return (String) consulta.getSingleResult();
    }

    @Transactional
    public String gerarIdLivro() {
        Query consulta = gerenciadorEntidade.createNativeQuery("SELECT gerar_id_livro()");
        return (String) consulta.getSingleResult();
    }

    @Transactional
    public String gerarIdEmprestimo() {
        Query consulta = gerenciadorEntidade.createNativeQuery("SELECT gerar_id_emprestimo()");
        return (String) consulta.getSingleResult();
    }
}



