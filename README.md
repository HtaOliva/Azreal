<div align="center">

# 🪽 Azreal

### Sua trajetória com orientação

Plataforma de orientação para startups em fase inicial, centrada em mentoria.

Projeto da **Unidade de Extensão Fullstack** — SENAC 2026.2
Docente: **Prof. Fábio Chicout**

`Spring Boot 3` · `Angular 22 Zoneless`

[![CI/CD](https://github.com/HtaOliva/Azreal/actions/workflows/ci-cd.yml/badge.svg)](https://github.com/HtaOliva/Azreal/actions/workflows/ci-cd.yml)
[![Quality Gate - Backend](https://sonar.fchicout.dev/api/project_badges/measure?project=azreal-backend&metric=alert_status)](https://sonar.fchicout.dev/dashboard?id=azreal-backend)
[![Quality Gate - Frontend](https://sonar.fchicout.dev/api/project_badges/measure?project=azreal-frontend&metric=alert_status)](https://sonar.fchicout.dev/dashboard?id=azreal-frontend)

![Java](https://img.shields.io/badge/Java-21_LTS-ED8B00?logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.3-6DB33F?logo=springboot&logoColor=white)
![Angular](https://img.shields.io/badge/Angular-22-DD0031?logo=angular&logoColor=white)
![SonarQube](https://img.shields.io/badge/SonarQube-Quality_Gate-4E9BCD?logo=sonarqube&logoColor=white)
![GitHub Actions](https://img.shields.io/badge/CI%2FCD-GitHub_Actions-2088FF?logo=githubactions&logoColor=white)

### 🌐 [Ver o site no ar](https://htaoliva.github.io/Azreal/) · 🔍 [Painel no SonarQube](https://sonar.fchicout.dev)

</div>

---

## 📑 Índice

- [Sobre o projeto](#-sobre-o-projeto)
  - [Contexto e problema](#contexto-e-problema)
  - [Público-alvo](#público-alvo)
  - [Necessidades e comportamento atual](#necessidades-e-comportamento-atual)
  - [Evidências e validação](#evidências-e-validação)
  - [Direção da solução](#direção-da-solução)
  - [Próximos passos](#próximos-passos)
  - [Equipe](#equipe)
- [Visão geral técnica](#️-visão-geral-técnica)
- [Pipeline de CI/CD](#-pipeline-de-cicd)
- [Política de branches](#-política-de-branches)
- [Configuração: SonarQube e Secrets](#️-configuração-sonarqube-e-secrets)
- [Rodando localmente](#-rodando-localmente)

---

## 🧭 Sobre o projeto

O **Azreal** ajuda startups em fase inicial a registrar sua situação, conhecer os tipos de apoio disponíveis, pedir orientação a mentores e manter o histórico das orientações e dos apoios buscados.

### Contexto e problema

Startups *early stage* em incubação tomam várias decisões ao longo da trajetória, como validar a ideia, desenvolver o produto, buscar mentoria e participar de editais.

**O que foi observado:** as duas startups entrevistadas esperavam um cronograma ou acompanhamento estruturado durante a incubação. Como não o receberam da forma esperada, buscaram direcionamento por conta própria e recorreram a mentores para dúvidas pontuais.

**Oportunidade:** oferecer orientação e acompanhamento mais estruturados a startups *early stage*, reunindo a trajetória da startup, os apoios disponíveis e as recomendações de mentores.

<details>
<summary><b>Possíveis causas e consequências</b></summary>

**Possíveis causas (ainda sem confirmação):**

- Ausência de cronograma ou de canal estruturado de acompanhamento.
- Desalinhamento entre o que as startups esperavam e o que a incubação oferece.
- Dificuldade das startups em identificar o próprio estágio ou os tipos de apoio.

> Ainda não se sabe qual dessas causas pesa mais.

**Consequências observadas:** frustração relatada e necessidade de buscar direcionamento por conta própria.

**Consequências sem evidência:** perda financeira, perda de oportunidades, atraso no desenvolvimento, abandono de startups, dificuldade de investimento, redução de faturamento e desperdício de recursos **não foram demonstrados**.

</details>

### Público-alvo

| | Descrição |
| --- | --- |
| **Segmento** | Startups *early stage*, sem receita recorrente relevante, que citam prova de conceito como elemento importante, especialmente no contexto de incubação. |
| **Usuário principal** | Fundadores ou integrantes dessas startups, com necessidade relatada diretamente. |
| **Público relacionado** | Mentores, usados pelas startups para dúvidas pontuais. A disposição, a disponibilidade e as necessidades dos mentores ainda não foram investigadas. |

**Características identificadas:** autodescrição como *early stage*; prova de conceito como elemento relevante; participação em incubação, editais, mentoria e eventos.

### Necessidades e comportamento atual

| Dores | Necessidades | Comportamento atual |
| --- | --- | --- |
| Expectativa de acompanhamento estruturado não atendida, com frustração *(relatado)* | Acompanhamento ou cronograma estruturado durante a incubação *(relatada diretamente)* | Buscam informações e direcionamento por conta própria |
| Necessidade de buscar informações e direcionamento por conta própria *(relatado)* | Direcionamento no momento em que o apoio é necessário *(inferida)* | Participam de incubação, editais, mentoria e eventos |
| Uso de mentores para dúvidas específicas *(relatado)* | Registro da trajetória, acesso a mentores e organização dos apoios buscados *(propostas do grupo, não relatadas pelas startups)* | Recorrem a mentores para dúvidas pontuais |

**Possíveis dificuldades (hipóteses):** falta de sequência clara, busca descentralizada, orientações pontuais sem histórico e dificuldade de relacionar estágio e apoio.

**Resultado desejado, compatível com a proposta:** maior visibilidade da startup sobre a própria trajetória, menor dependência de buscar informações dispersas e orientações e apoios buscados reunidos em um histórico.

### Evidências e validação

Hipóteses investigadas nas entrevistas:

| Hipótese | Situação |
| --- | --- |
| As startups têm dificuldade para identificar o próximo apoio | Evidência parcial |
| A origem do problema está na falta de acompanhamento, na falta de informação ou na falta de clareza sobre o próprio estágio | Não distinguido |
| Existe um modelo de estágios, com um apoio para cada estágio | Perspectiva de um mentor |
| Cerca de 90% das startups têm desalinhamento entre estágio e apoio | Percepção de um mentor |
| O investidor anjo tem papel residual e o edital o substituiria nas fases iniciais | Percepção de um mentor |
| Startups buscam investimento antes da hora | Percepção de um participante |

### Direção da solução

**Ideia inicial:** plataforma de orientação para startups em fase inicial, centrada em mentoria.

**Valor que pretende gerar:**

- Histórico da trajetória da startup
- Solicitação de orientação
- Registro de recomendações
- Encaminhamento para mentor

**Funcionalidades principais:**

- Organizar a trajetória da startup e registrar o momento em que ela está
- Apresentar e consultar os tipos de apoio disponíveis
- Marcar os apoios já buscados
- Possibilitar pedidos de orientação a mentores
- Manter o histórico de orientações e de apoios buscados

```mermaid
flowchart LR
    A([Startup registra<br/>o momento da trajetória]) --> B[Consulta os<br/>tipos de apoio]
    B --> C[Marca os apoios<br/>já buscados]
    B --> D[Pede orientação<br/>a um mentor]
    D --> E[Mentor registra<br/>recomendações]
    C --> F[(Histórico de orientações<br/>e apoios buscados)]
    E --> F
```

### Próximos passos

1. Validar com mais startups se o problema se repete.
2. Investigar se a principal dificuldade está na ausência de acompanhamento estruturado.
3. Ouvir incubadoras e instituições.
4. Validar com outros mentores.
5. Testar se a orientação resolve uma necessidade percebida.
6. Validar as regras de correspondência entre estágio e apoio antes de tratá-las como regra do produto.

### Equipe

**Grupo 2 – Azrael:** Alanna Santos, Giselly Lima (representante), Ágata Oliveira, César Silva e Arthur Cunha.

---

## 🏛️ Visão geral técnica

Scaffolding padronizado para os projetos da Unidade de Extensão Fullstack.

| Camada | Tecnologias |
| --- | --- |
| **Backend** | Java 21 LTS, Spring Boot 3.3, Spring Web, Spring Validation, Actuator, ArchUnit, Checkstyle, JaCoCo |
| **Frontend** | Angular 22 (100% Zoneless), Signals, OnPush, Vitest, ESLint, build com esbuild/Vite |
| **Qualidade** | SonarQube institucional com o padrão "Sonar way" |
| **Segurança** | Semgrep (SAST), Gitleaks (segredos), Syft + Grype (supply chain), Nuclei e OWASP ZAP (DAST) |
| **Entrega** | GitHub Actions, com deploy do frontend no GitHub Pages |

---

## 🔄 Pipeline de CI/CD

Todo push e todo pull request passam pelas mesmas etapas. Se qualquer *gate* falhar, nada é publicado.

```mermaid
flowchart LR
    A([Push ou PR]) --> B[Lint e Testes]
    B --> C[Semgrep<br/>SAST]
    B --> D[Gitleaks<br/>Segredos]
    B --> E[Syft + Grype<br/>Supply Chain]
    C --> F{{SonarQube<br/>Quality Gate}}
    D --> F
    E --> F
    F -->|development| G[Nuclei<br/>DAST] --> H([Deploy Staging])
    F -->|main| I[OWASP ZAP<br/>DAST] --> J([Deploy Produção<br/>GitHub Pages])
```

| Etapa | Ferramenta | Bloqueia o pipeline? |
| --- | --- | :---: |
| Lint (Java e Angular) | Checkstyle, ESLint | ✅ |
| Testes e cobertura | JUnit 5, JaCoCo, Vitest | ✅ |
| SAST | Semgrep (OWASP Top 10, Java, TypeScript) | ✅ erros de severidade alta |
| Segredos vazados | Gitleaks | ✅ |
| Supply chain | Syft (SBOM SPDX) + Grype (CVEs) | ✅ vulnerabilidades críticas |
| Qualidade de código | SonarQube (backend e frontend) | ✅ |
| DAST em staging | Nuclei | ⚠️ apenas informativo |
| DAST em produção | OWASP ZAP Baseline | ⚠️ apenas informativo |

---

## 🌳 Política de branches

| Branch | Função | O que o push dispara |
| --- | --- | --- |
| `development` | Integração ativa | Todos os gates, DAST com **Nuclei** e deploy em **Staging** |
| `main` | Versão estável | Todos os gates, DAST com **OWASP ZAP** e deploy em **Produção** |
| `feature/*` · `bugfix/*` | Trabalho individual ligado a Issues | Integradas via Pull Request em `development` |

O passo a passo completo do fluxo está no [GITHUB_SETUP_GUIDE.md](GITHUB_SETUP_GUIDE.md).

---

## ⚙️ Configuração: SonarQube e Secrets

Faça isto antes de abrir o primeiro Pull Request.

<details>
<summary><b>1. Criar o projeto no SonarQube institucional</b></summary>

1. Acesse <https://sonar.fchicout.dev> com as credenciais fornecidas pelo professor.
2. Clique em **Projects → Create Project → Manually**.
3. Preencha:
   - **Project display name:** nome do projeto (ex.: `azreal-backend`).
   - **Project key:** identificador único (ex.: `azreal-backend`).
     Deve ser **idêntico** ao `<sonar.projectKey>` do `pom.xml` da raiz.
   - **Main branch name:** `main`.
4. Gere o token em **My Account → Security → Generate Tokens**:
   - **Type:** `Project Analysis Token`
   - **Project:** o projeto criado
   - **Expires in:** escolha um prazo longo, para o pipeline não quebrar de repente.
5. Copie o token na hora, pois ele só aparece uma vez.

> Cada token vale para um projeto só. O frontend usa um projeto e um token próprios.

</details>

<details>
<summary><b>2. Cadastrar os Secrets no GitHub</b></summary>

Em **Settings → Secrets and variables → Actions → New repository secret**:

| Secret | Valor | Para quê |
| --- | --- | --- |
| `SONAR_TOKEN` *(obrigatório)* | Token do projeto do backend | Autentica a análise do backend |
| `AZREAL_FRONTEND` *(obrigatório)* | Token do projeto do frontend | Autentica a análise do frontend |
| `SONAR_HOST_URL` *(opcional)* | `https://sonar.fchicout.dev` | Endereço do SonarQube |

Depois, em **Settings → Actions → General → Workflow permissions**, escolha **Read and write permissions**.

</details>

<details>
<summary><b>3. Ativar o GitHub Pages</b></summary>

Em **Settings → Pages**, deixe **Source** como **GitHub Actions**. Não use os modelos sugeridos (Jekyll ou Static HTML), porque o `ci-cd.yml` já faz a publicação.

</details>

<details>
<summary><b>4. Proteger as branches</b></summary>

Em **Settings → Branches → Add branch protection rule**, para `development` e `main`:

- ✅ Require a pull request before merging (mínimo de 1 aprovação)
- ✅ Require status checks to pass before merging:
  - `🧪 Lint & Automated Tests`
  - `🔒 SAST - Semgrep Security Audit`
  - `📦 Supply Chain - Syft (SBOM) & Grype (CVEs)`
  - `🛡️ SonarQube Quality Gate`
  - `🛡️ SonarQube Quality Gate (Frontend)`
- ✅ Require branches to be up to date before merging

</details>

---

## 💻 Rodando localmente

Valide tudo antes de abrir o Pull Request.

### Backend · Spring Boot 3 + Java 21

```bash
# Checkstyle, testes JUnit 5 e relatório JaCoCo
mvn -B checkstyle:check test jacoco:report -f backend/pom.xml

# Subir a aplicação
mvn clean spring-boot:run -f backend/pom.xml
```

| Recurso | Endereço |
| --- | --- |
| Boas-vindas | <http://localhost:8080/api/v1/hello> |
| Healthcheck | <http://localhost:8080/api/v1/health> |
| Relatório JaCoCo | `backend/target/site/jacoco/index.html` |

### Frontend · Angular 22 Zoneless

```bash
cd frontend

npm ci                  # instalação limpa
npm run lint            # ESLint
npm run test:coverage   # testes com Vitest
npm run build           # build de produção com esbuild
npm start               # servidor local
```

Aplicação web: <http://localhost:4200>

---

<div align="center">

Feito com ☕ pela **Equipe Azrael** · SENAC 2026.2

</div>
