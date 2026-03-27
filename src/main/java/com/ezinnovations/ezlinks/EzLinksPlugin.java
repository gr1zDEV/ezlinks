package com.ezinnovations.ezlinks;

import com.ezinnovations.ezlinks.config.ConfigLoaderService;
import com.ezinnovations.ezlinks.model.CommandBindingReport;
import com.ezinnovations.ezlinks.model.CommandSyncResult;
import com.ezinnovations.ezlinks.model.PluginConfig;
import com.ezinnovations.ezlinks.model.ReloadReport;
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
        CommandBindingReport bindingReport = runtimeConfigService.setConfig(config);

        getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, event ->
                commandRegistrationService.registerCommands(event.registrar())
        );

        getLogger().info("EzLinks enabled with " + config.commands().size() + " dynamic command(s), bound "
                + bindingReport.boundLabelCount() + " labels.");
        if (!bindingReport.conflicts().isEmpty()) {
            getLogger().warning("Command binding conflicts at startup: " + String.join(" | ", bindingReport.conflicts()));
        }
    }

    public ReloadReport reloadPluginConfiguration() {
        PluginConfig config = configLoaderService.load();
        CommandBindingReport bindingReport = runtimeConfigService.setConfig(config);

        CommandSyncResult syncResult = syncCommandsIfAvailable();
        getLogger().info("EzLinks reloaded; loaded " + config.commands().size() + " dynamic command(s), bound "
                + bindingReport.boundLabelCount() + " labels.");
        if (!bindingReport.conflicts().isEmpty()) {
            getLogger().warning("Command binding conflicts on reload: " + String.join(" | ", bindingReport.conflicts()));
        }
        if (syncResult.succeeded()) {
            getLogger().info(syncResult.detail());
        } else {
            getLogger().warning(syncResult.detail());
        }

        return new ReloadReport(bindingReport, syncResult);
    }

    private CommandSyncResult syncCommandsIfAvailable() {
        // Some API variants do not declare syncCommands() on org.bukkit.Server at compile time.
        // Call it reflectively when present to keep compatibility across Paper/Bukkit targets.
        try {
            getServer().getClass().getMethod("syncCommands").invoke(getServer());
            return new CommandSyncResult(true, true, "Command sync invoked successfully.");
        } catch (NoSuchMethodException ex) {
            return new CommandSyncResult(false, false,
                    "Command sync unavailable: server does not expose syncCommands().");
        } catch (ReflectiveOperationException ex) {
            return new CommandSyncResult(true, false,
                    "Command sync failed via reflection: " + ex.getClass().getSimpleName() + " - " + ex.getMessage());
        }
    }

    public RuntimeConfigService getRuntimeConfigService() {
        return runtimeConfigService;
    }
}
