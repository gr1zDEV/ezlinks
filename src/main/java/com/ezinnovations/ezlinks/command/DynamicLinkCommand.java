package com.ezinnovations.ezlinks.command;

import com.ezinnovations.ezlinks.model.ConfiguredCommand;
import com.ezinnovations.ezlinks.service.CommandExecutionService;
import com.ezinnovations.ezlinks.service.RuntimeConfigService;
import io.papermc.paper.command.brigadier.BasicCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;

public class DynamicLinkCommand implements BasicCommand {
    private final String commandName;
    private final RuntimeConfigService runtimeConfigService;
    private final CommandExecutionService commandExecutionService;

    public DynamicLinkCommand(String commandName,
                              RuntimeConfigService runtimeConfigService,
                              CommandExecutionService commandExecutionService) {
        this.commandName = commandName;
        this.runtimeConfigService = runtimeConfigService;
        this.commandExecutionService = commandExecutionService;
    }

    @Override
    public void execute(CommandSourceStack commandSourceStack, String[] args) {
        ConfiguredCommand configuredCommand = runtimeConfigService.findByNameOrAlias(commandName);
        if (configuredCommand == null) {
            return;
        }

        commandExecutionService.executeDynamicCommand(commandSourceStack.getSender(), configuredCommand, runtimeConfigService.getConfig());
    }
}
