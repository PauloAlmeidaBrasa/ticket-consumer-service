# Ticket Consumer Service

Este projeto é um consumer em Spring Boot responsável por ler mensagens de compra de ingressos em uma fila AWS SQS, atualizar os dados do ingresso no banco de dados e acionar helpers de notificação após a compra ser processada com sucesso.

## Propósito

O consumer consulta a fila SQS em intervalos regulares, desserializa cada mensagem de compra, valida o payload, marca o ingresso como vendido para o usuário informado e depois aciona os helpers de email e WhatsApp.

Na prática, este serviço representa a parte assíncrona do fluxo de compra de ingressos. Ele desacopla o processamento da confirmação da compra da aplicação produtora que envia as mensagens para a fila.

## Tecnologias Utilizadas

- Java 21
- Spring Boot 4.1
- Spring Scheduling
- Spring Data JPA
- Jackson para parsing de payloads JSON
- AWS SDK v2 para Amazon SQS
- MySQL como banco de dados da aplicação
- H2 para testes e cenários locais de fallback
- Maven como ferramenta de build
- Docker com `Dockerfile.dev`

## Como Funciona

O fluxo principal é o seguinte:

1. O serviço lê mensagens da fila SQS configurada.
2. Cada mensagem é convertida em um `TicketPurchaseMessage`.
3. O consumer valida os campos obrigatórios.
4. O ingresso é atualizado no banco como `SOLD` e associado ao usuário.
5. Os helpers de email e WhatsApp são acionados.
6. Após o processamento com sucesso, a mensagem é removida da fila.

## Configuração de Ambiente

A aplicação importa variáveis do arquivo `.env` e também aceita variáveis padrão do Spring.

Antes de executar o serviço, revise e preencha no mínimo estas variáveis:

### Fila e AWS

```dotenv
TICKET_QUEUE_URL=
TICKET_QUEUE_MAX_MESSAGES=10
TICKET_QUEUE_WAIT_TIME_SECONDS=1
AWS_REGION=us-east-1
AWS_ACCESS_KEY_ID=
AWS_SECRET_ACCESS_KEY=
```

### Banco de Dados

Estas variáveis precisam estar preenchidas para que o consumer consiga se conectar ao banco correto:

```dotenv
SPRING_DATASOURCE_URL=
SPRING_DATASOURCE_USERNAME=
SPRING_DATASOURCE_PASSWORD=
```

Se você estiver usando MySQL fora do Docker Compose, configure também:

```dotenv
SPRING_DATASOURCE_DRIVER_CLASS_NAME=com.mysql.cj.jdbc.Driver
```

### Validação do Hibernate

Mantenha esta variável explicitamente configurada:

```dotenv
SPRING_JPA_HIBERNATE_DDL_AUTO=validate
```

Isso é importante porque o consumer depende de um schema de banco já existente. Com `validate`, o Hibernate apenas verifica se as entidades mapeadas estão compatíveis com o schema atual, sem criar nem alterar tabelas automaticamente.

## Executando Localmente com Maven

Use o Maven Wrapper incluído no projeto:

```bash
./mvnw spring-boot:run
```

## Executando com Docker

Este repositório inclui o arquivo `Dockerfile.dev`, então o serviço pode ser iniciado em um container.

### Build da imagem

```bash
docker build -f Dockerfile.dev -t ticket-consumer-service .
```

### Executando o container com `.env`

```bash
docker run --rm --env-file .env ticket-consumer-service
```

Se o seu banco estiver rodando em Docker Compose, garanta que `SPRING_DATASOURCE_URL` aponte para o hostname do serviço no Compose, por exemplo:

```dotenv
SPRING_DATASOURCE_URL=jdbc:mysql://db:3306/buy_tickets
SPRING_DATASOURCE_USERNAME=buy_tickets
SPRING_DATASOURCE_PASSWORD=buy_tickets_pwd
SPRING_JPA_HIBERNATE_DDL_AUTO=validate
```

Se o banco estiver rodando fora do Docker Compose, substitua a URL e as credenciais pelo host, porta, nome do banco e usuário corretos.

## Observações

- O perfil padrão da aplicação é `local`.
- O consumer roda em agendamento com atraso fixo e continua consultando a fila automaticamente.
- Se a URL da fila estiver vazia, o consumer não processa mensagens.
- O projeto inclui testes com H2 para validar o fluxo de processamento de forma isolada.
