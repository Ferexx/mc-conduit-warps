package dev.ferex.conduitWarpPlugin;

import org.bukkit.block.Block;
import org.bukkit.event.Listener;

import static dev.ferex.conduitWarpPlugin.ConduitWarpPlugin.existingWarps;

public class ConduitListener implements Listener {

    protected boolean isRegisteredConduit(Block block) {
        return existingWarps.stream().anyMatch(warp -> warp.location.clone().subtract(0.5, 1, 0.5).equals(block.getLocation()));
    }

}
