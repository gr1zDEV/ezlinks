package com.ezinnovations.ezlinks.service;

import com.ezinnovations.ezlinks.model.ConfiguredCommand;
import com.ezinnovations.ezlinks.model.PluginConfig;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

public class RuntimeConfigService {
    private volatile PluginConfig config;
    private volatile Map<String, ConfiguredCommand> commandLookup = Map.of();

    public void setConfig(PluginConfig config) {
        this.config = config;

        Map<String, ConfiguredCommand> lookup = new LinkedHashMap<>();
        config.commands().forEach((name, command) -> {
            lookup.put(name.toLowerCase(Locale.ROOT), command);
            for (String alias : command.aliases()) {
                lookup.put(alias.toLowerCase(Locale.ROOT), command);
            }
        });

        this.commandLookup = Collections.unmodifiableMap(lookup);
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
}
