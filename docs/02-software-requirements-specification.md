# 02 — Software Requirements Specification (SRS) — Azreal (POC-Match)

**Padrão:** Estruturado conforme a norma **IEEE 29148-2018** (Systems and software engineering — Life cycle processes — Requirements engineering) e alinhado ao [DOCUMENTO_OFICIAL_AZREAL_POC.md](../DOCUMENTO_OFICIAL_AZREAL_POC.md).

---

## 1. Requisitos Funcionais (Functional Requirements — `RF01` a `RF21`)

### Módulo 1: Acesso, Identidade, Perfis e LGPD (`AUTH & PERFIS`)
* **`RF01` (Cadastro e Perfil de Startup — Lado A):** A Startup cria seu perfil com nome, descrição da solução, setor/área de atuação, data de fundação, estágio do protótipo (`PROTOTIPO_INICIAL`, `MVP_FUNCIONAL` ou `EM_TESTE`), integrantes, localidade (UF e município via IBGE) e o que precisa validar em ambiente real, concluindo o cadastro apenas após aceitar os termos de uso e tratamento de dados (LGPD).
* **`RF02` (Temas e Modalidades da Startup):** A Startup seleciona temas do vocabulário padronizado (`RF08`) e modalidades de teste suportadas (`PRESENCIAL`, `REMOTO` ou `HIBRIDO`).
* **`RF03` (Cadastro e Perfil de Ambiente de Validação — Lado B):** O Ambiente de Validação cria seu perfil organizacional com razão social, nome fantasia, setor, porte (`PEQUENO`, `MEDIO`, `GRANDE`, `ORGAO_PUBLICO`, `INSTITUICAO_ENSINO_SAUDE`, `COMUNIDADE`), localidade (IBGE), canais de contato (e-mail obrigatório, telefone e LinkedIn opcionais) e aceita os termos de uso e LGPD.
* **`RF04` (Autenticação LLDAP/JWT e RBAC):** O sistema autentica usuários (integrado ao LLDAP institucional `lldap.fchicout.dev`) e restringe o acesso por perfil: `STARTUP`, `AMBIENTE_VALIDACAO` e `ADMIN`. Contas `ADMIN` são criadas de forma privada pela equipe (`azrael_admin`).

### Módulo 2: Desafios de Validação, Privacidade em Camadas e Catálogos (`DESAFIOS & ADMIN`)
* **`RF05` (Gestão de Desafios de Validação):** O Ambiente de Validação publica, edita e inativa Desafios de Validação informando: título do problema, descrição da dor operacional, prioridade (`BAIXA`, `MEDIA`, `ALTA`, `CRITICA`), setor/temas (`RF08`), ambiente disponível para teste (ex.: nº de usuários/setor), modalidade (`PRESENCIAL`, `REMOTO`, `HIBRIDO`), duração máxima do piloto (em dias) e restrições de dados/LGPD.
* **`RF06` (Exibição Anônima de Desafios):** Os Desafios de Validação ativos são exibidos de forma **estritamente anônima** para as startups, mostrando apenas o problema, setor, porte da organização, localidade, modalidade, ambiente de teste e restrições, ocultando razão social, nome fantasia e contatos da organização parceira até o aceite no `RF15`.
* **`RF07` (Moderação de Contas e Catálogo de Incubadoras):** A administração ativa e desativa contas de usuários e mantém o catálogo consultivo complementar de incubadoras/programas locais (com foco, critérios de entrada, modalidade e link externo).
* **`RF08` (Vocabulário Padronizado de Temas e Setores):** A administração cria, atualiza e ativa/inativa os temas e setores do vocabulário padronizado utilizado por startups e ambientes de validação.
* **`RF09` (Modelo Base do Termo de Piloto e Sigilo):** A administração redige e atualiza o modelo base do **Termo de Piloto e Cláusula de Sigilo**, exibido na submissão (`RF12`) e no aceite (`RF15`) da proposta de POC.

### Módulo 3: Matchmaking de Demanda ↔ Solução (`MATCHMAKING`)
* **`RF10` (Recomendação Ordenada por IA sem Score Percentual):** Quando a startup solicita recomendações, o sistema filtra os desafios ativos por critérios objetivos (modalidade, localidade e restrições) e utiliza a API de IA para ordenar os Desafios de Validação do mais ao menos compatível, gerando **uma frase curta que explica o motivo de cada sugestão** (sem pontuação percentual de 0 a 100%).
* **`RF11` (Ordenação Determinística e Fallback por Regras):** Além da ordenação por IA, o sistema calcula uma ordenação por regras objetivas (setor, temas em comum e modalidade/localidade). Caso a API de IA falhe ou exceda o *timeout* (`RNF03`), o sistema exibe automaticamente a lista ordenada por regras com uma frase justificativa gerada por template.

