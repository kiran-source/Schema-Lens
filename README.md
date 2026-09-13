# SchemaLens — AirTrace DB

> **iQOO Hackathon 2026 Submission**  
> Photograph or paste a database schema, trace every call site affected by an upcoming change offline, assess migration risk with 100% on-device air-gapped SLM reasoning, and copy the ORM patch to Office Kit.

---

## 🏗️ Architecture: Two Explicit Zones

| Phase Zone | Network Status | Features & Capabilities |
| :--- | :--- | :--- |
| **🔴 Red Light Zone** | **🔒 Strictly On-Device (0 Network Calls)** | • **CameraX + ML Kit OCR**: Photograph whiteboard or printed schemas; extracts candidate SQL/ORM identifiers on-device.<br>• **On-Device Voice Notes**: `SpeechRecognizer` with `EXTRA_PREFER_OFFLINE = true` and live partial results.<br>• **Pure Kotlin AST Import Parser**: Resolves named, aliased, namespace, and destructured CommonJS imports (`import { a as b }`, `require(...)`) across multi-file code buffers.<br>• **ER Relationship Visualizer**: Canvas-drawn table nodes and foreign-key link diagram. |
| **🟢 Green Light Zone** | **🧠 On-Device Air-Gapped SLM Reasoning** | • **On-Device SLM Engine**: Quantized Gemma 2B running via Google MediaPipe Tasks GenAI (`com.google.mediapipe:tasks-genai:0.10.14`) with Smart Schema heuristic fallback. 100% air-gapped with 0 network calls.<br>• **Per-Site Severity Breakdown**: Classifies each site as `SAFE`, `RISKY`, or `BREAKING` with impact notes.<br>• **Circular Risk Gauge & Telemetry**: Animated 0–100 score gauge with real-time hardware telemetry HUD (RAM heap, inference latency, 0 KB net traffic).<br>• **Office Kit Clipboard Bridge**: One-tap copy of the corrected ORM patch with system clipboard handoff.<br>• **Custom Endpoint Option**: Optional toggle for custom local/server LLM or OpenAI-compatible endpoint. |

---

## 🎨 Design System

- **Background:** `#12151A`
- **Panel:** `#1A1E25`
- **Nested Panel:** `#20252D`
- **Border:** `#2A2F38`
- **Text:** `#EDEFF3` (Primary), `#8B93A1` (Dim), `#5A6270` (Faint)
- **Accent:** `#2DD4BF` (Teal)
- **Risk Severity:**
  - `BREAKING`: `#E5484D` (Background: `#3A2325`)
  - `RISKY`: `#F5A623` (Background: `#3A311D`)
  - `SAFE`: `#3DD68C` (Background: `#1C3329`)

---

## 🚀 Building & Running

### Option 1: In Android Studio
1. Open this directory in **Android Studio (2024+)**.
2. Sync Gradle dependencies.
3. Select your device (e.g., `vivo V2511`) or emulator running Android 8.0+ (API 26+).
4. Click **Run 'app'** (or press `Shift + F10`). No cloud API keys or cloud tokens required!

### Option 2: Running Unit Tests
Execute the unit tests verifying AST parsing, aliased import resolution, OCR token filtering, and AI assessment parsing:
```bash
./gradlew test
```

---

## 🧪 Testing the Live Hackathon Demo Flow

1. **Launch App**: Immediate Red Light on-device view opens.
2. **Schema Input**:
   - Tap **Load Preset** (loads sample Drizzle ORM code and schema), OR
   - Tap the **Camera** button to take a photo of a whiteboard schema, OR
   - Paste code directly into the multi-file buffer.
3. **Voice Dictation**: Tap the **Mic** button and dictate schema changes offline with live partial text.
4. **Static Trace**: Tap **Trace "drizzle-orm" (Offline AST)** to instantly find and flag all affected call sites with zero network calls.
5. **ER Graph**: View the custom canvas ER relationship graph connecting tables and foreign keys.
6. **On-Device SLM Assessment**: Tap **Assess Breaking Risk (On-Device SLM)** to run local reasoning and get the risk score (0–100), per-call-site breakdown, and migration explanation with real-time hardware telemetry.
7. **Office Kit Sync**: Tap **Copy to Clipboard** to hand off the generated ORM patch to your development laptop via Office Kit!
