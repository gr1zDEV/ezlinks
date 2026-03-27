package com.ezinnovations.ezlinks.config;

import com.ezinnovations.ezlinks.model.ClickActionType;
import com.ezinnovations.ezlinks.model.ClickSpec;
import com.ezinnovations.ezlinks.model.ConfiguredCommand;
import com.ezinnovations.ezlinks.model.MessageLine;
import com.ezinnovations.ezlinks.model.PluginConfig;
import com.ezinnovations.ezlinks.model.PluginMessages;
import com.ezinnovations.ezlinks.model.SoundSpec;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class ConfigLoaderService {
    private final JavaPlugin plugin;

    public ConfigLoaderService(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public PluginConfig load() {
        plugin.reloadConfig();
        FileConfiguration config = plugin.getConfig();

        Map<String, SoundSpec> sounds = loadSounds(config.getConfigurationSection("sounds"));
        Map<String, ConfiguredCommand> commands = loadCommands(config.getConfigurationSection("commands"));
        PluginMessages messages = loadMessages(config.getConfigurationSection("messages"));

        return new PluginConfig(commands, sounds, messages);
    }

    private Map<String, SoundSpec> loadSounds(ConfigurationSection soundsSection) {
        Map<String, SoundSpec> sounds = new LinkedHashMap<>();
        if (soundsSection == null) {
            return sounds;
        }

        for (String key : soundsSection.getKeys(false)) {
            ConfigurationSection section = soundsSection.getConfigurationSection(key);
            if (section == null) {
                plugin.getLogger().warning("Skipping sound '" + key + "' because it is not a section.");
                continue;
            }

            boolean enabled = section.getBoolean("enabled", true);
            String soundName = section.getString("sound", "");
            float volume = (float) section.getDouble("volume", 1.0D);
            float pitch = (float) section.getDouble("pitch", 1.0D);

            Sound sound;
            try {
                sound = Sound.valueOf(soundName.toUpperCase(Locale.ROOT));
            } catch (IllegalArgumentException ex) {
                plugin.getLogger().warning("Invalid sound enum '" + soundName + "' for sound key '" + key + "'.");
                continue;
            }

            sounds.put(key, new SoundSpec(key, enabled, sound, volume, pitch));
        }

        return sounds;
    }

    private Map<String, ConfiguredCommand> loadCommands(ConfigurationSection commandsSection) {
        Map<String, ConfiguredCommand> commands = new LinkedHashMap<>();
        Map<String, String> occupiedLabels = new HashMap<>();
        if (commandsSection == null) {
            return commands;
        }

        for (String commandName : commandsSection.getKeys(false)) {
            String normalizedCommandName = normalizeCommandLabel(commandName);
            String collidingOwner = occupiedLabels.get(normalizedCommandName);
            if (collidingOwner != null) {
                plugin.getLogger().warning("Skipping command '" + commandName + "' because its name collides with '" + collidingOwner + "'.");
                continue;
            }

            ConfigurationSection section = commandsSection.getConfigurationSection(commandName);
            if (section == null) {
                plugin.getLogger().warning("Skipping command '" + commandName + "' because it is not a section.");
                continue;
            }

            String permission = section.getString("permission", "");
            String soundKey = section.getString("sound", "");
            List<String> aliases = section.getStringList("aliases");
            List<MessageLine> lines = loadLines(commandName, section.getList("lines"));

            if (lines.isEmpty()) {
                plugin.getLogger().warning("Skipping command '" + commandName + "' because it has no valid lines.");
                continue;
            }

            List<String> sanitizedAliases = sanitizeAliases(commandName, aliases, occupiedLabels);

            occupiedLabels.put(normalizedCommandName, commandName);
            commands.put(commandName, new ConfiguredCommand(commandName, emptyToNull(permission), sanitizedAliases, emptyToNull(soundKey), lines));
        }

        return commands;
    }

    private List<String> sanitizeAliases(String commandName, List<String> aliases, Map<String, String> occupiedLabels) {
        List<String> sanitizedAliases = new ArrayList<>();
        Set<String> seenInCommand = new HashSet<>();
        String normalizedCommandName = normalizeCommandLabel(commandName);

        for (String alias : aliases) {
            if (alias == null || alias.isBlank()) {
                plugin.getLogger().warning("Ignoring blank alias in command '" + commandName + "'.");
                continue;
            }

            String normalizedAlias = normalizeCommandLabel(alias);
            if (normalizedAlias.equals(normalizedCommandName)) {
                plugin.getLogger().warning("Ignoring alias '" + alias + "' in command '" + commandName + "' because it duplicates the command name.");
                continue;
            }

            if (!seenInCommand.add(normalizedAlias)) {
                plugin.getLogger().warning("Ignoring alias '" + alias + "' in command '" + commandName + "' because it is duplicated in the same command.");
                continue;
            }

            String collidingOwner = occupiedLabels.get(normalizedAlias);
            if (collidingOwner != null) {
                plugin.getLogger().warning("Ignoring alias '" + alias + "' in command '" + commandName + "' because it collides with '" + collidingOwner + "'.");
                continue;
            }

            occupiedLabels.put(normalizedAlias, commandName);
            sanitizedAliases.add(alias);
        }

        return sanitizedAliases;
    }

    private String normalizeCommandLabel(String label) {
        return label.toLowerCase(Locale.ROOT);
    }

    @SuppressWarnings("unchecked")
    private List<MessageLine> loadLines(String commandName, List<?> rawLines) {
        List<MessageLine> lines = new ArrayList<>();
        if (rawLines == null) {
            return lines;
        }

        int index = 0;
        for (Object rawLine : rawLines) {
            index++;
            if (!(rawLine instanceof Map<?, ?> map)) {
                plugin.getLogger().warning("Skipping line " + index + " in command '" + commandName + "' because it is not a map.");
                continue;
            }

            String text = asString(map.get("text"));
            if (text == null || text.isBlank()) {
                plugin.getLogger().warning("Skipping line " + index + " in command '" + commandName + "' due to missing text.");
                continue;
            }

            ClickSpec click = null;
            Object clickRaw = map.get("click");
            if (clickRaw instanceof Map<?, ?> clickMap) {
                String actionRaw = asString(clickMap.get("action"));
                String value = asString(clickMap.get("value"));

                Optional<ClickActionType> actionType = ClickActionType.fromConfig(actionRaw);
                if (actionType.isEmpty()) {
                    plugin.getLogger().warning("Invalid click action '" + actionRaw + "' on line " + index
                            + " in command '" + commandName + "'.");
                } else if (value == null || value.isBlank()) {
                    plugin.getLogger().warning("Missing click value on line " + index + " in command '" + commandName + "'.");
                } else {
                    click = new ClickSpec(actionType.get(), value);
                }
            }

            String hover = asString(map.get("hover"));
            lines.add(new MessageLine(text, click, hover));
        }

        return lines;
    }

    private PluginMessages loadMessages(ConfigurationSection messagesSection) {
        if (messagesSection == null) {
            return new PluginMessages(
                    "&aEzLinks config reloaded.",
                    "&cYou do not have permission to do that.",
                    "&cOnly players can use this command.",
                    "&cUnknown subcommand. Try &f/ezlinks reload"
            );
        }

        return new PluginMessages(
                messagesSection.getString("reload-success", "&aEzLinks config reloaded."),
                messagesSection.getString("no-permission", "&cYou do not have permission to do that."),
                messagesSection.getString("player-only", "&cOnly players can use this command."),
                messagesSection.getString("unknown-subcommand", "&cUnknown subcommand. Try &f/ezlinks reload")
        );
    }

    private static String emptyToNull(String input) {
        return input == null || input.isBlank() ? null : input;
    }

    private static String asString(Object value) {
        return value == null ? null : String.valueOf(value);
    }
}
