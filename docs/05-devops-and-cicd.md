# 05 — DevOps, Pipeline CI/CD, GitFlow & Gestão de Qualidade — Azreal (POC-Match)

**Padrão:** Governança de Engenharia de Software da Unidade de Extensão Full Stack (**GitHub Actions + SonarQube + SemVer 2.0.0 + Conventional Commits**).  
**Responsável DevOps / Scrum Master:** Arthur Cézar Félix da Cunha (`@arthur-cunha`)  
**Responsável QA / Database (Flyway & SonarQube):** Giselly Luiza Rocha de Lima (`@gixweb`)

---

## 1. Estratégia de Ramificação (GitFlow Institucional)

```
main (Produção Estável — Releases taggeadas: v0.1.0-alpha, v0.5.0-beta, v1.0.0)
  ▲
  │ (Pull Request aprovado + Quality Gate SonarQube Verde + OWASP ZAP DAST)
development (Integração Contínua & Homologação Staging + Nuclei DAST)
  ▲
  ├── feature/sprint1-fundacao-poc-match
  ├── feature/sprint2-desafios-matchmaking-ia
  ├── feature/sprint3-propostas-termo-piloto
  └── feature/sprint4-registro-poc-auditoria
```

* **Normas de Governança de Código e Pull Requests:**
  1. **Proibido "Big Bang Pull Request":** Cada branch `feature/*` ou `bugfix/*` deve focar em um incremento coeso de Sprint/Issue, mantendo Pull Requests enxutos e revisáveis.
  2. **Conventional Commits Obrigatórios:** Todo commit deve ser atômico e seguir o padrão `feat(escopo): ...`, `fix(escopo): ...`, `refactor(escopo): ...`, `docs(escopo): ...`, `test(escopo): ...` ou `chore(escopo): ...` referenciando a Issue ou Requisito correspondente.
  3. **Quality Gate Bloqueante:** Nenhum Pull Request pode sofrer merge em `development` ou `main` se qualquer check falhar (Checkstyle, ESLint, JUnit 5/JaCoCo, Vitest, ArchUnit, Semgrep, Gitleaks, Syft/Grype ou SonarQube `azreal-backend` / `azreal-frontend`).

---

## 2. Matriz Oficial de 23 Labels em 5 Dimensões (`Issue #26`)

1. **Tipo (`type:`):** `type:architecture` (`#5319e7`), `type:feature` (`#0e8a16`), `type:bug` (`#d73a4a`), `type:chore` (`#fef2c0`), `type:docs` (`#0075ca`), `type:test` (`#bfdadc`).
2. **Escopo (`scope:`):** `scope:auth` (`#fbca04`), `scope:catalog` (`#1d76db`), `scope:search` (`#c2e0c6`), `scope:negotiation` (`#d4c5f9`), `scope:rating` (`#bfd4f2`), `scope:dashboard` (`#f9d0c4`), `scope:devops` (`#0052cc`).
3. **Camada Técnica (`stack:`):** `stack:backend` (`#006b75`), `stack:frontend` (`#e99695`), `stack:database` (`#c5def5`).
4. **Prioridade (`priority:`):** `priority:critical` (`#b60205`), `priority:high` (`#e11d21`), `priority:medium` (`#fbca04`), `priority:low` (`#d4c5f9`).
5. **Status do Fluxo (`status:`):** `status:ready` (`#0e8a16`), `status:in-progress` (`#fef2c0`), `status:blocked` (`#d93f0b`).

---

## 3. Padrão de Versionamento Semântico (SemVer 2.0.0 — `Issue #25`)

Os manifestos [pom.xml](../pom.xml), [backend/pom.xml](../backend/pom.xml) e [frontend/package.json](../frontend/package.json) permanecem sincronizados com as tags Git anotadas:

* **`v0.1.0-alpha` (Sprint 1):** Saneamento de escopo (`#27`), documentação oficial completa sem placeholders (`#28`), migrations Flyway `V1` (`#29`), autenticação LLDAP/RBAC (`#37`), Swagger UI (`#39`) e perfis `Startup` / `AmbienteValidacao` (`RF01`–`RF04`, `RF07`–`RF09`).
* **`v0.5.0-beta` (Sprints 2 e 3):** Publicação anônima de Desafios de Validação (`RF05`, `RF06`), Matchmaking IA + Fallback por Regras (`RF10`, `RF11`), Proposta Enxuta de POC (limite máx. 3) e geração do Termo de Piloto congelado (`RF12`–`RF17`).
* **`v1.0.0` (Sprint 4 — Release Final 27/11/2026):** Registro de entrega, confirmação pelo Ambiente, emissão do Registro Oficial de POC verificável, avaliação do matchmaking e trilha imutável de auditoria (`RF18`–`RF21`, `RNF08`).
