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
import com.gmail.nossr50.events.skills.repair.McMMOPlayerRepairCheckEvent;
import com.gmail.nossr50.mcMMO;
import com.gmail.nossr50.skills.repair.RepairManager;
import com.gmail.nossr50.util.Permissions;
import com.gmail.nossr50.util.player.UserManager;
import com.gmail.nossr50.util.random.ProbabilityUtil;
import com.gmail.nossr50.util.skills.RankUtils;

public class McmmoRepairListener implements Listener {
    //code pretty much ripped from McMMO because they have it working and I figured I could as well
    private final String repairFail = ConfigManager.getInstance().getConfig("translations.yml").get().getString("repair_fail");
    private final String repairPartial = ConfigManager.getInstance().getConfig("translations.yml").get().getString("repair_partial");
    private final String repairFull = ConfigManager.getInstance().getConfig("translations.yml").get().getString("repair_success");

    @EventHandler
    public void onHandleRepair(McMMOPlayerRepairCheckEvent event){
        Player player = event.getPlayer();
        ItemStack repairItem = event.getRepairedObject();
        Map<CustomEnchant, Integer> customEnchants = CustomEnchantManager.getInstance().getItemsEnchantsFromPDC(repairItem);
        CustomEnchantManager.getInstance().removeAllEnchants(repairItem);

        if (!RankUtils.hasUnlockedSubskill(player, SubSkillType.REPAIR_ARCANE_FORGING) || !Permissions.isSubSkillEnabled(player, SubSkillType.REPAIR_ARCANE_FORGING)) return;
        if (customEnchants == null) return;
        
        McMMOPlayer mcmmoplayer = UserManager.getPlayer(player.getName());
        RepairManager repair = new RepairManager(mcmmoplayer);
        boolean downgraded = false;
        int arcaneFailureCount = 0;

        for (Entry<CustomEnchant, Integer> customEnchant : customEnchants.entrySet()) {

            int enchantLevel = customEnchant.getValue();

            String enchantment = customEnchant.getKey().getType();

            if (ProbabilityUtil.isStaticSkillRNGSuccessful(PrimarySkillType.REPAIR, mcmmoplayer,
                    repair.getKeepEnchantChance())) {

                if (mcMMO.p.getAdvancedConfig()
                        .getArcaneForgingDowngradeEnabled() && enchantLevel > 1
                        && (!ProbabilityUtil.isStaticSkillRNGSuccessful(PrimarySkillType.REPAIR,
                        mcmmoplayer, 100 - repair.getDowngradeEnchantChance()))) {
                    CustomEnchantManager.getInstance().addEnchant(repairItem, enchantment, enchantLevel - 1);
                    downgraded = true;
                } else {
                    CustomEnchantManager.getInstance().addEnchant(repairItem, enchantment, enchantLevel);
                }
            } else {
                downgraded = true;
            }
        }
        
        
        if (CustomEnchantManager.getInstance().getItemsEnchantsFromPDC(repairItem).isEmpty()) {
            player.sendMessage(ChatUtils.chat(repairFail));
        } else if(downgraded) {
            player.sendMessage(ChatUtils.chat(repairPartial));
        } else {
            player.sendMessage(ChatUtils.chat(repairFull));
        };
    }
}
