package dev.ferex.conduitWarpPlugin;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockBreakEvent;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Optional;

import static dev.ferex.conduitWarpPlugin.ConduitWarpPlugin.existingWarps;

public class BlockBreakListener extends ConduitListener {

    private final Connection dbConnection;

    public BlockBreakListener(final Connection connection) {
        this.dbConnection = connection;
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        final Block block = event.getBlock();
        if (block.getType() == Material.CONDUIT && isRegisteredConduit(block)) {
            final Warp warp = getWarpFromLocation(block.getLocation());
            if (warp != null) {
                try {
                    (dbConnection.prepareStatement("DELETE FROM warps WHERE Name='" + warp.name + "'")).executeUpdate();
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
                existingWarps.remove(warp);
            }
        }
    }

    private Warp getWarpFromLocation(final Location location) {
        final Optional<Warp> foundWarp =
                existingWarps.stream().filter(warp -> warp.location.equals(location)).findFirst();
        return foundWarp.orElse(null);
    }
}
