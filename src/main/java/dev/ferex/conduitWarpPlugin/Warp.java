package dev.ferex.conduitWarpPlugin;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;

public class Warp {
    public final String name;
    public final Material material;
    public final Location location;

    public Warp(final String name, final Material material, final World world, final int x, final int y, final int z) {
        this(name, material, new Location(world, x + 0.5, y + 1, z + 0.5));
    }

    public Warp(final String name, final Material material, final Location location) {
        this.name = name;
        this.material = material;
        this.location = location.add(0.5, 1, 0.5);
    }
}
