# java-cli

**English** | [中文说明](README_zh.md)

Modern, production-grade template repository for building standalone CLI tools with **Java 25, JPMS Modularity, Maven, Picocli**, and **GraalVM Native Image**.

Features executable sample commands, version filtering, strict ANSI color rules, comprehensive unit tests and native smoke assertions, cross-platform packaging, and Native Image best practices distilled from the open-source [`lihuu/li-tools`](https://github.com/lihuu/li-tools) ecosystem.

---

## 🌟 Key Features & Best Practices

- **Java 25 & JPMS Modularity**: Pre-configured `module-info.java` enforcing clean boundaries and explicit reflection access out-of-the-box.
- **Modern Maven Toolchain**:
  - `maven-compiler-plugin:3.16.0` (with bundled ASM 9.10 natively supporting Java 25 class file major version 69).
  - `maven-surefire-plugin:3.6.0` (with `<useModulePath>false</useModulePath>` ensuring clean test runner isolation).
- **Zero-Reflection Native Compilation**:
  - Pre-wired with `picocli-codegen` compile-time annotation processing, generating native reachability metadata during `javac` and eliminating dynamic runtime reflection.
- **Security Provider Detox**:
  - Pre-configured [`scripts/pruned-security.properties`](scripts/pruned-security.properties) that **strictly preserves all JDK baseline security and algorithm policies (e.g. TLS 1.0/1.1 rejection, weak key limits)** while pruning legacy enterprise providers (`JdkLDAP`, `SunJGSS`, `SunPCSC`), completely stripping out `java.rmi` and `java.naming` to shrink image footprint.
- **Balanced `-O2` Optimization**:
  - Defaulting to `-O2` for native compilation to avoid `-O3` code bloat and runaway inlining, achieving fast compilation (~19s) and compact standalone binaries (~12 MB).
- **Standalone Distribution**:
  - Produces a single, zero-dependency executable with sub-10ms startup times on macOS ARM64 and Linux.

---

## 🚀 Creating a New CLI from this Template

Click **Use this template → Create a new repository** on GitHub, or run via GitHub CLI:

```sh
gh repo create my-tool --template lihuu/java-cli --public --clone
cd my-tool
```

> **Note**: This creates a new standalone repository, not a fork. Subsequent updates to this template will not overwrite your project.

### 5-Minute Checklist for Your New Project:

1. **Update POM Coordinates**: Edit `pom.xml` to set your desired `artifactId`, `name`, `description`, and `version` (e.g., `0.1.0`).
2. **Rename Package**: Refactor `src/main/java/io/github/lihuu/javacli` to your preferred package name, updating `module-info.java` and the `<main.class>` property in `pom.xml`.
3. **Add Your Commands**: Replace `cli/HelloCommand.java` with your CLI logic.
4. **Verify Tests & Native Build**: Run `scripts/dev.sh test` and `scripts/dev.sh native-build`.
5. **Setup Git Hooks**: Run `git config core.hooksPath "$(pwd)/.githooks"` to enable commit validation.

---

## 🛠️ Quick Start

### Prerequisites
- **JDK 25** (GraalVM for JDK 25 recommended)
- **Maven** (3.9+)
- Native C/C++ build tools (Xcode Command Line Tools on macOS, `build-essential` on Linux)

```sh
# Set JDK 25
export JAVA_HOME=$(/usr/libexec/java_home -v 25)
export PATH="$JAVA_HOME/bin:$PATH"

# Run tests
scripts/dev.sh test

# Run JVM application
scripts/dev.sh run --help
scripts/dev.sh run hello --name Ada
```

Output:
```text
Hello, Ada!
```

---

## 📦 Native Image Build & Release

### 1. Build and Run Native Executable

```sh
# Compile standalone native binary (emitted to target/java-cli)
scripts/dev.sh native-build

# Run automated smoke assertions (help, version, argument parsing, ANSI escapes)
scripts/smoke.sh target/java-cli

# Run native binary
scripts/dev.sh native hello --name World
```

### 2. Local Installation

```sh
# Install to ~/.local/bin (default, or customize via CLI_INSTALL_DIR)
scripts/dev.sh install
```

### 3. Package Release Bundles

```sh
scripts/release.sh
```

Generates release artifacts in `dist/`:
- `java-cli-<version>-<platform>-<arch>.tar.gz`
- `java-cli-<version>-<platform>-<arch>.tar.gz.sha256`

Includes the standalone executable, README, and `THIRD-PARTY-NOTICES.txt` licenses.

---

## 🎨 Color and Exit Code Contract

- **Terminal-Adaptive Colors**: ANSI color output is enabled only in interactive TTY terminals. Full support for `--no-color` flag and the standard `NO_COLOR` environment variable. Redirected output strips all ANSI escapes automatically.
- **Exit Codes**:
  - `0`: Success.
  - `2`: CLI usage / argument syntax errors (written to `stderr`).
  - `1`: Expected business execution failures.

---

## 📄 License

This template is licensed under the [MIT License](LICENSE).
