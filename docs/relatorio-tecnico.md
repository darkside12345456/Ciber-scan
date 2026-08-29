# Relatório Técnico — App "Scanner de Privacidade" para Android

**Autora:** Jp
**Objetivo:** Desenvolver e publicar na Google Play Store uma app que analisa as permissões das apps instaladas, atribui um score de privacidade e recomenda ações concretas ao utilizador.
**Modelo de negócio:** Freemium (análise básica grátis; relatório detalhado e monitorização contínua por subscrição).

---

## 1. Conceito e proposta de valor

A app faz três coisas, por esta ordem:

1. **Inventaria** todas as apps instaladas e as permissões que cada uma declara e usa.
2. **Avalia** o risco de cada permissão em linguagem simples (ex.: *"O WhatsApp acede ao microfone — normal para chamadas. A App X acede ao microfone e não faz chamadas — suspeito."*).
3. **Recomenda** o que desligar, com um botão que leva diretamente às definições de sistema da permissão.

O diferencial face à concorrência genérica é a **explicação técnica credível**: não basta dizer "permissão perigosa", explica-se *porquê*, com contexto. É aqui que a tua formação em cibersegurança se traduz em valor real para o utilizador.

---

## 2. Limitação crítica a validar ANTES de começar

Este ponto decide a viabilidade da app, por isso vem primeiro.

O Android tem vindo a **restringir progressivamente** o acesso de uma app aos dados de outras apps, precisamente por razões de segurança. Desde o Android 11 (API 30) que o `QUERY_ALL_PACKAGES` é uma permissão sensível — a Google exige justificação na submissão e **rejeita apps que a usem sem motivo aprovado**. Ferramentas antivírus/segurança são um dos casos de uso aceites, o que joga a teu favor, mas tens de o declarar corretamente.

**O que ainda consegues ler (via `PackageManager`):**
- Lista de apps instaladas (com `QUERY_ALL_PACKAGES` justificado).
- Permissões *declaradas* por cada app (`PackageInfo.requestedPermissions`).
- Se cada permissão está concedida ou negada (`PackageManager.checkPermission` / flags).
- Metadados: nome, ícone, versão, data de instalação, se é app de sistema.

**O que NÃO consegues (e não deves prometer):**
- Ver dados que outra app recolheu.
- Saber em tempo real se uma app está a *usar* o microfone/câmara neste instante (há indicadores de sistema para isto desde o Android 12, mas não uma API livre de monitorização de terceiros).
- Revogar permissões de outra app programaticamente — só podes **encaminhar** o utilizador para as definições.

**Ação obrigatória:** cria um projeto de teste mínimo e confirma, no dispositivo real com a versão de Android mais recente, o que a API te devolve. Não escrevas uma linha de produção antes disto.

---

## 3. Funcionalidades

### MVP (versão gratuita — o mínimo para lançar)
- Scan das apps instaladas e respetivas permissões.
- Score de privacidade global (0–100) e por app.
- Categorização das permissões por sensibilidade (localização, microfone, câmara, contactos, SMS, etc.).
- Explicação em texto simples de cada permissão sensível.
- Atalho direto para as definições de permissão de cada app.
- Ordenação das apps por risco.

### Versão premium (subscrição)
- **Monitorização contínua:** notificação quando uma app nova pede permissões sensíveis ou uma atualização adiciona permissões.
- **Relatório detalhado exportável** (PDF) — útil para quem quer prova/registo.
- **Histórico** da evolução do score ao longo do tempo.
- **Perfis de recomendação** (ex.: "modo mínimo", "modo equilibrado").
- Sem anúncios.

---

## 4. Arquitetura técnica

Sugiro app **100% nativa Android** (não híbrida), porque dependes fortemente de APIs de sistema e queres fiabilidade nas permissões.

| Camada | Tecnologia recomendada |
|---|---|
| Linguagem | **Kotlin** (padrão atual do Android) |
| UI | **Jetpack Compose** (declarativo, moderno) |
| Arquitetura | MVVM (Model-View-ViewModel) |
| Acesso a apps/permissões | `PackageManager`, `AppOpsManager` (uso avançado) |
| Base de dados local | **Room** (SQLite) — para histórico e cache |
| Assíncrono | Coroutines + Flow |
| Notificações | `WorkManager` (scans periódicos em segundo plano) |
| Subscrições | **Google Play Billing Library** |
| Geração de PDF | `PdfDocument` (nativo) ou biblioteca leve |

**Princípio de design:** *privacy by design*. A tua própria app não deve enviar dados do utilizador para fora do dispositivo. Todo o processamento é **local**. Isto é a tua maior arma de marketing e evita quase todos os problemas de RGPD. Se um dia adicionares backend, minimiza ao extremo.

