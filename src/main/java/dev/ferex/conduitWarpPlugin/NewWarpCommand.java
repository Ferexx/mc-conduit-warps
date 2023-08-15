package dev.ferex.conduitWarpPlugin;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static dev.ferex.conduitWarpPlugin.ConduitWarpPlugin.getEconomy;

public class NewWarpCommand implements CommandExecutor, TabExecutor {
    private final Connection dbConnection;
    private final int NEW_WARP_COST;

    public NewWarpCommand(final Connection dbConnection, final FileConfiguration config) {
        this.dbConnection = dbConnection;
        this.NEW_WARP_COST = config.getInt(ConduitWarpPlugin.NEW_WARP_COST);
    }
    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        if (!(commandSender instanceof Player)) return false;
        final Player player = (Player) commandSender;

        try {
            Material.valueOf(strings[1]);
        } catch (IllegalArgumentException e) {
            player.sendMessage("Make sure you have entered a valid item");
            return false;
        }

        final Block targetedBlock = player.getTargetBlock(null, 20);
        if (targetedBlock.getType() == Material.CONDUIT) {
            if (getEconomy().has(player, NEW_WARP_COST)) {
                try {
                    (dbConnection.prepareStatement("INSERT INTO warps VALUES ('" + strings[0] + "','" + strings[1] + "'," + targetedBlock.getLocation().getBlockX() + "," + targetedBlock.getLocation().getBlockY() + "," + targetedBlock.getLocation().getBlockZ() + ")")).executeUpdate();
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
                getEconomy().withdrawPlayer(player, NEW_WARP_COST);
                player.sendMessage("Warp added.");
                return true;
            } else {
                player.sendMessage("You need $" + NEW_WARP_COST + " to add a warp.");
            }
        } else {
            player.sendMessage("Please make sure you are looking at a Conduit before running this command.");
        }
        return false;
    }

    @Override
    public List<String> onTabComplete(CommandSender commandSender, Command command, String s, String[] strings) {
        if (strings.length == 2) {
            return Arrays.stream(Material.values()).map(Enum::name).filter(name -> name.startsWith(strings[1])).collect(Collectors.toList());
        }
        return null;
    }
}
