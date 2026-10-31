# 03 — Especificação de Casos de Uso (Use Cases) — Azreal (POC-Match)

**Padrão:** Especificações formais no padrão **Alistair Cockburn (Fully-Dressed)**, rastreadas aos requisitos do [DOCUMENTO_OFICIAL_AZREAL_POC.md](../DOCUMENTO_OFICIAL_AZREAL_POC.md).

---

## 📌 `UC-01`: Autenticar Usuário e Segregar Acesso por Perfil (LLDAP / JWT)

* **Ator Principal:** Usuário da Plataforma (`STARTUP`, `AMBIENTE_VALIDACAO` ou `ADMIN`).
* **Pré-condições:** Usuário possui credenciais ativas no diretório LLDAP (`lldap.fchicout.dev`) ou base autenticada e conta ativa (`RF04`, `RF07`).
* **Pós-condições:** Token JWT emitido e sessão autenticada com as *authorities* (`ROLE_STARTUP`, `ROLE_AMBIENTE_VALIDACAO`, `ROLE_ADMIN`).
* **Requisitos Rastreados:** `RF04`, `RF07`, `RNF04`.

### Fluxo Principal (Caminho Feliz):
1. O usuário acessa a tela de login e informa `username` e `password`.
2. O frontend envia `POST /auth/login` para o backend.
3. O backend autentica as credenciais via `AuthenticationManager` / LLDAP (`Service Account: azrael_app`), verifica se o usuário pertence ao grupo `azrael_admin` (mapeando `ROLE_ADMIN`) ou aos perfis `ROLE_STARTUP` / `ROLE_AMBIENTE_VALIDACAO`.
4. O backend retorna HTTP `200 OK` com o token e os papéis do usuário.
5. O usuário consulta `GET /me` e é direcionado ao painel correspondente ao seu papel.

### Fluxos Alternativos & Exceções:
* **3a. Credenciais Inválidas:** O backend retorna HTTP `401 Unauthorized` sem expor detalhes internos da infraestrutura.
* **3b. Exclusão de Conta pelo Titular (LGPD — `RNF05`):** O usuário autenticado aciona `DELETE /me`, e o sistema inativa/exclui os dados pessoais vinculados à sua conta, encerrando a sessão com HTTP `204 No Content`.

---

## 📌 `UC-02`: Cadastrar Startup e Ambiente de Validação com Consentimento LGPD

* **Atores Principais:** Fundador de Startup (*Lado A*) e Representante do Ambiente de Validação (*Lado B*).
* **Pré-condições:** Vocabulário padronizado de setores e temas ativo (`RF08`).
* **Pós-condições:** Consentimento LGPD gravado com *timestamp* imutável (`AceiteTermo`) e perfil persistido (`Startup` ou `AmbienteValidacao`).
* **Requisitos Rastreados:** `RF01`, `RF02`, `RF03`, `RNF05`.

### Fluxo Principal:
1. O usuário lê os Termos de Uso, a Política de Privacidade/LGPD e a cláusula explícita sobre processamento de textos descritivos (sem dados pessoais) pela IA (`RNF05`).
2. O frontend envia `POST /termos/aceite` e recebe o `aceiteTermoId` (`201 Created`).
3. **Se Startup (`RF01`, `RF02`):** preenche nome, e-mail, descrição da solução, setor, data de fundação, estágio do protótipo (`PROTOTIPO_INICIAL`, `MVP_FUNCIONAL`, `EM_TESTE`), integrantes, UF/município IBGE, o que precisa validar em ambiente real, temas (`RF08`) e modalidades suportadas (`PRESENCIAL`, `REMOTO`, `HIBRIDO`), enviando `POST /startups`.
4. **Se Ambiente de Validação (`RF03`):** preenche razão social, nome fantasia, setor, porte, UF/município IBGE, e-mail obrigatório, telefone/LinkedIn opcionais e `aceiteTermoId`, enviando `POST /ambientes-validacao`.
5. O backend valida a existência do `aceiteTermoId` e persiste o cadastro retornando HTTP `201 Created`.

### Fluxos Alternativos & Exceções:
* **5a. Ausência de Consentimento LGPD:** Se `aceiteTermoId` for nulo ou inexistente, o backend rejeita a requisição com HTTP `400 Bad Request` (*"Aceite de termos obrigatorio"*).

---

## 📌 `UC-03`: Publicar Desafio Anônimo e Receber Recomendações de Matchmaking (IA + Fallback)

* **Atores Principais:** Ambiente de Validação (*Lado B*) e Startup (*Lado A*).
* **Pré-condições:** Ambiente de Validação e Startup cadastrados e ativos.
* **Pós-condições:** Desafio ativo publicado sem exposição de identidade e lista recomendada ordenada com justificativa de 1 frase curta.
* **Requisitos Rastreados:** `RF05`, `RF06`, `RF10`, `RF11`, `RNF01`, `RNF03`, `RNF04`, `RNF06`.

### Fluxo Principal:
1. O Ambiente de Validação publica um desafio via `POST /desafios` informando título do problema, dor operacional, prioridade, setor/temas, ambiente disponível para teste, modalidade, duração máxima do piloto e restrições LGPD (`RF05`).
2. A Startup solicita recomendações em `GET /startups/{id}/desafios-recomendados`.
3. O backend aplica o pré-filtro objetivo (modalidade compatível, localidade IBGE e restrições LGPD).
4. O backend sanitiza e delimita os textos de problema e solução (`RNF06`), sem incluir razão social ou contatos (`RNF04`, `RNF05`), e solicita a ordenação semântica à API de IA.
5. A IA retorna a ordenação do mais ao menos compatível (sem pontuação percentual) com **1 frase curta explicando o motivo de cada sugestão** (`origemOrdenacao: IA`).
6. A Startup visualiza os desafios por meio do contrato `DesafioAnonimoResponseDTO` (`RF06`).

