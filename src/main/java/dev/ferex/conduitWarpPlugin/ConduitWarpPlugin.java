package dev.ferex.conduitWarpPlugin;

import org.bukkit.plugin.java.JavaPlugin;

public class ConduitWarpPlugin extends JavaPlugin {
    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(new InteractListener(), this);
    }
}