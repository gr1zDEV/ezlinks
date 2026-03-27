package com.ezinnovations.ezlinks.util;

import com.ezinnovations.ezlinks.model.ClickSpec;
import com.ezinnovations.ezlinks.model.MessageLine;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class MessageComponentUtil {
    private static final Pattern HEX_PATTERN = Pattern.compile("&#([A-Fa-f0-9]{6})");
    private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();

    private static final LegacyComponentSerializer LEGACY_SERIALIZER = LegacyComponentSerializer.builder()
            .character('&')
            .hexColors()
            .useUnusualXRepeatedCharacterHexFormat()
            .build();

    private MessageComponentUtil() {
    }

    public static Component buildLine(MessageLine line) {
        Component component = deserializeLegacy(line.text());

        ClickSpec click = line.click();
        if (click != null) {
            component = component.clickEvent(click.action().toClickEvent(click.value()));
        }

        if (line.hover() != null && !line.hover().isBlank()) {
            component = component.hoverEvent(HoverEvent.showText(deserializeLegacy(line.hover())));
        }

        return component;
    }

    public static Component deserializeLegacy(String input) {
        if (input == null || input.isEmpty()) {
            return Component.empty();
        }

        Component legacyComponent = LEGACY_SERIALIZER.deserialize(convertHex(input));
        // Keep MiniMessage in the flow so users can gradually transition to MM-style markup.
        return MINI_MESSAGE.deserialize(MINI_MESSAGE.serialize(legacyComponent));
    }

    private static String convertHex(String input) {
        Matcher matcher = HEX_PATTERN.matcher(input);
        StringBuffer result = new StringBuffer();

        while (matcher.find()) {
            String hex = matcher.group(1);
            StringBuilder replacement = new StringBuilder("&x");
            for (char c : hex.toCharArray()) {
                replacement.append('&').append(c);
            }
            matcher.appendReplacement(result, replacement.toString());
        }

        matcher.appendTail(result);
        return result.toString();
    }
}
