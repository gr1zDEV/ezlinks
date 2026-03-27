package com.ezinnovations.ezlinks.service;

import com.ezinnovations.ezlinks.model.ConfiguredCommand;
import com.ezinnovations.ezlinks.model.PluginConfig;
import com.ezinnovations.ezlinks.model.SoundSpec;
import com.ezinnovations.ezlinks.util.MessageComponentUtil;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CommandExecutionService {
    private final SoundService soundService;

    public CommandExecutionService(SoundService soundService) {
        this.soundService = soundService;
    }

    public void executeDynamicCommand(CommandSender sender, ConfiguredCommand command, PluginConfig config) {
        if (command.permission() != null && !sender.hasPermission(command.permission())) {
            sender.sendMessage(MessageComponentUtil.deserializeLegacy(config.messages().noPermission()));
            return;
        }

        command.lines().forEach(line -> sender.sendMessage(MessageComponentUtil.buildLine(line)));

        if (sender instanceof Player player && command.soundKey() != null) {
            SoundSpec soundSpec = config.sounds().get(command.soundKey());
            soundService.play(player, soundSpec);
        }
    }
}
