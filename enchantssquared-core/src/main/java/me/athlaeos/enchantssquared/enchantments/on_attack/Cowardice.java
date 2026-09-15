package me.athlaeos.enchantssquared.enchantments.on_attack;

import me.athlaeos.enchantssquared.EnchantsSquared;
import me.athlaeos.enchantssquared.config.ConfigManager;
import me.athlaeos.enchantssquared.domain.MaterialClassType;
import me.athlaeos.enchantssquared.enchantments.CustomEnchant;
import me.athlaeos.enchantssquared.enchantments.LevelService;
import me.athlaeos.enchantssquared.enchantments.LevelsFromAllEquipment;
import me.athlaeos.enchantssquared.utility.EntityUtils;
import me.athlaeos.enchantssquared.utility.ItemUtils;

import org.bukkit.attribute.Attribute;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Projectile;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Collection;
import java.util.HashSet;

public class Cowardice extends CustomEnchant implements TriggerOnAttackEnchantment {

    private final double damageBase;
    private final double damageLv;
    private final double rangedPenalty;
    private final double graceHP;
    private final YamlConfiguration config;
    private final Collection<String> incompatibleVanillaEnchantments;
    private final Collection<String> incompatibleCustomEnchantments;

    public Cowardice(int id, String type) {
        super(id, type);
        this.config = ConfigManager.getInstance().getConfig("config.yml").get();
        this.damageBase = config.getDouble("enchantment_configuration.cowardice.damage_base");
        this.damageLv = config.getDouble("enchantment_configuration.cowardice.damage_lv");
        this.rangedPenalty = config.getDouble("enchantment_configuration.cowardice.ranged_damage_penalty");
        this.graceHP = config.getDouble("enchantment_configuration.cowardice.grace_hp_amount");
        this.naturallyCompatibleWith = new HashSet<>(config.getStringList("enchantment_configuration.cowardice.compatible_with"));
        this.incompatibleVanillaEnchantments = new HashSet<>(config.getStringList("enchantment_configuration.cowardice.incompatible_vanilla_enchantments"));
        this.incompatibleCustomEnchantments = new HashSet<>(config.getStringList("enchantment_configuration.cowardice.incompatible_custom_enchantments"));

        this.icon = ItemUtils.getIconFromConfig(config, "enchantment_configuration.cowardice.icon", createIcon(Material.ARMOR_STAND));
    }

    private final LevelService levelService = new LevelsFromAllEquipment(this);
    @Override
    public LevelService getLevelService(boolean offHand, LivingEntity entity) {
        return levelService;
    }

    @Override
    public void onAttack(EntityDamageByEntityEvent e, int level, LivingEntity realAttacker) {
        LivingEntity victim = (LivingEntity) e.getEntity();
        if (shouldEnchantmentCancel(level, realAttacker, victim.getLocation())) return;
        if ((realAttacker.getAttribute(Attribute.MAX_HEALTH).getValue() - realAttacker.getHealth()) < graceHP) {
            double multiplier = damageBase + ((level - 1) * damageLv);
            if (e.getDamager() instanceof Projectile) multiplier *= (1 - rangedPenalty);
            e.setDamage(e.getDamage() * (1 + multiplier));
        }        
    }

    @Override
    public String getDisplayEnchantment() {
        return config.getString("enchantment_configuration.cowardice.enchant_name", getType())
                .replace(" %lv_roman%", "")
                .replace(" %lv_number%", "");
    }

    @Override
    public String getDescription() {
        return config.getString("enchantment_configuration.cowardice.description");
    }

    @Override
    public boolean isEnabled() {
        return config.getBoolean("enchantment_configuration.cowardice.enabled");
    }

    @Override
    public String getRequiredPermission() {
        return "es.enchant.cowardice";
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
        return true;
    }

    @Override
    public int getWeight() {
        return config.getInt("enchantment_configuration.cowardice.weight");
    }

    @Override
    public int getMaxLevel() {
        return config.getInt("enchantment_configuration.cowardice.max_level");
    }

    @Override
    public int getMaxTableLevel() {
        return config.getInt("enchantment_configuration.cowardice.max_level_table");
    }

    @Override
    public boolean isTreasure() {
        return config.getBoolean("enchantment_configuration.cowardice.is_treasure");
    }

    @Override
    public boolean isBookOnly() {
        return config.getBoolean("enchantment_configuration.cowardice.book_only");
    }

    @Override
    public boolean isTradingEnabled() {
        return config.getBoolean("enchantment_configuration.cowardice.trade_enabled");
    }

    @Override
    public int getTradingMinBasePrice() {
        return config.getInt("enchantment_configuration.cowardice.trade_cost_base_lower");
    }

    @Override
    public int getTradingMaxBasePrice() {
        return config.getInt("enchantment_configuration.cowardice.trade_cost_base_upper");
    }

    @Override
    public int getTradingMinLeveledPrice() {
        return config.getInt("enchantment_configuration.cowardice.trade_cost_lv_lower");
    }

    @Override
    public int getTradingMaxLeveledPrice() {
        return config.getInt("enchantment_configuration.cowardice.trade_cost_base_upper");
    }

    private final ItemStack icon;
    @Override
    public ItemStack getIcon() {
        return icon;
    }

    @Override
    public String getWorldGuardFlagName() {
        return "es-deny-cowardice";
    }

    @Override
    public Collection<String> getCompatibleItems() {
        return naturallyCompatibleWith;
    }
}
