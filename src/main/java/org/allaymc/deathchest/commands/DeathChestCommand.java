package org.allaymc.deathchest.commands;

import org.allaymc.api.command.Command;
import org.allaymc.api.command.tree.CommandTree;
import org.allaymc.api.entity.interfaces.EntityPlayer;
import org.allaymc.deathchest.DeathChestPlugin;
import org.allaymc.deathchest.data.ChestData;
import org.allaymc.deathchest.managers.ChestManager;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public class DeathChestCommand extends Command {
    
    private final DeathChestPlugin plugin;
    private final ChestManager chestManager;
    
    public DeathChestCommand(DeathChestPlugin plugin) {
        super("deathchest", "Death chest management", "deathchest.use");
        this.plugin = plugin;
        this.chestManager = plugin.getChestManager();
        aliases.add("dc");
    }
    
    @Override
    public void prepareCommandTree(CommandTree tree) {
        tree.getRoot()
            .key("list")
            .exec(context -> {
                if (!(context.getSender() instanceof EntityPlayer player)) {
                    context.getSender().sendMessage("§cThis command can only be used by players!");
                    return context.fail();
                }
                
                List<ChestData> chests = chestManager.getPlayerChests(player.getUniqueId());
                
                if (chests.isEmpty()) {
                    player.sendMessage("§7You have no death chests.");
                    return context.success();
                }
                
                player.sendMessage("§6=== Your Death Chests ===");
                
                // Use locale-independent format to avoid issues on non-English systems
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.US);
                
                for (int i = 0; i < chests.size(); i++) {
                    ChestData chest = chests.get(i);
                    String dateStr = sdf.format(new Date(chest.getDeathTime()));
                    int itemCount = chest.getItems() != null ? chest.getItems().size() : 0;
                    player.sendMessage("§e[" + (i + 1) + "] §f" + chest.getWorldName() +
                        " §7(§f" + (int)chest.getX() + ", " + (int)chest.getY() + ", " + (int)chest.getZ() + "§7)" +
                        " §7- " + dateStr + " §7- " + itemCount + " items §7- ID: §f" + chest.getChestId().toString().substring(0, 8));
                }
                
                player.sendMessage("§7Use §e/deathchest recover <id> §7to recover items");
                return context.success();
            })
            .root()
            .key("recover")
            .str("chestId")
            .exec(context -> {
                if (!(context.getSender() instanceof EntityPlayer player)) {
                    context.getSender().sendMessage("§cThis command can only be used by players!");
                    return context.fail();
                }
                
                String chestIdStr = context.getResult(1);
                UUID chestId;
                
                try {
                    chestId = UUID.fromString(chestIdStr);
                } catch (IllegalArgumentException e) {
                    List<ChestData> chests = chestManager.getPlayerChests(player.getUniqueId());
                    chestId = null;
                    
                    for (ChestData chest : chests) {
                        if (chest.getChestId().toString().startsWith(chestIdStr.toLowerCase())) {
                            chestId = chest.getChestId();
                            break;
                        }
                    }
                    
                    if (chestId == null) {
                        player.sendMessage("§cInvalid chest ID! Use §e/deathchest list §cto see valid IDs.");
                        return context.fail();
                    }
                }
                
                if (chestManager.recoverChest(player, chestId)) {
                    return context.success();
                } else {
                    player.sendMessage("§cCould not recover chest. It may have expired, already been recovered, or your inventory is full.");
                    return context.fail();
                }
            })
            .root()
            .key("help")
            .exec(context -> {
                context.getSender().sendMessage("§6=== DeathChest Help ===");
                context.getSender().sendMessage("§e/deathchest list §7- List all your death chests");
                context.getSender().sendMessage("§e/deathchest recover <id> §7- Recover items from a death chest");
                context.getSender().sendMessage("§e/deathchest help §7- Show this help message");
                context.getSender().sendMessage("§7");
                context.getSender().sendMessage("§7Death chests expire after 24 hours.");
                return context.success();
            })
            .root()
            .exec(context -> {
                context.getSender().sendMessage("§6=== DeathChest Help ===");
                context.getSender().sendMessage("§e/deathchest list §7- List all your death chests");
                context.getSender().sendMessage("§e/deathchest recover <id> §7- Recover items from a death chest");
                context.getSender().sendMessage("§e/deathchest help §7- Show this help message");
                return context.success();
            });
    }
}
