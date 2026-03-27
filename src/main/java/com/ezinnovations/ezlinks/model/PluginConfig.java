package com.ezinnovations.ezlinks.model;

import java.util.Map;

public record PluginConfig(Map<String, ConfiguredCommand> commands,
                           Map<String, SoundSpec> sounds,
                           PluginMessages messages) {
}
