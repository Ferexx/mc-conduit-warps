package dev.ferex.warpPlugin;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.event.Listener;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;

public class WarpGui implements Listener {
    private final Inventory inv;

    public WarpGui() {
        inv = Bukkit.createInventory(null, 9, "Warps");

        initialiseItems();
    }

    public void initialiseItems() {
        inv.addItem(createGuiItem(Material.DIAMOND_SWORD, "Example Sword", "First line of lore", "Second line of lore"));
    }

    protected ItemStack createGuiItem(final Material material, final String name, final String... lore) {
        final ItemStack item = new ItemStack(material, 1);
        final ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        meta.setLore(Arrays.asList(lore));
        item.setItemMeta(meta);

        return item;
    }
}
