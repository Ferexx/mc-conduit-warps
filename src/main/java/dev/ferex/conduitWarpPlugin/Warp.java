package dev.ferex.conduitWarpPlugin;

import org.bukkit.Material;

public class Warp {
    public final String name;
    public final Material material;
    public final int x;
    public final int y;
    public final int z;

    public Warp(final String name, final Material material, final int x, final int y, final int z) {
        this.name = name;
        this.material = material;
        this.x = x;
        this.y = y;
        this.z = z;
    }
}
