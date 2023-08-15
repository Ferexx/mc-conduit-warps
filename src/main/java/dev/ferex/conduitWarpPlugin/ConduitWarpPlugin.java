package dev.ferex.conduitWarpPlugin;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ConduitWarpPlugin extends JavaPlugin {
    public static final String WARPS_LIST = "warps";
    public static final String NEW_WARP_COST = "newWarpCost";
    private static Economy economy = null;
    private static final Logger logger = Logger.getLogger("Minecraft");
    private Connection dbConnection = null;

    FileConfiguration config = getConfig();
    @Override
    public void onEnable() {
        try {
            File sqliteFile = new File(getDataFolder(), "db.db");
            sqliteFile.createNewFile();
            dbConnection = DriverManager.getConnection("jdbc:sqlite:" + sqliteFile);
            (dbConnection.prepareStatement("CREATE TABLE IF NOT EXISTS warps(Name varchar(64),Item varchar(64),X Integer, Y Integer, Z Integer)")).executeUpdate();
        } catch (SQLException | IOException e) {
            throw new RuntimeException(e);
        }

        getServer().getPluginManager().registerEvents(new InteractListener(dbConnection, config), this);

        this.getCommand("addWarp").setExecutor(new NewWarpCommand(dbConnection, config));

        config.addDefault(NEW_WARP_COST, 1_000_000);
        config.options().copyDefaults(true);
        saveConfig();

        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            logger.log(Level.SEVERE, "Vault not found, warps cannot be loaded");
            getServer().getPluginManager().disablePlugin(this);
        }
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            logger.log(Level.SEVERE, "Economy not found, warps cannot be loaded");
            getServer().getPluginManager().disablePlugin(this);
        }
        economy = rsp.getProvider();

        logger.log(Level.ALL, String.format("[%s] Loaded %d warps", getDescription().getName(), config.getList(WARPS_LIST).size()));
    }

    @Override
    public void onDisable() {
        try {
            dbConnection.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static Economy getEconomy() {
        return economy;
    }
}