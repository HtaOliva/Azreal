# 00 — Architecture Frontier & Decisões Arquiteturais (ADR)

**Projeto:** Azreal (POC-Match) — Plataforma de Matchmaking de Validação e Provas de Conceito (POC)  
**Unidade Curricular:** Unidade de Extensão: Full Stack (ADS 5P — 2026.2) — Faculdade Senac Pernambuco  
**Docente / Avaliador:** Prof. Fábio Chicout  
**Equipe Grupo 2 (Azreal):**  
- **Ágata Maria Ferraz de Oliveira** (`agata.oliveira`) — Product Owner & Business Analyst  
- **Alanna Beatriz Bezerra dos Santos** (`alanna.santos`) — Tech Lead Front-End (Angular 22)  
- **Arthur Cézar Félix da Cunha** (`arthur.cunha`) — Scrum Master & DevOps Lead (GitFlow & CI/CD)  
- **Cesar Kauan Barbosa Silva** (`cesar.silva`) — Software Engineer (Back-End REST & LLDAP Security)  
- **Giselly Luiza Rocha de Lima** (`giselly.lima`) — Database Engineer & QA Lead (Flyway & Testes)  

**Públicos Validados em Campo:** Startups *early stage* de Recife/PE (*Nook*, *Rede Leito*) e Ambientes de Validação parceiros (empresas, instituições de ensino/saúde e órgãos públicos locais).

---

## 1. Visão Geral da Arquitetura (System Context — C4 Model)

O **Azreal (POC-Match)** conecta **Startups *early stage* (Lado A)** a **Ambientes de Validação (Lado B)** com demandas operacionais reais para realizar **Provas de Conceito (POCs)** estruturadas, protegidas por anonimato em camadas até o aceite do **Termo de Piloto** e culminando na emissão de um **Registro Oficial de POC** verificável.

```mermaid
flowchart TD
    subgraph Client ["Frontend: Single Page Application (SPA)"]
        UI ["Angular 22 (100% Zoneless, Signals, OnPush, Vitest)"]
    end

    subgraph Gateway ["Camada de Borda / Segurança RBAC"]
        AuthGuard ["Auth Interceptor / Route Guards (ROLE_STARTUP, ROLE_AMBIENTE_VALIDACAO, ROLE_ADMIN)"]
    end

    subgraph Backend ["Backend: API REST Enterprise (azreal-backend)"]
        Spring ["Spring Boot 3.3 (Java 21 LTS, Validation, SpringDoc OpenAPI 3)"]
        MatchEngine ["Matchmaking Engine (Filtro Determinístico + Cliente IA + Fallback por Regras)"]
        LLDAPClient ["Spring Security LDAP / JWT Provider"]
    end

    subgraph External ["Serviços Externos & Infraestrutura Institucional"]
        Postgres [("PostgreSQL + Flyway Migrations (db/migration/V1..V4)")]
        LLDAPServer [("Servidor LLDAP Institucional (lldap.fchicout.dev — azrael_admin / azrael_app)")]
        LLM ["API Externa de IA (Ordenação Semântica Anonimizada c/ Timeout Seguro)"]
    end

    UI -->|HTTPS / REST JSON| Gateway
    Gateway -->|Bearer JWT / Basic| Spring
    Spring --> MatchEngine
    MatchEngine -->|Textos Sanitizados s/ Dados Pessoais| LLM
    Spring -->|Bind & Group Lookup| LLDAPClient
    LLDAPClient <-->|LDAP Protocol| LLDAPServer
    Spring <-->|Spring Data JPA + Flyway| Postgres
```

---

## 2. Matriz Tecnológica Obrigatória (Tech Stack)

