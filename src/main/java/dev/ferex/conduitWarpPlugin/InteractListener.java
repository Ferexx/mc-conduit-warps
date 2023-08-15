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
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

import static dev.ferex.conduitWarpPlugin.ConduitWarpPlugin.WARP_COST_PATH;

public class InteractListener implements Listener {
    public static final List<Warp> existingWarps = new ArrayList<>();
    private final Map<String, Inventory> openInventories = new HashMap<>();
    private final Map<String, Integer> pageNumbers = new HashMap<>();
    private final Connection dbConnection;
    private final int WARP_COST;

    public InteractListener(final Connection dbConnection, final FileConfiguration config) {
        this.dbConnection = dbConnection;
        this.WARP_COST = config.getInt(WARP_COST_PATH);

        initialiseWarps();
    }

    private void initialiseWarps() {
        try {
            final ResultSet warpsFromDb = (dbConnection.prepareStatement("SELECT * FROM warps")).executeQuery();
            while (warpsFromDb.next()) {
                if (warpsFromDb.isClosed()) break;
                existingWarps.add(new Warp(warpsFromDb.getString(1), Material.valueOf(warpsFromDb.getString(2)), warpsFromDb.getInt(3), warpsFromDb.getInt(4), warpsFromDb.getInt(5)));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private ItemStack createWarpItem(final Material material, final String name, final String... lore) {
        final ItemStack item = new ItemStack(material, 1);
        final ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        meta.setLore(Arrays.asList(lore));
        item.setItemMeta(meta);

        return item;
    }

    private ItemStack createWarpItem(final Warp warp) {
        return createWarpItem(warp.material, warp.name);
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK
                && event.getClickedBlock().getBlockData().getMaterial() == Material.CONDUIT) {
            if (isRegisteredConduit(event.getClickedBlock())) {
                event.getPlayer().openInventory(new InventoryView() {
                    @Override
                    public Inventory getTopInventory() {
                        final Inventory inventory = Bukkit.createInventory(null, 9, "Warps");
                        inventory.addItem(createWarpItem(Material.CONDUIT, "Previous Page"));
                        final int pageNumber = pageNumbers.computeIfAbsent(event.getPlayer().getName(), number -> 0);
                        final int startNumber = pageNumber * 7;
                        for (int i = startNumber; i <= startNumber + 6; i++) {
                            inventory.addItem(createWarpItem(existingWarps.get(i)));
                        }
                        inventory.addItem(createWarpItem(Material.CONDUIT, "Next Page"));
                        openInventories.put(event.getPlayer().getName(), inventory);
                        return inventory;
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
        final Player player = (Player) event.getWhoClicked();
        if (!event.getInventory().equals(openInventories.get(player.getName()))) return;
        event.setCancelled(true);

        final ItemStack clickedItem = event.getCurrentItem();
        if (clickedItem == null || clickedItem.getType().isAir()) return;

        if (Objects.requireNonNull(clickedItem.getItemMeta()).hasDisplayName()) {
            if (clickedItem.getItemMeta().getDisplayName().equals("Previous Page")) {
                // previous page
            }
            if (clickedItem.getItemMeta().getDisplayName().equals("Next Page")) {
                // next page
            }
            final Warp toWarp = existingWarps.stream().filter(warp -> warp.name.equals(clickedItem.getItemMeta().getDisplayName())).findFirst().get();
            player.teleport(new Location(player.getWorld(), toWarp.x + 0.5, toWarp.y + 1, toWarp.z + 0.5));
        }
    }

    @EventHandler
    public void onInventoryClose(final InventoryCloseEvent event) {
        final Player player = (Player) event.getPlayer();
        if (openInventories.containsKey(player.getName())) {
            openInventories.remove(player.getName());
            pageNumbers.remove(player.getName());
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
