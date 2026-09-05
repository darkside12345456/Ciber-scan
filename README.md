# PrivacyScanner Suite — duas apps Android

Projeto Gradle com **dois produtos separados** e uma biblioteca comum, para que
cada app tenha o seu público, a sua ficha na Play Store e a sua revisão de
permissões (ver `docs/`):

| Módulo | O que é |
|---|---|
| **`:app-scanner`** | **Scanner de Privacidade** — analisa as permissões das apps instaladas, dá um score e recomenda ações. 100% local, **sem internet**. `applicationId = com.jp.privacyscanner`. |
| **`:app-bounty`** | **Bug Bounty** — workspace para alvos autorizados (programas, checklist, achados, CVSS, relatório + assistente de IA opcional). `applicationId = com.jp.bugbounty`. |
| **`:core`** | Tema/design base partilhado pelas duas apps. |

Porquê separadas: juntar o `QUERY_ALL_PACKAGES` (Scanner) com ferramentas de bug
bounty na mesma app aumenta o risco de rejeição na Google e confunde públicos. O
assistente de IA (única funcionalidade com internet) vive **só** no Bug Bounty,
para o Scanner poder prometer, sem asteriscos, que nada sai do dispositivo.

> **Princípio central:** *privacy by design*. No Scanner, todo o processamento é
> **local** e não há sequer permissão de internet.

---

## O que está implementado (MVP — versão gratuita)

- ✅ **Scan** das apps instaladas e respetivas permissões via `PackageManager`.
- ✅ **Score de privacidade** global (0–100) e por app.
- ✅ **Categorização** das permissões por sensibilidade (localização, microfone,
  câmara, contactos, SMS, etc.).
- ✅ **Explicação em linguagem simples** de cada permissão sensível — o valor
  técnico credível que distingue a app.
- ✅ **Recomendações acionáveis por app** ("Desliga já: microfone", com o porquê),
  ordenadas por urgência.
- ✅ **Ecrã de onboarding** que explica a app e justifica o acesso à lista de apps.
- ✅ **Ordenação das apps por risco** (a mais arriscada primeiro).
- ✅ **Atalho direto** para as definições de permissão de cada app.
- ✅ **Histórico** do score global (base de dados local Room).
- ✅ **Monitorização contínua** em segundo plano (`WorkManager`): avisa quando uma
  app passa a ter permissões sensíveis novas.
- ✅ **Módulo Bug Bounty** (para alvos autorizados): gestão de programas e âmbito
  (editável), checklist metodológica por fases, registo de achados e geração de
  rascunho de relatório em Markdown, com **exportação para PDF** e partilha.
- ✅ **Calculadora CVSS v3.1** (métricas Base) integrada no editor de achados:
  calcula o score e a severidade e preenche-os no relatório.
- ✅ **Assistente de redação com IA** (opcional, opt-in): melhora o rascunho do
  relatório via API do Claude, com a chave do próprio utilizador (guardada
  cifrada). É a **única** funcionalidade que usa a internet.
- ✅ **Ecrã de Definições**: gestão da chave da IA (definir, trocar, apagar) e
  nota de privacidade. Score CVSS visível na lista de achados.

> **Privacidade da IA:** o assistente é a única parte da app que envia dados para
> fora (o texto do relatório → API do Claude). Está desligado por omissão, requer
> a chave do utilizador e a permissão `INTERNET` só existe por causa dele. Todo o
> resto — scanner, scoring, monitorização, bug bounty — é 100% local. Declarar na
> secção *Data safety* da Play Store.
- ✅ Testes unitários da lógica pura — scoring, recomendações, monitorização,
  gerador de relatório e CVSS v3.1 (22 testes).

> **Nota ética do módulo Bug Bounty:** é uma ferramenta *organizacional e
> educativa* (workspace + checklist + relatório). Não executa ataques nem
> varreduras automáticas. Usa-a apenas em alvos dentro do âmbito autorizado de
> um programa.

## Arquitetura

Segue a stack recomendada no relatório (secção 4):

| Camada | Tecnologia |
|---|---|
| Linguagem | Kotlin |
| UI | Jetpack Compose + Material 3 |
| Arquitetura | MVVM |
| Acesso a apps/permissões | `PackageManager` |
| Base de dados local | Room (SQLite) |
| Assíncrono | Coroutines + Flow |
| Navegação | Navigation Compose |

```
app/src/main/java/com/jp/privacyscanner/
├── data/
│   ├── model/          AppInfo, PermissionInfo, PermissionCategory, RiskLevel
│   ├── permissions/    PermissionCatalog  (categorias + explicações — o catálogo)
│   ├── scanner/        AppScanner         (leitura via PackageManager)
│   ├── scoring/        ScoringEngine      (o "cérebro": lógica de pontuação)
│   └── local/          Room: ScoreHistory, DAO, AppDatabase
├── domain/             PrivacyRepository  (orquestra scan + score + histórico)
├── ui/
│   ├── theme/          Cores, tema Material 3
│   ├── components/     ScoreGauge, RiskChip
│   ├── home/           HomeScreen + HomeViewModel
│   └── detail/         AppDetailScreen
├── util/               SettingsNavigator  (atalho para as definições)
├── MainActivity.kt     Navegação Compose
└── PrivacyScannerApp.kt
```

## Como compilar

Requer **Android Studio** (Ladybug ou mais recente) e um dispositivo/emulador
com **Android 8.0+ (API 26)**.

```bash
# Testes unitários das duas apps
./gradlew test

# Instalar o Scanner / o Bug Bounty num dispositivo ligado
./gradlew :app-scanner:installDebug
./gradlew :app-bounty:installDebug
```

> **Nota:** ao compilar pela primeira vez o Gradle descarrega o Android Gradle
> Plugin e as dependências AndroidX (precisa de rede). Em Android Studio, basta
> abrir a pasta do projeto e deixar o Gradle sincronizar.

### Build automática (sem PC) — GitHub Actions

Cada `push` dispara o workflow `.github/workflows/android.yml`, que corre os
testes e compila o APK de debug num runner com o Android SDK. Vai a **Actions**
no repositório e descarrega o artefacto **`privacyscanner-debug-apk`** para
instalar no telemóvel. É a forma recomendada de obter uma build enquanto o
ambiente de desenvolvimento não tiver acesso ao `dl.google.com`.

## Limitações conhecidas (por design do Android — relatório, secção 2)

- Só conseguimos ler as permissões **declaradas** e se estão **concedidas**.
- **Não** conseguimos ver o uso em tempo real do microfone/câmara.
- **Não** conseguimos revogar permissões de outra app — só **encaminhar** o
  utilizador para as Definições.
- `QUERY_ALL_PACKAGES` exige **justificação** na submissão à Play Store
  (caso de uso: segurança). Ver `AndroidManifest.xml`.

## O que falta / próximos passos

Ver a secção **"O que acrescentar"** na resposta técnica e o roadmap por fases
no relatório (secção 5). Em resumo:

1. **Fase 0 — validação técnica** num dispositivo real com o Android mais recente.
2. **Premium:** monitorização contínua (`WorkManager`), relatório PDF, perfis de
   recomendação, remoção de anúncios, via Google Play Billing.
3. **Polimento:** ícones de app reais, onboarding, tradução EN, testes em vários
   dispositivos.
4. **Publicação:** política de privacidade (URL), declaração *Data safety*,
   formulário de justificação de `QUERY_ALL_PACKAGES`.

---

*Valores e políticas da Play Store mudam com frequência — confirma sempre na
documentação oficial da Google antes de decidir.*
