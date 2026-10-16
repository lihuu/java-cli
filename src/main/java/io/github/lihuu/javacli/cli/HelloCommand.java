package io.github.lihuu.javacli.cli;

import java.io.PrintWriter;
import org.jetbrains.annotations.NotNull;
import picocli.CommandLine.Command;

@Command(name = "hello", mixinStandardHelpOptions = true, description = "Print a greeting")
public final class HelloCommand implements Runnable {
    private final PrintWriter out;
    private final CliCommand root;

    @picocli.CommandLine.Option(names = "--name", defaultValue = "world", description = "Who to greet")
    private String name;

    public HelloCommand(@NotNull PrintWriter out, @NotNull CliCommand root) {
        this.out = out;
        this.root = root;
    }

    @Override
    public void run() {
        out.println(root.outputStyle().success("Hello, " + name + "!"));
    }
}
