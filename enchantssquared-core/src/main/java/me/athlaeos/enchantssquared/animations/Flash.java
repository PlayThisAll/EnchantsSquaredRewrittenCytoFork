package me.athlaeos.enchantssquared.animations;

import me.athlaeos.enchantssquared.domain.MinecraftVersion;
import me.athlaeos.enchantssquared.domain.Version;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;

public class Flash extends Animation{
    private final double size;

    public Flash(String argument) {
        super(argument);
        try {
            size = Double.parseDouble(argument);
        } catch (IllegalArgumentException e){
            throw new IllegalArgumentException("invalid flash size given for animation type " + this.getClass().getSimpleName());
        }
    }

    @Override
    public Animation getNew(String argument) {
        return new Flash(argument);
    }

    @Override
    public void play(Location l) {
        if (l.getWorld() == null) return;
        if (Version.currentVersionOrNewerThan(Version.MINECRAFT_1_19))
            if (MinecraftVersion.currentVersionNewerThan(MinecraftVersion.MINECRAFT_1_21_9)) {
                l.getWorld().spawnParticle(Particle.FLASH, l, 0, Color.WHITE);
            } else l.getWorld().spawnParticle(Particle.FLASH, l, 0, size);
        else {
            l.getWorld().spawnParticle(Particle.FLASH, l, 0);
        }
    }
}
