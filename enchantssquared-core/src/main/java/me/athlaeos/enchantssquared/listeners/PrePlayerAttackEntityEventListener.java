package me.athlaeos.enchantssquared.listeners;

import io.papermc.paper.event.player.PrePlayerAttackEntityEvent;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.UUID;
import java.util.HashMap;

public class PrePlayerAttackEntityEventListener implements Listener {
    //this is a small helper listener to help keep track of latest cooldown attack
    private Map<UUID, Float> cooldownMap = new HashMap<>();

    public PrePlayerAttackEntityEventListener() {
        //empty
    }

    @EventHandler
    public void preAttack(PrePlayerAttackEntityEvent e) {
        Player player = e.getPlayer();
        cooldownMap.put(player.getUniqueId(), player.getCooledAttackStrength(0.0F));
    }

    public Float getAttackCooldown(UUID playerId) {
        return cooldownMap.get(playerId);
    }
}