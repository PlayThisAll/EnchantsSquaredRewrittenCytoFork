package me.athlaeos.enchantssquared.enchantments.on_attack;

import me.athlaeos.enchantssquared.config.ConfigManager;
import me.athlaeos.enchantssquared.domain.EntityClassificationType;
import me.athlaeos.enchantssquared.domain.MinecraftVersion;
import me.athlaeos.enchantssquared.enchantments.*;
import me.athlaeos.enchantssquared.domain.MaterialClassType;
import me.athlaeos.enchantssquared.utility.ItemUtils;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Collection;
import java.util.HashSet;
import java.util.UUID;

public class MaceAOE extends CustomEnchant implements TriggerOnAttackEnchantment {

    private final double aoe_damage_base;
    private final double aoe_damage_lv;
    private final double radius;
    private final YamlConfiguration config;
    private final Collection<String> incompatibleVanillaEnchantments;
    private final Collection<String> incompatibleCustomEnchantments;

    private Collection<UUID> ignorePlayerEvents = new HashSet<>();

    public MaceAOE(int id, String type) {
        super(id, type);
        this.config = ConfigManager.getInstance().getConfig("config.yml").get();
        this.aoe_damage_base = config.getDouble("enchantment_configuration.mace_aoe.damage_base");
        this.aoe_damage_lv = config.getDouble("enchantment_configuration.mace_aoe.damage_lv");
        this.radius = config.getDouble("enchantment_configuration.mace_aoe.effect_radius");
        this.naturallyCompatibleWith = new HashSet<>(config.getStringList("enchantment_configuration.mace_aoe.compatible_with"));
        this.incompatibleVanillaEnchantments = new HashSet<>(config.getStringList("enchantment_configuration.mace_aoe.incompatible_vanilla_enchantments"));
        this.incompatibleCustomEnchantments = new HashSet<>(config.getStringList("enchantment_configuration.mace_aoe.incompatible_custom_enchantments"));

        this.icon = ItemUtils.getIconFromConfig(config, "enchantment_configuration.mace_aoe.icon", createIcon(Material.FIRE_CHARGE));
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
        if (shouldEnchantmentCancel(level, realAttacker, victim.getLocation()) || ignorePlayerEvents.contains(realAttacker.getUniqueId())) return;

        double finalRadius = this.radius;
        double finalDamage = this.aoe_damage_base + ((level - 1) * aoe_damage_lv);
        double damage = e.getDamage();
        Collection<Entity> surroundingEntities = victim.getWorld().getNearbyEntities(victim.getLocation(), finalRadius, finalRadius, finalRadius);
        surroundingEntities.remove(victim);
        surroundingEntities.remove(realAttacker);
        ignorePlayerEvents.add(realAttacker.getUniqueId());
        for (Entity entity : surroundingEntities){
            if (entity instanceof LivingEntity && !EntityClassificationType.isMatchingClassification(entity.getType(), EntityClassificationType.UNALIVE)){
                ((LivingEntity) entity).damage(damage * finalDamage, e.getDamager());
            }
        }
        ignorePlayerEvents.remove(realAttacker.getUniqueId());
    }

    @Override
    public String getDisplayEnchantment() {
        return config.getString("enchantment_configuration.mace_aoe.enchant_name", getType())
                .replace(" %lv_roman%", "")
                .replace(" %lv_number%", "");
    }

    @Override
    public String getDescription() {
        return config.getString("enchantment_configuration.mace_aoe.description");
    }

    @Override
    public boolean isEnabled() {
        return config.getBoolean("enchantment_configuration.mace_aoe.enabled");
    }

    @Override
    public String getRequiredPermission() {
        return "es.enchant.mace_aoe";
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
        return config.getInt("enchantment_configuration.mace_aoe.weight");
    }

    @Override
    public int getMaxLevel() {
        return config.getInt("enchantment_configuration.mace_aoe.max_level");
    }

    @Override
    public int getMaxTableLevel() {
        return config.getInt("enchantment_configuration.mace_aoe.max_level_table");
    }

    @Override
    public boolean isTreasure() {
        return config.getBoolean("enchantment_configuration.mace_aoe.is_treasure");
    }

    @Override
    public boolean isBookOnly() {
        return config.getBoolean("enchantment_configuration.mace_aoe.book_only");
    }

    @Override
    public boolean isTradingEnabled() {
        return config.getBoolean("enchantment_configuration.mace_aoe.trade_enabled");
    }

    @Override
    public int getTradingMinBasePrice() {
        return config.getInt("enchantment_configuration.mace_aoe.trade_cost_base_lower");
    }

    @Override
    public int getTradingMaxBasePrice() {
        return config.getInt("enchantment_configuration.mace_aoe.trade_cost_base_upper");
    }

    @Override
    public int getTradingMinLeveledPrice() {
        return config.getInt("enchantment_configuration.mace_aoe.trade_cost_lv_lower");
    }

    @Override
    public int getTradingMaxLeveledPrice() {
        return config.getInt("enchantment_configuration.mace_aoe.trade_cost_base_upper");
    }

    private final ItemStack icon;
    @Override
    public ItemStack getIcon() {
        return icon;
    }

    @Override
    public String getWorldGuardFlagName() {
        return "es-deny-mace-aoe";
    }

    @Override
    public Collection<String> getCompatibleItems() {
        return naturallyCompatibleWith;
    }
}
