package dev.ferex.conduitWarpPlugin;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;

public class InteractListener implements Listener {
    private final Inventory warpInventory;

    public InteractListener() {
        warpInventory = Bukkit.createInventory(null, 9, "Warps");

        initialiseWarps();
    }

    private void initialiseWarps() {
        warpInventory.addItem(createWarpItem(Material.DIAMOND_SWORD, "Example Sword", "First line of lore", "Second line of lore"));
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
        if (event.getClickedBlock().getBlockData().getMaterial() == Material.CONDUIT) {
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
}
