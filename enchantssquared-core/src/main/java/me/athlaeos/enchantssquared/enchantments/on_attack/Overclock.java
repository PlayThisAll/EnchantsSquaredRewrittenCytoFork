package me.athlaeos.enchantssquared.enchantments.on_attack;

import me.athlaeos.enchantssquared.EnchantsSquared;
import me.athlaeos.enchantssquared.config.ConfigManager;
import me.athlaeos.enchantssquared.domain.MaterialClassType;
import me.athlaeos.enchantssquared.enchantments.CustomEnchant;
import me.athlaeos.enchantssquared.enchantments.LevelService;
import me.athlaeos.enchantssquared.enchantments.LevelsFromMainHandAndEquipment;
import me.athlaeos.enchantssquared.enchantments.LevelsFromOffHandAndEquipment;
import me.athlaeos.enchantssquared.enchantments.on_heal.TriggerOnHealthRegainedEnchantment;
import me.athlaeos.enchantssquared.utility.ItemUtils;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.inventory.ItemStack;

import java.util.*;
import java.util.stream.Collectors;

public class Overclock extends CustomEnchant implements TriggerOnAttackEnchantment {

    private final int durationBase;
    private final int durationLv;
    private final double damageBase;
    private final double damageLv;
    private final YamlConfiguration config;
    private final Collection<String> incompatibleVanillaEnchantments;
    private final Collection<String> incompatibleCustomEnchantments;

    public Overclock(int id, String type) {
        super(id, type);
        this.config = ConfigManager.getInstance().getConfig("config.yml").get();
        this.damageBase = config.getDouble("enchantment_configuration.overclock.damage_base");
        this.damageLv = config.getDouble("enchantment_configuration.overclock.damage_lv");
        this.durationBase = config.getInt("enchantment_configuration.overclock.cooldown_base");
        this.durationLv = config.getInt("enchantment_configuration.overclock.cooldown_lv");
        this.naturallyCompatibleWith = new HashSet<>(config.getStringList("enchantment_configuration.overclock.compatible_with"));
        this.incompatibleVanillaEnchantments = new HashSet<>(config.getStringList("enchantment_configuration.overclock.incompatible_vanilla_enchantments"));
        this.incompatibleCustomEnchantments = new HashSet<>(config.getStringList("enchantment_configuration.overclock.incompatible_custom_enchantments"));

        this.icon = ItemUtils.getIconFromConfig(config, "enchantment_configuration.overclock.icon", createIcon(Material.BONE));
    }

    private final LevelService mainHandLevels = new LevelsFromMainHandAndEquipment(this);
    private final LevelService offHandLevels = new LevelsFromOffHandAndEquipment(this);
    @Override
    public LevelService getLevelService(boolean offHand, LivingEntity entity) {
        return offHand ? offHandLevels : mainHandLevels;
    }

    @Override
    public void onAttack(EntityDamageByEntityEvent e, int level, LivingEntity realAttacker) {
        LivingEntity victim = (LivingEntity) e.getEntity();
        if (shouldEnchantmentCancel(level, realAttacker, victim.getLocation())) return;
        double damageMulti = 1 + damageBase + ((level - 1) * damageLv);
        int cooldownDuration = durationBase + ((level - 1) * durationLv);
        if(realAttacker instanceof Player player) {
            if(player.hasCooldown(Material.MACE)) {
                e.setCancelled(true);
                return;
            }
            e.setDamage(e.getDamage() * damageMulti);
            new BukkitRunnable(){
                @Override
                public void run() {
                    player.setCooldown(Material.MACE, cooldownDuration);
                }
            }.runTaskLater(EnchantsSquared.getPlugin(), 1L);
        }
        
    }

    @Override
    public String getDisplayEnchantment() {
        return config.getString("enchantment_configuration.overclock.enchant_name", getType())
                .replace(" %lv_roman%", "")
                .replace(" %lv_number%", "");
    }

    @Override
    public String getDescription() {
        return config.getString("enchantment_configuration.overclock.description");
    }

    @Override
    public boolean isEnabled() {
        return config.getBoolean("enchantment_configuration.overclock.enabled");
    }

    @Override
    public String getRequiredPermission() {
        return "es.enchant.overclock";
    }

    @Override
    public boolean conflictsWithEnchantment(String enchantment) {
        return incompatibleCustomEnchantments.contains(enchantment) || incompatibleVanillaEnchantments.contains(enchantment);
    }

    private final Collection<String> naturallyCompatibleWith;
    @Override
    public boolean isNaturallyCompatible(Material material) {
        return MaterialClassType.isMatchingClass(material, naturallyCompatibleWith);
    }

    @Override
    public boolean isFunctionallyCompatible(Material material) {
        return material == Material.MACE;
    }

    @Override
    public int getWeight() {
        return config.getInt("enchantment_configuration.overclock.weight");
    }

    @Override
    public int getMaxLevel() {
        return config.getInt("enchantment_configuration.overclock.max_level");
    }

    @Override
    public int getMaxTableLevel() {
        return config.getInt("enchantment_configuration.overclock.max_level_table");
    }

    @Override
    public boolean isTreasure() {
        return config.getBoolean("enchantment_configuration.overclock.is_treasure");
    }

    @Override
    public boolean isBookOnly() {
        return config.getBoolean("enchantment_configuration.overclock.book_only");
    }

    @Override
    public boolean isTradingEnabled() {
        return config.getBoolean("enchantment_configuration.overclock.trade_enabled");
    }

    @Override
    public int getTradingMinBasePrice() {
        return config.getInt("enchantment_configuration.overclock.trade_cost_base_lower");
    }

    @Override
    public int getTradingMaxBasePrice() {
        return config.getInt("enchantment_configuration.overclock.trade_cost_base_upper");
    }

    @Override
    public int getTradingMinLeveledPrice() {
        return config.getInt("enchantment_configuration.overclock.trade_cost_lv_lower");
    }

    @Override
    public int getTradingMaxLeveledPrice() {
        return config.getInt("enchantment_configuration.overclock.trade_cost_base_upper");
    }

    private final ItemStack icon;
    @Override
    public ItemStack getIcon() {
        return icon;
    }

    @Override
    public String getWorldGuardFlagName() {
        return "es-deny-overclock";
    }

    @Override
    public Collection<String> getCompatibleItems() {
        return naturallyCompatibleWith;
    }
}