### Módulo 4: Proposta Enxuta de POC, Fila e Termo de Piloto (`NEGOCIAÇÃO & PILOTO`)
* **`RF12` (Submissão de Proposta Enxuta de POC com Aceite Prévio):** A startup envia uma Proposta Enxuta de POC para um desafio ativo preenchendo: objetivo/escopo do teste, entregas previstas, responsabilidades sugeridas (startup vs. ambiente), duração sugerida (dentro do limite do desafio) e **métrica de validação**, além de assinar preventivamente o **aceite prévio do Termo de Piloto e Cláusula de Sigilo**.
* **`RF13` (Limite de 3 Propostas Pendentes Simultâneas):** O limite de propostas de POC com status `PENDENTE` em aberto por startup é de **3**, sempre para desafios diferentes. Propostas aceitas (`EM_TESTE`), recusadas, canceladas ou concluídas deixam de contar no limite de pendências.
* **`RF14` (Fila do Ambiente com Prazo de 5 Dias Úteis de PE e Cancelamento):** O Ambiente de Validação consulta a fila de propostas recebidas para seus desafios e tem **5 dias úteis** (pelo calendário estadual de Pernambuco) para aceitar ou recusar. A startup pode cancelar uma proposta `PENDENTE` a qualquer momento antes do aceite.
* **`RF15` (Aceite, Congelamento do Termo de Piloto e Revelação de Identidade):** Ao aceitar uma proposta, o sistema congela os campos combinados, gera o **Termo de Piloto** assinado por ambas as partes (`Status: EM_TESTE`), **revela a identidade da organização parceira** e libera os canais de contato mútuos.
* **`RF16` (Recusa Motivada e Liberação de Vaga):** Ao recusar uma proposta, o Ambiente de Validação informa um motivo curto padronizado ou textual, encerrando a proposta como `RECUSADA` e liberando imediatamente a vaga no limite de 3 propostas da startup.
* **`RF17` (Acompanhamento de Status de Propostas e Pilotos):** A startup acompanha em tempo real o status de suas propostas e pilotos: `PENDENTE`, `EM_TESTE`, `RECUSADA`, `CANCELADA`, `EXPIRADA`, `POC_CONCLUIDA` ou `POC_NAO_CONFIRMADA`.

### Módulo 5: Execução, Confirmação e Registro Oficial de POC (`ATESTAÇÃO & QUALIDADE`)
* **`RF18` (Registro de Entrega do Piloto):** Ao final do período do piloto, a startup registra na plataforma o resumo do que foi entregue na execução do teste, notificando o Ambiente de Validação para confirmação.
* **`RF19` (Confirmação de Execução e Avaliação de Métrica pelo Ambiente):** O Ambiente de Validação confirma se o piloto foi efetivamente executado:
  - Se **sim**: avalia o resultado frente à métrica de validação definida no Termo (`ATENDEU_TOTALMENTE`, `ATENDEU_PARCIALMENTE` ou `NAO_ATINGIU_METRICA`), registra seu parecer sobre a solução e escolhe se autoriza exibir o **nome da organização** (`NOMINAL_ORGANIZACAO`) ou manter **exibição anônima qualificada** (`ANONIMA_QUALIFICADA_SETOR_PORTE`, exibindo apenas setor e porte).
  - Se **não foi executado**: encerra como `POC_NAO_CONFIRMADA`, sem emitir registro no portfólio.
* **`RF20` (Emissão do Registro Oficial de POC Verificável):** Com a confirmação positiva (`RF19`), o sistema emite o **Registro Oficial de POC** no histórico/portfólio verificável da startup, contendo código verificador, escopo testado, duração realizada, métrica avaliada, parecer do ambiente e identificação (nominal ou anônima por setor/porte).
* **`RF21` (Avaliação da Qualidade do Matchmaking):** Após a conclusão, tanto a startup quanto o ambiente avaliam a qualidade do matchmaking (se o encaixe entre problema e solução fez sentido), permitindo medir a eficácia da ordenação por IA frente à ordenação por regras.

---

## 2. Requisitos Não Funcionais (Non-Functional Requirements — `RNF01` a `RNF08`)

| ID | Categoria | Especificação Técnica |
| :--- | :--- | :--- |
| **`RNF01`** | Desempenho de Listagens | Listagens de desafios, filas de propostas e histórico de POCs exibidas em até **3 segundos** em condições normais (sem contar o processamento da IA). |
| **`RNF02`** | Desempenho de Registros | Confirmação de envio de proposta, aceite do Termo de Piloto e emissão do Registro Oficial de POC processados em até **2 segundos**. |
| **`RNF03`** | Desempenho e Resiliência da IA | A ordenação semântica por IA possui indicador visual de progresso e *timeout* seguro acionando automaticamente o *fallback* por regras (`RF11`). |
| **`RNF04`** | Segurança e Isolamento em Camadas | Comunicação via HTTPS, senhas com hash BCrypt + *salt*, e RBAC estrito. Antes do aceite do Termo de Piloto (`RF15`), nenhuma rota ou payload da API pode expor razão social, nome fantasia, CNPJ ou contatos do Ambiente de Validação para a startup. |
| **`RNF05`** | Privacidade e LGPD | Coleta mínima de dados pessoais, registro de consentimento com data/hora imutável, cláusula explícita sobre envio de textos descritivos do problema/solução à API de IA (sem envio de dados pessoais ou contatos) e opção de exclusão da própria conta (`DELETE /me`). |
| **`RNF06`** | Segurança contra Prompt Injection | Textos livres de desafios e perfis de startups são delimitados e tratados exclusivamente como dados de entrada, nunca como instruções ao modelo de linguagem. |
| **`RNF07`** | Acessibilidade e Compatibilidade | Conformidade com diretrizes **WCAG 2.1 Nível AA** e layout responsivo nas duas versões mais recentes de Chrome, Firefox, Edge e Safari. |
| **`RNF08`** | Rastreabilidade e Auditoria | Toda mudança de status da proposta/piloto (`PENDENTE` → `EM_TESTE` → `POC_CONCLUIDA` / `POC_NAO_CONFIRMADA` / `RECUSADA` / `CANCELADA`), aceite do Termo de Piloto e emissão do Registro Oficial de POC é gravada com *timestamp* imutável e identificação dos usuários responsáveis. |
