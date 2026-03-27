package com.ezinnovations.ezlinks.service;

import com.ezinnovations.ezlinks.model.SoundSpec;
import org.bukkit.entity.Player;

public class SoundService {
    public void play(Player player, SoundSpec soundSpec) {
        if (soundSpec == null || !soundSpec.enabled()) {
            return;
        }

        player.playSound(player.getLocation(), soundSpec.sound(), soundSpec.volume(), soundSpec.pitch());
    }
}
