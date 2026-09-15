package me.athlaeos.enchantssquared.enchantments.on_attack;

import me.athlaeos.enchantssquared.EnchantsSquared;
import me.athlaeos.enchantssquared.config.ConfigManager;
import me.athlaeos.enchantssquared.domain.EntityClassificationType;
import me.athlaeos.enchantssquared.domain.MinecraftVersion;
import me.athlaeos.enchantssquared.enchantments.*;
import me.athlaeos.enchantssquared.enchantments.on_death.TriggerOnDeathEnchantment;
import me.athlaeos.enchantssquared.domain.MaterialClassType;
import me.athlaeos.enchantssquared.utility.ItemUtils;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.UUID;

import org.jetbrains.annotations.Nullable;


public class VoidStrike extends CustomEnchant implements TriggerOnAttackEnchantment {

    private final double damage_base;
    private final double damage_lv;
    private final double time_base;
    private final double time_lv;
    private final double ranged_damage_penalty;
    private final YamlConfiguration config;
    private final Collection<String> incompatibleVanillaEnchantments;
    private final Collection<String> incompatibleCustomEnchantments;
    private final CustomStatus statusEffect; 

    public VoidStrike(int id, String type) {
        super(id, type);
        this.config = ConfigManager.getInstance().getConfig("config.yml").get();
        this.time_base = config.getDouble("enchantment_configuration.void_strike.time_base");
        this.time_lv = config.getDouble("enchantment_configuration.void_strike.time_lv");
        this.naturallyCompatibleWith = new HashSet<>(config.getStringList("enchantment_configuration.void_strike.compatible_with"));
        this.incompatibleVanillaEnchantments = new HashSet<>(config.getStringList("enchantment_configuration.void_strike.incompatible_vanilla_enchantments"));
        this.incompatibleCustomEnchantments = new HashSet<>(config.getStringList("enchantment_configuration.void_strike.incompatible_custom_enchantments"));
        this.statusEffect = CustomStatusManager.getInstance().getStatusFromType("void_touch");

        this.icon = ItemUtils.getIconFromConfig(config, "enchantment_configuration.void_strike.icon", createIcon(Material.CRYING_OBSIDIAN));
    }

    private final LevelService mainHandLevels = new LevelsFromMainHandAndEquipment(this);
    private final LevelService offHandLevels = new LevelsFromOffHandAndEquipment(this);
    @Override
    public LevelService getLevelService(boolean offHand, LivingEntity entity) {
        return offHand ? offHandLevels : mainHandLevels;
    }
    //attacker, <victim, logTime>
    private final Map<UUID, Map<UUID, Long>> voidTouchedSet = new HashMap<>();

    @Override
    public void onAttack(EntityDamageByEntityEvent e, int level, LivingEntity realAttacker) {
        LivingEntity victim = (LivingEntity) e.getEntity();
        if (shouldEnchantmentCancel(level, realAttacker, victim.getLocation())) return;
        long length = time_base + ((level - 1) * time_lv);
        Bukkit.getScheduler().runTaskLater(EnchantsSquared.getPlugin(), () -> {statusEffect.addStatus(victim.getUniqueId(), realAttacker.getUniqueId(), length, level);}, 1L);
        
    }

    @Override
    public String getDisplayEnchantment() {
        return config.getString("enchantment_configuration.void_strike.enchant_name", getType())
                .replace(" %lv_roman%", "")
                .replace(" %lv_number%", "");
    }

    @Override
    public String getDescription() {
        return config.getString("enchantment_configuration.void_strike.description");
    }

    @Override
    public boolean isEnabled() {
        return config.getBoolean("enchantment_configuration.void_strike.enabled");
    }

    @Override
    public String getRequiredPermission() {
        return "es.enchant.void_strike";
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
        return config.getInt("enchantment_configuration.void_strike.weight");
    }

    @Override
    public int getMaxLevel() {
        return config.getInt("enchantment_configuration.void_strike.max_level");
    }

    @Override
    public int getMaxTableLevel() {
        return config.getInt("enchantment_configuration.void_strike.max_level_table");
    }

    @Override
    public boolean isTreasure() {
        return config.getBoolean("enchantment_configuration.void_strike.is_treasure");
    }

    @Override
    public boolean isBookOnly() {
        return config.getBoolean("enchantment_configuration.void_strike.book_only");
    }

    @Override
    public boolean isTradingEnabled() {
        return config.getBoolean("enchantment_configuration.void_strike.trade_enabled");
    }

    @Override
    public int getTradingMinBasePrice() {
        return config.getInt("enchantment_configuration.void_strike.trade_cost_base_lower");
    }

    @Override
    public int getTradingMaxBasePrice() {
        return config.getInt("enchantment_configuration.void_strike.trade_cost_base_upper");
    }

    @Override
    public int getTradingMinLeveledPrice() {
        return config.getInt("enchantment_configuration.void_strike.trade_cost_lv_lower");
    }

    @Override
    public int getTradingMaxLeveledPrice() {
        return config.getInt("enchantment_configuration.void_strike.trade_cost_base_upper");
    }

    private final ItemStack icon;
    @Override
    public ItemStack getIcon() {
        return icon;
    }

    @Override
    public String getWorldGuardFlagName() {
        return "es-deny-void-strike";
    }

    @Override
    public Collection<String> getCompatibleItems() {
        return naturallyCompatibleWith;
    }
}
