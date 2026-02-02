package org.allaymc.playerhomes.commands;

import org.allaymc.api.command.Command;
import org.allaymc.api.command.tree.CommandTree;
import org.allaymc.api.entity.interfaces.EntityPlayer;
import org.allaymc.api.permission.Tristate;
import org.allaymc.playerhomes.PlayerHomesPlugin;
import org.allaymc.playerhomes.data.HomeData;
import org.allaymc.playerhomes.managers.HomeManager;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

public class HomeCommand extends Command {
    
    private final PlayerHomesPlugin plugin;
    private final HomeManager homeManager;
    
    public HomeCommand(PlayerHomesPlugin plugin) {
        super("home", "Player homes management", "playerhomes.use");
        this.plugin = plugin;
        this.homeManager = plugin.getHomeManager();
        aliases.add("h");
    }
    
    @Override
    public void prepareCommandTree(CommandTree tree) {
        tree.getRoot()
            .key("set")
            .str("name")
            .exec(context -> {
                if (!(context.getSender() instanceof EntityPlayer player)) {
                    context.getSender().sendMessage("§cThis command can only be used by players!");
                    return context.fail();
                }
                
                String homeName = context.getResult(1);
                if (!homeName.matches("^[a-zA-Z0-9_]+$")) {
                    player.sendMessage("§cHome name can only contain letters, numbers, and underscores!");
                    return context.fail();
                }
                
                if (homeName.length() > 16) {
                    player.sendMessage("§cHome name must be 16 characters or less!");
                    return context.fail();
                }
                
                int maxHomes = homeManager.getMaxHomes(player.getUniqueId());
                int currentHomes = homeManager.getHomeCount(player.getUniqueId());
                
                var location = player.getLocation();
                boolean updated = homeManager.getHome(player.getUniqueId(), homeName) != null;
                
                if (!updated && currentHomes >= maxHomes) {
                    player.sendMessage("§cYou have reached the maximum number of homes (" + maxHomes + ")!");
                    player.sendMessage("§7Delete a home with /home delete <name>");
                    return context.fail();
                }
                
                if (homeManager.setHome(player.getUniqueId(), homeName, location)) {
                    if (updated) {
                        player.sendMessage("§aHome §e" + homeName + "§a has been updated!");
                    } else {
                        player.sendMessage("§aHome §e" + homeName + "§a has been set!");
                        player.sendMessage("§7You have §e" + (currentHomes + 1) + "/" + maxHomes + "§7 homes set.");
                    }
                    return context.success();
                } else {
                    player.sendMessage("§cFailed to set home. Please try again.");
                    return context.fail();
                }
            })
            .root()
            .key("delete")
            .str("name")
            .exec(context -> {
                if (!(context.getSender() instanceof EntityPlayer player)) {
                    context.getSender().sendMessage("§cThis command can only be used by players!");
                    return context.fail();
                }
                
                if (player.hasPermission("playerhomes.delete") != Tristate.TRUE) {
                    player.sendMessage("§cYou don't have permission to delete homes!");
                    return context.fail();
                }
                
                String homeName = context.getResult(1);
                
                if (homeManager.deleteHome(player.getUniqueId(), homeName)) {
                    player.sendMessage("§aHome §e" + homeName + "§a has been deleted!");
                    return context.success();
                } else {
                    player.sendMessage("§cHome §e" + homeName + "§c does not exist!");
                    player.sendMessage("§7Use /home list to see your homes.");
                    return context.fail();
                }
            })
            .root()
            .key("list")
            .exec(context -> {
                if (!(context.getSender() instanceof EntityPlayer player)) {
                    context.getSender().sendMessage("§cThis command can only be used by players!");
                    return context.fail();
                }
                
                Map<String, HomeData> homes = homeManager.getPlayerHomes(player.getUniqueId());
                
                if (homes.isEmpty()) {
                    player.sendMessage("§7You have no homes set.");
                    player.sendMessage("§7Use /home set <name> to create a home.");
                    return context.success();
                }
                
                int maxHomes = homeManager.getMaxHomes(player.getUniqueId());
                player.sendMessage("§6=== Your Homes (" + homes.size() + "/" + maxHomes + ") ===");
                
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
                
                for (Map.Entry<String, HomeData> entry : homes.entrySet()) {
                    HomeData home = entry.getValue();
                    String dateStr = sdf.format(new Date(home.getCreatedAt()));
                    player.sendMessage("§e" + entry.getKey() + " §7- §f" + home.getWorldName() + 
                        " §7(§f" + (int)home.getX() + ", " + (int)home.getY() + ", " + (int)home.getZ() + "§7)" +
                        " §7- " + dateStr);
                }
                
                player.sendMessage("§7Use /home <name> to teleport");
                return context.success();
            })
            .root()
            .str("name")
            .exec(context -> {
                if (!(context.getSender() instanceof EntityPlayer player)) {
                    context.getSender().sendMessage("§cThis command can only be used by players!");
                    return context.fail();
                }
                
                String homeName = context.getResult(0);
                
                if (homeManager.teleportToHome(player, homeName)) {
                    player.sendMessage("§aTeleported to home §e" + homeName + "§a!");
                    return context.success();
                } else {
                    player.sendMessage("§cHome §e" + homeName + "§c does not exist!");
                    player.sendMessage("§7Use /home list to see your homes.");
                    return context.fail();
                }
            })
            .root()
            .key("help")
            .exec(context -> {
                context.getSender().sendMessage("§6=== PlayerHomes Help ===");
                context.getSender().sendMessage("§e/home set <name> §7- Set a home at your current location");
                context.getSender().sendMessage("§e/home <name> §7- Teleport to a home");
                context.getSender().sendMessage("§e/home delete <name> §7- Delete a home");
                context.getSender().sendMessage("§e/home list §7- List all your homes");
                context.getSender().sendMessage("§e/home help §7- Show this help message");
                return context.success();
            });
    }
}
