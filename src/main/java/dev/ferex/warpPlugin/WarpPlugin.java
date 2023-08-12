package dev.ferex.warpPlugin;

import org.bukkit.plugin.java.JavaPlugin;

public class WarpPlugin extends JavaPlugin {
    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(new InteractListener(), this);
    }
}