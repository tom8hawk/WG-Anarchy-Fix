package ru.overwrite.wggf;

import dev.espi.protectionstones.ProtectionStones;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;

public class WorldGuardGriefFixPlugin extends JavaPlugin implements Listener {
    private static final List<ProtectedRegion> protectedRegionList = new ArrayList<>();
    private static FileConfiguration configuration;

    public void onEnable() {
        saveDefaultConfig();
        configuration = getConfig();
        loadRegions();

        getCommand("wggf").setExecutor((sender, command, label, args) -> {
            if (!sender.hasPermission("wggf.command.reload")) {
                sender.sendMessage(color("&cYou don't have permission!"));
                return true;
            }

            if (args.length == 0 || !args[0].equalsIgnoreCase("reload")) {
                sender.sendMessage(color("&f[&6WGGF&f] /wggf reload - перезагрузить плагин"));
                return true;
            }

            reloadConfig();

            if (!protectedRegionList.isEmpty()) {
                protectedRegionList.clear();
            }

            loadRegions();
            sender.sendMessage(color("&f[&6WGGF&f] Вы успешно перезагрузили плагин!"));

            return true;
        });

        Bukkit.getPluginManager().registerEvents(this, this);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onItemMove(InventoryMoveItemEvent event) {
        if (event.isCancelled() && configuration.getBoolean("enable-minecart")) {
            event.setCancelled(false);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onEntityExplode(EntityExplodeEvent event) {
        if (event.isCancelled() && configuration.getBoolean("enable-any-explosion") ||
                (event.getEntityType() == EntityType.WITHER_SKULL && configuration.getBoolean("enable-wither-skull"))) {

            event.blockList().removeIf(exploded -> !canBreak(exploded));
            event.setCancelled(false);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPistonExtend(BlockPistonExtendEvent event) {
        if (event.isCancelled() && configuration.getBoolean("enable-pistons")) {
            for (Block block : event.getBlocks()) {
                if (canBreak(block)) {
                    event.setCancelled(false);
                    return;
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onBlockRetract(BlockPistonRetractEvent event) {
        if (event.isCancelled() && configuration.getBoolean("enable-pistons")) {
            for (Block block : event.getBlocks()) {
                if (canBreak(block)) {
                    event.setCancelled(false);
                    return;
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onWitherBlockDamage(EntityChangeBlockEvent event) {
        if (event.isCancelled() && event.getEntityType() == EntityType.WITHER &&
                configuration.getBoolean("enable-wither") && canBreak(event.getBlock())) {

            event.setCancelled(false);
        }
    }

    private boolean canBreak(Block block) {
        if (!ProtectionStones.isProtectBlock(block)) {
            Location location = block.getLocation();
            for (ProtectedRegion protectedRegion : protectedRegionList) {
                if (protectedRegion.contains(location.getBlockX(), location.getBlockY(), location.getBlockZ())) {
                    return true;
                }
            }
        }

        return false;
    }

    private static void loadRegions() {
        for (String regionName : configuration.getConfigurationSection("regions").getKeys(false)) {
            int x1 = configuration.getInt("regions." + regionName + ".x1", 0);
            int y1 = configuration.getInt("regions." + regionName + ".y1", 0);
            int z1 = configuration.getInt("regions." + regionName + ".z1", 0);

            int x2 = configuration.getInt("regions." + regionName + ".x2", 0);
            int y2 = configuration.getInt("regions." + regionName + ".y2", 0);
            int z2 = configuration.getInt("regions." + regionName + ".z2", 0);

            protectedRegionList.add(new ProtectedRegion(x1, y1, z1, x2, y2, z2));
        }
    }

    private static String color(String line) {
        return ChatColor.translateAlternateColorCodes('&', line);
    }
}