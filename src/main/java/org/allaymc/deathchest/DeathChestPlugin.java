package org.allaymc.deathchest;

import org.allaymc.api.plugin.Plugin;
import org.allaymc.api.registry.Registries;
import org.allaymc.api.server.Server;
import org.allaymc.deathchest.commands.DeathChestCommand;
import org.allaymc.deathchest.listeners.DeathListener;
import org.allaymc.deathchest.managers.ChestManager;

public class DeathChestPlugin extends Plugin {
    
    private static DeathChestPlugin instance;
    private ChestManager chestManager;
    
    @Override
    public void onLoad() {
        instance = this;
        this.pluginLogger.info("DeathChest is loading...");
    }
    
    @Override
    public void onEnable() {
        this.pluginLogger.info("DeathChest is enabling...");
        this.chestManager = new ChestManager(this);
        Server.getInstance().getEventBus().registerListener(new DeathListener(this));
        Registries.COMMANDS.register(new DeathChestCommand(this));
        this.pluginLogger.info("DeathChest enabled successfully!");
    }
    
    @Override
    public void onDisable() {
        this.pluginLogger.info("DeathChest is disabling...");
        if (this.chestManager != null) {
            this.chestManager.saveAllChests();
        }
        this.pluginLogger.info("DeathChest disabled!");
    }
    
    public static DeathChestPlugin getInstance() {
        return instance;
    }
    
    public ChestManager getChestManager() {
        return this.chestManager;
    }
}
