package org.allaymc.deathchest.managers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import org.allaymc.deathchest.DeathChestPlugin;
import org.allaymc.deathchest.data.ChestData;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class ChestManager {
    
    private final DeathChestPlugin plugin;
    private final Path dataFolder;
    private final Gson gson;
    private final Map<UUID, List<ChestData>> playerChests;
    private static final long EXPIRATION_TIME = 24 * 60 * 60 * 1000;
    
    public ChestManager(DeathChestPlugin plugin) {
        this.plugin = plugin;
        this.dataFolder = plugin.getPluginContainer().dataFolder().resolve("chests");
        this.gson = new GsonBuilder().setPrettyPrinting().create();
        this.playerChests = new ConcurrentHashMap<>();
        
        File folder = this.dataFolder.toFile();
        if (!folder.exists()) {
            folder.mkdirs();
        }
        
        loadAllChests();
    }
    
    public void addChest(ChestData chestData) {
        playerChests.computeIfAbsent(chestData.getPlayerId(), k -> new ArrayList<>()).add(chestData);
        savePlayerChests(chestData.getPlayerId());
    }
    
    public List<ChestData> getPlayerChests(UUID playerId) {
        loadPlayerChests(playerId);
        List<ChestData> chests = playerChests.getOrDefault(playerId, new ArrayList<>());
        long currentTime = System.currentTimeMillis();
        return chests.stream()
                .filter(chest -> !chest.isRecovered())
                .filter(chest -> (currentTime - chest.getDeathTime()) < EXPIRATION_TIME)
                .collect(Collectors.toList());
    }
    
    public boolean recoverChest(UUID playerId, UUID chestId) {
        loadPlayerChests(playerId);
        List<ChestData> chests = playerChests.get(playerId);
        
        if (chests == null) {
            return false;
        }
        
        for (ChestData chest : chests) {
            if (chest.getChestId().equals(chestId) && !chest.isRecovered()) {
                chest.setRecovered(true);
                savePlayerChests(playerId);
                return true;
            }
        }
        
        return false;
    }
    
    public ChestData getChest(UUID playerId, UUID chestId) {
        loadPlayerChests(playerId);
        List<ChestData> chests = playerChests.getOrDefault(playerId, new ArrayList<>());
        
        for (ChestData chest : chests) {
            if (chest.getChestId().equals(chestId)) {
                return chest;
            }
        }
        
        return null;
    }
    
    public void saveAllChests() {
        for (UUID playerId : playerChests.keySet()) {
            savePlayerChests(playerId);
        }
    }
    
    private void loadAllChests() {
        File folder = dataFolder.toFile();
        if (!folder.exists() || !folder.isDirectory()) {
            return;
        }
        
        File[] files = folder.listFiles((dir, name) -> name.endsWith(".json"));
        if (files == null) {
            return;
        }
        
        for (File file : files) {
            String fileName = file.getName();
            String uuidStr = fileName.substring(0, fileName.length() - 5);
            try {
                UUID playerId = UUID.fromString(uuidStr);
                loadPlayerChests(playerId);
            } catch (IllegalArgumentException e) {
                plugin.getPluginLogger().error("Invalid player UUID in filename: " + fileName);
            }
        }
    }
    
    private void loadPlayerChests(UUID playerId) {
        if (playerChests.containsKey(playerId)) {
            return;
        }
        
        File playerFile = new File(dataFolder.toFile(), playerId.toString() + ".json");
        if (!playerFile.exists()) {
            playerChests.put(playerId, new ArrayList<>());
            return;
        }
        
        try (FileReader reader = new FileReader(playerFile)) {
            List<ChestData> chests = gson.fromJson(reader, new TypeToken<List<ChestData>>(){}.getType());
            if (chests != null) {
                playerChests.put(playerId, new ArrayList<>(chests));
            } else {
                playerChests.put(playerId, new ArrayList<>());
            }
        } catch (IOException e) {
            plugin.getPluginLogger().error("Failed to load chests for player: " + playerId, e);
            playerChests.put(playerId, new ArrayList<>());
        }
    }
    
    private void savePlayerChests(UUID playerId) {
        List<ChestData> chests = playerChests.get(playerId);
        if (chests == null) {
            return;
        }
        
        File playerFile = new File(dataFolder.toFile(), playerId.toString() + ".json");
        
        if (chests.isEmpty()) {
            if (playerFile.exists()) {
                playerFile.delete();
            }
            return;
        }
        
        try (FileWriter writer = new FileWriter(playerFile)) {
            gson.toJson(chests, writer);
        } catch (IOException e) {
            plugin.getPluginLogger().error("Failed to save chests for player: " + playerId, e);
        }
    }
    
    public void cleanExpiredChests() {
        long currentTime = System.currentTimeMillis();
        
        for (Map.Entry<UUID, List<ChestData>> entry : playerChests.entrySet()) {
            List<ChestData> chests = entry.getValue();
            chests.removeIf(chest -> (currentTime - chest.getDeathTime()) >= EXPIRATION_TIME);
            savePlayerChests(entry.getKey());
        }
    }
}
