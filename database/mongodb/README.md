# MongoDB - Justificativa e Uso

## Por que MongoDB?

O MongoDB foi escolhido para armazenar:
1. **Logs de atividades** - Histórico de ações dos usuários
2. **Cache de consultas** - Melhorar performance de buscas frequentes
3. **Dados não estruturados** - Comentários, avaliações de veículos

## Vantagens:
- **Flexibilidade**: Estrutura de documentos permite variações
- **Performance**: Ideal para leituras rápidas (cache)
- **Escalabilidade**: Fácil expansão horizontal
- **JSON nativo**: Integração simples com APIs REST

## Estrutura de Coleções:

### logs_atividades
```json
{
  "usuario_id": "U000001",
  "acao": "login",
  "timestamp": "2024-01-15T10:30:00Z",
  "detalhes": {}
}
```

### cache_veiculos
```json
{
  "filtro": "marca=Honda&status=disponivel",
  "resultado": [...],
  "expira_em": "2024-01-15T11:30:00Z"
}
```

