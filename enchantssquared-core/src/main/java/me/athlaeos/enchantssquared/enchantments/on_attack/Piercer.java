package me.athlaeos.enchantssquared.enchantments.on_attack;

import io.papermc.paper.datacomponent.DataComponentType;

import me.athlaeos.enchantssquared.EnchantsSquared;
import me.athlaeos.enchantssquared.config.ConfigManager;
import me.athlaeos.enchantssquared.domain.MaterialClassType;
import me.athlaeos.enchantssquared.enchantments.CustomEnchant;
import me.athlaeos.enchantssquared.enchantments.LevelService;
import me.athlaeos.enchantssquared.enchantments.LevelsFromMainHandAndEquipment;
import me.athlaeos.enchantssquared.listeners.PrePlayerAttackEntityEventListener;
import me.athlaeos.enchantssquared.utility.EntityUtils;
import me.athlaeos.enchantssquared.utility.ItemUtils;
import me.athlaeos.valhallammo.playerstats.EntityCache;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.EntityEffect;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityResurrectEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.Bukkit;

import java.util.Collection;
import java.util.HashSet;


public class Piercer extends CustomEnchant implements TriggerOnAttackEnchantment {

    private final double swordMulti;
    private final double potionMulti;
    private final boolean allowCrits;
    private final boolean allowPotionCrits;
    private final YamlConfiguration config;
    private final Collection<String> incompatibleVanillaEnchantments;
    private final Collection<String> incompatibleCustomEnchantments;
    private final PrePlayerAttackEntityEventListener cooldownListener;

    public Piercer(int id, String type) {
        super(id, type);
        this.config = ConfigManager.getInstance().getConfig("config.yml").get();
        this.swordMulti = config.getDouble("enchantment_configuration.piercer.sword_damage_pierce");
        this.potionMulti = config.getDouble("enchantment_configuration.piercer.potion_damage_pierce");
        this.allowCrits = config.getBoolean("enchantment_configuration.piercer.allow_critical_hits");
        this.allowPotionCrits = config.getBoolean("enchantment_configuration.piercer.allow_potion_crits");
        this.naturallyCompatibleWith = new HashSet<>(config.getStringList("enchantment_configuration.piercer.compatible_with"));
        this.incompatibleVanillaEnchantments = new HashSet<>(config.getStringList("enchantment_configuration.piercer.incompatible_vanilla_enchantments"));
        this.incompatibleCustomEnchantments = new HashSet<>(config.getStringList("enchantment_configuration.piercer.incompatible_custom_enchantments"));
        this.cooldownListener = EnchantsSquared.getPlugin().getPrePlayerAttackEntityEventListener();
        this.icon = ItemUtils.getIconFromConfig(config, "enchantment_configuration.piercer.icon", createIcon(Material.NETHERITE_SWORD));
    }

    private final LevelService mainHandLevels = new LevelsFromMainHandAndEquipment(this);

    @Override
    public LevelService getLevelService(boolean offHand, LivingEntity entity) {
        return mainHandLevels;
    }

    @Override
    public void onAttack(EntityDamageByEntityEvent e, int level, LivingEntity realAttacker) {
        LivingEntity victim = (LivingEntity) e.getEntity();
        if (shouldEnchantmentCancel(level, realAttacker, victim.getLocation())) return;
        //if (e.getCause() != EntityDamageEvent.DamageCause.ENTITY_ATTACK) return;
        double critMultiBase = e.isCritical() ? 1.5 : 1;
        double swordDamageBase = getSwordDamage(realAttacker.getEquipment().getItemInMainHand());
        double potionDamageBase = (realAttacker.getPotionEffect(PotionEffectType.STRENGTH) != null ? (realAttacker.getPotionEffect(PotionEffectType.STRENGTH).getAmplifier() + 1) * 3 : 0);
        double attackStrength = 1D;
        if(e.getDamager() instanceof Player player) attackStrength = cooldownListener.getAttackCooldown(player.getUniqueId());
        double critMulti = (allowCrits && e.isCritical()) ? 1.5 : 1;
        double finalSwordDamage = swordDamageBase * swordMulti * critMulti * attackStrength;
        double finalPotionDamage = potionDamageBase * potionMulti * attackStrength * critMulti;
        double finalDamage = finalSwordDamage + finalPotionDamage;
        e.setDamage(e.getDamage() - finalDamage);
        if(victim.getAbsorptionAmount() > finalDamage) {
            victim.setAbsorptionAmount(victim.getAbsorptionAmount() - finalDamage);
            return;
        } else {
            finalDamage -= victim.getAbsorptionAmount();
            victim.setAbsorptionAmount(0D);
        }
        if (victim.getHealth() > finalDamage || getTotemSlot(victim) == null) victim.setHealth(Math.max(victim.getHealth() - finalDamage, 0));
        else resurrectCheck(victim);
    }

