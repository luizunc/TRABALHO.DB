-- Criar usuário luiz@admin.com
-- Senha: 12345 (criptografada com BCrypt)

USE concessionaria;

-- Inserir usuário luiz
INSERT INTO usuarios (usuario_id, nome, email, senha, grupo_id, ativo) VALUES
('U000002', 'Luiz', 'luiz@admin.com', '$2a$10$rOzJ8K8qK8qK8qK8qK8qK.8qK8qK8qK8qK8qK8qK8qK8qK8qK8qK', 'GRP001', TRUE);

-- A senha acima é um hash BCrypt de exemplo. 
-- Para gerar o hash correto, execute este comando Java ou use um gerador online de BCrypt

