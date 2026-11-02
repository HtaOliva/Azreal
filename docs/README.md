# 📚 Suite de Documentação de Engenharia de Software — Projeto de Extensão Full Stack

Esta pasta `docs/` reúne os templates padronizados para guiar as equipes da disciplina **Unidade de Extensão: Full Stack (ADS 5P - 2026.2)** desde a concepção com o parceiro real até o deploy em produção.

---

## 🧭 Ordem Recomendada de Preenchimento & Leitura

1. 📄 [`01-project-charter.md`](01-project-charter.md) — **O Porquê:** Justificativa da dor da ONG parceira, escopo in/out, stakeholders e metas SMART. *(Entrega 1)*
2. 📄 [`04-user-stories.md`](04-user-stories.md) — **O Backlog:** As 5 primeiras User Stories no padrão INVEST com critérios Given/When/Then. *(Entrega 2)*
3. 📄 [`00-architecture-frontier.md`](00-architecture-frontier.md) — **A Arquitetura:** Diagramas C4 de contexto e decisões técnicas (ADRs).
4. 📄 [`02-software-requirements-specification.md`](02-software-requirements-specification.md) — **A Especificação:** Requisitos funcionais `REQ-*` e não funcionais `NFR-*` (IEEE 29148).
5. 📄 [`03-use-cases.md`](03-use-cases.md) — **Os Fluxos:** Casos de uso detalhados com fluxos alternativos e exceções (Cockburn).
6. 📄 [`05-devops-and-cicd.md`](05-devops-and-cicd.md) — **A Esteira:** Git Flow, CI/CD GitHub Actions, SonarQube Quality Gate e SemVer.
7. 📄 [`06-ui-ux-screen-descriptions.md`](06-ui-ux-screen-descriptions.md) — **A Interface:** Wireframes estruturais, mapa de telas e acessibilidade WCAG.
8. 📄 [`07-ui-ux-walkthrough-roadmap.md`](07-ui-ux-walkthrough-roadmap.md) — **A Validação:** Checklist de aprovação de telas com a ONG parceira.
9. 📄 [`08-bootstrap-plan.md`](08-bootstrap-plan.md) — **O Setup:** Estrutura de pastas do repositório monorepo (`backend/`, `frontend/`, `docs/`).
10. 📄 [`09-sprints-backend-novembro.md`](09-sprints-backend-novembro.md) — **O Planejamento de Sprints (Novembro/2026):** Cronograma de 4 Sprints do Backend alinhado ao escopo oficial POC-Match (`RF01`–`RF21`, `RNF01`–`RNF08`).
11. 📄 [`endpoints-map.md`](endpoints-map.md) — **Mapa de Endpoints:** Índice de rotas REST por Sprint, regras de RBAC e diagrama de sequência POC-Match.
12. 📄 [`openapi.yaml`](openapi.yaml) — **Contrato OpenAPI 3.0.3 (v2.0.0):** Especificação completa de rotas e schemas do Azreal POC-Match.

---

## 🔗 Cadeia de Rastreabilidade (Traceability Chain)

```
Project Charter (Visão & Dores)
  └── SRS (REQ-*)
        └── Casos de Uso (UC-*)
              └── User Stories (US-* com GWT)
                    └── GitHub Issues / Pull Requests (Branches feature/US-*)
                          └── Commits & Pipeline CI/CD (SonarQube Quality Gate)
```
