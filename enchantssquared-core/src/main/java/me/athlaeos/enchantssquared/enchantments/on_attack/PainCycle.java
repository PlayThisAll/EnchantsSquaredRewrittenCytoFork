package me.athlaeos.enchantssquared.enchantments.on_attack;

import me.athlaeos.enchantssquared.EnchantsSquared;
import me.athlaeos.enchantssquared.config.ConfigManager;
import me.athlaeos.enchantssquared.domain.EntityClassificationType;
import me.athlaeos.enchantssquared.domain.MinecraftVersion;
import me.athlaeos.enchantssquared.enchantments.*;
import me.athlaeos.enchantssquared.enchantments.on_death.TriggerOnDeathEnchantment;
import me.athlaeos.enchantssquared.domain.MaterialClassType;
import me.athlaeos.enchantssquared.listeners.PrePlayerAttackEntityEventListener;
import me.athlaeos.enchantssquared.utility.ItemUtils;
import me.athlaeos.enchantssquared.utility.ChatUtils;

import org.bukkit.attribute.Attribute;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.EntityEffect;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityResurrectEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.UUID;

import org.jetbrains.annotations.Nullable;


public class PainCycle extends CustomEnchant implements TriggerOnAttackEnchantment {

    private final double hp_drain_base;
    private final double hp_drain_lv;
    private final double damage_efficiency;
    private final double safe_threshold;
    private final YamlConfiguration config;
    private final Collection<String> incompatibleVanillaEnchantments;
    private final Collection<String> incompatibleCustomEnchantments;
    private final PrePlayerAttackEntityEventListener cooldownListener;

    private final String bloodStrikeReady;

    public PainCycle(int id, String type) {
        super(id, type);
        this.config = ConfigManager.getInstance().getConfig("config.yml").get();
        this.hp_drain_base = config.getDouble("enchantment_configuration.pain_cycle.hp_drain_base");
        this.hp_drain_lv = config.getDouble("enchantment_configuration.pain_cycle.hp_drain_lv");
        this.damage_efficiency = config.getDouble("enchantment_configuration.pain_cycle.damage_efficiency");
        this.safe_threshold = config.getDouble("enchantment_configuration.pain_cycle.safe_threshold");
        this.naturallyCompatibleWith = new HashSet<>(config.getStringList("enchantment_configuration.pain_cycle.compatible_with"));
        this.incompatibleVanillaEnchantments = new HashSet<>(config.getStringList("enchantment_configuration.pain_cycle.incompatible_vanilla_enchantments"));
        this.incompatibleCustomEnchantments = new HashSet<>(config.getStringList("enchantment_configuration.pain_cycle.incompatible_custom_enchantments"));
        this.cooldownListener = EnchantsSquared.getPlugin().getPrePlayerAttackEntityEventListener();
        
        this.bloodStrikeReady = ConfigManager.getInstance().getConfig("translations.yml").get().getString("blood_strike_ready");

        this.icon = ItemUtils.getIconFromConfig(config, "enchantment_configuration.pain_cycle.icon", createIcon(Material.MELON_SLICE));
    }

    private final LevelService mainHandLevels = new LevelsFromMainHandAndEquipment(this);
    private final LevelService offHandLevels = new LevelsFromOffHandAndEquipment(this);
    @Override
    public LevelService getLevelService(boolean offHand, LivingEntity entity) {
        return offHand ? offHandLevels : mainHandLevels;
    }
    //attacker, health, timesAttacked

    private final Map<UUID, Double> sacrificedHealthMap = new HashMap<>();
    private final Map<UUID, Integer> attackNumMap = new HashMap<>();

