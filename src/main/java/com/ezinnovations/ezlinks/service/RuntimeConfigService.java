package com.ezinnovations.ezlinks.service;

import com.ezinnovations.ezlinks.model.CommandBindingReport;
import com.ezinnovations.ezlinks.model.ConfiguredCommand;
import com.ezinnovations.ezlinks.model.PluginConfig;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class RuntimeConfigService {
    private volatile PluginConfig config;
    private volatile Map<String, ConfiguredCommand> commandLookup = Map.of();

    public CommandBindingReport setConfig(PluginConfig config) {
        this.config = config;

        Map<String, ConfiguredCommand> lookup = new LinkedHashMap<>();
        List<String> conflicts = new ArrayList<>();

        config.commands().forEach((name, command) -> {
            bindLabel(lookup, name, command, conflicts);
            for (String alias : command.aliases()) {
                bindLabel(lookup, alias, command, conflicts);
            }
        });

        this.commandLookup = Collections.unmodifiableMap(lookup);
        return new CommandBindingReport(config.commands().size(), lookup.size(), List.copyOf(conflicts));
    }

    public PluginConfig getConfig() {
        return config;
    }

    public Map<String, ConfiguredCommand> getCommands() {
        return config.commands();
    }

    public ConfiguredCommand findByNameOrAlias(String name) {
        return commandLookup.get(name.toLowerCase(Locale.ROOT));
    }

    private void bindLabel(Map<String, ConfiguredCommand> lookup,
                           String label,
                           ConfiguredCommand command,
                           List<String> conflicts) {
        String normalizedLabel = label.toLowerCase(Locale.ROOT);
        ConfiguredCommand existing = lookup.get(normalizedLabel);

        if (existing == null) {
            lookup.put(normalizedLabel, command);
            return;
        }

        if (existing.name().equalsIgnoreCase(command.name())) {
            return;
        }

        conflicts.add("'" + label + "' kept for '" + existing.name() + "', skipped '" + command.name() + "'.");
    }
}
