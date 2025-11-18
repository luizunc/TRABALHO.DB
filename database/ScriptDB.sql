-- Criar banco de dados
CREATE DATABASE IF NOT EXISTS biblioteca_db;
USE biblioteca_db;

DROP TABLE IF EXISTS emprestimo;
DROP TABLE IF EXISTS livro;
DROP TABLE IF EXISTS usuario_grupo;
DROP TABLE IF EXISTS grupos_usuarios;
DROP TABLE IF EXISTS usuario;
DROP TABLE IF EXISTS autor;

-- Funções para geração de IDs customizados
DELIMITER //
CREATE FUNCTION gerar_id_autor() RETURNS VARCHAR(20)
READS SQL DATA
DETERMINISTIC
BEGIN
    DECLARE novo_id VARCHAR(20);
    DECLARE ultimo_num INT DEFAULT 0;
    
    SELECT COALESCE(MAX(CAST(SUBSTRING(id_autor, 4) AS UNSIGNED)), 0) INTO ultimo_num
    FROM autor
    WHERE id_autor LIKE 'AUT%';
    
    SET novo_id = CONCAT('AUT', LPAD(ultimo_num + 1, 6, '0'));
    RETURN novo_id;
END //
DELIMITER ;

DELIMITER //
CREATE FUNCTION gerar_id_usuario() RETURNS VARCHAR(20)
READS SQL DATA
DETERMINISTIC
BEGIN
    DECLARE novo_id VARCHAR(20);
    DECLARE ultimo_num INT DEFAULT 0;
    
    SELECT COALESCE(MAX(CAST(SUBSTRING(id_usuario, 4) AS UNSIGNED)), 0) INTO ultimo_num
    FROM usuario
    WHERE id_usuario LIKE 'USR%';
    
    SET novo_id = CONCAT('USR', LPAD(ultimo_num + 1, 6, '0'));
    RETURN novo_id;
END //
DELIMITER ;

DELIMITER //
CREATE FUNCTION gerar_id_livro() RETURNS VARCHAR(20)
READS SQL DATA
DETERMINISTIC
BEGIN
    DECLARE novo_id VARCHAR(20);
    DECLARE ultimo_num INT DEFAULT 0;
    
    SELECT COALESCE(MAX(CAST(SUBSTRING(id_livro, 4) AS UNSIGNED)), 0) INTO ultimo_num
    FROM livro
    WHERE id_livro LIKE 'LIV%';
    
    SET novo_id = CONCAT('LIV', LPAD(ultimo_num + 1, 6, '0'));
    RETURN novo_id;
END //
DELIMITER ;

DELIMITER //
CREATE FUNCTION gerar_id_emprestimo() RETURNS VARCHAR(20)
READS SQL DATA
DETERMINISTIC
BEGIN
    DECLARE novo_id VARCHAR(20);
    DECLARE ultimo_num INT DEFAULT 0;
    
    SELECT COALESCE(MAX(CAST(SUBSTRING(id_emprestimo, 4) AS UNSIGNED)), 0) INTO ultimo_num
    FROM emprestimo
    WHERE id_emprestimo LIKE 'EMP%';
    
    SET novo_id = CONCAT('EMP', LPAD(ultimo_num + 1, 6, '0'));
    RETURN novo_id;
END //
DELIMITER ;

