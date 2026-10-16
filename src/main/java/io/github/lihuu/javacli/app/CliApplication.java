package io.github.lihuu.javacli.app;

import java.io.PrintWriter;
import java.util.Map;
import io.github.lihuu.javacli.cli.HelloCommand;
import io.github.lihuu.javacli.cli.CliCommand;
import org.jetbrains.annotations.NotNull;
import picocli.CommandLine;

public final class CliApplication {
    private CliApplication() { }

    public static void main(@NotNull String[] args) {
        System.exit(execute(args, new PrintWriter(System.out, true), new PrintWriter(System.err, true)));
    }

    static int execute(@NotNull String[] args, @NotNull PrintWriter out, @NotNull PrintWriter err) {
        return execute(args, out, err, System.getenv(), System.console() != null);
    }

    static int execute(@NotNull String[] args, @NotNull PrintWriter out, @NotNull PrintWriter err,
            @NotNull Map<String, String> environment, boolean interactive) {
        ApplicationInfo info = ApplicationInfo.load();
        CliCommand root = new CliCommand(environment, interactive);
        CommandLine commandLine = new CommandLine(root);
        commandLine.setColorScheme(CommandLine.Help.defaultColorScheme(CommandLine.Help.Ansi.OFF));
        commandLine.setCommandName(info.name());
        commandLine.getCommandSpec().version(info.name() + " " + info.version());
        commandLine.addSubcommand("hello", new HelloCommand(out, root));
        commandLine.setOut(out);
        commandLine.setErr(err);
        return commandLine.execute(args);
    }

}
