# SchemaLens — On-Device Schema Migration Governance & Risk Intelligence

> **Zero Cloud Egress · 100% On-Device Small Language Model (SLM) · Real-Time AST Impact Tracing**  
> Photograph or paste database schemas, trace affected call sites across your codebase offline, assess migration risks locally, and stream safe hot-patches directly to your desktop IDE.

---

## 📌 Overview

**SchemaLens** is an air-gapped developer tool designed to eliminate silent, breaking schema changes in production databases. By running lightweight Small Language Models (SLMs) and deterministic Abstract Syntax Tree (AST) static analysis entirely on-device, SchemaLens gives engineering teams deep architectural visibility without exposing proprietary source code or database schemas to external cloud APIs.

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                             SCHEMALENS PIPELINE                             │
│                                                                             │
│  [Whiteboard OCR / DDL] ──► [On-Device AST Trace] ──► [On-Device SLM Audit] │
│           │                          │                         │            │
│     CameraX / ML Kit           Multi-Dialect               Gemma 2B /       │
│      Canvas ER Graph          Call Site Parser          Smart Engine        │
│                                                                │            │
│  [Git Pre-Commit Gate]  ◄── [IDE Hot-Patch Bridge] ◄───────────┘            │
│     CI/CD Enforcement        Dual-Screen Companion                          │
└─────────────────────────────────────────────────────────────────────────────┘
```

---

## ⚡ Key Features

### 1. 📷 Multimodal Schema Ingestion
- **On-Device Vision OCR:** Capture physical whiteboard architecture diagrams or ER sketches with CameraX and Google ML Kit.
- **SQL DDL Parser:** Ingests raw SQL `CREATE TABLE` definitions and automatically detects primary keys, foreign keys, unique constraints, and datatypes.
- **Interactive ER Diagram:** Renders database schemas as an interactive Entity-Relationship graph directly on an Android Compose canvas.
- **Identifier Extraction:** Automatically isolates table names and column identifiers into quick-action filter chips.

### 2. 🔍 Zero-Latency AST Call Site Tracing
- **Deterministic Static Parser:** Traces usage of target ORM packages (`drizzle-orm`, `prisma`, `typeorm`, etc.) across multi-file code buffers.
- **Import Resolution:** Resolves named imports, aliased imports (`import { eq as matchEq }`), namespace imports, and CommonJS `require(...)`.
- **Instant Call Site Mapping:** Identifies file names, line numbers, and exact code context with 0ms network latency.

### 3. 🧠 Air-Gapped SLM Risk Assessment & Telemetry
- **On-Device SLM Inference:** Uses quantized Google Gemma 2B via MediaPipe Tasks GenAI, backed by a local Smart Schema heuristic engine (100% offline, zero network requests).
- **270° Dynamic Risk Dial:** Animated visual risk gauge scoring proposed migrations from 0 to 100.
- **Granular Severity Verdicts:** Categorizes every affected call site as `BREAKING`, `RISKY`, or `SAFE` with inline remediation recommendations.
- **Hardware Telemetry HUD:** Displays real-time device stats, including heap usage, inference latency (e.g., ~135ms), and 0 KB network traffic guarantees.
- **Voice Q&A:** Voice query interface to ask questions about schema risks directly to the local model.

### 4. ⚡ Dual-Screen Hot-Patch Companion Bridge
- **Desktop IDE Integration:** Wirelessly bridges migration patches from your mobile device to your desktop IDE (VS Code, Android Studio, IntelliJ) via structured clipboard payloads and local intents.
- **Multi-Dialect Code Generation:** Dynamically generates migrations and schema definitions for:
  - **Drizzle ORM** (`TypeScript`)
  - **Prisma Schema** (`.prisma`)
  - **TypeORM** (`TypeScript`)
  - **PostgreSQL / Standard SQL** (`.sql`)

### 5. 🛡️ CI/CD & Governance Hub
- **Git Pre-Commit Hook Generator:** Generates custom Bash and PowerShell `.git/hooks/pre-commit` scripts that inspect staged files and block commits altering protected columns without migration scripts.
- **Markdown Audit Reports:** Exports compliance-ready migration audit reports with timestamps, risk breakdowns, and line-by-line verdicts.

---

## 🏗️ Architecture: Two Architectural Zones

| Zone | Network Mode | Core Responsibilities |
| :--- | :--- | :--- |
| **🔴 Red Light Zone** | **🔒 Strictly On-Device (0 Network Calls)** | CameraX Vision OCR, on-device voice dictation, pure Kotlin AST import parsing, and Canvas ER relationship visualization. |
| **🟢 Green Light Zone** | **🧠 On-Device Air-Gapped SLM Reasoning** | Gemma 2B SLM inference, 270° risk scoring, migration diff synthesis, IDE Hot-Patch Companion, and Git Hook generation. |

---

## 📋 Prerequisites

To clone, build, and run SchemaLens, you will need:
- **Operating System:** Windows, macOS, or Linux
- **JDK:** Java Development Kit 17 or higher
- **Android Studio:** Android Studio 2024.1+ (Koala, Ladybug, Meerkat, or newer)
- **Target Device / Emulator:**
  - Android 8.0+ (API Level 26 or higher)
  - Camera support (for live whiteboard scanning)

---

## 🚀 How to Clone and Run

### Method 1: Using Android Studio (Recommended)

1. **Clone the repository:**
   ```bash
   git clone https://github.com/kiran-source/Schema-Lens.git
   cd Schema-Lens
   ```
2. **Open the project in Android Studio:**
   - Launch Android Studio.
   - Click **Open** (or **File → Open...**).
   - Select the cloned `Schema-Lens` directory.
3. **Sync Gradle:**
   - Android Studio will automatically recognize the project and sync Gradle dependencies using the included wrapper.
4. **Run the application:**
   - Connect your physical Android phone (enable **USB Debugging** in Developer Options) or start an Android Virtual Device (AVD).
   - Click the green **Run 'app'** button (or press `Shift + F10`).
   - The application will build and launch directly on your device.

---

### Method 2: Command Line (CLI / Terminal)

You can build and install the application directly using the Gradle wrapper without opening Android Studio:

#### On Windows (PowerShell / Command Prompt):
```powershell
# Clone the repository
git clone https://github.com/kiran-source/Schema-Lens.git
cd Schema-Lens

