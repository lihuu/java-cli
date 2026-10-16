package io.github.lihuu.javacli.cli;

import java.util.Map;
import org.jetbrains.annotations.NotNull;
import picocli.CommandLine.Command;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Option;
import picocli.CommandLine.ScopeType;
import picocli.CommandLine.Spec;

@Command(name = "java-cli", mixinStandardHelpOptions = true, description = "A Java command-line tool")
public final class CliCommand implements Runnable {
    private final Map<String, String> environment;
    private final boolean interactive;

    @Option(names = "--no-color", scope = ScopeType.INHERIT, description = "Disable ANSI colors")
    private boolean noColor;

    @Spec
    private CommandSpec spec;

    public CliCommand(@NotNull Map<String, String> environment, boolean interactive) {
        this.environment = Map.copyOf(environment);
        this.interactive = interactive;
    }

    @NotNull OutputStyle outputStyle() {
        String terminal = environment.getOrDefault("TERM", "");
        boolean enabled = interactive && !terminal.isBlank() && !"dumb".equalsIgnoreCase(terminal)
                && !noColor && !environment.containsKey("NO_COLOR");
        return enabled ? OutputStyle.ansi() : OutputStyle.plain();
    }

    @Override
    public void run() {
        spec.commandLine().usage(spec.commandLine().getOut());
    }
}
