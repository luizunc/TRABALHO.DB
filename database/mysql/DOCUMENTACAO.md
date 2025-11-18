# Documentação do Banco de Dados MySQL

## Estrutura de Tabelas

### grupos_usuarios
Armazena os grupos de permissão do sistema.
- **grupo_id** (PK): Identificador único do grupo
- **nome_grupo**: Nome do grupo (único)
- **descricao**: Descrição do grupo
- **nivel_acesso**: Nível numérico de acesso (1=Consultor, 2=Vendedor, 3=Admin)

### usuarios
Armazena os usuários do sistema.
- **usuario_id** (PK): Identificador único gerado pela função gerar_id_usuario()
- **nome**: Nome completo do usuário
- **email**: Email único do usuário
- **senha**: Senha criptografada (BCrypt)
- **grupo_id** (FK): Referência a grupos_usuarios
- **ativo**: Status do usuário (true/false)

### clientes
Armazena os clientes da concessionária.
- **cliente_id** (PK): Identificador único gerado pela função gerar_id_cliente()
- **nome**: Nome completo
- **cpf**: CPF único
- **telefone**: Telefone de contato
- **email**: Email de contato
- **endereco**: Endereço completo

### veiculos
Armazena o estoque de veículos.
- **veiculo_id** (PK): Identificador único gerado pela função gerar_id_veiculo()
- **modelo**: Modelo do veículo
- **marca**: Marca do veículo
- **ano**: Ano de fabricação
- **cor**: Cor do veículo
- **placa**: Placa única
- **preco**: Preço de venda
- **status**: disponivel, vendido ou reservado
- **quilometragem**: Quilometragem atual

### vendas
Registra as vendas realizadas.
- **venda_id** (PK): Identificador único gerado pela função gerar_id_venda()
- **cliente_id** (FK): Referência a clientes
- **veiculo_id** (FK): Referência a veiculos
- **vendedor_id** (FK): Referência a usuarios
- **valor_venda**: Valor da venda
- **data_venda**: Data da venda
- **forma_pagamento**: Forma de pagamento utilizada

## Índices e Justificativas

### idx_email (usuarios)
**Justificativa**: Busca frequente por email durante login e validações. Índice único garante unicidade e performance.

### idx_cpf (clientes)
**Justificativa**: Validação de CPF único e buscas rápidas por CPF. Índice único previne duplicatas.

### idx_status (veiculos)
**Justificativa**: Filtros constantes por status (disponível, vendido, reservado). Melhora performance de consultas.

### idx_marca_modelo (veiculos)
**Justificativa**: Buscas frequentes por marca e modelo. Índice composto otimiza essas consultas.

### idx_placa (veiculos)
**Justificativa**: Validação de placa única e buscas rápidas. Índice único previne duplicatas.

### idx_data_venda (vendas)
**Justificativa**: Relatórios e consultas ordenadas por data. Essencial para análises temporais.

### idx_cliente (vendas)
**Justificativa**: Consultas de histórico de compras por cliente. Melhora performance de JOINs.

### idx_vendedor (vendas)
**Justificativa**: Relatórios de vendas por vendedor. Otimiza consultas de desempenho.

## Triggers

### trg_veiculo_vendido
**Função**: Atualiza automaticamente o status do veículo para 'vendido' quando uma nova venda é registrada.
**Justificativa**: Garante consistência dos dados sem necessidade de atualização manual. Previne veículos vendidos aparecerem como disponíveis.

### trg_validar_email_usuario
**Função**: Valida se o email já existe antes de inserir um novo usuário.
**Justificativa**: Previne duplicação de emails no banco de dados, garantindo integridade referencial mesmo antes da inserção.

## Views

### vw_veiculos_disponiveis
**Função**: Lista apenas veículos com status 'disponivel'.
**Justificativa**: Simplifica consultas frequentes de veículos disponíveis para venda. Reduz complexidade de queries e melhora performance.

### vw_relatorio_vendas
**Função**: Relatório completo de vendas com JOINs de clientes, veículos e vendedores.
**Justificativa**: Facilita geração de relatórios sem necessidade de JOINs complexos em cada consulta. Centraliza lógica de relatórios.

## Procedures e Functions

### Funções de Geração de IDs

#### gerar_id_usuario()
Gera IDs no formato: U000001, U000002, U000003...
**Justificativa**: IDs sequenciais e legíveis. Facilita identificação manual e rastreamento.

#### gerar_id_cliente()
Gera IDs no formato: C000001, C000002, C000003...
**Justificativa**: Prefixo 'C' identifica clientes. Sequencial facilita organização.

#### gerar_id_veiculo()
Gera IDs no formato: V000001, V000002, V000003...
**Justificativa**: Prefixo 'V' identifica veículos. Sequencial facilita gestão de estoque.

#### gerar_id_venda()
Gera IDs no formato: S000001, S000002, S000003...
**Justificativa**: Prefixo 'S' identifica vendas (Sales). Sequencial facilita rastreamento.

### Procedures

#### criar_cliente()
**Função**: Cria um novo cliente com ID gerado automaticamente.
**Parâmetros**: nome, cpf, telefone, email, endereco
**Retorno**: cliente_id gerado
**Justificativa**: Encapsula lógica de criação e geração de ID. Facilita uso e manutenção.

#### registrar_venda()
**Função**: Registra uma venda e atualiza o status do veículo em uma transação.
**Parâmetros**: cliente_id, veiculo_id, vendedor_id, valor_venda, forma_pagamento
**Retorno**: venda_id gerado
**Justificativa**: Garante atomicidade da operação. Se a venda falhar, o veículo não é marcado como vendido. Previne inconsistências.

## Controle de Acesso

### Usuário: app_concessionaria
- **Permissões**: SELECT, INSERT, UPDATE, DELETE em todas as tabelas
- **Uso**: Aplicação backend
- **Justificativa**: Aplicação precisa de acesso completo para operações CRUD

### Usuário: readonly_concessionaria
- **Permissões**: SELECT em todas as tabelas
- **Uso**: Relatórios e consultas apenas leitura
- **Justificativa**: Segurança - usuários de relatórios não devem modificar dados

### Política: Sem acesso root
- **Justificativa**: Segurança. Root tem privilégios totais e não deve ser usado pela aplicação. Usuários específicos limitam danos em caso de comprometimento.