# Run unit tests to verify build integrity
.\gradlew testDebugUnitTest

# Build the debug APK
.\gradlew assembleDebug

# Install directly to a connected device or running emulator
.\gradlew installDebug
```

#### On Linux / macOS (Bash / Zsh):
```bash
# Clone the repository
git clone https://github.com/kiran-source/Schema-Lens.git
cd Schema-Lens

# Grant executable permission to the wrapper
chmod +x gradlew

# Run unit tests
./gradlew testDebugUnitTest

# Build and install
./gradlew assembleDebug
./gradlew installDebug
```

The compiled APK will be generated at:
```
app/build/outputs/apk/debug/app-debug.apk
```

---

## 🧪 Validating Features with Included Test Assets

### 1. Testing On-Device Whiteboard OCR
The repository includes a sample whiteboard ER diagram:
- Located at: [`sample_whiteboard_er_diagram.jpg`](./sample_whiteboard_er_diagram.jpg)
- **Test via Camera:** Open SchemaLens, navigate to **Step 0: Connect**, tap **"Scan"**, and point your camera at `sample_whiteboard_er_diagram.jpg` displayed on your monitor.
- **Test via Gallery:** Tap the **Gallery** icon next to the camera button, select the image, and observe real-time identifier extraction and ER graph generation.

### 2. Testing Pre-Configured Scenarios
In **Step 0 (Connect)** and **Step 2 (Review)**, tap the preset chips:
- **E-Commerce:** Tests breaking column renames (`hashed_password` → `password_hash`) and dropped permissions.
- **SaaS Multi-Tenant:** Tests multi-tenant organization isolation and API key expiration.
- **FinTech Ledger:** Tests precision currency types and immutable audit logs.

### 3. Running Automated Tests
Run the unit test suite covering the AST parser, DDL entity extractor, and risk evaluation logic:
```bash
./gradlew test
```

---

## 🛠️ Technology Stack

- **Platform:** Android (Kotlin 2.0.21, Jetpack Compose, Material 3)
- **Architecture:** MVVM + Kotlin Coroutines + StateFlow
- **Vision OCR:** Google ML Kit Text Recognition (`com.google.mlkit:text-recognition:16.0.1`)
- **Camera:** CameraX (`androidx.camera:camera-camera2:1.4.1`)
- **SLM & GenAI:** Google MediaPipe Tasks GenAI (`com.google.mediapipe:tasks-genai:0.10.14`)
- **Build System:** Gradle 8.8 with Kotlin DSL (`build.gradle.kts`)

---

## 📄 License

This project is licensed under the Apache License 2.0 - see the [LICENSE](LICENSE) file for details.
