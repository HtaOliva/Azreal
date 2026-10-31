# 04 — Backlog de Histórias de Usuário (User Stories) — Azreal (POC-Match)

**Padrão:** Backlog Ágil refinado no padrão **INVEST** (Independent, Negotiable, Valuable, Estimable, Small, Testable) com critérios de aceitação no formato **Given / When / Then (GWT)**, rastreado ao [DOCUMENTO_OFICIAL_AZREAL_POC.md](../DOCUMENTO_OFICIAL_AZREAL_POC.md).

---

## 🎯 Épico 1: Identidade, Perfis (`Startup` & `AmbienteValidacao`), LGPD e Administração (Sprint 1)

### 🔹 `US-01`: Autenticação LLDAP / JWT e Controle de Acesso por Perfil (`RF04`, `RNF04`, `RNF05`)
* **Como** usuário da plataforma Azreal (`STARTUP`, `AMBIENTE_VALIDACAO` ou `ADMIN`),
* **Quero** autenticar-me com minhas credenciais institucionais LLDAP / locais e ter acesso segregado ao meu papel, podendo também excluir minha conta quando desejar,
* **Para que** meus dados e operações estejam protegidos em conformidade com a LGPD.

> **Critérios de Aceitação (GWT):**
> * **Cenário 1 (Login e identificação de papel):**  
>   * **Dado** que possuo credenciais válidas associadas ao grupo `azrael_admin` (`ROLE_ADMIN`), `ROLE_STARTUP` ou `ROLE_AMBIENTE_VALIDACAO`,  
>   * **Quando** envio `POST /auth/login` e consulto `GET /me`,  
>   * **Então** recebo HTTP `200 OK` com meu token e meu perfil exato (`ADMIN`, `STARTUP` ou `AMBIENTE_VALIDACAO`).
> * **Cenário 2 (Exclusão da própria conta — LGPD):**  
>   * **Dado** que estou autenticado na plataforma,  
>   * **Quando** aciono `DELETE /me`,  
>   * **Então** minha conta é inativada/removida e recebo HTTP `204 No Content`.
* *Rastreabilidade:* `UC-01`, `RF04`, `RF07`, `RNF04`, `RNF05`, `Issue #37`.

---

### 🔹 `US-02`: Cadastro de Startup e Ambiente de Validação com Consentimento LGPD (`RF01`, `RF02`, `RF03`, `RNF05`)
* **Como** fundador de Startup (*Lado A*) ou representante de um Ambiente de Validação (*Lado B*),
* **Quero** registrar meu consentimento LGPD (incluindo ciência de uso de IA sem dados pessoais) e cadastrar o perfil completo da minha organização com localidade IBGE, temas e modalidades,
* **Para que** eu esteja apto a participar do matchmaking de Provas de Conceito (POCs).

> **Critérios de Aceitação (GWT):**
> * **Cenário 1 (Cadastro válido de Startup com estágio do protótipo e o que precisa validar):**  
>   * **Dado** que registrei previamente o aceite de termos (`POST /termos/aceite`) obtendo um `aceiteTermoId` válido,  
>   * **Quando** envio `POST /startups` informando nome, e-mail, solução, setor, estágio (`PROTOTIPO_INICIAL`, `MVP_FUNCIONAL` ou `EM_TESTE`), UF/município IBGE, `oQuePrecisaValidar`, `temasIds` e `modalidadesSuportadas`,  
>   * **Então** o perfil da startup é criado com HTTP `201 Created`.
> * **Cenário 2 (Cadastro válido de Ambiente de Validação):**  
>   * **Dado** que possuo um `aceiteTermoId` válido,  
>   * **Quando** envio `POST /ambientes-validacao` com razão social, nome fantasia, setor, porte, UF/município IBGE e e-mail de contato obrigatório,  
>   * **Então** o perfil do Ambiente de Validação é salvo com HTTP `201 Created` e seus dados de identificação permanecem restritos (`RNF04`).
> * **Cenário 3 (Tentativa de cadastro sem consentimento LGPD):**  
>   * **Dado** que informo um `aceiteTermoId` inexistente,  
>   * **Quando** tento cadastrar uma Startup ou Ambiente de Validação,  
>   * **Então** o backend rejeita a operação com HTTP `400 Bad Request`.
* *Rastreabilidade:* `UC-02`, `RF01`, `RF02`, `RF03`, `RF08`, `RF09`, `RNF05`, `Issue #27`.

---

## 🎯 Épico 2: Desafios de Validação Anônimos & Matchmaking Híbrido (Sprint 2)

### 🔹 `US-03`: Publicação e Exibição Anônima de Desafios de Validação (`RF05`, `RF06`, `RNF04`)
* **Como** Ambiente de Validação (*Lado B*),
* **Quero** publicar Desafios de Validação descrevendo minha dor operacional, ambiente de teste, modalidade e duração máxima sem que minha razão social ou contatos sejam exibidos às startups antes do aceite,
* **Para que** eu possa prospectar soluções inovadoras preservando o sigilo estratégico da organização.

> **Critérios de Aceitação (GWT):**
> * **Cenário 1 (Listagem anônima para Startups):**  
>   * **Dado** que um Ambiente de Validação publicou um desafio ativo em `POST /desafios`,  
>   * **Quando** uma Startup consulta `GET /desafios` ou `GET /desafios/{id}`,  
>   * **Então** o payload JSON retorna problema, setor, porte da organização, UF/município, modalidade, ambiente de teste e restrições LGPD, mas **não contém** razão social, nome fantasia, CNPJ nem contatos do Ambiente.
* *Rastreabilidade:* `UC-03`, `RF05`, `RF06`, `RNF01`, `RNF04`.

