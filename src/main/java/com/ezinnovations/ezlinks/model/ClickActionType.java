package com.ezinnovations.ezlinks.model;

import net.kyori.adventure.text.event.ClickEvent;

import java.util.Locale;
import java.util.Optional;

public enum ClickActionType {
    OPEN_URL,
    RUN_COMMAND,
    SUGGEST_COMMAND,
    COPY_TO_CLIPBOARD;

    public static Optional<ClickActionType> fromConfig(String raw) {
        if (raw == null || raw.isBlank()) {
            return Optional.empty();
        }

        String normalized = raw.trim().toUpperCase(Locale.ROOT);
        try {
            return Optional.of(ClickActionType.valueOf(normalized));
        } catch (IllegalArgumentException ignored) {
            return Optional.empty();
        }
    }

    public ClickEvent toClickEvent(String value) {
        return switch (this) {
            case OPEN_URL -> ClickEvent.openUrl(value);
            case RUN_COMMAND -> ClickEvent.runCommand(value);
            case SUGGEST_COMMAND -> ClickEvent.suggestCommand(value);
            case COPY_TO_CLIPBOARD -> ClickEvent.copyToClipboard(value);
        };
    }
}