    //yes, this is a hack because I couldn't be bothered to properly get the damage values.
    //I know I should do better, but I want to get it working first.
    private static double getSwordDamage(ItemStack item) {
        return switch (item.getType()) {
            case WOODEN_SWORD, GOLDEN_SWORD -> 4.0;
            case STONE_SWORD -> 5.0;
            case IRON_SWORD -> 6.0;
            case DIAMOND_SWORD -> 7.0;
            case NETHERITE_SWORD -> 8.0;
            default -> 0.0;
        };
    }

    private static EquipmentSlot getTotemSlot(LivingEntity victim) {
        EquipmentSlot totemSlot = null;
        EntityEquipment equipment = victim.getEquipment();
        //read the item to see if it has a tag in the future, for now... ye
        if(equipment.getItemInOffHand().getType() == Material.TOTEM_OF_UNDYING) totemSlot = EquipmentSlot.OFF_HAND;
        if(equipment.getItemInMainHand().getType() == Material.TOTEM_OF_UNDYING) totemSlot = EquipmentSlot.HAND;
        return totemSlot;
    }
    //technically there's NMS but I have no idea how to use that so this will do for now
    private static void resurrectCheck(LivingEntity victim) {
            EquipmentSlot totemSlot = getTotemSlot(victim);
            EntityEquipment equipment = victim.getEquipment();

            EntityResurrectEvent event = new EntityResurrectEvent(victim, totemSlot);
            Bukkit.getPluginManager().callEvent(event);
            if(event.isCancelled()) {
                return;
            }
            if (totemSlot == EquipmentSlot.HAND) equipment.setItemInMainHand(null);
            else equipment.setItemInOffHand(null);

            victim.setHealth(1.0);
            victim.setFireTicks(0);
            victim.setFallDistance(0);

            victim.clearActivePotionEffects();
            victim.addPotionEffect(
                new PotionEffect(PotionEffectType.REGENERATION, 900, 1)
            );
            victim.addPotionEffect(
                new PotionEffect(PotionEffectType.ABSORPTION, 100, 1)
            );
            victim.addPotionEffect(
                new PotionEffect(PotionEffectType.FIRE_RESISTANCE, 800, 0)
            );
            victim.playEffect(EntityEffect.PROTECTED_FROM_DEATH);
    }

    @Override
    public String getDisplayEnchantment() {
        return config.getString("enchantment_configuration.piercer.enchant_name", getType())
                .replace(" %lv_roman%", "")
                .replace(" %lv_number%", "");
    }

    @Override
    public String getDescription() {
        return config.getString("enchantment_configuration.piercer.description");
    }

    @Override
    public boolean isEnabled() {
        return config.getBoolean("enchantment_configuration.piercer.enabled");
    }

    @Override
    public String getRequiredPermission() {
        return "es.enchant.piercer";
    }

    @Override
    public boolean conflictsWithEnchantment(String enchantment) {
        return true;
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
        return config.getInt("enchantment_configuration.piercer.weight");
    }

    @Override
    public int getMaxLevel() {
        return config.getInt("enchantment_configuration.piercer.max_level");
    }

    @Override
    public int getMaxTableLevel() {
        return config.getInt("enchantment_configuration.piercer.max_level_table");
    }

    @Override
    public boolean isTreasure() {
        return config.getBoolean("enchantment_configuration.piercer.is_treasure");
    }

    @Override
    public boolean isBookOnly() {
        return config.getBoolean("enchantment_configuration.piercer.book_only");
    }

    @Override
    public boolean isTradingEnabled() {
        return config.getBoolean("enchantment_configuration.piercer.trade_enabled");
    }

    @Override
    public int getTradingMinBasePrice() {
        return config.getInt("enchantment_configuration.piercer.trade_cost_base_lower");
    }

    @Override
    public int getTradingMaxBasePrice() {
        return config.getInt("enchantment_configuration.piercer.trade_cost_base_upper");
    }

    @Override
    public int getTradingMinLeveledPrice() {
        return config.getInt("enchantment_configuration.piercer.trade_cost_lv_lower");
    }

    @Override
    public int getTradingMaxLeveledPrice() {
        return config.getInt("enchantment_configuration.piercer.trade_cost_base_upper");
    }

    private final ItemStack icon;
    @Override
    public ItemStack getIcon() {
        return icon;
    }

    @Override
    public String getWorldGuardFlagName() {
        return "es-deny-piercer";
    }

    @Override
    public Collection<String> getCompatibleItems() {
        return naturallyCompatibleWith;
    }
}
