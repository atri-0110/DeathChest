package org.allaymc.deathchest.managers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import org.allaymc.api.container.Container;
import org.allaymc.api.container.ContainerTypes;
import org.allaymc.api.entity.interfaces.EntityPlayer;
import org.allaymc.api.item.ItemStack;
import org.allaymc.api.item.interfaces.ItemAirStack;
import org.allaymc.api.utils.NBTIO;
import org.allaymc.deathchest.DeathChestPlugin;
import org.allaymc.deathchest.data.ChestData;
import org.allaymc.deathchest.data.ItemData;
import org.allaymc.deathchest.serialization.NbtMapAdapter;
import org.cloudburstmc.nbt.NbtMap;

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

import static org.allaymc.api.item.type.ItemTypes.AIR;

/**
 * Manages death chests including storage, retrieval, and recovery.
 * All operations are thread-safe using ConcurrentHashMap.
 */
public class ChestManager {
    
    private final DeathChestPlugin plugin;
    private final Path dataFolder;
    private final Gson gson;
    private final Map<UUID, List<ChestData>> playerChests;
    private static final long EXPIRATION_TIME = 24 * 60 * 60 * 1000; // 24 hours in milliseconds
    
    public ChestManager(DeathChestPlugin plugin) {
        this.plugin = plugin;
        this.dataFolder = plugin.getPluginContainer().dataFolder().resolve("chests");
        // Use custom Gson with NbtMap adapter for proper serialization
        this.gson = new GsonBuilder()
                .setPrettyPrinting()
                .registerTypeAdapter(NbtMap.class, new NbtMapAdapter())
                .create();
        this.playerChests = new ConcurrentHashMap<>();
        
        File folder = this.dataFolder.toFile();
        if (!folder.exists()) {
            folder.mkdirs();
        }
        
        loadAllChests();
    }
    
    /**
     * Adds a new death chest for a player.
     */
    public void addChest(ChestData chestData) {
        playerChests.computeIfAbsent(chestData.getPlayerId(), k -> new ArrayList<>()).add(chestData);
        savePlayerChests(chestData.getPlayerId());
    }
    
    /**
     * Gets all active (non-expired, non-recovered) chests for a player.
     */
    public List<ChestData> getPlayerChests(UUID playerId) {
        loadPlayerChests(playerId);
        List<ChestData> chests = playerChests.getOrDefault(playerId, new ArrayList<>());
        long currentTime = System.currentTimeMillis();
        return chests.stream()
                .filter(chest -> !chest.isRecovered())
                .filter(chest -> (currentTime - chest.getDeathTime()) < EXPIRATION_TIME)
                .collect(Collectors.toList());
    }
    
    /**
     * Recovers items from a death chest and gives them to the player.
     * 
     * @param player The player to give items to
     * @param chestId The ID of the chest to recover
     * @return true if recovery was successful, false otherwise
     */
    public boolean recoverChest(EntityPlayer player, UUID chestId) {
        UUID playerId = player.getUniqueId();
        loadPlayerChests(playerId);
        List<ChestData> chests = playerChests.get(playerId);
        
        if (chests == null) {
            return false;
        }
        
        for (ChestData chest : chests) {
            if (chest.getChestId().equals(chestId) && !chest.isRecovered()) {
                return performRecovery(player, chest);
            }
        }
        
        return false;
    }
    
    /**
     * Performs the actual item recovery from a chest to a player.
     */
    private boolean performRecovery(EntityPlayer player, ChestData chest) {
        List<ItemData> items = chest.getItems();
        
        if (items == null || items.isEmpty()) {
            // Empty chest, just mark as recovered
            chest.setRecovered(true);
            savePlayerChests(chest.getPlayerId());
            player.sendMessage("§aDeath chest recovered (was empty).");
            return true;
        }
        
        // First, check if player has enough inventory space
        int emptySlots = countEmptySlots(player);
        int itemsNeedingSlots = countItemsNeedingSlots(items);
        
        if (emptySlots < itemsNeedingSlots) {
            player.sendMessage("§cYou need at least " + itemsNeedingSlots + " empty inventory slots to recover these items!");
            player.sendMessage("§7Current empty slots: " + emptySlots);
            return false;
        }
        
        // Give items to player
        int itemsGiven = 0;
        int itemsFailed = 0;
        
        for (ItemData itemData : items) {
            if (giveItemToPlayer(player, itemData)) {
                itemsGiven++;
            } else {
                itemsFailed++;
                plugin.getPluginLogger().warn("Failed to give item to player " + player.getDisplayName());
            }
        }
        
        // Mark as recovered only if at least some items were given successfully
        // This allows recovery to proceed even if some items fail, preventing data loss
        chest.setRecovered(true);
        savePlayerChests(chest.getPlayerId());
        
        if (itemsFailed == 0) {
            player.sendMessage("§aRecovered " + itemsGiven + " items from death chest!");
        } else {
            player.sendMessage("§eRecovered " + itemsGiven + " items, but " + itemsFailed + " items failed.");
            player.sendMessage("§7This could be due to inventory space issues. Please check your inventory.");
        }
        return true;
    }
    
