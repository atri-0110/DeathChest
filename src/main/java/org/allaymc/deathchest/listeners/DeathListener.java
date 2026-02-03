package org.allaymc.deathchest.listeners;

import org.allaymc.api.container.Container;
import org.allaymc.api.container.ContainerTypes;
import org.allaymc.api.entity.interfaces.EntityPlayer;
import org.allaymc.api.eventbus.EventHandler;
import org.allaymc.api.eventbus.event.entity.EntityDieEvent;
import org.allaymc.api.item.ItemStack;
import org.allaymc.api.item.interfaces.ItemAirStack;
import org.allaymc.api.world.Dimension;
import org.allaymc.api.world.World;
import org.allaymc.deathchest.DeathChestPlugin;
import org.allaymc.deathchest.data.ChestData;
import org.allaymc.deathchest.data.ItemData;
import org.cloudburstmc.nbt.NbtMap;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.allaymc.api.item.type.ItemTypes.AIR;

/**
 * Listener for player death events.
 * Saves player inventory to a death chest when they die.
 */
public class DeathListener {
    
    private final DeathChestPlugin plugin;
    
    public DeathListener(DeathChestPlugin plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler
    public void onEntityDie(EntityDieEvent event) {
        if (!(event.getEntity() instanceof EntityPlayer player)) {
            return;
        }
        
        List<ItemData> items = collectItems(player);
        
        if (items.isEmpty()) {
            return;
        }
        
        var location = player.getLocation();
        Dimension dimension = location.dimension();
        World world = dimension.getWorld();
        
        // Safety check: world should never be null, but we check anyway
        if (world == null) {
            plugin.getPluginLogger().error("Player " + player.getDisplayName() + " died in a dimension with no world!");
            return;
        }
        
        ChestData chestData = new ChestData();
        chestData.setChestId(UUID.randomUUID());
        chestData.setPlayerId(player.getUniqueId());
        chestData.setPlayerName(player.getDisplayName());
        chestData.setWorldName(world.getName());
        chestData.setDeathTime(System.currentTimeMillis());
        chestData.setX(location.x());
        chestData.setY(location.y());
        chestData.setZ(location.z());
        chestData.setDimensionId(dimension.getDimensionInfo().dimensionId());
        chestData.setItems(items);
        chestData.setRecovered(false);
        
        plugin.getChestManager().addChest(chestData);
        
        player.sendMessage("§aYour items have been stored in a death chest!");
        player.sendMessage("§7Use §e/deathchest list §7to see your chests");
        player.sendMessage("§7Use §e/deathchest recover <id> §7to recover items");
    }
    
    /**
     * Collects all items from player's containers and clears them.
     * Uses NBT serialization to preserve complete item state.
     */
    private List<ItemData> collectItems(EntityPlayer player) {
        List<ItemData> items = new ArrayList<>();
        
        // Collect from main inventory
        Container inventory = player.getContainer(ContainerTypes.INVENTORY);
        if (inventory != null) {
            items.addAll(collectFromContainer(inventory));
        }
        
        // Collect from armor slots
        Container armor = player.getContainer(ContainerTypes.ARMOR);
        if (armor != null) {
            items.addAll(collectFromContainer(armor));
        }
        
        // Collect from offhand
        Container offhand = player.getContainer(ContainerTypes.OFFHAND);
        if (offhand != null) {
            items.addAll(collectFromContainer(offhand));
        }
        
        return items;
    }
    
    /**
     * Collects items from a specific container.
     */
    private List<ItemData> collectFromContainer(Container container) {
        List<ItemData> items = new ArrayList<>();
        
        int size = container.getContainerType().getSize();
        for (int i = 0; i < size; i++) {
            ItemStack item = container.getItemStack(i);
            if (item != null && item.getItemType() != AIR) {
                ItemData itemData = serializeItem(item);
                if (itemData != null) {
                    items.add(itemData);
                }
                container.setItemStack(i, ItemAirStack.AIR_STACK);
            }
        }
        
        return items;
    }
    
    /**
     * Serializes an ItemStack to ItemData using NBT.
     * This preserves all item metadata including enchantments, durability, etc.
     */
    private ItemData serializeItem(ItemStack item) {
        try {
            NbtMap nbt = item.saveNBT();
            if (nbt == null) {
                return null;
            }
            
            ItemData itemData = new ItemData();
            itemData.setNbtData(nbt);
            return itemData;
        } catch (Exception e) {
            plugin.getPluginLogger().warn("Failed to serialize item: " + item.getItemType().getIdentifier(), e);
            return null;
        }
    }
}