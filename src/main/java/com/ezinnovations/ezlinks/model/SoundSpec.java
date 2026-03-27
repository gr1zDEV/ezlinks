package com.ezinnovations.ezlinks.model;

import org.bukkit.Sound;

public record SoundSpec(String key, boolean enabled, Sound sound, float volume, float pitch) {
}