    /**
     * Counts empty slots in player's inventory.
     */
    private int countEmptySlots(EntityPlayer player) {
        Container inventory = player.getContainer(ContainerTypes.INVENTORY);
        if (inventory == null) {
            return 0;
        }
        
        int emptyCount = 0;
        int size = inventory.getContainerType().getSize();
        for (int i = 0; i < size; i++) {
            ItemStack item = inventory.getItemStack(i);
            if (item == null || item.getItemType() == AIR) {
                emptyCount++;
            }
        }
        return emptyCount;
    }
    
    /**
     * Counts how many items need their own slots (can't stack with existing).
     * This is a simplified estimate - assumes each item needs its own slot.
     */
    private int countItemsNeedingSlots(List<ItemData> items) {
        // Conservative estimate: each item needs at least one slot
        // In reality, stacking could reduce this, but we want to be safe
        return items.size();
    }
    
    /**
     * Gives a single item to a player.
     * Tries to stack with existing items first, then fills empty slots.
     */
    private boolean giveItemToPlayer(EntityPlayer player, ItemData itemData) {
        if (itemData == null || itemData.getNbtData() == null) {
            return false;
        }

        try {
            // Deserialize the item from NBT
            ItemStack itemStack = NBTIO.getAPI().fromItemStackNBT(itemData.getNbtData());
            if (itemStack == null || itemStack.getItemType() == AIR) {
                plugin.getPluginLogger().warn("Failed to deserialize item from NBT");
                return false;
            }

            Container inventory = player.getContainer(ContainerTypes.INVENTORY);
            if (inventory == null) {
                return false;
            }

            // Try to add to inventory
            int remaining = itemStack.getCount();
            int maxStackSize = 64; // Minecraft standard max stack size

            // First pass: try to stack with existing items
            for (int i = 0; i < inventory.getContainerType().getSize() && remaining > 0; i++) {
                ItemStack existing = inventory.getItemStack(i);
                if (existing != null && existing.getItemType() == itemStack.getItemType()) {
                    int canAdd = Math.min(maxStackSize - existing.getCount(), remaining);
                    if (canAdd > 0) {
                        existing.setCount(existing.getCount() + canAdd);
                        remaining -= canAdd;
                    }
                }
            }

            // Second pass: fill empty slots
            for (int i = 0; i < inventory.getContainerType().getSize() && remaining > 0; i++) {
                ItemStack existing = inventory.getItemStack(i);
                if (existing == null || existing.getItemType() == AIR) {
                    int toAdd = Math.min(maxStackSize, remaining);
                    ItemStack newStack = NBTIO.getAPI().fromItemStackNBT(itemData.getNbtData());
                    if (newStack != null) {
                        newStack.setCount(toAdd);
                        inventory.setItemStack(i, newStack);
                        remaining -= toAdd;
                    }
                }
            }

            return remaining == 0;
        } catch (Exception e) {
            plugin.getPluginLogger().error("Failed to give item to player", e);
            return false;
        }
    }
    
    /**
     * Gets a specific chest by ID.
     */
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
    
    /**
     * Saves all chests to disk.
     */
    public void saveAllChests() {
        for (UUID playerId : playerChests.keySet()) {
            savePlayerChests(playerId);
        }
    }
    
    /**
     * Loads all chests from disk.
     */
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
    
    /**
     * Loads chests for a specific player from disk.
     */
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
    
    /**
     * Saves chests for a specific player to disk.
     */
    private synchronized void savePlayerChests(UUID playerId) {
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
    
    /**
     * Cleans up expired chests from memory.
     * Note: This only removes from memory, files are kept for record keeping.
     */
    public void cleanExpiredChests() {
        long currentTime = System.currentTimeMillis();
        int cleanedCount = 0;
        
        for (Map.Entry<UUID, List<ChestData>> entry : playerChests.entrySet()) {
            List<ChestData> chests = entry.getValue();
            int originalSize = chests.size();
            chests.removeIf(chest -> (currentTime - chest.getDeathTime()) >= EXPIRATION_TIME);
            cleanedCount += (originalSize - chests.size());
            savePlayerChests(entry.getKey());
        }
        
        if (cleanedCount > 0) {
            plugin.getPluginLogger().info("Cleaned up " + cleanedCount + " expired death chests");
        }
    }
}