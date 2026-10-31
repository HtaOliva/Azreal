# 06 — Especificação de Telas & Diretrizes de UI/UX — Azreal (POC-Match)

**Responsável UI/UX & Front-End:** Alanna Beatriz Bezerra dos Santos (`alanna.santos`)  
**Stack Front-End:** Angular 22 (100% Zoneless, Signals, OnPush) · WCAG 2.1 Nível AA (`RNF07`)

---

## 1. Mapa de Navegação da Aplicação (Sitemap POC-Match)

* `/login` &rarr; Tela de Autenticação LLDAP / Local e Redirecionamento por Perfil (`RF04`).
* `/cadastro/startup` &rarr; Onboarding de Startup (*Lado A*) com aceite LGPD, estágio do protótipo, IBGE, o que precisa validar, temas e modalidades (`RF01`, `RF02`).
* `/cadastro/ambiente` &rarr; Onboarding de Ambiente de Validação (*Lado B*) com aceite LGPD, porte, setor, IBGE e contatos (`RF03`).
* `/startup/desafios` &rarr; Vitrine de Recomendações (Matchmaking IA + Fallback por Regras) exibindo Desafios **Anônimos** com 1 frase curta de compatibilidade (`RF06`, `RF10`, `RF11`).
* `/startup/propostas` &rarr; Submissão de Proposta Enxuta de POC (contador de até **3 pendentes simultâneas**), acompanhamento de status, Termo de Piloto assinado e envio do resumo de entrega (`RF12`, `RF13`, `RF17`, `RF18`).
* `/startup/portfolio-poc` &rarr; Histórico verificável de **Registros Oficiais de POC** emitidos (nominal ou anônimo qualificado por setor e porte — `RF20`) + Catálogo consultivo de Incubadoras (`RF07`).
* `/ambiente/desafios` &rarr; Gestão de Desafios de Validação publicados pela organização parceira (`RF05`).
* `/ambiente/fila-propostas` &rarr; Fila de avaliação de Propostas de POC recebidas (prazo de **5 dias úteis de PE**), aceite com geração de **Termo de Piloto** ou recusa motivada (`RF14`, `RF15`, `RF16`) e confirmação final do resultado da POC (`RF19`).
* `/admin` &rarr; Curadoria do vocabulário de temas/setores (`RF08`), catálogo de incubadoras (`RF07`), modelo base do Termo de Piloto e Sigilo (`RF09`) e moderação de contas.

---

## 2. Wireframes Estruturais das Telas Principais

### 🖥️ Tela 01: Vitrine de Matchmaking de Desafios Anônimos (`/startup/desafios`)
```
+-----------------------------------------------------------------------------------+
| 🪽 Azreal POC-Match          Startup: Nook (MVP Funcional) | Propostas: 1/3 | Sair |
+-----------------------------------------------------------------------------------+
| Recomendações de Desafios de Validação (Ordenação Semântica IA / Fallback Regras) |
| [Status IA: Ativa]  [Filtro: Presencial / Híbrido - Recife/PE]                    |
+-----------------------------------------------------------------------------------+
| #1 [Prioridade: ALTA] Desafio: Triagem ágil de leitos e filas ambulatoriais       |
|    Organização: [IDENTIDADE PROTEGIDA ATÉ O ACEITE DO PILOTO — Porte: Grande]     |
|    Setor: Saúde | Local: Recife/PE | Modalidade: Híbrido | Duração máx.: 30 dias  |
|    Ambiente cedido: 25 profissionais do setor de regulação por 30 dias            |
|    💡 Por que deu match: "Sua solução de gestão hospitalar em MVP atende          |
|       diretamente a dor de regulação presencial/híbrida em Recife/PE."            |
|                                            [ Enviar Proposta Enxuta de POC ]      |
+-----------------------------------------------------------------------------------+
```

### 🖥️ Tela 02: Termo de Piloto & Emissão do Registro Oficial de POC (`/ambiente/fila-propostas`)
```
+-----------------------------------------------------------------------------------+
| 🪽 Azreal POC-Match          Ambiente Parceiro | Fila (Prazo: 5 dias úteis PE)    |
+-----------------------------------------------------------------------------------+
| Piloto #POC-2026-01 — Status: EM_TESTE (Termo de Piloto Assinado em 16/11/2026)   |
| Startup: Rede Leito (Contato liberado: contato@redeleito.com.br)                  |
| • Escopo congelado: Teste do painel de ocupação em 2 alas durante 30 dias         |
| • Métrica de validação: Redução de 20% no tempo de atualização de status de leito |
| • Entrega registrada pela Startup: "Painel implantado e operado por 30 dias..."   |
+-----------------------------------------------------------------------------------+
| Confirmação de Execução pelo Ambiente de Validação (RF19 / RF20):                 |
| (x) Confirmo que o piloto foi executado no ambiente real                          |
| Resultado da Métrica: [ Atendeu totalmente (v) ]                                  |
| Exibição no Portfólio da Startup:                                                 |
| ( ) Exibir nome da minha organização   (x) Exibição anônima (apenas Setor/Porte)  |
| Parecer: [ Solução validada com sucesso pela equipe operacional.               ]  |
|                                     [ Confirmar e Emitir Registro Oficial de POC ]|
+-----------------------------------------------------------------------------------+
```

---

## 3. Checklist de Acessibilidade (WCAG 2.1 Nível AA — `RNF07`)

- [x] Contraste de texto mínimo de 4.5:1 para texto normal e 3:1 para texto grande.
- [x] Todos os campos de formulário possuem `<label>` semanticamente associado via `for`/`id`.
- [x] Navegação integral por teclado (<kbd>Tab</kbd>, <kbd>Enter</kbd>, <kbd>Esc</kbd>) com estado `:focus-visible` nítido.
- [x] Indicador visual acessível (`aria-live="polite"`) durante o processamento da ordenação por IA (`RNF03`).
