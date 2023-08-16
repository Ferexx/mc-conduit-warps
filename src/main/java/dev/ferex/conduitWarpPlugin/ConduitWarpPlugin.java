package dev.ferex.conduitWarpPlugin;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ConduitWarpPlugin extends JavaPlugin {
    public static final String WARPS_LIST_PATH = "warps";
    public static final String NEW_WARP_COST_PATH = "newWarpCost";
    public static final String WARP_COST_PATH = "warpCost";

    public static final List<Warp> existingWarps = new ArrayList<>();
    private static Economy economy = null;
    private static final Logger logger = Logger.getLogger("Minecraft");
    private Connection dbConnection = null;

    FileConfiguration config = getConfig();
    @Override
    public void onEnable() {
        try {
            File sqliteFile = new File(getDataFolder(), "db.sqlite");
            sqliteFile.createNewFile();
            dbConnection = DriverManager.getConnection("jdbc:sqlite:" + sqliteFile);
            (dbConnection.prepareStatement("CREATE TABLE IF NOT EXISTS warps(Name varchar(64),Item varchar(64), World varchar(64), X Integer, Y Integer, Z Integer, PRIMARY KEY(Name))")).executeUpdate();
        } catch (SQLException | IOException e) {
            throw new RuntimeException(e);
        }

        initialiseWarps();

        config.addDefault(NEW_WARP_COST_PATH, 1_000_000);
        config.addDefault(WARP_COST_PATH, 1_000);
        config.options().copyDefaults(true);
        saveConfig();

        getServer().getPluginManager().registerEvents(new InteractListener(config.getInt(WARP_COST_PATH)), this);
        getServer().getPluginManager().registerEvents(new BlockBreakListener(dbConnection), this);

        this.getCommand("addWarp").setExecutor(new NewWarpCommand(dbConnection, config));

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

        logger.log(Level.ALL, String.format("[%s] Loaded %d warps", getDescription().getName(), config.getList(WARPS_LIST_PATH).size()));
    }

    @Override
    public void onDisable() {
        try {
            dbConnection.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private void initialiseWarps() {
        try {
            final ResultSet warpsFromDb = (dbConnection.prepareStatement("SELECT * FROM warps")).executeQuery();
            while (warpsFromDb.next()) {
                if (warpsFromDb.isClosed()) break;
                existingWarps.add(createWarpFromDbEntry(warpsFromDb));
            }
        } catch (SQLException | IllegalArgumentException e) {
            throw new RuntimeException(e);
        }
    }

    private Warp createWarpFromDbEntry(final ResultSet warpFromDb) throws SQLException, IllegalArgumentException {
        final String name = warpFromDb.getString(1);
        final Material material = Material.valueOf(warpFromDb.getString(2));
        final World world = Bukkit.getWorld(warpFromDb.getString(3));
        final int x = warpFromDb.getInt(4);
        final int y = warpFromDb.getInt(5);
        final int z = warpFromDb.getInt(6);
        return new Warp(name, material, world, x, y, z);
    }

    public static Economy getEconomy() {
        return economy;
    }
}