package com.ezinnovations.ezlinks;

import com.ezinnovations.ezlinks.config.ConfigLoaderService;
import com.ezinnovations.ezlinks.model.PluginConfig;
import com.ezinnovations.ezlinks.service.CommandExecutionService;
import com.ezinnovations.ezlinks.service.CommandRegistrationService;
import com.ezinnovations.ezlinks.service.RuntimeConfigService;
import com.ezinnovations.ezlinks.service.SoundService;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import org.bukkit.plugin.java.JavaPlugin;

public class EzLinksPlugin extends JavaPlugin {
    private ConfigLoaderService configLoaderService;
    private RuntimeConfigService runtimeConfigService;
    private CommandRegistrationService commandRegistrationService;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        this.configLoaderService = new ConfigLoaderService(this);
        this.runtimeConfigService = new RuntimeConfigService();

        SoundService soundService = new SoundService();
        CommandExecutionService commandExecutionService = new CommandExecutionService(soundService);
        this.commandRegistrationService = new CommandRegistrationService(this, runtimeConfigService, commandExecutionService);

        PluginConfig config = configLoaderService.load();
        runtimeConfigService.setConfig(config);

        getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, event ->
                commandRegistrationService.registerCommands(event.registrar())
        );

        getLogger().info("EzLinks enabled with " + config.commands().size() + " dynamic command(s).");
    }

    public void reloadPluginConfiguration() {
        PluginConfig config = configLoaderService.load();
        runtimeConfigService.setConfig(config);

        syncCommandsIfAvailable();
        getLogger().info("EzLinks reloaded; loaded " + config.commands().size() + " dynamic command(s).");
    }

    private void syncCommandsIfAvailable() {
        // Some API variants do not declare syncCommands() on org.bukkit.Server at compile time.
        // Call it reflectively when present to keep compatibility across Paper/Bukkit targets.
        try {
            getServer().getClass().getMethod("syncCommands").invoke(getServer());
        } catch (ReflectiveOperationException ignored) {
            // No-op: command registration still works; clients pick up changes on reconnect/rejoin.
        }
    }

    public RuntimeConfigService getRuntimeConfigService() {
        return runtimeConfigService;
    }
}
