package me.athlaeos.enchantssquared.listeners;

import me.athlaeos.enchantssquared.config.ConfigManager;
import me.athlaeos.enchantssquared.enchantments.CustomEnchant;
import me.athlaeos.enchantssquared.managers.CustomEnchantManager;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.GameMode;
import org.bukkit.inventory.ItemStack;

import com.gmail.nossr50.events.skills.fishing.McMMOPlayerMagicHunterEvent;

public class McmmoArcaneFishingListener implements Listener {
    private final int bookCustomEnchantRolls;
    public McmmoArcaneFishingListener(){
        bookCustomEnchantRolls = Math.max(1, ConfigManager.getInstance().getConfig("config.yml").get().getInt("custom_enchant_fish_rolls"));
    }

    @EventHandler
    public void onArcaneFishing(McMMOPlayerMagicHunterEvent event){
        ItemStack caughtItem = event.getTreasure();
        CustomEnchantManager manager = CustomEnchantManager.getInstance();
        manager.setItemEnchants(caughtItem, manager.getRandomEnchantments(caughtItem, event.getPlayer(), bookCustomEnchantRolls, true, manager.getCompatibleEnchants(caughtItem, GameMode.SURVIVAL)));
        event.setTreasure(caughtItem);
    }
}
