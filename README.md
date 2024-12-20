# 🔒 Sensitive Prison - Sistema de Autenticação

## 📝 Descrição
Sistema de autenticação avançado desenvolvido especialmente para o Sensitive Prison. Oferece um sistema seguro e profissional de login, registro e sessões, com integração ao MongoDB para persistência de dados.

## 🌟 Características

### Sistema de Autenticação
- ✅ Login e registro seguros
- 🔑 Senhas criptografadas com BCrypt
- ⚡ Sistema de sessões persistente
- 🌐 Suporte a múltiplos IPs
- ⏰ Timeout configurável
- 🚫 Proteção contra força bruta
- 📊 Sistema de logs detalhado

### Sistema de Localização
- 🎯 Spawn configurável via comando
- 🌍 Área de autenticação segura
- 🏗️ Plataforma de vidro escuro no Y=1000
- 🔄 Teleporte automático após autenticação

### Sistema de Sessões
- ⏱️ Duração configurável (padrão: 24 horas)
- 💾 Persistência via MongoDB
- 🔍 Verificação por IP
- 📱 Comando para verificar status

### Sistema Administrativo
- 👑 Painel administrativo completo
- 📋 Logs detalhados de ações
- 🔧 Gerenciamento de jogadores
- 📊 Monitoramento de tentativas falhas

## 📋 Comandos

### Comandos para Jogadores
- `/login <senha>` - Faz login no servidor
- `/register <senha> <senha>` - Registra-se no servidor
- `/session` - Verifica o status da sua sessão

### Comandos Administrativos
- `/auth admin info <jogador>` - Ver informações de um jogador
- `/auth admin logs <jogador>` - Ver logs de um jogador
- `/auth admin unregister <jogador>` - Remover registro de um jogador
- `/auth admin forcelogout <jogador>` - Forçar logout de um jogador
- `/auth admin recent` - Ver logs recentes
- `/auth admin setspawn` - Definir o spawn do servidor
- `/auth admin spawn` - Teleportar para o spawn

## ⚙️ Configuração
```yaml
# Configuração do MongoDB
mongodb-uri: "sua_uri_aqui"

# Configurações de localização
locations:
  spawn:
    world: "world"
    x: 0.0
    y: 64.0
    z: 0.0
    yaw: 0.0
    pitch: 0.0

# Configurações de sessão
session:
  duration: 1440 # 24 horas em minutos
  enabled: true
  messages:
    active: "Sua sessão está ativa! Tempo restante: &e%time%"
    expired: "Sua sessão expirou. Por favor, faça login novamente"
    not-found: "Você não possui uma sessão ativa no momento"

# Configurações de autenticação
auth:
  timeout: 60 # Tempo em segundos
  spawn-radius: 10
```

## 🔒 Sistema de Segurança

### Proteção contra Força Bruta
- Limite de tentativas de login (5 tentativas)
- Cooldown entre tentativas (3 segundos)
- Kicka o jogador após exceder limite
- Logs de tentativas falhas

### Proteção durante Autenticação
- Bloqueio de movimentação
- Bloqueio de comandos
- Bloqueio de chat
- Bloqueio de interações
- Bloqueio de dano
- Bloqueio de construção/destruição
- Área segura para autenticação

## 📊 Sistema de Logs

### Tipos de Logs
- Login (sucesso/falha)
- Registro
- Tentativas falhas
- Ações administrativas

### Informações Registradas
- Data e hora
- Nome do jogador
- UUID
- IP
- Tipo de ação
- Resultado
- Detalhes adicionais

## 💾 Armazenamento de Dados

### MongoDB Collections
- `players`: Dados dos jogadores
- `sessions`: Sessões ativas
- `auth_logs`: Logs do sistema

### Estrutura dos Dados
```javascript
// Players Collection
{
  uuid: String,
  username: String,
  passwordHash: String,
  lastIp: String,
  lastLogin: Long
}

// Sessions Collection
{
  key: String,
  uuid: String,
  ip: String,
  creationTime: Long
}

// Auth Logs Collection
{
  type: String,
  player: String,
  uuid: String,
  ip: String,
  timestamp: Long,
  date: String,
  details: Object
}
```

## 🚀 Desempenho
- Cache local para dados frequentes
- Operações assíncronas para MongoDB
- Timeouts reduzidos para melhor resposta
- Carregamento otimizado de chunks
- Sistema de retry para operações críticas

## 📦 Dependências
- Spigot 1.8.8
- MongoDB Driver 4.11.1
- jBCrypt 0.4

## ⚖️ Permissões
- `sensitive.auth.admin` - Acesso aos comandos administrativos

## 🎨 Mensagens
- Sistema de cores com ChatColor
- Prefixo personalizado
- Títulos e subtítulos
- Mensagens configuráveis
- Formatação profissional

## 🔧 Instalação
1. Baixe o plugin
2. Coloque na pasta plugins
3. Configure o MongoDB no config.yml
4. Inicie o servidor
5. Configure o spawn usando `/auth admin setspawn`

## ⚠️ Requisitos
- Java 8 ou superior
- Spigot/Paper 1.8.8
- MongoDB
- 512MB RAM mínimo recomendado

## 🤝 Suporte
Para suporte, entre em contato com a equipe do Sensitive Prison.

## 👨‍💻 Desenvolvedor
Desenvolvido por ZeroLegion para o Sensitive Prison.

---
Plugin feito com ❤️ para o Sensitive Prison 