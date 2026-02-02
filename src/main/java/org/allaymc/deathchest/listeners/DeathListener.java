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

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.allaymc.api.item.type.ItemTypes.AIR;

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
        
        List<ItemData> items = new ArrayList<>();
        Container inventory = player.getContainer(ContainerTypes.INVENTORY);
        if (inventory != null) {
            int size = inventory.getContainerType().getSize();
            for (int i = 0; i < size; i++) {
                ItemStack item = inventory.getItemStack(i);
                if (item != null && item.getItemType() != AIR) {
                    ItemData itemData = new ItemData();
                    itemData.setItemType(item.getItemType().getIdentifier().toString());
                    itemData.setCount(item.getCount());
                    items.add(itemData);
                    inventory.setItemStack(i, ItemAirStack.AIR_STACK);
                }
            }
        }
        Container armor = player.getContainer(ContainerTypes.ARMOR);
        if (armor != null) {
            int size = armor.getContainerType().getSize();
            for (int i = 0; i < size; i++) {
                ItemStack item = armor.getItemStack(i);
                if (item != null && item.getItemType() != AIR) {
                    ItemData itemData = new ItemData();
                    itemData.setItemType(item.getItemType().getIdentifier().toString());
                    itemData.setCount(item.getCount());
                    items.add(itemData);
                    armor.setItemStack(i, ItemAirStack.AIR_STACK);
                }
            }
        }
        Container offhand = player.getContainer(ContainerTypes.OFFHAND);
        if (offhand != null) {
            int size = offhand.getContainerType().getSize();
            for (int i = 0; i < size; i++) {
                ItemStack item = offhand.getItemStack(i);
                if (item != null && item.getItemType() != AIR) {
                    ItemData itemData = new ItemData();
                    itemData.setItemType(item.getItemType().getIdentifier().toString());
                    itemData.setCount(item.getCount());
                    items.add(itemData);
                    offhand.setItemStack(i, ItemAirStack.AIR_STACK);
                }
            }
        }
        if (items.isEmpty()) {
            return;
        }
        var location = player.getLocation();
        Dimension dimension = location.dimension();
        World world = dimension.getWorld();
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
}
