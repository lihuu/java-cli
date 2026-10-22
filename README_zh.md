# java-cli

[English](README.md) | **中文说明**

现代 Java CLI 工具脚手架模板：**Java 25、JPMS 模块化、Maven、Picocli**，使用 **GraalVM Native Image** 进行零依赖单文件分发。

包含了可运行的示例命令、版本注入、终端 ANSI 颜色规则、全套单元测试与冒烟断言、跨平台打包发布脚本，以及沉淀自开源项目 [`lihuu/li-tools`](https://github.com/lihuu/li-tools) 的 Native Image 编译与安全最佳实践。

---

## 🌟 核心特性与最佳实践

- **Java 25 & JPMS 模块化**：预置 `module-info.java`，原生利用 Java 强封装与模块反射边界，开箱即用。
- **现代化构建链**：
  - `maven-compiler-plugin:3.16.0`（内置 ASM 9.10 原生支持 Java 25 字节码版本 69，无需外部 ASM 依赖 hack）。
  - `maven-surefire-plugin:3.6.0`（配置 `<useModulePath>false</useModulePath>`，隔离测试 classpath 与模块类加载）。
- **零反射 Native Image**：
  - 集成 `picocli-codegen` 编译期注解处理器，在 `javac` 阶段提前生成 GraalVM 所需的指令元数据，无需运行时动态反射或配置庞大的反射 JSON。
- **安全 Provider 深度脱毒（Detox）**：
  - 预置 [`scripts/pruned-security.properties`](scripts/pruned-security.properties)，在**严格保留构建 JDK 算法安全策略（TLS 1.0/1.1 禁用、弱密钥阻断）**的前提下，剔除 JdkLDAP、SunJGSS 等企业内网包，阻断 `java.rmi`、`java.naming` 等无用模块的传递引入，大幅精简二进制体积。
- **默认 `-O2` 优化策略**：
  - 经严格性能评测，在静态无 PGO 编译下默认采用 `-O2`，避免 `-O3` 引发的代码内联爆炸（消除 83% 的编译耗时膨胀与 37% 的二进制体积膨胀）。
- **单文件独立可执行文件**：
  - Native 产物仅 ~12 MB，启动时间低于 10ms，零依赖交付，支持 macOS ARM64 与 Linux x86_64/aarch64。

---

## 🚀 从模板创建新工具

在 GitHub 仓库主页点击 **Use this template → Create a new repository**，也可以通过 GitHub CLI：

```sh
gh repo create my-tool --template lihuu/java-cli --public --clone
cd my-tool
```

> **注意**：这是独立的新仓库，不是 fork；模板后续更新不会自动影响已有项目。

### 新项目 5 分钟上手清单：

1. **修改坐标**：编辑 `pom.xml`，修改 `artifactId`、`name`、`description` 与 `version`（例如从 `0.1.0` 开始）。
2. **重命名包名**：按需将 `src/main/java/io/github/lihuu/javacli` 目录和 package 声明替换为你自己的命名空间，同步修改 `module-info.java` 以及 `pom.xml` 中的 `<main.class>`。
3. **实现业务命令**：修改或替换 `cli/HelloCommand.java` 为你的业务命令逻辑。
4. **验证构建与冒烟测试**：运行 `scripts/dev.sh test` 以及 `scripts/dev.sh native-build`。
5. **初始化 Git hooks**：执行 `git config core.hooksPath "$(pwd)/.githooks"` 启用规范提交防护。

---

## 🛠️ 快速开始

### 环境依赖
- **JDK 25**（推荐使用 GraalVM for JDK 25）
- **Maven** (3.9+)
- 本机 C/C++ 工具链（macOS 使用 Xcode Command Line Tools，Linux 使用 `build-essential`）

```sh
# macOS 选择 JDK 25（其他平台自行设置 JAVA_HOME）
export JAVA_HOME=$(/usr/libexec/java_home -v 25)
export PATH="$JAVA_HOME/bin:$PATH"

# 运行单元测试
scripts/dev.sh test

# JVM 模式运行开发命令
scripts/dev.sh run --help
scripts/dev.sh run hello --name Ada
```

输出：
```text
Hello, Ada!
```

---

## 📦 Native Image 构建与分发

### 1. 构建与本地运行

```sh
# 构建 Native 二进制 (产物生成在 target/java-cli)
scripts/dev.sh native-build

# 运行全套自动化冒烟测试
scripts/smoke.sh target/java-cli

# 直接运行原生二进制
scripts/dev.sh native hello --name World
```

### 2. 本机安装

```sh
# 安装到 ~/.local/bin（默认路径，或通过 CLI_INSTALL_DIR 自定义）
scripts/dev.sh install
```

### 3. 本地打包发布归档

```sh
scripts/release.sh
```
将在 `dist/` 目录下生成：
- `java-cli-<version>-<platform>-<arch>.tar.gz`
- `java-cli-<version>-<platform>-<arch>.tar.gz.sha256`

包含独立原生二进制、第三方开源许可证声明（`THIRD-PARTY-NOTICES.txt`）以及说明文档。

---

## 🎨 终端颜色与退出码契约

- **终端自适应色彩**：仅在支持颜色的交互终端开启彩色高亮；支持全局 `--no-color` 参数以及遵循开源标准的 `NO_COLOR` 环境变量。重定向输出时自动移除 ANSI 逃逸码。
- **严格退出码约定**：
  - `0`：成功执行；
  - `2`：命令行参数错误或非法选项（错误信息写至 `stderr`）；
  - `1`：预期的业务执行异常。

---

## 📄 开源许可证

本项目采用 [MIT 许可证](LICENSE)。
