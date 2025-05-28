# Sistema de Empacotamento de Pedidos - Arquitetura de Microsserviços

Este projeto consiste em uma API RESTful desenvolvida em Java com Spring Boot para otimizar o empacotamento de produtos em caixas de papelão, minimizando o número de caixas utilizadas. A aplicação foi refatorada para utilizar uma arquitetura de microsserviços.

## Requisitos

- Java 11 ou superior
- Maven 3.6 ou superior
- Docker e Docker Compose (para execução containerizada)

## Tecnologias Utilizadas

- Spring Boot 2.7
- Spring Cloud (Eureka, Gateway)
- Spring Security com JWT
- Spring Data JPA
- H2 Database (banco de dados em memória)
- Swagger/OpenAPI para documentação da API
- JUnit 5 para testes unitários
- Lombok para redução de código boilerplate
- Docker para containerização

## Estrutura da Aplicação

O sistema está dividido em três microsserviços:

- **eureka-server**: Serviço de descoberta que registra e localiza os outros microsserviços
- **api-gateway**: Gateway API que encaminha as requisições para os serviços corretos
- **order-service**: Responsável pelo gerenciamento de pedidos, produtos e otimização do empacotamento

## Funcionalidades

- Recebimento de pedidos contendo produtos com suas dimensões
- Cálculo da melhor combinação de caixas para embalar os produtos
- Otimização do espaço, minimizando o número de caixas utilizadas
- Autenticação de usuários com JWT
- Documentação interativa da API via Swagger
- Registro e descoberta de serviços com Eureka
- Roteamento inteligente com API Gateway

## Tamanhos de Caixas Disponíveis

- Caixa 1: 30 x 40 x 80 cm
- Caixa 2: 80 x 50 x 40 cm
- Caixa 3: 50 x 80 x 60 cm

## Execução do Projeto

### Usando Docker Compose (Recomendado)

```bash
# Na raiz do projeto
docker-compose up --build
```

O flag `--build` garante que todas as imagens sejam construídas a partir do zero, evitando problemas com versões anteriores.

### Verificando o Status dos Serviços

```bash
# Verificar status dos contêineres
docker ps

# Ver logs de um serviço específico
docker logs -f eureka-server
docker logs -f api-gateway
docker logs -f order-service
```

### Execução Local dos Serviços Separadamente

```bash
# Na raiz do projeto
mvn clean install -DskipTests

# Iniciar o Eureka Server
cd eureka-server
mvn spring-boot:run

# Iniciar o Order Service (em novo terminal)
cd order-service
mvn spring-boot:run

# Iniciar o API Gateway (em novo terminal)
cd api-gateway
mvn spring-boot:run
```

## Acessando os Serviços

- **Eureka Server**: http://localhost:8761
- **API Gateway**: http://localhost:8090
- **Order Service API (via Gateway)**: http://localhost:8090/api/v1/packaging/optimize
- **Documentação Swagger (via Gateway)**: http://localhost:8090/swagger-ui/index.html
- **Documentação Swagger (direto)**: http://localhost:8080/swagger-ui/index.html
- **Console H2 (direto)**: http://localhost:8080/h2-console (JDBC URL: jdbc:h2:mem:orderdb, User: sa, Password: orderdb2025)

## Autenticação

Para acessar endpoints protegidos, primeiro obtenha um token JWT:

```bash
# Login com usuário padrão
curl -X POST http://localhost:8090/api/auth/login -H "Content-Type: application/json" -d '{"username":"admin", "password":"admin123"}'

# Resposta conterá um token:
# { "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..." }

# Use este token para autenticar outras solicitações:
curl -X POST http://localhost:8090/api/v1/packaging/optimize \
  -H "Authorization: Bearer SEU_TOKEN_AQUI" \
  -H "Content-Type: application/json" \
  -d '{"orders": [...]}'
```

### Usuários padrão
- Admin: username `admin`, senha `admin123`
- Usuário Comum: username `user`, senha `user123`

## Algoritmo de Empacotamento

O algoritmo de empacotamento utiliza uma abordagem de "First-Fit Decreasing":

1. Ordena os produtos por volume, do maior para o menor
2. Para cada produto, tenta colocá-lo na primeira caixa onde ele caiba
3. Se não houver caixa disponível, cria uma nova caixa
4. Considera as 6 possíveis rotações do produto para maximizar o uso do espaço

O algoritmo verifica tanto o volume total quanto as dimensões físicas para garantir que os produtos realmente caibam nas caixas.

## Solução de Problemas

### Serviços não se registram no Eureka

- Verifique se o Eureka Server está em execução
- Verifique se os endereços na configuração estão corretos
- Tente reiniciar os serviços na ordem: Eureka Server > Order Service > API Gateway

### Erro de conexão ao H2 Console

- Certifique-se de que o parâmetro `spring.h2.console.settings.web-allow-others` está definido como `true`
- Acesse diretamente em http://localhost:8080/h2-console em vez de passar pelo Gateway

### JWT Token inválido ou expirado

- Obtenha um novo token com o endpoint `/api/auth/login`
- Verifique se está enviando o token no formato correto no header: `Authorization: Bearer SEU_TOKEN_AQUI`

## Exemplo de Uso

Requisição:

```json
{
  "orders": [
    {
      "orderNumber": "ORD123",
      "products": [
        {
          "name": "Produto 1",
          "height": 20.0,
          "width": 30.0,
          "length": 40.0
        },
        {
          "name": "Produto 2",
          "height": 10.0,
          "width": 15.0,
          "length": 25.0
        }
      ]
    }
  ]
}
```

Resposta:

```json
{
  "orders": [
    {
      "orderNumber": "ORD123",
      "boxes": [
        {
          "name": "Caixa 1",
          "height": 30.0,
          "width": 40.0,
          "length": 80.0,
          "products": [
            {
              "name": "Produto 1",
              "height": 20.0,
              "width": 30.0,
              "length": 40.0
            },
            {
              "name": "Produto 2",
              "height": 10.0,
              "width": 15.0,
              "length": 25.0
            }
          ]
        }
      ]
    }
  ]
}
```

## Testes

Os testes unitários cobrem as principais funcionalidades do sistema de empacotamento.

```bash
# Executar testes
mvn test
```

## Arquitetura de Microsserviços

### 1. Eureka Server

O Eureka Server atua como um serviço de descoberta onde todos os microsserviços se registram. Isso permite:
- Localização dinâmica de serviços
- Balanceamento de carga
- Resiliência em caso de falha

### 2. API Gateway

O API Gateway serve como um único ponto de entrada para todo o sistema, oferecendo:
- Roteamento inteligente de requisições para os serviços apropriados
- Simplificação do acesso ao cliente
- Implementação de políticas de segurança centralizadas
- Agregação de endpoints de diferentes serviços

### 3. Order Service

O serviço principal responsável por:
- Autenticação e autorização de usuários
- Gestão de pedidos
- Algoritmos de otimização de empacotamento
- Lógica de negócio relacionada à gestão de produtos e caixas

### Comunicação entre Serviços

Os serviços se comunicam através de:
- Chamadas REST via API Gateway
- Registro e descoberta através do Eureka Server
- Balanceamento de carga gerenciado pelo Spring Cloud

### Segurança

A segurança é implementada usando:
- Autenticação JWT
- HTTPS para comunicação segura
- Políticas de acesso baseadas em roles
