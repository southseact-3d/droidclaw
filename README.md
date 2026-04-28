# Agent — OpenClaw-style AI Agent for Android

A fully self-contained AI agent that runs entirely on Android. No server required.
Battery-efficient WorkManager scheduling, multi-provider LLM support with automatic fallback,
skills system, and heartbeat/cron scheduling.

---

## Features

| Feature | Details |
|---|---|
| **Chat** | Streaming chat with any configured provider |
| **Skills** | Install SKILL.md files via URL or write inline — injected into agent context |
| **Heartbeat** | Periodic background check (min 15 min) — notifies only when needed |
| **Cron jobs** | Scheduled prompts that run at intervals and push results as notifications |
| **Provider fallback** | Nvidia NIM → OpenRouter → Kilo Gateway (configurable priority order) |
| **Battery efficient** | WorkManager only — zero background presence between ticks |

---

## Supported Providers

| Provider | Default Base URL | Notes |
|---|---|---|
| **Nvidia NIM** | `https://integrate.api.nvidia.com/v1` | OpenAI-compatible, access to Llama, Mistral, Nemotron |
| **OpenRouter** | `https://openrouter.ai/api/v1` | 200+ models, pay-per-use |
| **Kilo Gateway** | `https://api.kilo.ai/api/gateway/` | Claude, GPT-4o and more via single key |

All three use the OpenAI-compatible chat completions endpoint. Add your API key in Settings and the app tries providers in priority order, falling back automatically on any error.

---

## Getting Started

### Prerequisites
- Android Studio Ladybug (2024.2) or newer
- JDK 17
- Android SDK 35
- A device or emulator running Android 8.0 (API 26)+

### Build & run

```bash
git clone https://github.com/yourname/agent-android.git
cd agent-android
./gradlew assembleDebug
adb install app/build/outputs/apk/debug/app-debug.apk
```

### First-time setup

1. Open the app → tap **Settings**
2. Tap the pencil icon next to any provider
3. Paste your API key and select a model
4. Enable the provider toggle
5. Reorder providers by priority using the ↑ / ↓ arrows
6. Head back to **Chat** and send a message

---

## Skills system

Skills are Markdown files that get injected into the agent's system context.

### SKILL.md format

```markdown
---
name: My Skill
description: Does something useful
version: 1.0.0
---

## Instructions

You can use the following capabilities when this skill is active:
- Do X when the user asks for Y
- Always format Z as a table

## Tools

tool_name: my_tool
description: Runs my tool
parameters:
  input: string
```

### Installing a skill

- **From URL**: Settings → Skills → Add → Install URL → paste raw SKILL.md URL
- **Manually**: Settings → Skills → Add → Write manually → fill in name + content

---

## Heartbeat

The heartbeat runs a background check at your configured interval (min 15 minutes).

1. Go to **Schedule**
2. Enable the **Heartbeat** toggle
3. Set interval and active hours (e.g. 08:00–22:00)
4. Optionally fill in the **HEARTBEAT.md** checklist — bullet points describing what the agent should check

The agent responds `HEARTBEAT_OK` if nothing needs attention (silent) or pushes a notification with a summary if something does.

**Note**: Android Doze mode may delay heartbeats when the device is stationary and unplugged. This is expected behaviour.

---

## Cron jobs

Cron jobs run a prompt on a fixed interval.

1. Go to **Schedule** → **Add job**
2. Give it a name and a prompt (e.g. "Summarise what I should focus on today")
3. Set the interval
4. Enable "Notify on result" to push the response as a notification
5. Tap ▶ to run immediately

---

## GitHub Actions CI

The workflow at `.github/workflows/build.yml` builds on every push:

| Job | Trigger | Output |
|---|---|---|
| `lint` | All pushes/PRs | Lint HTML report |
| `build-debug` | All pushes/PRs | Debug APK (14-day retention) |
| `build-release` | `main` branch or manual | Signed AAB (30-day retention) |
| `release` | Version tags `v*` | GitHub Release with APK + AAB |

### Setting up signed release builds

In your GitHub repo → Settings → Secrets → Actions, add:

| Secret | Value |
|---|---|
| `KEYSTORE_BASE64` | `base64 -i your.keystore` output |
| `KEYSTORE_PASSWORD` | Your keystore password |
| `KEY_ALIAS` | Your key alias |
| `KEY_PASSWORD` | Your key password |

To create a release:

```bash
git tag v1.0.0
git push origin v1.0.0
```

---

## Architecture

```
app/
├── agent/          AgentCore — context assembly, tool loop, heartbeat/cron execution
├── data/
│   ├── db/         Room database, DAOs
│   ├── models/     Entities, type converters
│   └── repository/ SettingsRepository (DataStore)
├── providers/      LlmProviderClient — OkHttp streaming + fallback logic
├── ui/
│   ├── chat/       Chat screen + ViewModel
│   ├── skills/     Skills screen + ViewModel
│   ├── scheduler/  Scheduler screen + ViewModel
│   ├── settings/   Settings screen + ViewModel
│   └── theme/      Compose theme, colours, typography
└── worker/         HeartbeatWorker, CronWorker, BootReceiver
```

### Battery design

- No foreground service — zero persistent background presence
- `PeriodicWorkRequest` with 15-min minimum interval
- Workers start, do their work (2–8 seconds), then exit
- Notification gated behind `HEARTBEAT_OK` check — silent by default
- Respects Doze mode and active hours config

---

## Manufacturer battery optimisation

Some manufacturers (Samsung, Xiaomi, Huawei, OnePlus) aggressively kill background workers.
If the heartbeat stops working, guide the user to:

**Settings → Battery → Background app management → Agent → No restrictions**

Or link directly with:

```kotlin
val intent = Intent().apply {
    component = ComponentName(
        "com.miui.powerkeeper",
        "com.miui.powerkeeper.ui.HiddenAppsContainerManagementActivity"
    )
}
```

See [dontkillmyapp.com](https://dontkillmyapp.com) for per-manufacturer deep links.

---

## Licence

MIT