### Fluxos Alternativos & Exceções:
* **4a. Indisponibilidade ou Timeout da API de IA (`RF11`, `RNF03`):** O backend aciona instantaneamente o ordenador determinístico por regras (setor, temas em comum e modalidade/localidade) e gera a frase justificativa padronizada por template (`origemOrdenacao: FALLBACK_REGRAS`), garantindo resposta sem erro ao usuário.

---

## 📌 `UC-04`: Submeter Proposta Enxuta de POC e Firmar Termo de Piloto

* **Atores Principais:** Startup (*Lado A*) e Ambiente de Validação (*Lado B*).
* **Pré-condições:** Desafio ativo selecionado e Startup com menos de 3 propostas `PENDENTE` em aberto (`RF13`).
* **Pós-condições:** No aceite, campos congelados no `TermoPiloto` (`Status: EM_TESTE`) e identidade/contatos do Ambiente de Validação liberados para a Startup.
* **Requisitos Rastreados:** `RF12`, `RF13`, `RF14`, `RF15`, `RF16`, `RF17`, `RNF02`, `RNF04`, `RNF08`.

### Fluxo Principal:
1. A Startup preenche a Proposta Enxuta de POC (objetivo/escopo, entregas, responsabilidades de cada parte, duração dentro do limite do desafio e métrica de validação), marca o aceite prévio do Termo de Piloto e Cláusula de Sigilo e envia `POST /propostas-poc` (`RF12`).
2. O backend valida que a Startup possui no máximo 2 propostas `PENDENTE` anteriores (totalizando até 3 abertas para desafios distintos — `RF13`), calcula o prazo de resposta de **5 dias úteis pelo calendário de Pernambuco** (`RF14`) e grava a proposta como `PENDENTE`.
3. O Ambiente de Validação consulta sua fila em `GET /ambientes-validacao/{id}/fila-propostas` e aceita a proposta via `PUT /propostas-poc/{id}/responder` (`aceitar: true`).
4. O backend muda o status para `EM_TESTE`, congela os campos combinados gerando o `TermoPiloto` assinado pelas duas partes, registra o evento de auditoria (`RNF08`) e libera a consulta a `GET /termos-piloto/{propostaId}` com a razão social, nome fantasia e canais de contato do Ambiente (`RF15`).

### Fluxos Alternativos & Exceções:
* **2a. Limite de 3 Propostas Pendentes Excedido (`RF13`):** O backend rejeita com HTTP `422 Unprocessable Entity` (*"Limite de 3 propostas pendentes em aberto excedido"*).
* **3a. Cancelamento pela Startup antes do Aceite (`RF14`):** A Startup aciona `PUT /propostas-poc/{id}/cancelar`; o status muda para `CANCELADA` e a vaga no limite de 3 é liberada imediatamente.
* **3b. Recusa pelo Ambiente de Validação (`RF16`):** O Ambiente responde com `aceitar: false` e informa um `motivoRecusa` curto; o status muda para `RECUSADA` e a vaga da Startup no limite de 3 propostas é liberada automaticamente.

---

## 📌 `UC-05`: Confirmar Execução do Piloto e Emitir Registro Oficial de POC

* **Atores Principais:** Startup (*Lado A*) e Ambiente de Validação (*Lado B*).
* **Pré-condições:** Piloto com `Status: EM_TESTE`.
* **Pós-condições:** `RegistroOficialPoc` verificável emitido no portfólio da Startup (`Status: POC_CONCLUIDA`) e avaliação de matchmaking registrada.
* **Requisitos Rastreados:** `RF18`, `RF19`, `RF20`, `RF21`, `RNF08`.

### Fluxo Principal:
1. Ao concluir o período do teste real, a Startup registra o resumo do que foi entregue em `POST /propostas-poc/{id}/entrega` (`RF18`).
2. O Ambiente de Validação acessa `POST /propostas-poc/{id}/confirmacao-resultado` e confirma que o piloto ocorreu (`pilotoExecutado: true`), avaliando a métrica definida no Termo (`ATENDEU_TOTALMENTE`, `ATENDEU_PARCIALMENTE` ou `NAO_ATINGIU_METRICA`), registrando seu parecer e escolhendo a forma de exibição (`NOMINAL_ORGANIZACAO` ou `ANONIMA_QUALIFICADA_SETOR_PORTE` — `RF19`).
3. O backend atualiza o status para `POC_CONCLUIDA`, grava a trilha de auditoria (`RNF08`) e emite automaticamente o **Registro Oficial de POC** (`RF20`), disponível em `GET /startups/{id}/registros-poc`.
4. Startup e Ambiente avaliam a qualidade do matchmaking em `POST /propostas-poc/{id}/avaliacao-match` (`RF21`).

### Fluxos Alternativos & Exceções:
* **2a. Piloto Não Executado / Abandonado (`RF19`):** Se o Ambiente informar `pilotoExecutado: false`, o backend encerra a proposta como `POC_NAO_CONFIRMADA` e **não emite** nenhum Registro Oficial de POC no portfólio da startup.
