package dev.ferex.conduitWarpPlugin;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;

public class InteractListener implements Listener {
    private final Inventory warpInventory;
    private final Connection dbConnection;
    private final FileConfiguration config;

    public InteractListener(final Connection dbConnection, final FileConfiguration config) {
        this.warpInventory = Bukkit.createInventory(null, 9, "Warps");
        this.dbConnection = dbConnection;
        this.config = config;

        initialiseWarps();
    }

    private void initialiseWarps() {
        warpInventory.addItem(createWarpItem(Material.CONDUIT, "Previous Page"));
        // warps
        warpInventory.addItem(createWarpItem(Material.CONDUIT, "Next Page"));
    }

    private ItemStack createWarpItem(final Material material, final String name, final String... lore) {
        final ItemStack item = new ItemStack(material, 1);
        final ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        meta.setLore(Arrays.asList(lore));
        item.setItemMeta(meta);

        return item;
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK
                && event.getClickedBlock().getBlockData().getMaterial() == Material.CONDUIT) {
            if (isRegisteredConduit(event.getClickedBlock())) {
                event.getPlayer().openInventory(new InventoryView() {
                    @Override
                    public Inventory getTopInventory() {
                        return warpInventory;
                    }

                    @Override
                    public Inventory getBottomInventory() {
                        return event.getPlayer().getInventory();
                    }

                    @Override
                    public HumanEntity getPlayer() {
                        return event.getPlayer();
                    }

                    @Override
                    public InventoryType getType() {
                        return InventoryType.CHEST;
                    }

                    @Override
                    public String getTitle() {
                        return "Warps";
                    }
                });
            }
        }
    }

    @EventHandler
    public void onInventoryClick(final InventoryClickEvent event) {
        if (!event.getInventory().equals(warpInventory)) return;
        event.setCancelled(true);

        final ItemStack clickedItem = event.getCurrentItem();
        if (clickedItem == null || clickedItem.getType().isAir()) return;

        final Player player = (Player) event.getWhoClicked();
        if (clickedItem.getType() == Material.DIAMOND_SWORD) {
            player.teleport(new Location(player.getWorld(), 307, 84, 123));
        }
    }

    private boolean isRegisteredConduit(Block block) {
        try {
            final ResultSet warps = (dbConnection.prepareStatement("SELECT * FROM warps")).executeQuery();
            do {
                if (warps.getInt(3) == block.getLocation().getBlockX()
                    && warps.getInt(4) == block.getLocation().getBlockY()
                    && warps.getInt(5) == block.getLocation().getBlockZ()) {
                    return true;
                }
            } while (warps.next());
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
}
