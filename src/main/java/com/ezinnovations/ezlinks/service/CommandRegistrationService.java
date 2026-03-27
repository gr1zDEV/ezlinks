package com.ezinnovations.ezlinks.service;

import com.ezinnovations.ezlinks.EzLinksPlugin;
import com.ezinnovations.ezlinks.command.DynamicLinkCommand;
import com.ezinnovations.ezlinks.command.EzLinksAdminCommand;
import com.ezinnovations.ezlinks.model.ConfiguredCommand;
import io.papermc.paper.command.brigadier.Commands;

public class CommandRegistrationService {
    private final EzLinksPlugin plugin;
    private final RuntimeConfigService runtimeConfigService;
    private final CommandExecutionService commandExecutionService;

    public CommandRegistrationService(EzLinksPlugin plugin,
                                      RuntimeConfigService runtimeConfigService,
                                      CommandExecutionService commandExecutionService) {
        this.plugin = plugin;
        this.runtimeConfigService = runtimeConfigService;
        this.commandExecutionService = commandExecutionService;
    }

    public void registerCommands(Commands registrar) {
        registrar.register("ezlinks", "Admin command for EzLinks", new EzLinksAdminCommand(plugin));

        for (ConfiguredCommand command : runtimeConfigService.getCommands().values()) {
            registrar.register(
                    command.name(),
                    "Dynamic EzLinks command",
                    command.aliases(),
                    new DynamicLinkCommand(command.name(), runtimeConfigService, commandExecutionService)
            );
        }
    }
}
