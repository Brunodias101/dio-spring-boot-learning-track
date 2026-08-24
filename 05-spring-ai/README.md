# Budgeting API com Spring AI

Projeto final do módulo Spring AI da DIO. A aplicação registra e consulta transações financeiras e aceita comandos de voz em português para que o modelo escolha uma operação de negócio disponível.

O foco é integrar IA sem deslocar regras de negócio para o controlador ou para o modelo: a IA chama casos de uso já existentes, que continuam sendo a porta de entrada para o domínio e a persistência.

## Objetivo

- Persistir transações financeiras por HTTP.
- Consultar transações por categoria.
- Receber áudio, transcrever o comando e usar Tool Calling para executar uma ação de negócio.
- Devolver a resposta da interação por áudio.
- Manter valores monetários internamente em centavos.

## Arquitetura

| Camada | Responsabilidade |
| --- | --- |
| `domain` | Entidades de negócio, categorias e o contrato `TransactionRepository`. |
| `application` | Casos de uso expostos tanto ao HTTP quanto ao Tool Calling. |
| `infrastructure` | Adaptadores HTTP, JPA/MySQL e integração com Spring AI. |

### Fluxo de áudio e Tool Calling

```text
Arquivo de áudio
  → TranscriptionModel
  → texto transcrito
  → ChatClient
  → Tool Calling
  → caso de uso
  → repositório JPA / MySQL
  → resposta textual do modelo
  → TextToSpeechModel
  → áudio MP3
```

O `TransactionController` registra os casos de uso anotados com `@Tool` no `ChatClient`. O modelo decide quando chamar uma ferramenta, mas não acessa o banco diretamente.

## Tecnologias

- Java 25 e Gradle Wrapper
- Spring Boot 4 e Spring AI 2.0.0-M4
- OpenAI: chat, transcrição e síntese de voz
- Spring Data JPA e MySQL 9.6
- Docker Compose
- JUnit 5, AssertJ, Mockito e Lombok

## Como executar

### Pré-requisitos

- JDK 25
- Docker e Docker Compose
- Chave de API da OpenAI para o fluxo de áudio/IA

### 1. Suba o banco de dados

Na pasta deste projeto:

```bash
docker compose up -d
```

O Compose inicia o MySQL na porta `3307`, com banco `transaction`. A integração do Spring Boot com Docker Compose localiza o serviço.

Para encerrá-lo:

```bash
docker compose down
```

### 2. Configure a chave da OpenAI com segurança

`application.properties` lê `OPENAI_API_KEY`. Nunca coloque uma chave real no código, em arquivos versionados ou em exemplos enviados ao Git.

No PowerShell, somente para a sessão atual:

```powershell
$env:OPENAI_API_KEY = "<sua-chave>"
```

Para persistir a variável para novos terminais no Windows:

```powershell
[Environment]::SetEnvironmentVariable("OPENAI_API_KEY", "<sua-chave>", "User")
```

Abra um novo terminal depois da configuração persistente. A OpenAI orienta carregar a chave como variável de ambiente ou por um serviço de segredos no servidor e nunca expô-la no cliente. Consulte a [documentação oficial da OpenAI](https://developers.openai.com/api/reference/overview#authentication).

### 3. Inicie a aplicação

```bash
./gradlew bootRun
```

No Windows:

```powershell
.\gradlew.bat bootRun
```

## Endpoints HTTP

### Criar uma transação

`POST /transactions`

```bash
curl -X POST http://localhost:8080/transactions \
  -H "Content-Type: application/json" \
  -d '{"description":"Supermercado","category":"GROCERIES","amount":5000}'
```

`amount` é informado em centavos. A resposta devolve o valor convertido para reais, por exemplo `50.0`.

### Listar transações de uma categoria

`GET /transactions/{category}`

```bash
curl http://localhost:8080/transactions/GROCERIES
```

Categorias disponíveis: `GROCERIES`, `PHARMA` e `AUTO`.

### Enviar um comando de voz

`POST /transactions/ai` recebe `multipart/form-data` no campo `file` e produz `audio/mp3`.

```bash
curl -X POST http://localhost:8080/transactions/ai \
  -F "file=@comando.mp3" \
  --output resposta.mp3
```

Esse endpoint requer chave válida e acesso à API da OpenAI.

## Valores monetários: centavos no domínio, reais na saída

O domínio, os casos de uso e a persistência trabalham com `long` em centavos, evitando imprecisão de ponto flutuante durante cálculos. `TransactionOutput` converte o valor antes de devolvê-lo ao cliente:

| Valor armazenado | Valor exposto |
| ---: | ---: |
| `5000` | `50.00` |
| `3500` | `35.00` |
| `1050` | `10.50` |

## Ferramentas disponíveis para o modelo

O `ChatClient` registra estes casos de uso:

- `persist-transaction`: registra uma transação;
- `list-transactions-by-category`: lista transações de uma categoria;
- `calculate-total-by-category`: calcula o total de uma categoria e retorna um `long` em centavos.

### `calculate-total-by-category`

`CalculateTotalByCategoryUseCase` recebe uma `Category` com `@ToolParam` e delega o cálculo ao `TransactionRepository`. A ferramenta retorna centavos para manter a convenção monetária do domínio.

O adaptador JPA executa a agregação no banco:

```java
SELECT COALESCE(SUM(transaction.amount), 0)
FROM TransactionEntity transaction
WHERE transaction.category = :category
```

`SUM` evita carregar todas as transações para somá-las em Java. `COALESCE(..., 0)` retorna zero quando não há registros na categoria.

## Testes

Execute a suíte completa:

```bash
./gradlew test --no-daemon
```

Resultado verificado: **6 testes passaram** e **5 testes de integração da OpenAI foram ignorados** porque `OPENAI_API_KEY` não estava configurada. Eles são protegidos por uma condição de ambiente e não chamam a API sem a variável.

A cobertura inclui conversão de centavos para reais, o novo caso de uso de totalização, o adaptador JPA e o carregamento do contexto da aplicação.

## Limitações atuais

- O endpoint de áudio depende de conectividade, chave válida e disponibilidade dos serviços da OpenAI.
- Não há endpoint REST específico para obter o total por categoria; essa capacidade está disponível ao modelo via Tool Calling.
- As categorias são fixas no enum `Category`.
- Não há autenticação, autorização, paginação ou validação detalhada das requisições HTTP.
- A resposta de valores em endpoints HTTP usa `double`; o domínio continua usando `long` em centavos.

## Aprendizados

- Tool Calling expõe capacidades de negócio por casos de uso, sem dar ao modelo acesso direto à infraestrutura.
- Interfaces de repositório no domínio preservam o desacoplamento entre regras de negócio e JPA.
- Valores financeiros ficam mais previsíveis quando persistidos em centavos.
- Agregações como totalização pertencem ao banco quando podem ser calculadas por uma consulta simples.
- Chaves de API devem ser tratadas como segredos e fornecidas por variáveis de ambiente, nunca commitadas.

## Referências

- [Spring AI Reference](https://docs.spring.io/spring-ai/reference/index.html)
- [Spring AI Tools](https://docs.spring.io/spring-ai/reference/api/tools.html)
- [OpenAI API — autenticação](https://developers.openai.com/api/reference/overview#authentication)
