package org.allaymc.playerhomes.managers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import org.allaymc.api.math.location.Location3dc;
import org.allaymc.api.server.Server;
import org.allaymc.api.world.Dimension;
import org.allaymc.api.world.World;
import org.allaymc.playerhomes.PlayerHomesPlugin;
import org.allaymc.playerhomes.data.HomeData;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class HomeManager {
    
    private final PlayerHomesPlugin plugin;
    private final Path dataFolder;
    private final Gson gson;
    private final Map<UUID, Map<String, HomeData>> playerHomes;
    
    public HomeManager(PlayerHomesPlugin plugin) {
        this.plugin = plugin;
        this.dataFolder = plugin.getPluginContainer().dataFolder().resolve("homes");
        this.gson = new GsonBuilder().setPrettyPrinting().create();
        this.playerHomes = new ConcurrentHashMap<>();
        
        File folder = this.dataFolder.toFile();
        if (!folder.exists()) {
            folder.mkdirs();
        }
    }
    
    public void loadPlayerHomes(UUID playerId) {
        File playerFile = new File(dataFolder.toFile(), playerId.toString() + ".json");
        if (!playerFile.exists()) {
            playerHomes.put(playerId, new ConcurrentHashMap<>());
            return;
        }
        
        try (FileReader reader = new FileReader(playerFile)) {
            Map<String, HomeData> homes = gson.fromJson(reader, new TypeToken<Map<String, HomeData>>(){}.getType());
            if (homes != null) {
                playerHomes.put(playerId, new ConcurrentHashMap<>(homes));
            } else {
                playerHomes.put(playerId, new ConcurrentHashMap<>());
            }
        } catch (IOException e) {
            plugin.getPluginLogger().error("Failed to load homes for player: " + playerId, e);
            playerHomes.put(playerId, new ConcurrentHashMap<>());
        }
    }
    
    public void savePlayerHomes(UUID playerId) {
        Map<String, HomeData> homes = playerHomes.get(playerId);
        if (homes == null || homes.isEmpty()) {
            File playerFile = new File(dataFolder.toFile(), playerId.toString() + ".json");
            if (playerFile.exists()) {
                playerFile.delete();
            }
            return;
        }
        
        File playerFile = new File(dataFolder.toFile(), playerId.toString() + ".json");
        try (FileWriter writer = new FileWriter(playerFile)) {
            gson.toJson(homes, writer);
        } catch (IOException e) {
            plugin.getPluginLogger().error("Failed to save homes for player: " + playerId, e);
        }
    }
    
    public void saveAllHomes() {
        for (UUID playerId : playerHomes.keySet()) {
            savePlayerHomes(playerId);
        }
    }
    
    public boolean setHome(UUID playerId, String homeName, Location3dc location) {
        loadPlayerHomes(playerId);
        Map<String, HomeData> homes = playerHomes.get(playerId);
        
        if (homes.size() >= 10 && !homes.containsKey(homeName)) {
            return false;
        }
        
        Dimension dimension = location.dimension();
        World world = dimension.getWorld();
        String worldName = world.getName();
        int dimensionId = dimension.getDimensionInfo().dimensionId();
        
        HomeData homeData = new HomeData(homeName, (float) location.x(), (float) location.y(), (float) location.z(), worldName, dimensionId);
        homes.put(homeName, homeData);
        savePlayerHomes(playerId);
        return true;
    }
    
    public boolean deleteHome(UUID playerId, String homeName) {
        loadPlayerHomes(playerId);
        Map<String, HomeData> homes = playerHomes.get(playerId);
        if (homes == null || !homes.containsKey(homeName)) {
            return false;
        }
        homes.remove(homeName);
        savePlayerHomes(playerId);
        return true;
    }
    
    public HomeData getHome(UUID playerId, String homeName) {
        loadPlayerHomes(playerId);
        Map<String, HomeData> homes = playerHomes.get(playerId);
        if (homes == null) {
            return null;
        }
        return homes.get(homeName);
    }
    
    public Map<String, HomeData> getPlayerHomes(UUID playerId) {
        loadPlayerHomes(playerId);
        return playerHomes.getOrDefault(playerId, new ConcurrentHashMap<>());
    }
    
    public boolean teleportToHome(org.allaymc.api.entity.interfaces.EntityPlayer player, String homeName) {
        HomeData home = getHome(player.getUniqueId(), homeName);
        if (home == null) {
            return false;
        }
        
        World world = Server.getInstance().getWorldPool().getWorld(home.getWorldName());
        if (world == null) {
            world = player.getWorld();
        }
        
        Dimension dimension = world.getDimension(home.getDimensionId());
        if (dimension == null) {
            dimension = player.getDimension();
        }
        
        Location3dc targetLoc = home.toLocation(dimension);
        player.teleport(targetLoc);
        return true;
    }
    
    public int getMaxHomes(UUID playerId) {
        return 10;
    }
    
    public int getHomeCount(UUID playerId) {
        loadPlayerHomes(playerId);
        Map<String, HomeData> homes = playerHomes.get(playerId);
        return homes != null ? homes.size() : 0;
    }
}
