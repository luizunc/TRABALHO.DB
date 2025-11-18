-- ============================================
-- CONCESSIONÁRIA DE AUTOMÓVEIS - SCHEMA MYSQL
-- ============================================

-- Criar banco de dados
CREATE DATABASE IF NOT EXISTS concessionaria CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE concessionaria;

-- ============================================
-- TABELAS DE CONTROLE DE ACESSO
-- ============================================

-- Tabela de grupos de usuários
CREATE TABLE grupos_usuarios (
    grupo_id VARCHAR(20) PRIMARY KEY,
    nome_grupo VARCHAR(50) NOT NULL UNIQUE,
    descricao TEXT,
    nivel_acesso INT NOT NULL DEFAULT 1,
    criado_em TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

-- Tabela de usuários
CREATE TABLE usuarios (
    usuario_id VARCHAR(20) PRIMARY KEY,
    nome VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    senha VARCHAR(255) NOT NULL,
    grupo_id VARCHAR(20) NOT NULL,
    ativo BOOLEAN DEFAULT TRUE,
    criado_em TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (grupo_id) REFERENCES grupos_usuarios(grupo_id),
    INDEX idx_email (email),
    INDEX idx_grupo (grupo_id)
) ENGINE=InnoDB;

-- ============================================
-- TABELAS PRINCIPAIS
-- ============================================

-- Tabela de clientes
CREATE TABLE clientes (
    cliente_id VARCHAR(20) PRIMARY KEY,
    nome VARCHAR(100) NOT NULL,
    cpf VARCHAR(14) UNIQUE,
    telefone VARCHAR(20),
    email VARCHAR(100),
    endereco TEXT,
    criado_em TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_cpf (cpf),
    INDEX idx_nome (nome)
) ENGINE=InnoDB;

-- Tabela de veículos
CREATE TABLE veiculos (
    veiculo_id VARCHAR(20) PRIMARY KEY,
    modelo VARCHAR(100) NOT NULL,
    marca VARCHAR(50) NOT NULL,
    ano INT NOT NULL,
    cor VARCHAR(30),
    placa VARCHAR(10) UNIQUE,
    preco DECIMAL(10,2) NOT NULL,
    status ENUM('disponivel', 'vendido', 'reservado') DEFAULT 'disponivel',
    quilometragem INT DEFAULT 0,
    criado_em TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_status (status),
    INDEX idx_marca_modelo (marca, modelo),
    INDEX idx_placa (placa)
) ENGINE=InnoDB;

-- Tabela de vendas
CREATE TABLE vendas (
    venda_id VARCHAR(20) PRIMARY KEY,
    cliente_id VARCHAR(20) NOT NULL,
    veiculo_id VARCHAR(20) NOT NULL,
    vendedor_id VARCHAR(20) NOT NULL,
    valor_venda DECIMAL(10,2) NOT NULL,
    data_venda DATE NOT NULL,
    forma_pagamento VARCHAR(50),
    criado_em TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (cliente_id) REFERENCES clientes(cliente_id),
    FOREIGN KEY (veiculo_id) REFERENCES veiculos(veiculo_id),
    FOREIGN KEY (vendedor_id) REFERENCES usuarios(usuario_id),
    INDEX idx_data_venda (data_venda),
    INDEX idx_cliente (cliente_id),
    INDEX idx_vendedor (vendedor_id)
) ENGINE=InnoDB;

-- ============================================
-- FUNÇÕES DE GERAÇÃO DE IDs
-- ============================================

DELIMITER //

-- Função para gerar ID de usuário
CREATE FUNCTION gerar_id_usuario() RETURNS VARCHAR(20)
READS SQL DATA
DETERMINISTIC
BEGIN
    DECLARE novo_id VARCHAR(20);
    DECLARE contador INT;
    SET contador = (SELECT COALESCE(MAX(CAST(SUBSTRING(usuario_id, 2) AS UNSIGNED)), 0) FROM usuarios WHERE usuario_id LIKE 'U%');
    SET novo_id = CONCAT('U', LPAD(contador + 1, 6, '0'));
    RETURN novo_id;
END //

-- Função para gerar ID de cliente
CREATE FUNCTION gerar_id_cliente() RETURNS VARCHAR(20)
READS SQL DATA
DETERMINISTIC
BEGIN
    DECLARE novo_id VARCHAR(20);
    DECLARE contador INT;
    SET contador = (SELECT COALESCE(MAX(CAST(SUBSTRING(cliente_id, 2) AS UNSIGNED)), 0) FROM clientes WHERE cliente_id LIKE 'C%');
    SET novo_id = CONCAT('C', LPAD(contador + 1, 6, '0'));
    RETURN novo_id;
END //

-- Função para gerar ID de veículo
CREATE FUNCTION gerar_id_veiculo() RETURNS VARCHAR(20)
READS SQL DATA
DETERMINISTIC
BEGIN
    DECLARE novo_id VARCHAR(20);
    DECLARE contador INT;
    SET contador = (SELECT COALESCE(MAX(CAST(SUBSTRING(veiculo_id, 2) AS UNSIGNED)), 0) FROM veiculos WHERE veiculo_id LIKE 'V%');
    SET novo_id = CONCAT('V', LPAD(contador + 1, 6, '0'));
    RETURN novo_id;
END //

-- Função para gerar ID de venda
CREATE FUNCTION gerar_id_venda() RETURNS VARCHAR(20)
READS SQL DATA
DETERMINISTIC
BEGIN
    DECLARE novo_id VARCHAR(20);
    DECLARE contador INT;
    SET contador = (SELECT COALESCE(MAX(CAST(SUBSTRING(venda_id, 2) AS UNSIGNED)), 0) FROM vendas WHERE venda_id LIKE 'S%');
    SET novo_id = CONCAT('S', LPAD(contador + 1, 6, '0'));
    RETURN novo_id;
END //

DELIMITER ;

-- ============================================
-- PROCEDURES
-- ============================================

DELIMITER //

-- Procedure para criar novo cliente
CREATE PROCEDURE criar_cliente(
    IN p_nome VARCHAR(100),
    IN p_cpf VARCHAR(14),
    IN p_telefone VARCHAR(20),
    IN p_email VARCHAR(100),
    IN p_endereco TEXT,
    OUT p_cliente_id VARCHAR(20)
)
BEGIN
    SET p_cliente_id = gerar_id_cliente();
    INSERT INTO clientes (cliente_id, nome, cpf, telefone, email, endereco)
    VALUES (p_cliente_id, p_nome, p_cpf, p_telefone, p_email, p_endereco);
END //

-- Procedure para registrar venda
CREATE PROCEDURE registrar_venda(
    IN p_cliente_id VARCHAR(20),
    IN p_veiculo_id VARCHAR(20),
    IN p_vendedor_id VARCHAR(20),
    IN p_valor_venda DECIMAL(10,2),
    IN p_forma_pagamento VARCHAR(50),
    OUT p_venda_id VARCHAR(20)
)
BEGIN
    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN
        ROLLBACK;
        RESIGNAL;
    END;
    
    START TRANSACTION;
    
    SET p_venda_id = gerar_id_venda();
    
    INSERT INTO vendas (venda_id, cliente_id, veiculo_id, vendedor_id, valor_venda, data_venda, forma_pagamento)
    VALUES (p_venda_id, p_cliente_id, p_veiculo_id, p_vendedor_id, p_valor_venda, CURDATE(), p_forma_pagamento);
    
    UPDATE veiculos SET status = 'vendido' WHERE veiculo_id = p_veiculo_id;
    
    COMMIT;
END //

DELIMITER ;

-- ============================================
-- TRIGGERS
-- ============================================

DELIMITER //

-- Trigger: Atualizar status do veículo quando vendido
CREATE TRIGGER trg_veiculo_vendido
AFTER INSERT ON vendas
FOR EACH ROW
BEGIN
    UPDATE veiculos 
    SET status = 'vendido' 
    WHERE veiculo_id = NEW.veiculo_id;
END //

-- Trigger: Validar email único ao inserir usuário
CREATE TRIGGER trg_validar_email_usuario
BEFORE INSERT ON usuarios
FOR EACH ROW
BEGIN
    IF EXISTS (SELECT 1 FROM usuarios WHERE email = NEW.email) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Email já cadastrado';
    END IF;
END //

DELIMITER ;

-- ============================================
-- VIEWS
-- ============================================

-- View: Veículos disponíveis com informações completas
CREATE VIEW vw_veiculos_disponiveis AS
SELECT 
    v.veiculo_id,
    v.marca,
    v.modelo,
    v.ano,
    v.cor,
    v.placa,
    v.preco,
    v.quilometragem
FROM veiculos v
WHERE v.status = 'disponivel'
ORDER BY v.marca, v.modelo;

-- View: Relatório de vendas com detalhes
CREATE VIEW vw_relatorio_vendas AS
SELECT 
    s.venda_id,
    s.data_venda,
    c.nome AS cliente_nome,
    c.cpf AS cliente_cpf,
    v.marca AS veiculo_marca,
    v.modelo AS veiculo_modelo,
    v.ano AS veiculo_ano,
    u.nome AS vendedor_nome,
    s.valor_venda,
    s.forma_pagamento
FROM vendas s
JOIN clientes c ON s.cliente_id = c.cliente_id
JOIN veiculos v ON s.veiculo_id = v.veiculo_id
JOIN usuarios u ON s.vendedor_id = u.usuario_id
ORDER BY s.data_venda DESC;

-- ============================================
-- DADOS INICIAIS
-- ============================================

-- Inserir grupos de usuários
INSERT INTO grupos_usuarios (grupo_id, nome_grupo, descricao, nivel_acesso) VALUES
('GRP001', 'Administrador', 'Acesso total ao sistema', 3),
('GRP002', 'Vendedor', 'Pode realizar vendas e consultar dados', 2),
('GRP003', 'Consultor', 'Apenas consulta de informações', 1);

-- Inserir usuário admin padrão (senha: admin123)
INSERT INTO usuarios (usuario_id, nome, email, senha, grupo_id) VALUES
('U000001', 'Administrador', 'admin@concessionaria.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'GRP001');

-- Inserir alguns veículos de exemplo
INSERT INTO veiculos (veiculo_id, modelo, marca, ano, cor, placa, preco, status, quilometragem) VALUES
('V000001', 'Civic', 'Honda', 2023, 'Branco', 'ABC1234', 120000.00, 'disponivel', 0),
('V000002', 'Corolla', 'Toyota', 2022, 'Preto', 'XYZ5678', 110000.00, 'disponivel', 15000),
('V000003', 'Onix', 'Chevrolet', 2023, 'Prata', 'DEF9012', 75000.00, 'disponivel', 5000);