    @Override
    public void onAttack(EntityDamageByEntityEvent e, int level, LivingEntity realAttacker) {
        LivingEntity victim = (LivingEntity) e.getEntity();
        if (shouldEnchantmentCancel(level, realAttacker, victim.getLocation())) return;
        if (realAttacker instanceof Player player) {
            UUID playerId = player.getUniqueId();
            if(cooldownListener.getAttackCooldown(playerId) < 1) return;

            if(!sacrificedHealthMap.containsKey(playerId)) sacrificedHealthMap.put(playerId, 0D);
            if(!attackNumMap.containsKey(playerId)) attackNumMap.put(playerId, 0);

            if(attackNumMap.get(playerId) >= 5) {
                attackNumMap.put(playerId, 0);
                double bonusDmg = sacrificedHealthMap.get(playerId) * damage_efficiency;
                if(e.isCritical()) bonusDmg *= 1.5;
                e.setDamage(e.getDamage() + bonusDmg);
                sacrificedHealthMap.put(playerId, 0D);
            }

            if(player.getHealth() < safe_threshold) return;

            double drainPercent = hp_drain_base + (hp_drain_lv * (level - 1));
            double amountDrained = player.getAttribute(Attribute.MAX_HEALTH).getValue() * drainPercent;
            double newHealthDrained;
            if(player.getHealth() <= amountDrained) {
                newHealthDrained = sacrificedHealthMap.get(playerId) + player.getHealth();
                resurrectCheck(player);
            } else {
                player.setHealth(player.getHealth() - amountDrained);
                newHealthDrained = sacrificedHealthMap.get(playerId) + amountDrained;
            }
            int newAttackCount = attackNumMap.get(playerId) + 1;
            sacrificedHealthMap.put(playerId, newHealthDrained);
            attackNumMap.put(playerId, newAttackCount);
            if(newAttackCount >= 5) {
                realAttacker.sendMessage(ChatUtils.chat(bloodStrikeReady));
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        Player player = Bukkit.getPlayer(playerId);
                        if (player == null || !player.isOnline() || attackNumMap.get(playerId) < 5) {
                            cancel();
                            return;
                        }
                        Particle effect = Particle.ANGRY_VILLAGER;
                        Vector offsets = new Vector(0.2, 0.2, 0.2);
                        player.getWorld().spawnParticle(
                            effect,
                            player.getLocation().add(0, 1.5, 0),
                            2,
                            offsets.getX(), offsets.getY(), offsets.getZ()
                        );
                    }
                }.runTaskTimer(EnchantsSquared.getPlugin(), 0L, 20L);
            }
        }
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
        return config.getString("enchantment_configuration.pain_cycle.enchant_name", getType())
                .replace(" %lv_roman%", "")
                .replace(" %lv_number%", "");
    }

    @Override
    public String getDescription() {
        return config.getString("enchantment_configuration.pain_cycle.description");
    }

    @Override
    public boolean isEnabled() {
        return config.getBoolean("enchantment_configuration.pain_cycle.enabled");
    }

    @Override
    public String getRequiredPermission() {
        return "es.enchant.pain_cycle";
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
        return config.getInt("enchantment_configuration.pain_cycle.weight");
    }

    @Override
    public int getMaxLevel() {
        return config.getInt("enchantment_configuration.pain_cycle.max_level");
    }

    @Override
    public int getMaxTableLevel() {
        return config.getInt("enchantment_configuration.pain_cycle.max_level_table");
    }

    @Override
    public boolean isTreasure() {
        return config.getBoolean("enchantment_configuration.pain_cycle.is_treasure");
    }

    @Override
    public boolean isBookOnly() {
        return config.getBoolean("enchantment_configuration.pain_cycle.book_only");
    }

    @Override
    public boolean isTradingEnabled() {
        return config.getBoolean("enchantment_configuration.pain_cycle.trade_enabled");
    }

    @Override
    public int getTradingMinBasePrice() {
        return config.getInt("enchantment_configuration.pain_cycle.trade_cost_base_lower");
    }

    @Override
    public int getTradingMaxBasePrice() {
        return config.getInt("enchantment_configuration.pain_cycle.trade_cost_base_upper");
    }

    @Override
    public int getTradingMinLeveledPrice() {
        return config.getInt("enchantment_configuration.pain_cycle.trade_cost_lv_lower");
    }

    @Override
    public int getTradingMaxLeveledPrice() {
        return config.getInt("enchantment_configuration.pain_cycle.trade_cost_base_upper");
    }

    private final ItemStack icon;
    @Override
    public ItemStack getIcon() {
        return icon;
    }

    @Override
    public String getWorldGuardFlagName() {
        return "es-deny-pain-cycle";
    }

    @Override
    public Collection<String> getCompatibleItems() {
        return naturallyCompatibleWith;
    }
}
