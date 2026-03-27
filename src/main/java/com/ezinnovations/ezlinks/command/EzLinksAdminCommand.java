package com.ezinnovations.ezlinks.command;

import com.ezinnovations.ezlinks.EzLinksPlugin;
import com.ezinnovations.ezlinks.util.MessageComponentUtil;
import io.papermc.paper.command.brigadier.BasicCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.bukkit.command.CommandSender;

import java.util.Collection;
import java.util.List;

public class EzLinksAdminCommand implements BasicCommand {
    private final EzLinksPlugin plugin;

    public EzLinksAdminCommand(EzLinksPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void execute(CommandSourceStack commandSourceStack, String[] args) {
        CommandSender sender = commandSourceStack.getSender();

        if (!sender.hasPermission("ezlinks.admin")) {
            sender.sendMessage(MessageComponentUtil.deserializeLegacy(plugin.getRuntimeConfigService().getConfig().messages().noPermission()));
            return;
        }

        if (args.length == 1 && args[0].equalsIgnoreCase("reload")) {
            plugin.reloadPluginConfiguration();
            sender.sendMessage(MessageComponentUtil.deserializeLegacy(plugin.getRuntimeConfigService().getConfig().messages().reloadSuccess()));
            return;
        }

        sender.sendMessage(MessageComponentUtil.deserializeLegacy(plugin.getRuntimeConfigService().getConfig().messages().unknownSubcommand()));
    }

    @Override
    public Collection<String> suggest(CommandSourceStack commandSourceStack, String[] args) {
        CommandSender sender = commandSourceStack.getSender();
        if (!sender.hasPermission("ezlinks.admin")) {
            return List.of();
        }

        if (args.length <= 1) {
            java.util.ArrayList<String> suggestions = new java.util.ArrayList<>();
            suggestions.add("reload");
            suggestions.addAll(plugin.getRuntimeConfigService().getCommands().keySet());
            return suggestions;
        }
        return List.of();
    }
}
