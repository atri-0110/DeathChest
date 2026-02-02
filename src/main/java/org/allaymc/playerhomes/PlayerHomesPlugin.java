package org.allaymc.playerhomes;

import org.allaymc.api.plugin.Plugin;
import org.allaymc.api.registry.Registries;
import org.allaymc.api.server.Server;
import org.allaymc.playerhomes.commands.HomeCommand;
import org.allaymc.playerhomes.managers.HomeManager;

public class PlayerHomesPlugin extends Plugin {
    
    private static PlayerHomesPlugin instance;
    private HomeManager homeManager;
    
    @Override
    public void onLoad() {
        instance = this;
        this.pluginLogger.info("PlayerHomes is loading...");
        this.homeManager = new HomeManager(this);
    }
    
    @Override
    public void onEnable() {
        this.pluginLogger.info("PlayerHomes is enabling...");
        Registries.COMMANDS.register(new HomeCommand(this));
        this.pluginLogger.info("PlayerHomes enabled successfully!");
    }
    
    @Override
    public void onDisable() {
        this.pluginLogger.info("PlayerHomes is disabling...");
        if (this.homeManager != null) {
            this.homeManager.saveAllHomes();
        }
        this.pluginLogger.info("PlayerHomes disabled!");
    }
    
    public static PlayerHomesPlugin getInstance() {
        return instance;
    }
    
    public HomeManager getHomeManager() {
        return this.homeManager;
    }
}
