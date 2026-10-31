# 01 — Project Charter (Termo de Abertura do Projeto)

**Padrão:** Alinhado aos princípios de planejamento de projetos de software da **IEEE/ISO/IEC 15288**.  
**Componente Curricular:** Unidade de Extensão: Full Stack (ADS 5P — 2026.2)  
**Instituição:** Faculdade Senac Pernambuco — Recife/PE  
**Docente / Sponsor:** Prof. Fábio Chicout  

---

## 1. Justificativa & Problema de Negócio (Problem Statement)

* **Público-Alvo & Parceiros Validados:** Startups *early stage* de base tecnológica e economia criativa de Recife/PE (validadas nas entrevistas com as startups **Nook** e **Rede Leito** e com os articuladores/mentores **Edil** e **Prof. Genésio**) e **Ambientes de Validação** (pequenas e médias empresas, instituições de ensino/saúde, comunidades e órgãos públicos locais).
* **Diagnóstico da Dor:** Startups *early stage* apontaram literalmente a **validação em ambiente real — e especificamente a realização da Prova de Conceito (POC)** — como sua maior dificuldade para conquistar os primeiros clientes e comprovar tração em editais e programas de fomento. Hoje, tentam prospectar parceiros de teste de forma improvisada e fria (*direct* no Instagram, e-mails, formulários, eventos e grupos de WhatsApp), enquanto empresas e órgãos locais têm receio de expor publicamente suas dores operacionais sem filtro prévio ou proteção de sigilo.
* **Impacto Gerado no Ecossistema Local:** O **Azreal (POC-Match)** substitui a prospecção fria por um canal estruturado de inovação aberta que conecta **Startups (Lado A)** a **Desafios de Validação anônimos (Lado B)**, formaliza um **Termo de Piloto** com proteção de identidade/LGPD e emite um **Registro Oficial de POC** confirmado pelo parceiro após o teste real, gerando evidência verificável de tração.

---

## 2. Objetivos & Critérios de Sucesso do Projeto (SMART)

* **O1 (Fluxo Fim a Fim POC-Match):** Desenvolver e homologar até 27/Nov/2026 uma plataforma web Full Stack (`Angular 22 Zoneless` + `Spring Boot 3.3`) que automatize 100% do ciclo **Desafio Anônimo → Matchmaking (IA + Fallback por Regras) → Proposta Enxuta de POC (máx. 3 abertas) → Termo de Piloto (5 dias úteis PE) → Registro Oficial de POC**.
* **O2 (Segurança, Anonimato em Camadas e LLDAP):** Integrar autenticação RBAC (`ROLE_STARTUP`, `ROLE_AMBIENTE_VALIDACAO`, `ROLE_ADMIN`) com o servidor institucional `lldap.fchicout.dev` (`Service Account: azrael_app`, grupo `azrael_admin`) e garantir isolamento em camadas (`RNF04`), impedindo qualquer exposição da razão social ou contatos do Ambiente de Validação antes do aceite do Termo de Piloto.
* **O3 (Qualidade & DevSecOps):** Manter pipeline CI/CD automatizado no GitHub Actions com aprovação obrigatória no Quality Gate do SonarQube (`azreal-backend` e `azreal-frontend`), cobertura de testes unitários &ge; 80%, Checkstyle, ArchUnit, Semgrep, Gitleaks e Grype sem vulnerabilidades críticas.
* **O4 (Governança Documental & SemVer):** Manter 100% da documentação de engenharia (`docs/`) e contrato OpenAPI 3 (`Swagger UI`) atualizados e releases versionadas segundo **SemVer 2.0.0** (`v0.1.0-alpha`, `v0.5.0-beta`, `v1.0.0`).

---

## 3. Escopo do Projeto (Scope Baseline & Demarcação Pedagógica)

### ✅ Dentro do Escopo (In-Scope — Azreal POC-Match)
- Cadastro de **Startups (Lado A)** com estágio do protótipo (`PROTOTIPO_INICIAL`, `MVP_FUNCIONAL`, `EM_TESTE`), localidade IBGE, o que precisa validar, temas do vocabulário e modalidades suportadas (`RF01`, `RF02`).
- Cadastro de **Ambientes de Validação (Lado B)** e publicação de **Desafios de Validação** exibidos de forma **anônima** para startups até o aceite do piloto (`RF03`, `RF05`, `RF06`).
- **Matchmaking de Demanda ↔ Solução** com pré-filtro objetivo + ordenação semântica por IA explicando o motivo em 1 frase curta (sem pontuação percentual) + **Fallback automático por regras e template** (`RF10`, `RF11`).
- Submissão de **Proposta Enxuta de POC** com aceite prévio do Termo de Piloto e Cláusula de Sigilo, respeitando o limite de até **3 propostas pendentes simultâneas** por startup (`RF12`, `RF13`).
- Fila de avaliação do Ambiente de Validação com prazo de **5 dias úteis (calendário de Pernambuco)**, congelamento de escopo/métrica no **Termo de Piloto** (`Status: EM_TESTE`) e liberação progressiva da identidade e contatos (`RF14`, `RF15`, `RF16`).
- Registro de entrega pela startup, confirmação e avaliação de métrica pelo Ambiente e emissão do **Registro Oficial de POC** (nominal ou anônimo qualificado por setor e porte) + avaliação da qualidade do match (`RF18`, `RF19`, `RF20`, `RF21`).

