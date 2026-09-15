package me.athlaeos.enchantssquared.listeners;

import me.athlaeos.enchantssquared.config.ConfigManager;
import me.athlaeos.enchantssquared.enchantments.CustomEnchant;
import me.athlaeos.enchantssquared.managers.CustomEnchantManager;
import me.athlaeos.enchantssquared.utility.ChatUtils;

import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.Material;
import org.bukkit.plugin.java.JavaPlugin;

import com.gmail.nossr50.datatypes.player.McMMOPlayer;
import com.gmail.nossr50.datatypes.skills.PrimarySkillType;
import com.gmail.nossr50.datatypes.skills.SubSkillType;
import com.gmail.nossr50.events.skills.salvage.McMMOPlayerSalvageCheckEvent;
import com.gmail.nossr50.mcMMO;
import com.gmail.nossr50.skills.salvage.SalvageManager;
import com.gmail.nossr50.util.Permissions;
import com.gmail.nossr50.util.player.UserManager;
import com.gmail.nossr50.util.random.ProbabilityUtil;
import com.gmail.nossr50.util.skills.RankUtils;

public class McmmoSalvageListener implements Listener {
    //code pretty much ripped from McMMO because they have it working and I figured I could as well
    private final String salvageFail = ConfigManager.getInstance().getConfig("translations.yml").get().getString("salvage_fail");
    private final String salvagePartial = ConfigManager.getInstance().getConfig("translations.yml").get().getString("salvage_partial");

    @EventHandler
    public void onHandleSalvage(McMMOPlayerSalvageCheckEvent event){
        Player player = event.getPlayer();
        Map<CustomEnchant, Integer> customEnchants = CustomEnchantManager.getInstance().getItemsEnchantsFromPDC(event.getSalvageItem());

        if (!RankUtils.hasUnlockedSubskill(player, SubSkillType.SALVAGE_ARCANE_SALVAGE) || !Permissions.arcaneSalvage(player)) return;
        if (customEnchants == null) return;
        
        McMMOPlayer mcmmoplayer = UserManager.getPlayer(player.getName());
        ItemStack book = new ItemStack(Material.ENCHANTED_BOOK);
        SalvageManager salvage = new SalvageManager(mcmmoplayer);
        boolean downgraded = false;
        int arcaneFailureCount = 0;

        for (Entry<CustomEnchant, Integer> customEnchant : customEnchants.entrySet()) {

            int enchantLevel = customEnchant.getValue();

            if (!mcMMO.p.getAdvancedConfig().getArcaneSalvageEnchantLossEnabled()
                    || Permissions.hasSalvageEnchantBypassPerk(player)
                    || ProbabilityUtil.isStaticSkillRNGSuccessful(
                    PrimarySkillType.SALVAGE, mcmmoplayer, salvage.getExtractFullEnchantChance())) {
                CustomEnchantManager.getInstance().addEnchant(book, customEnchant.getKey().getType(), enchantLevel);
            } else if (enchantLevel > 1
                    && mcMMO.p.getAdvancedConfig().getArcaneSalvageEnchantDowngradeEnabled()
                    && ProbabilityUtil.isStaticSkillRNGSuccessful(
                    PrimarySkillType.SALVAGE, mcmmoplayer, salvage.getExtractPartialEnchantChance())) {
                CustomEnchantManager.getInstance().addEnchant(book, customEnchant.getKey().getType(), enchantLevel - 1);
                downgraded = true;
            } else {
                arcaneFailureCount++;
            }
        }
        
        
        if (CustomEnchantManager.getInstance().getItemsEnchantsFromPDC(book).isEmpty()) {
            player.sendMessage(ChatUtils.chat(salvageFail));
            return;
        } else if(downgraded) {
            player.sendMessage(ChatUtils.chat(salvagePartial));
        };


        
        player.getInventory().addItem(book);
    }
}
