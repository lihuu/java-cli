package io.github.lihuu.javacli.cli;

import org.jetbrains.annotations.NotNull;

/** Semantic terminal styles. Plain mode preserves text and ANSI mode always resets. */
final class OutputStyle {
    private static final OutputStyle PLAIN = new OutputStyle(false);
    private static final OutputStyle ANSI = new OutputStyle(true);
    private final boolean ansi;

    private OutputStyle(boolean ansi) {
        this.ansi = ansi;
    }

    static @NotNull OutputStyle plain() {
        return PLAIN;
    }

    static @NotNull OutputStyle ansi() {
        return ANSI;
    }

    @NotNull String success(@NotNull String text) {
        return ansi ? "\u001b[32m" + text + "\u001b[0m" : text;
    }
}
