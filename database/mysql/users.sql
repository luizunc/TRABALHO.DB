-- ============================================
-- CRIAÇÃO DE USUÁRIOS E PERMISSÕES
-- ============================================

-- Criar usuário para aplicação (não root)
CREATE USER IF NOT EXISTS 'app_concessionaria'@'localhost' IDENTIFIED BY 'app_senha_123';
CREATE USER IF NOT EXISTS 'app_concessionaria'@'%' IDENTIFIED BY 'app_senha_123';

-- Conceder permissões ao usuário da aplicação
GRANT SELECT, INSERT, UPDATE, DELETE ON concessionaria.* TO 'app_concessionaria'@'localhost';
GRANT SELECT, INSERT, UPDATE, DELETE ON concessionaria.* TO 'app_concessionaria'@'%';

-- Criar usuário somente leitura para relatórios
CREATE USER IF NOT EXISTS 'readonly_concessionaria'@'localhost' IDENTIFIED BY 'readonly_123';
GRANT SELECT ON concessionaria.* TO 'readonly_concessionaria'@'localhost';

-- Aplicar permissões
FLUSH PRIVILEGES;

