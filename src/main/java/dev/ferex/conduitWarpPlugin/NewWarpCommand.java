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

import static dev.ferex.conduitWarpPlugin.ConduitWarpPlugin.*;

public class NewWarpCommand implements CommandExecutor, TabExecutor {
    private final Connection dbConnection;
    private final int NEW_WARP_COST;

    public NewWarpCommand(final Connection dbConnection, final FileConfiguration config) {
        this.dbConnection = dbConnection;
        this.NEW_WARP_COST = config.getInt(ConduitWarpPlugin.NEW_WARP_COST_PATH);
    }
    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        if (!(commandSender instanceof Player)) return false;
        final Player player = (Player) commandSender;

        if (strings.length != 2) return false;

        try {
            Material.valueOf(strings[1]);
        } catch (IllegalArgumentException e) {
            player.sendMessage(String.format("%s Make sure you have entered a valid item", WARP_MESSAGE_PREFIX));
            return false;
        }

        final Block targetedBlock = player.getTargetBlock(null, 20);
        if (targetedBlock.getType() == Material.CONDUIT) {
            if (getEconomy().has(player, NEW_WARP_COST)) {
                try {
                    (dbConnection.prepareStatement("INSERT INTO warps VALUES ('" + strings[0] + "','" + strings[1] + "','" + targetedBlock.getWorld().getName() + "'," + targetedBlock.getLocation().getBlockX() + "," + targetedBlock.getLocation().getBlockY() + "," + targetedBlock.getLocation().getBlockZ() + ")")).executeUpdate();
                } catch (SQLException e) {
                    if (e.getErrorCode() == 19) {
                        player.sendMessage(String.format("%s Warp name must be unique", WARP_MESSAGE_PREFIX));
                    }
                    return false;
                }
                existingWarps.add(new Warp(strings[0], Material.valueOf(strings[1]), targetedBlock.getLocation()));
                getEconomy().withdrawPlayer(player, NEW_WARP_COST);
                player.sendMessage(String.format("%s Warp added.", WARP_MESSAGE_PREFIX));
                return true;
            } else {
                player.sendMessage(String.format("%s You need $%d to add a warp.", WARP_MESSAGE_PREFIX, NEW_WARP_COST));
            }
        } else {
            player.sendMessage(String.format("%s Please make sure you are looking at a Conduit before running this command.", WARP_MESSAGE_PREFIX));
        }
        return false;
    }

    @Override
    public List<String> onTabComplete(CommandSender commandSender, Command command, String s, String[] strings) {
        if (strings.length == 2) {
            return Arrays.stream(Material.values()).map(Enum::name).filter(name -> name.contains(strings[1].toUpperCase())).collect(Collectors.toList());
        }
        return null;
    }
}