| Camada | Tecnologia Adotada | Justificativa Técnica no Azreal (POC-Match) |
| :--- | :--- | :--- |
| **Front-End** | **Angular 22 (Zoneless + Signals)** | Reatividade moderna sem `zone.js`, componentes standalone, tipagem estrita e testes com Vitest. |
| **Back-End** | **Java 21 LTS + Spring Boot 3.3** | API REST robusta, validação declarativa (`jakarta.validation`), isolamento arquitetural verificado com **ArchUnit** e estilo validado por **Checkstyle**. |
| **Persistência & Schema** | **Spring Data JPA + PostgreSQL + Flyway** | Persistência relacional transacional para o ciclo `Desafio -> PropostaPoc -> TermoPiloto -> RegistroOficialPoc`, com versionamento determinístico via **Flyway** (`db/migration`). |
| **Autenticação & RBAC** | **Spring Security + LLDAP (`lldap.fchicout.dev`)** | Autenticação integrada ao diretório LLDAP institucional (`Service Account: azrael_app`, grupo `azrael_admin` [ID: 12]) e segregação estrita entre `STARTUP`, `AMBIENTE_VALIDACAO` e `ADMIN`. |
| **Documentação API** | **SpringDoc OpenAPI 3 + Swagger UI** | Geração automática e interativa do contrato OpenAPI 3.0.3 em `/swagger-ui.html` e `/v3/api-docs`. |
| **Qualidade & DevSecOps** | **GitHub Actions + SonarQube + Semgrep + Gitleaks + Grype** | Pipeline completo com Quality Gate institucional (`sonar.fchicout.dev`), cobertura JaCoCo &ge; 80%, SAST, detecção de segredos e análise de vulnerabilidades (SBOM). |

---

## 3. Architecture Decision Records (ADR)

### 📄 ADR-001: Privacidade e Anonimato em Camadas via DTOs Segregados (`RNF04`)
* **Status:** Aprovado.
* **Contexto:** Organizações parceiras (Ambientes de Validação) precisam publicar dores operacionais reais sem expor sua razão social, CNPJ ou contatos antes de aprovarem uma proposta de piloto, evitando abordagens frias fora da plataforma.
* **Decisão:** Segregar estritamente os contratos de saída no backend: rotas voltadas a `STARTUP` na fase de descoberta (`GET /desafios`, `GET /startups/{id}/desafios-recomendados`) utilizam exclusivamente `DesafioAnonimoResponseDTO` (exibindo apenas problema, setor, porte, localidade IBGE, modalidade, ambiente de teste e restrições LGPD). Os dados nominais e contatos só são trafegados no `TermoPilotoResponseDTO` após a transição da proposta para `Status: EM_TESTE` (`RF15`).
* **Consequências:** Impede vazamento acidental de identidade no JSON da API; auditado por testes automatizados de serialização.

### 📄 ADR-002: Matchmaking Híbrido com IA Semântica, Sanitização Anti-Prompt Injection e Fallback Determinístico (`RF10`, `RF11`, `RNF03`, `RNF06`)
* **Status:** Aprovado.
* **Contexto:** O matchmaking entre soluções de startups e desafios reais deve explicar em uma frase curta o motivo da compatibilidade (sem usar *scorecard* percentual de 0 a 100%, diferenciando-se do projeto *Percentangel*), mas não pode parar de funcionar caso a API externa de IA sofra *timeout* ou indisponibilidade.
* **Decisão:** Implementar o `MatchmakingService` em duas fases: (1) pré-filtro determinístico por modalidade, localidade e restrições LGPD; (2) ordenação via API de IA com delimitadores estritos de dados de entrada (*anti-prompt injection* `RNF06`) e sem envio de dados pessoais (`RNF05`). Caso ocorra falha ou *timeout*, o sistema aciona automaticamente o ordenador determinístico por regras (setor, temas do vocabulário `RF08` e modalidade/município) com frase justificativa gerada por template.
* **Consequências:** Alta resiliência operacional (`RNF03`) e rastreabilidade da eficácia da IA frente às regras (`RF21`).

### 📄 ADR-003: Gestão de Identidades via LLDAP Institucional e Versionamento de Banco com Flyway
* **Status:** Aprovado.
* **Contexto:** Atender às normas de governança da Unidade de Extensão Full Stack (Issues `#29` e `#37`).
* **Decisão:** Utilizar o servidor institucional `lldap.fchicout.dev` para bind LDAP e mapeamento do grupo `azrael_admin` para `ROLE_ADMIN`, mantendo suporte de fallback em memória via `ObjectProvider` para execução rápida e isolada da suíte de testes unitários JUnit 5, e versionar o schema relacional PostgreSQL com **Flyway**.
