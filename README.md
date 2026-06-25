# Rede Raízes do Nordeste — Sistema de Pedidos (Back-End)

> Projeto Multidisciplinar · Trilha Back-End · UNINTER  
> Autor: **Pedro Marques Mesquita** · RU: **4710499**

API REST para gerenciamento de pedidos de uma rede de franquias de alimentação regional, desenvolvida em Java 21 com Spring Boot 3.2.5.

---

## Tecnologias

| Tecnologia | Versão |
|---|---|
| Java | 21 (OpenJDK) |
| Spring Boot | 3.2.5 |
| Spring Security + JWT | jjwt 0.12.5 |
| Spring Data JPA + Flyway | — |
| H2 (demo) / PostgreSQL (produção) | — |
| Swagger / OpenAPI | springdoc-openapi 2.5.0 |
| Maven | 3.9.6+ |

---

## Pré-requisitos

- Java 21+ instalado e configurado no `JAVA_HOME`
- Maven 3.9.6+ no `PATH` (ou usar o wrapper `./mvnw`)
- **Nenhum banco de dados necessário no modo demo** (usa H2 em memória)

---

## Como executar — Modo Demo (H2, sem PostgreSQL)

```bash
# Clone o repositório
git clone <URL_DO_REPOSITORIO>
cd 4710499_raizes

# Execute a aplicação
mvn spring-boot:run
```

A aplicação sobe em **3–5 segundos** na porta `8080`.

---

## Como executar — Modo Produção (PostgreSQL)

1. Instale o PostgreSQL e crie o banco `raizes_db`
2. Edite `src/main/resources/application.properties`:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/raizes_db
spring.datasource.username=postgres
spring.datasource.password=sua_senha
spring.datasource.driver-class-name=org.postgresql.Driver
spring.jpa.database-platform=org.hibernate.dialect.PostgreSQLDialect
```

3. No `pom.xml`, substitua a dependência H2 pela do PostgreSQL:

```xml
<dependency>
    <groupId>org.postgresql</groupId>
    <artifactId>postgresql</artifactId>
    <scope>runtime</scope>
</dependency>
```

4. Execute: `mvn spring-boot:run`

---

## URLs de acesso

| Recurso | URL |
|---|---|
| Swagger UI (documentação interativa) | http://localhost:8080/swagger-ui.html |
| H2 Console (somente modo demo) | http://localhost:8080/h2-console |
| Especificação OpenAPI (JSON) | http://localhost:8080/api-docs |

**H2 Console — configurações de conexão:**
- JDBC URL: `jdbc:h2:mem:raizes_db`
- User: `sa`
- Password: *(vazio)*

---

## Credenciais padrão (seed)

| Email | Senha | Perfil |
|---|---|---|
| `admin@raizes.com` | `raizes4710499` | ADMIN |

Ou registre um novo usuário em `POST /autenticacao/registro`.

---

## Fluxo principal de teste

```
1. POST /autenticacao/login           → obtém token JWT
2. POST /pedidos                      → cria pedido (status: AGUARDANDO_PAGAMENTO)
3. POST /pagamentos/processar/{id}    → processa pagamento (APROVADO se valor ≤ R$500)
4. GET  /pedidos/{id}                 → confirma status PAGAMENTO_APROVADO
```

---

## Endpoints da API

| Método | Endpoint | Auth | Descrição |
|---|---|---|---|
| POST | `/autenticacao/registro` | ❌ | Registrar usuário |
| POST | `/autenticacao/login` | ❌ | Login + token JWT |
| GET | `/pedidos` | ✅ | Listar pedidos (filtro: `?canalPedido=APP`) |
| POST | `/pedidos` | ✅ | Criar pedido |
| GET | `/pedidos/{id}` | ✅ | Buscar pedido por ID |
| PATCH | `/pedidos/{id}/situacao` | ✅ | Atualizar situação |
| POST | `/pagamentos/processar/{id}` | ✅ | Processar pagamento (mock) |
| GET | `/produtos` | ✅ | Listar produtos |
| GET | `/clientes` | ✅ | Listar clientes |
| GET | `/unidades` | ✅ | Listar unidades |

> **Autenticação:** Header `Authorization: Bearer {token}`

---

## Coleção Postman

Importe o arquivo `/postman/Raizes_Pedro_4710499.postman_collection.json` no Postman.

A coleção contém 13 cenários de teste pré-configurados. A requisição de login salva o token automaticamente na variável `{{token}}`.

---

## Estrutura do projeto

```
4710499_raizes/
├── src/
│   └── main/
│       ├── java/com/nordeste/raizes/
│       │   ├── api/controller/          # Controllers REST
│       │   ├── dominio/modelo/          # Entidades JPA
│       │   ├── dominio/enumeracao/      # Enums de domínio
│       │   └── infraestrutura/          # Segurança, repositórios, pagamento mock
│       └── resources/
│           ├── application.properties   # Configuração H2 (demo)
│           └── db/migration/
│               └── V1__criar_tabelas.sql
├── docs/
│   └── 4710499_Projeto_Back_End.pdf    # Documentação acadêmica
├── postman/
│   └── Raizes_Pedro_4710499.postman_collection.json
├── pom.xml
├── .gitignore
└── README.md
```

---

## Regras de negócio — Gateway de Pagamento Mock

| Valor do pedido | Resultado |
|---|---|
| ≤ R$ 500,00 | APROVADO — pedido vai para PAGAMENTO_APROVADO |
| > R$ 500,00 | RECUSADO — pedido vai para CANCELADO |

---

## Execução manual (sem Java/Maven instalados no PATH)

```powershell
$env:JAVA_HOME = "C:\Users\pedrom\AppData\Local\Programs\PyCharm 2025.2.0.1\jbr"
$env:PATH = "$env:JAVA_HOME\bin;" + $env:PATH
$env:M2_HOME = "C:\Users\pedrom\Downloads\maven\apache-maven-3.9.6"
$env:PATH = "$env:M2_HOME\bin;" + $env:PATH
cd C:\Users\pedrom\Downloads\4710499_raizes
mvn spring-boot:run
```

---

## Documentação acadêmica

Disponível em [`docs/4710499_Projeto_Back_End.pdf`](docs/4710499_Projeto_Back_End.pdf).

---

**Pedro Marques Mesquita** — RU 4710499 — UNINTER — Análise e Desenvolvimento de Sistemas — 2026