---

## 5. Fases de desenvolvimento

**Fase 0 — Validação técnica (dias)**
Prova de conceito que lista apps e permissões no teu telemóvel. Confirma o ponto 2.

**Fase 1 — MVP funcional (semanas)**
Scan + score + explicações + atalhos para definições. UI simples mas limpa.

**Fase 2 — Motor de scoring**
Define a lógica de pontuação: peso por tipo de permissão, se a permissão faz sentido para a categoria da app, se está concedida. Este é o "cérebro" e o teu valor técnico. Documenta bem os critérios.

**Fase 3 — Monetização**
Integra o Google Play Billing, separa funcionalidades free/premium, adiciona monitorização em background com `WorkManager`.

**Fase 4 — Polimento**
Design, textos (a clareza das explicações vende), tradução PT/EN, ecrãs de onboarding, testes em vários dispositivos.

**Fase 5 — Publicação e iteração**
Lançamento, recolha de feedback, correções.

---

## 6. Publicação na Play Store — o processo

- **Conta de programador Google Play:** pagamento único de **25 USD** (uma vez, vitalício).
- **Verificação de identidade:** obrigatória para contas novas (documento + morada). Para conta individual há regras adicionais de verificação; prepara-te para este passo com antecedência.
- **Ficha da app:** ícone, screenshots, descrição, categoria, política de privacidade (obrigatória — precisas de um URL com uma política, mesmo simples).
- **Declaração de dados ("Data safety"):** vais ter de declarar que não recolhes dados — o que, se seguires o design local, é verdade e fácil.
- **Justificação de permissões sensíveis:** para `QUERY_ALL_PACKAGES` terás de preencher um formulário a explicar o caso de uso (segurança/antivírus). **Este é o passo onde apps deste tipo mais são rejeitadas** — sê rigorosa e honesta.
- **Revisão:** pode demorar de horas a vários dias.

---

## 7. Custos estimados

| Item | Custo |
|---|---|
| Conta programador Google Play | 25 USD (única vez) |
| Domínio para política de privacidade | ~10 €/ano (opcional; há alternativas grátis) |
| Ambiente de desenvolvimento (Android Studio) | Grátis |
| Design/ícones | 0 € se fizeres tu; variável se externalizares |
| **Marketing** | **O custo real e o fator decisivo** |

O desenvolvimento é essencialmente gratuito em ferramentas. O investimento a sério é **tempo** e, se quiseres tração, **divulgação**.

---

## 8. O lado realista do dinheiro

Sê honesta contigo nesta parte, porque é onde a maioria das apps falha:

- **A app não vende sozinha.** Há milhares de apps de "privacidade/limpeza/segurança" na Play Store. Uma app boa sem divulgação fica invisível.
- **A conversão de freemium é baixa** — tipicamente 1–5% dos utilizadores gratuitos passam a pagantes. Precisas de *volume* para que a subscrição gere algo relevante.
- **O rendimento inicial provável é modesto.** Encara isto como (a) um projeto de portefólio fortíssimo para a tua carreira em cibersegurança, e (b) um possível rendimento passivo que cresce devagar — não como um retorno rápido.

**Onde está a alavanca:** o teu ângulo de credibilidade técnica. Se acompanhares a app com conteúdo (posts, um vídeo curto a explicar permissões perigosas, presença em comunidades de privacidade), atrais utilizadores certos. O marketing de nicho baseado em *expertise* real é o que te distingue de apps clonadas.

---

## 9. Riscos principais

1. **Restrições futuras do Android** podem reduzir o que a app consegue ler — arquiteta para degradar com elegância.
2. **Rejeição na revisão** por má justificação de permissões — mitigável com rigor no formulário.
3. **Saturação do mercado** — mitigável com nicho e conteúdo, não com features a mais.
4. **Manutenção contínua** — cada nova versão do Android pode partir algo. Não é "lançar e esquecer".

---

## 10. Próximo passo concreto

Antes de qualquer código de produção: **Fase 0**. Instala o Android Studio, cria um projeto Kotlin + Compose, e escreve um ecrã único que lista as apps instaladas com as suas permissões no teu telemóvel. Isso responde à única pergunta que importa agora — *"a app que imagino é sequer possível na versão atual do Android?"* — e dá-te a base de tudo o resto.

---

*Nota: valores e políticas da Play Store (taxa de programador, regras de permissões, requisitos de verificação) mudam com frequência. Confirma sempre na documentação oficial da Google antes de decidir.*
