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
import static dev.ferex.conduitWarpPlugin.ConduitWarpPlugin.getEconomy;

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
                final Inventory inventory = Bukkit.createInventory(null, 9, "Warps");
                pageNumbers.put(event.getPlayer().getName(), 0);
                openInventories.put(event.getPlayer().getName(), inventory);
                showWarpInventory(event.getPlayer(), inventory);
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
                pageNumbers.put(player.getName(), pageNumbers.get(player.getName()) - 1);
                showWarpInventory(player, event.getClickedInventory());
            } else if (clickedItem.getItemMeta().getDisplayName().equals("Next Page")) {
                pageNumbers.put(player.getName(), pageNumbers.get(player.getName()) + 1);
                showWarpInventory(player, event.getClickedInventory());
            } else {
                if (getEconomy().has(player, WARP_COST)) {
                    final Warp toWarp = existingWarps.stream().filter(warp -> warp.name.equals(clickedItem.getItemMeta().getDisplayName())).findFirst().get();
                    player.teleport(new Location(player.getWorld(), toWarp.x + 0.5, toWarp.y + 1, toWarp.z + 0.5));
                    getEconomy().withdrawPlayer(player, WARP_COST);
                } else {
                    player.sendMessage("You need $" + WARP_COST + " to warp.");
                }
            }
        }
    }

    private void showWarpInventory(final Player player, final Inventory warpInventory) {
        populateInventory(warpInventory, player.getName());
        player.openInventory(new InventoryView() {
            @Override
            public Inventory getTopInventory() {
                return warpInventory;
            }

            @Override
            public Inventory getBottomInventory() {
                return player.getInventory();
            }

            @Override
            public HumanEntity getPlayer() {
                return player;
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

    private boolean isRegisteredConduit(Block block) {
        return existingWarps.stream().anyMatch(warp -> warp.x == block.getX() && warp.y == block.getY() && warp.z == block.getZ());
    }

    private void populateInventory(final Inventory inventory, final String playerName) {
        inventory.clear();
        int currentSlot = 0;
        final int pageNumber = pageNumbers.get(playerName);
        int startNumber = pageNumber * 7;
        if (pageNumber > 0) {
            inventory.addItem(createWarpItem(Material.CONDUIT, "Previous Page"));
            currentSlot++;
        }
        while (currentSlot++ < 8 && startNumber < existingWarps.size()) {
            inventory.addItem(createWarpItem(existingWarps.get(startNumber++)));
        }
        if (startNumber == existingWarps.size() - 1) {
            inventory.addItem(createWarpItem(existingWarps.get(startNumber)));
        } else if (startNumber != existingWarps.size()) {
            inventory.addItem(createWarpItem(Material.CONDUIT, "Next Page"));
        }
    }
}