---

### 🔹 `US-04`: Matchmaking Semântico com 1 Frase de Justificativa e Fallback por Regras (`RF10`, `RF11`, `RNF03`, `RNF06`)
* **Como** Startup *early stage* (*Lado A*),
* **Quero** visualizar os desafios ativos ordenados do mais ao menos compatível com uma frase curta explicando o motivo de cada recomendação (sem pontuação percentual),
* **Para que** eu identifique rapidamente onde realizar minha Prova de Conceito mesmo se o serviço externo de IA estiver indisponível.

> **Critérios de Aceitação (GWT):**
> * **Cenário 1 (Ordenação via IA com sucesso):**  
>   * **Dado** que há desafios ativos compatíveis com a modalidade e localidade da minha startup,  
>   * **Quando** consulto `GET /startups/{id}/desafios-recomendados` e a API de IA responde dentro do *timeout*,  
>   * **Então** recebo a lista ordenada com `motivoSugestao` (1 frase curta) e `origemOrdenacao = IA`, sem nenhum campo de porcentagem.
> * **Cenário 2 (Contingência automática via Fallback por Regras):**  
>   * **Dado** que a API externa de IA está indisponível ou excede o *timeout*,  
>   * **Quando** consulto `GET /startups/{id}/desafios-recomendados`,  
>   * **Então** o sistema ordena automaticamente os desafios por setor, temas em comum e modalidade/localidade e preenche `motivoSugestao` via template padronizado (`origemOrdenacao = FALLBACK_REGRAS`).
* *Rastreabilidade:* `UC-03`, `RF10`, `RF11`, `RNF03`, `RNF06`.

---

## 🎯 Épico 3: Proposta Enxuta de POC, Termo de Piloto e Registro Oficial de POC (Sprints 3 e 4)

### 🔹 `US-05`: Submissão de Proposta Enxuta de POC (Limite Máx. 3) e Formalização do Termo de Piloto (`RF12`–`RF17`)
* **Como** Startup (*Lado A*) e Ambiente de Validação (*Lado B*),
* **Queremos** submeter e avaliar Propostas Enxutas de POC dentro do prazo de 5 dias úteis (calendário de PE), congelando um Termo de Piloto assinado que libera a identidade e os contatos mútuos no aceite,
* **Para que** o teste piloto ocorra com escopo, duração, responsabilidades, métrica de validação e sigilo formalizados.

> **Critérios de Aceitação (GWT):**
> * **Cenário 1 (Bloqueio acima de 3 propostas pendentes simultâneas):**  
>   * **Dado** que a startup já possui 3 propostas com status `PENDENTE` para desafios distintos,  
>   * **Quando** tenta enviar uma 4ª proposta em `POST /propostas-poc`,  
>   * **Então** o backend rejeita com HTTP `422 Unprocessable Entity`.
> * **Cenário 2 (Aceite pelo Ambiente gera Termo de Piloto e revela identidade):**  
>   * **Dado** que existe uma proposta `PENDENTE` dentro do prazo de 5 dias úteis de PE com aceite prévio assinado pela startup,  
>   * **Quando** o Ambiente envia `PUT /propostas-poc/{id}/responder` com `aceitar = true`,  
>   * **Então** o status muda para `EM_TESTE`, o `TermoPiloto` é gerado com os campos congelados e a identidade/contatos do Ambiente ficam disponíveis em `GET /termos-piloto/{propostaId}`.
* *Rastreabilidade:* `UC-04`, `RF12`, `RF13`, `RF14`, `RF15`, `RF16`, `RF17`, `RNF02`, `RNF08`.

---

### 🔹 `US-06`: Confirmação do Piloto e Emissão do Registro Oficial de POC Verificável (`RF18`–`RF21`)
* **Como** Startup (*Lado A*) que concluiu um piloto em ambiente real,
* **Quero** que o Ambiente de Validação confirme a execução e avalie a métrica combinada para que a plataforma emita meu **Registro Oficial de POC** (nominal ou anônimo qualificado por setor e porte),
* **Para que** eu possua um documento verificável de tração para apresentar em editais de fomento e prospecção comercial.

> **Critérios de Aceitação (GWT):**
> * **Cenário 1 (Piloto executado e confirmado emite Registro Oficial de POC):**  
>   * **Dado** que a startup registrou a entrega do piloto (`POST /propostas-poc/{id}/entrega`),  
>   * **Quando** o Ambiente confirma em `POST /propostas-poc/{id}/confirmacao-resultado` com `pilotoExecutado = true`, informa o resultado da métrica, parecer e autorização de exibição (`NOMINAL_ORGANIZACAO` ou `ANONIMA_QUALIFICADA_SETOR_PORTE`),  
>   * **Então** o status muda para `POC_CONCLUIDA` e o `RegistroOficialPoc` passa a constar no portfólio verificável `GET /startups/{id}/registros-poc`.
> * **Cenário 2 (Piloto não executado não emite registro):**  
>   * **Dado** que o teste foi abandonado ou não ocorreu,  
>   * **Quando** o Ambiente informa `pilotoExecutado = false`,  
>   * **Então** o status muda para `POC_NAO_CONFIRMADA` e nenhum registro é emitido no portfólio.
* *Rastreabilidade:* `UC-05`, `RF18`, `RF19`, `RF20`, `RF21`, `RNF08`.
