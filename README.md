# SchemaLens — AirTrace DB

> **iQOO Hackathon 2026 Submission**  
> Photograph or paste a database schema, trace every call site affected by an upcoming change offline, assess migration risk, and copy the ORM patch to Office Kit.

---

## 🏗️ Architecture: Two Explicit Zones

| Phase Zone | Network Status | Features & Capabilities |
| :--- | :--- | :--- |
| **🔴 Red Light Zone** | **🔒 Strictly On-Device (0 Network Calls)** | • **CameraX + ML Kit OCR**: Photograph whiteboard or printed schemas; extracts candidate SQL/ORM identifiers on-device.<br>• **On-Device Voice Notes**: `SpeechRecognizer` with `EXTRA_PREFER_OFFLINE = true` and live partial results.<br>• **Pure Kotlin AST Import Parser**: Resolves named, aliased, namespace, and destructured CommonJS imports (`import { a as b }`, `require(...)`) across multi-file code buffers.<br>• **ER Relationship Visualizer**: Canvas-drawn table nodes and foreign-key link diagram. |
| **🟢 Green Light Zone** | **☁️ Cloud Reasoning (Minimal Payload)** | • **Deep Risk Assessment**: Sends ONLY the target package name, change notes, and flagged call sites to Claude Sonnet 4.6 (`POST https://api.anthropic.com/v1/messages`).<br>• **Per-Site Severity Breakdown**: Classifies each site as `SAFE`, `RISKY`, or `BREAKING` with impact notes.<br>• **Circular Risk Gauge**: Animated 0–100 score gauge.<br>• **Office Kit Clipboard Bridge**: One-tap copy of the corrected ORM patch with system clipboard handoff. |

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
1. Open this directory in **Android Studio Hedgehog / Jellyfish / Koala (2024+)**.
2. Sync Gradle dependencies.
3. Add your Anthropic API key to `local.properties` (optional, can also be entered in the app settings modal):
   ```properties
   ANTHROPIC_API_KEY=sk-ant-api03-...
   ```
4. Run on an Android device or emulator running Android 8.0+ (API 26+).

### Option 2: Running Unit Tests
Execute the unit tests verifying pure AST parsing, aliased import resolution, and JSON parsing:
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
6. **Deep Cloud Assessment**: Tap **Run Deep Risk Assessment** (Green Light) to get Claude's risk score (0–100), per-call-site breakdown, and migration explanation.
7. **Office Kit Sync**: Tap **Copy to Clipboard** to hand off the generated ORM patch to your development laptop via Office Kit!
