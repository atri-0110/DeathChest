package org.allaymc.deathchest;

import org.allaymc.api.plugin.Plugin;
import org.allaymc.api.registry.Registries;
import org.allaymc.api.server.Server;
import org.allaymc.deathchest.commands.DeathChestCommand;
import org.allaymc.deathchest.listeners.DeathListener;
import org.allaymc.deathchest.managers.ChestManager;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class DeathChestPlugin extends Plugin {

    private static DeathChestPlugin instance;
    private ChestManager chestManager;
    private final Set<String> activeCleanupTasks = ConcurrentHashMap.newKeySet();
    
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

        // Schedule periodic cleanup of expired chests (every 30 minutes = 36000 ticks)
        String taskId = UUID.randomUUID().toString();
        activeCleanupTasks.add(taskId);
        Server.getInstance().getScheduler().scheduleRepeating(this, () -> {
            if (!activeCleanupTasks.contains(taskId)) {
                return false; // Stop this task if plugin disabled
            }
            if (this.chestManager != null) {
                this.chestManager.cleanExpiredChests();
            }
            return true;
        }, 36000);

        this.pluginLogger.info("DeathChest enabled successfully!");
    }
    
    @Override
    public void onDisable() {
        this.pluginLogger.info("DeathChest is disabling...");
        // Stop all cleanup tasks by clearing the tracking set
        activeCleanupTasks.clear();
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