### ❌ Fora do Escopo (Out-of-Scope — Diferenciação Estrita na Turma)
- **Não replica o *DaMatch*:** Não realiza matchmaking startup–mentor, agendamento de sessões de mentoria nem registro de atas de mentoria.
- **Não replica o *Percentangel*:** Não conecta startups a investidores-anjo nem utiliza sistema de pontuação ponderada com soma 100% ou *scorecards* numéricos.
- Intermediação financeira, assinatura jurídica ICP-Brasil complexa ou aplicativos móveis nativos.

---

## 4. Partes Interessadas (Stakeholders) e Equipe Oficial

| Papel | Nome / Identificador | Responsabilidade no Projeto Azreal |
| :--- | :--- | :--- |
| **Product Owner & Business Analyst** | **Ágata Maria Ferraz de Oliveira** (`@HtaOliva` / `agata.oliveira`) | Gestão do backlog POC-Match, entrevistas de validação (*Nook*, *Rede Leito*), documentação oficial em `docs/` e critérios de aceite. |
| **Tech Lead Front-End (Angular 22)** | **Alanna Beatriz Bezerra dos Santos** (`alanna.santos`) | Arquitetura SPA Angular 22 Zoneless, acessibilidade WCAG 2.1 AA, telas do fluxo POC-Match e testes Vitest. |
| **Scrum Master & DevOps Lead** | **Arthur Cézar Félix da Cunha** (`arthur.cunha`) | Governança GitFlow, versionamento SemVer 2.0.0, labels do GitHub, pipeline CI/CD, integração LLDAP e SpringDoc OpenAPI. |
| **Software Engineer (Back-End REST)** | **Cesar Kauan Barbosa Silva** (`cesar.silva`) | Implementação das APIs Spring Boot 3.3, regras de negócio de Desafios, Matchmaking IA/Fallback, Propostas de POC e Termo de Piloto. |
| **Database Engineer & QA Lead** | **Giselly Luiza Rocha de Lima** (`@gixweb` / `giselly.lima`) | Modelagem relacional PostgreSQL, migrations **Flyway**, suíte de testes JUnit 5 / JaCoCo / ArchUnit e Quality Gate no SonarQube. |
| **Docente / Avaliador** | **Prof. Fábio Chicout** | Governança acadêmica, auditoria técnica de engenharia de software e avaliação das releases. |

---

## 5. Cronograma de Marcos e Sprints Finais (Milestones)

| Marco / Sprint | Prazo | Entregável Principal |
| :---: | :---: | :--- |
| **Baseline & Auditoria** | 30/Out/2026 | Parecer Técnico de Auditoria, consolidação do escopo oficial POC-Match e tag `v0.1.0-alpha`. |
| **Sprint 1 (Backend)** | 02/Nov a 08/Nov/2026 | Desacoplamento total de mentoria (`#27`), Flyway `V1` (`#29`), LLDAP (`#37`), Swagger UI (`#39`), perfis `Startup` e `AmbienteValidacao` (`RF01`–`RF04`, `RF07`–`RF09`). |
| **Sprint 2 (Backend)** | 09/Nov a 15/Nov/2026 | CRUD de `DesafioValidacao` (`RF05`), DTO Anônimo em camadas (`RF06`, `RNF04`) e Motor de Matchmaking IA + Fallback por Regras (`RF10`, `RF11`). |
| **Sprint 3 (Backend)** | 16/Nov a 22/Nov/2026 | `PropostaPoc` (limite máx. 3 abertas — `RF12`, `RF13`), Fila com 5 dias úteis de PE (`RF14`, `RF16`) e `TermoPiloto` congelado com revelação de identidade (`RF15`) — Release `v0.5.0-beta`. |
| **Sprint 4 & Marco M6** | 23/Nov a 30/Nov/2026 | Entrega do piloto (`RF18`), confirmação pelo Ambiente (`RF19`), emissão do `RegistroOficialPoc` (`RF20`), avaliação do match (`RF21`), auditoria (`RNF08`) e Release `v1.0.0` (Pitch Final em 27/Nov/2026). |

---

## 6. Riscos & Plano de Mitigação

| Risco Identificado | Severidade | Ação Preventiva / Mitigação |
| :--- | :---: | :--- |
| Indisponibilidade ou *timeout* da API externa de IA durante o matchmaking (`RF10`). | Alta | Implementação obrigatória de *timeout* curto e acionamento automático do **Fallback por Regras e Template** (`RF11`, `RNF03`). |
| Vazamento acidental de razão social ou contatos do Ambiente de Validação antes do aceite (`RNF04`). | Crítica | Uso de DTO segregado (`DesafioAnonimoResponseDTO`) nas rotas de descoberta e testes automatizados verificando ausência de campos sensíveis no JSON. |
| Reprovação no Quality Gate do SonarQube ou bloqueio de Pull Request. | Alta | Execução local de `mvn clean verify` (Checkstyle + ArchUnit + JUnit 5 + JaCoCo) e PRs curtos e atômicos antes de cada merge em `development`. |