-- Tabelas
CREATE TABLE grupos_usuarios (
    id_grupo VARCHAR(20) PRIMARY KEY,
    nome VARCHAR(100) NOT NULL UNIQUE,
    descricao TEXT,
    nivel_acesso INT NOT NULL DEFAULT 1,
    data_criacao TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_nivel_acesso (nivel_acesso)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE autor (
    id_autor VARCHAR(20) PRIMARY KEY,
    nome VARCHAR(255) NOT NULL,
    data_cadastro TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_nome (nome),
    INDEX idx_nome (nome)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE usuario (
    id_usuario VARCHAR(20) PRIMARY KEY,
    nome VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL,
    senha VARCHAR(255) NOT NULL,
    matricula VARCHAR(50) NOT NULL,
    ativo BOOLEAN DEFAULT TRUE,
    data_cadastro TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    ultimo_acesso TIMESTAMP NULL,
    UNIQUE KEY uk_email (email),
    UNIQUE KEY uk_matricula (matricula),
    INDEX idx_email (email),
    INDEX idx_matricula (matricula),
    INDEX idx_ativo (ativo)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE usuario_grupo (
    id_usuario VARCHAR(20) NOT NULL,
    id_grupo VARCHAR(20) NOT NULL,
    data_vinculo TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id_usuario, id_grupo),
    FOREIGN KEY (id_usuario) REFERENCES usuario(id_usuario) ON DELETE CASCADE,
    FOREIGN KEY (id_grupo) REFERENCES grupos_usuarios(id_grupo) ON DELETE CASCADE,
    INDEX idx_grupo (id_grupo)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE livro (
    id_livro VARCHAR(20) PRIMARY KEY,
    titulo VARCHAR(255) NOT NULL,
    ano INT NOT NULL,
    id_autor VARCHAR(20) NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'Disponivel',
    data_cadastro TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (id_autor) REFERENCES autor(id_autor) ON DELETE RESTRICT,
    INDEX idx_status (status),
    INDEX idx_titulo (titulo),
    INDEX idx_autor (id_autor),
    INDEX idx_ano (ano)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE emprestimo (
    id_emprestimo VARCHAR(20) PRIMARY KEY,
    id_usuario VARCHAR(20) NOT NULL,
    id_livro VARCHAR(20) NOT NULL,
    data_emprestimo DATE NOT NULL,
    data_devolucao_prevista DATE NOT NULL,
    data_devolucao_real DATE NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'Ativo',
    dias_atraso INT DEFAULT 0,
    multa DECIMAL(10,2) DEFAULT 0.00,
    data_criacao TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (id_usuario) REFERENCES usuario(id_usuario) ON DELETE RESTRICT,
    FOREIGN KEY (id_livro) REFERENCES livro(id_livro) ON DELETE RESTRICT,
    INDEX idx_status (status),
    INDEX idx_usuario (id_usuario),
    INDEX idx_livro (id_livro),
    INDEX idx_data_emprestimo (data_emprestimo),
    INDEX idx_data_devolucao_prevista (data_devolucao_prevista)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Triggers
DELIMITER //
CREATE TRIGGER trg_emprestimo_insert
AFTER INSERT ON emprestimo
FOR EACH ROW
BEGIN
    UPDATE livro 
    SET status = 'Emprestado' 
    WHERE id_livro = NEW.id_livro AND status = 'Disponivel';
END //
DELIMITER ;

DELIMITER //
CREATE TRIGGER trg_emprestimo_update_devolucao
AFTER UPDATE ON emprestimo
FOR EACH ROW
BEGIN
    IF NEW.status = 'Devolvido' AND OLD.status != 'Devolvido' THEN
        UPDATE livro SET status = 'Disponivel' WHERE id_livro = NEW.id_livro;
    END IF;
END //
DELIMITER ;

DELIMITER //
CREATE TRIGGER trg_usuario_update_acesso
BEFORE UPDATE ON usuario
FOR EACH ROW
BEGIN
    IF NEW.ultimo_acesso IS NOT NULL AND (OLD.ultimo_acesso IS NULL OR NEW.ultimo_acesso > OLD.ultimo_acesso) THEN
        SET NEW.ultimo_acesso = CURRENT_TIMESTAMP;
    END IF;
END //
DELIMITER ;

-- Views
CREATE OR REPLACE VIEW vw_emprestimos_detalhados AS
SELECT 
    e.id_emprestimo,
    e.data_emprestimo,
    e.data_devolucao_prevista,
    e.data_devolucao_real,
    e.status,
    e.dias_atraso,
    e.multa,
    u.id_usuario,
    u.nome AS nome_usuario,
    u.email AS email_usuario,
    u.matricula,
    l.id_livro,
    l.titulo AS titulo_livro,
    l.ano AS ano_livro,
    a.nome AS nome_autor,
    CASE 
        WHEN e.status = 'Ativo' AND e.data_devolucao_prevista < CURDATE() THEN 'Atrasado'
        WHEN e.status = 'Ativo' THEN 'Em dia'
        ELSE 'Devolvido'
    END AS situacao
FROM emprestimo e
INNER JOIN usuario u ON e.id_usuario = u.id_usuario
INNER JOIN livro l ON e.id_livro = l.id_livro
INNER JOIN autor a ON l.id_autor = a.id_autor;

CREATE OR REPLACE VIEW vw_estatisticas_autores AS
SELECT 
    a.id_autor,
    a.nome AS nome_autor,
    COUNT(DISTINCT l.id_livro) AS total_livros,
    COUNT(DISTINCT CASE WHEN l.status = 'Emprestado' THEN l.id_livro END) AS livros_emprestados,
    COUNT(DISTINCT CASE WHEN l.status = 'Disponivel' THEN l.id_livro END) AS livros_disponiveis,
    COUNT(DISTINCT e.id_emprestimo) AS total_emprestimos,
    COUNT(DISTINCT CASE WHEN e.status = 'Ativo' THEN e.id_emprestimo END) AS emprestimos_ativos
FROM autor a
LEFT JOIN livro l ON a.id_autor = l.id_autor
LEFT JOIN emprestimo e ON l.id_livro = e.id_livro
GROUP BY a.id_autor, a.nome;

CREATE OR REPLACE VIEW vw_usuarios_grupos AS
SELECT 
    u.id_usuario,
    u.nome,
    u.email,
    u.matricula,
    u.ativo,
    u.ultimo_acesso,
    g.id_grupo,
    g.nome AS nome_grupo,
    g.nivel_acesso,
    CASE g.nivel_acesso
        WHEN 1 THEN 'Leitura'
        WHEN 2 THEN 'Escrita'
        WHEN 3 THEN 'Administrador'
        ELSE 'Desconhecido'
    END AS descricao_nivel
FROM usuario u
LEFT JOIN usuario_grupo ug ON u.id_usuario = ug.id_usuario
LEFT JOIN grupos_usuarios g ON ug.id_grupo = g.id_grupo;

-- Stored Procedures
DELIMITER //
CREATE PROCEDURE sp_realizar_emprestimo(
    IN p_id_usuario VARCHAR(20),
    IN p_id_livro VARCHAR(20),
    IN p_dias_emprestimo INT,
    OUT p_resultado VARCHAR(255),
    OUT p_id_emprestimo VARCHAR(20)
)
BEGIN
    DECLARE v_livro_status VARCHAR(50);
    DECLARE v_usuario_ativo BOOLEAN;
    DECLARE v_novo_id VARCHAR(20);
    
    SELECT ativo INTO v_usuario_ativo FROM usuario WHERE id_usuario = p_id_usuario;
    
    IF v_usuario_ativo IS NULL THEN
        SET p_resultado = 'ERRO: Usuário não encontrado';
        SET p_id_emprestimo = NULL;
    ELSEIF v_usuario_ativo = FALSE THEN
        SET p_resultado = 'ERRO: Usuário inativo';
        SET p_id_emprestimo = NULL;
    ELSE
        SELECT status INTO v_livro_status FROM livro WHERE id_livro = p_id_livro;
        
        IF v_livro_status IS NULL THEN
            SET p_resultado = 'ERRO: Livro não encontrado';
            SET p_id_emprestimo = NULL;
        ELSEIF v_livro_status != 'Disponivel' THEN
            SET p_resultado = CONCAT('ERRO: Livro não está disponível. Status atual: ', v_livro_status);
            SET p_id_emprestimo = NULL;
        ELSE
            SET v_novo_id = gerar_id_emprestimo();
            
            INSERT INTO emprestimo (
                id_emprestimo, id_usuario, id_livro, 
                data_emprestimo, data_devolucao_prevista, status
            ) VALUES (
                v_novo_id, p_id_usuario, p_id_livro,
                CURDATE(), DATE_ADD(CURDATE(), INTERVAL p_dias_emprestimo DAY), 'Ativo'
            );
            
            SET p_id_emprestimo = v_novo_id;
            SET p_resultado = 'SUCESSO: Empréstimo realizado com sucesso';
        END IF;
    END IF;
END //
DELIMITER ;

DELIMITER //
CREATE PROCEDURE sp_devolver_emprestimo(
    IN p_id_emprestimo VARCHAR(20),
    OUT p_resultado VARCHAR(255),
    OUT p_multa DECIMAL(10,2)
)
BEGIN
    DECLARE v_status_atual VARCHAR(50);
    DECLARE v_data_prevista DATE;
    DECLARE v_dias_atraso INT;
    
    SELECT status, data_devolucao_prevista INTO v_status_atual, v_data_prevista
    FROM emprestimo WHERE id_emprestimo = p_id_emprestimo;
    
    IF v_status_atual IS NULL THEN
        SET p_resultado = 'ERRO: Empréstimo não encontrado';
        SET p_multa = 0.00;
    ELSEIF v_status_atual = 'Devolvido' THEN
        SET p_resultado = 'ERRO: Empréstimo já foi devolvido';
        SET p_multa = 0.00;
    ELSE
        SET v_dias_atraso = GREATEST(0, DATEDIFF(CURDATE(), v_data_prevista));
        SET p_multa = CASE WHEN v_dias_atraso > 0 THEN v_dias_atraso * 2.00 ELSE 0.00 END;
        
        UPDATE emprestimo 
        SET status = 'Devolvido',
            data_devolucao_real = CURDATE(),
            dias_atraso = v_dias_atraso,
            multa = p_multa
        WHERE id_emprestimo = p_id_emprestimo;
        
        SET p_resultado = CONCAT('SUCESSO: Devolução realizada. Multa: R$ ', FORMAT(p_multa, 2, 'de_DE'));
    END IF;
END //
DELIMITER ;

-- Stored Functions
DELIMITER //
CREATE FUNCTION fn_obter_nivel_acesso_usuario(p_id_usuario VARCHAR(20)) 
RETURNS INT
READS SQL DATA
DETERMINISTIC
BEGIN
    DECLARE v_nivel_max INT DEFAULT 0;
    
    SELECT COALESCE(MAX(nivel_acesso), 0) INTO v_nivel_max
    FROM usuario_grupo ug
    INNER JOIN grupos_usuarios g ON ug.id_grupo = g.id_grupo
    WHERE ug.id_usuario = p_id_usuario;
    
    RETURN v_nivel_max;
END //
DELIMITER ;

DELIMITER //
CREATE FUNCTION fn_count_emprestimos_ativos(p_id_usuario VARCHAR(20)) 
RETURNS INT
READS SQL DATA
DETERMINISTIC
BEGIN
    DECLARE v_count INT DEFAULT 0;
    
    SELECT COUNT(*) INTO v_count
    FROM emprestimo
    WHERE id_usuario = p_id_usuario AND status = 'Ativo';
    
    RETURN v_count;
END //
DELIMITER ;

-- Dados iniciais

-- Inserir grupos de usuários
INSERT INTO grupos_usuarios (id_grupo, nome, descricao, nivel_acesso) VALUES
('GRP001', 'Administradores', 'Acesso total ao sistema', 3),
('GRP002', 'Bibliotecários', 'Gerenciamento de livros e empréstimos', 2),
('GRP003', 'Usuários', 'Acesso básico para consulta e empréstimo', 1)
ON DUPLICATE KEY UPDATE nome = VALUES(nome), nivel_acesso = VALUES(nivel_acesso);

-- Inserir usuários (senha padrão: "123456")
-- Usando IDs fixos para garantir que as associações funcionem
INSERT INTO usuario (id_usuario, nome, email, senha, matricula) VALUES
('USR000001', 'Luiz', 'luiz@admin.com', SHA2('123456', 256), 'ADM001'),
('USR000002', 'Leandro', 'leandro@biblioteca.com', SHA2('123456', 256), 'BIB001'),
('USR000003', 'Lucas', 'lucas@user.com', SHA2('123456', 256), 'USR001')
ON DUPLICATE KEY UPDATE 
    nome = VALUES(nome),
    email = VALUES(email),
    senha = VALUES(senha),
    matricula = VALUES(matricula);

-- Vincular usuários aos grupos
-- Primeiro, remover qualquer associação existente para evitar duplicatas
DELETE FROM usuario_grupo;

-- Agora criar as associações corretas usando SELECT para garantir que funcionem
-- Usando INSERT IGNORE para evitar erros se já existir
INSERT IGNORE INTO usuario_grupo (id_usuario, id_grupo) 
SELECT id_usuario, 'GRP001' 
FROM usuario 
WHERE email = 'luiz@admin.com'
AND EXISTS (SELECT 1 FROM grupos_usuarios WHERE id_grupo = 'GRP001');

INSERT IGNORE INTO usuario_grupo (id_usuario, id_grupo) 
SELECT id_usuario, 'GRP002' 
FROM usuario 
WHERE email = 'leandro@biblioteca.com'
AND EXISTS (SELECT 1 FROM grupos_usuarios WHERE id_grupo = 'GRP002');

INSERT IGNORE INTO usuario_grupo (id_usuario, id_grupo) 
SELECT id_usuario, 'GRP003' 
FROM usuario 
WHERE email = 'lucas@user.com'
AND EXISTS (SELECT 1 FROM grupos_usuarios WHERE id_grupo = 'GRP003');

-- Garantir que todos os usuários tenham pelo menos um grupo
-- Se algum usuário não tiver grupo, associar ao grupo de Usuários por padrão
INSERT IGNORE INTO usuario_grupo (id_usuario, id_grupo)
SELECT u.id_usuario, 'GRP003'
FROM usuario u
WHERE u.id_usuario NOT IN (SELECT id_usuario FROM usuario_grupo)
AND EXISTS (SELECT 1 FROM grupos_usuarios WHERE id_grupo = 'GRP003');

-- Verificar se as associações foram criadas
SELECT '=== VERIFICAÇÃO DE ASSOCIAÇÕES ===' AS status;
SELECT 
    u.email,
    u.nome AS nome_usuario,
    g.nome AS grupo,
    g.nivel_acesso,
    CASE g.nivel_acesso
        WHEN 3 THEN 'ADMIN - Acesso total'
        WHEN 2 THEN 'BIBLIOTECARIO - Gerenciamento de livros e empréstimos'
        WHEN 1 THEN 'USER - Acesso básico'
        ELSE 'Desconhecido'
    END AS descricao_permissao
FROM usuario u
INNER JOIN usuario_grupo ug ON u.id_usuario = ug.id_usuario
INNER JOIN grupos_usuarios g ON ug.id_grupo = g.id_grupo
ORDER BY u.email;

-- Usuários do banco de dados e permissões
CREATE USER IF NOT EXISTS 'biblioteca_app'@'localhost' IDENTIFIED BY 'biblioteca_app_2024!';
CREATE USER IF NOT EXISTS 'biblioteca_readonly'@'localhost' IDENTIFIED BY 'readonly_2024!';

-- Conceder permissões
GRANT SELECT, INSERT, UPDATE, DELETE, EXECUTE ON biblioteca_db.* TO 'biblioteca_app'@'localhost';
GRANT SELECT ON biblioteca_db.* TO 'biblioteca_readonly'@'localhost';

FLUSH PRIVILEGES;
