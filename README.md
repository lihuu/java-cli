# java-cli

Java CLI 小工具模板：Java 25、Maven、Picocli，使用 GraalVM Native Image 分发。

包含可运行的示例命令、版本注入、颜色规则、测试、开发与发布脚本，以及供 AI 编码助手遵守的工程约束。单模块起步，按实际需求增加业务代码。

## 从模板创建工具

在 GitHub 点击 **Use this template → Create a new repository**，也可以：

```sh
gh repo create my-tool --template lihuu/java-cli --private --clone
cd my-tool
```

这是独立仓库，不是 fork；模板后续更新不会自动同步到已有工具。

新项目修改清单：

1. 修改 pom.xml 的 artifactId、name、description，按需修改 groupId，版本从自己的起始版本开始。
2. 命令名、JAR、Native 文件名、安装名和发布包名都从 artifactId 派生；版本从 project.version 注入，不需要改 Java 版本常量。
3. 按需把 `io.github.lihuu.javacli` 包目录和 package 声明改为自己的包名，同步 pom.xml 的 main.class、测试和 AGENTS.md。保留示例包名也可运行。
4. 修改根命令说明、替换 hello 示例、相关测试和 scripts/smoke.sh 中的示例断言，更新本 README 的项目定位与命令示例。
5. 如果 README 中继续提供模板创建命令，更新其中的 GitHub owner/repo。选择适合新项目的许可证；本模板不代选许可证。
6. 安装 Git hooks，并按 AGENTS.md 在自己的 worktree 中开发。

## 快速开始

需要 JDK 25 和 Maven。Native 构建还需要 GraalVM for JDK 25 与本机 C/C++ 工具链；macOS 使用 Xcode Command Line Tools，Linux 使用对应编译工具包。

```sh
# macOS 选择 JDK 25；其他平台自行设置 JAVA_HOME。
export JAVA_HOME=$(/usr/libexec/java_home -v 25)
export PATH="$JAVA_HOME/bin:$PATH"

scripts/dev.sh test
scripts/dev.sh run --help
scripts/dev.sh run hello --name Ada
```

```text
Hello, Ada!
```

构建 JVM JAR：

```sh
scripts/dev.sh build
java -jar target/java-cli.jar --version
```

构建、测试与运行 Native：

```sh
scripts/dev.sh native-test
scripts/dev.sh native-build
scripts/smoke.sh target/java-cli
scripts/dev.sh native hello
```

`scripts/dev.sh run` 每次执行 Maven 打包，反映当前源码；`native` 使用已有二进制，源码改动后请先执行 `native-build`。脚本支持 macOS/Linux 的 Bash 环境；不声明 Windows 脚本支持。

本机安装（默认 `~/.local/bin`）：

```sh
scripts/dev.sh install
# 或指定安装目录
CLI_INSTALL_DIR=/your/bin scripts/dev.sh install
```

裸命令显示帮助，支持 `--help`、`--version`。成功退出 0，参数错误退出 2。结果写 stdout，参数错误写 stderr；业务命令应将预期执行失败转换为简洁错误与退出码 1。

## 颜色与版本

`hello` 只在支持颜色的交互终端使用颜色。全局 `--no-color` 可位于子命令前后；只要环境中存在 `NO_COLOR`，即使值为空，也关闭颜色。帮助、参数错误和重定向输出保持纯文本。

pom.xml 是项目版本的唯一来源，Maven 仅过滤 application.properties。Picocli 所需 Native 元数据由编译期注解处理器生成；版本资源有独立的窄范围注册。

## 依赖与代码约定

默认运行时依赖只有 Picocli。JUnit 仅用于测试，JetBrains annotations 和 javac2 工具链使用 provided scope，不进入分发 JAR。

@NotNull 不只是文档：构建在 process-classes 阶段执行 javac2，注入运行时空值检查。NullContractTest 验证实际异常行为。如果以后移除插桩，必须同时调整 AGENTS.md 的判空约定与对应测试。

Jackson、JLine、MCP、LLM SDK、日志框架与 DI 框架均未预装。JSON 需求出现时再添加 Jackson；交互编辑需求出现时再选择 JLine，并验证其 Native 元数据。

完整约束见 [AGENTS.md](AGENTS.md)。SourceConventionsTest 是轻量源码检查，不是完整的 Java 解析器或空值分析器。

## 开发与发布

```sh
git config core.hooksPath "$(pwd)/.githooks"
git fetch origin
git worktree add .worktrees/me -b codex/my-change origin/main
```

主 checkout 留在 main；修改、测试、提交、PR 在自己的 worktree 中完成。hooks 拒绝主 checkout 提交、main 分支提交和直接推送 main。它们不会随 clone 自动启用，每个仓库需安装一次。

日常 PR 执行 JVM CI；Native CI 手动触发，使用 GitHub 托管的 Linux runner，没有个人机器或自托管标签绑定。

发布前通过 PR 更新 pom.xml 版本，再验证：

```sh
scripts/release.sh
```

本地发布脚本运行 JVM 与 Native 测试、构建并冒烟 Native，生成：

```text
dist/java-cli-<version>-<platform>-<architecture>.tar.gz
dist/java-cli-<version>-<platform>-<architecture>.tar.gz.sha256
```

压缩包包含可执行文件、README、运行时依赖声明及 JAR 中的许可证/NOTICE 资源。依赖声明工具会标出缺失或继承的许可信息，发布者仍需检查实际依赖的分发要求。

脚本只打包。需要发布时，对已批准的提交创建并推送匹配版本的 `v<version>` 标签；GitHub workflow 验证标签与 POM 一致，运行同一发布脚本，构建 Linux x86_64 包并创建 GitHub Release。macOS 包可在本机打包后另行上传；不声称未验证平台的兼容性。

本地打包不要求工作区干净，适合检查当前改动；正式发布以标签 checkout 为准。

## 来源

从 java-pi 提炼 CLI 工程底座、开发约定与构建流程。没有携带 Agent、会话、MCP、TUI、GUI 或模型配置。

GitHub Template 的行为见 [GitHub 文档](https://docs.github.com/en/repositories/creating-and-managing-repositories/creating-a-template-repository)。
