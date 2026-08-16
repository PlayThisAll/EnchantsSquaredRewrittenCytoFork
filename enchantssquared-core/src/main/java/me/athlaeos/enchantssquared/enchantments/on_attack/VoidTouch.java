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
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.UUID;

import org.jetbrains.annotations.Nullable;


public class VoidTouch extends CustomEnchant implements TriggerOnAttackEnchantment {

    private final double damage_base;
    private final double damage_lv;
    private final double time_base;
    private final double time_lv;
    private final double ranged_damage_penalty;
    private final YamlConfiguration config;
    private final Collection<String> incompatibleVanillaEnchantments;
    private final Collection<String> incompatibleCustomEnchantments;

    public VoidTouch(int id, String type) {
        super(id, type);
        this.config = ConfigManager.getInstance().getConfig("config.yml").get();
        this.damage_base = config.getDouble("enchantment_configuration.void_touch.damage_base");
        this.damage_lv = config.getDouble("enchantment_configuration.void_touch.damage_lv");
        this.time_base = config.getDouble("enchantment_configuration.void_touch.time_base");
        this.time_lv = config.getDouble("enchantment_configuration.void_touch.time_lv");
        this.ranged_damage_penalty = config.getDouble("enchantment_configuration.void_touch.ranged_damage_penalty");
        this.naturallyCompatibleWith = new HashSet<>(config.getStringList("enchantment_configuration.void_touch.compatible_with"));
        this.incompatibleVanillaEnchantments = new HashSet<>(config.getStringList("enchantment_configuration.void_touch.incompatible_vanilla_enchantments"));
        this.incompatibleCustomEnchantments = new HashSet<>(config.getStringList("enchantment_configuration.void_touch.incompatible_custom_enchantments"));

        this.icon = ItemUtils.getIconFromConfig(config, "enchantment_configuration.void_touch.icon", createIcon(Material.CRYING_OBSIDIAN));

        //handles clean up and particle spawning, which only the attacker can see
        new BukkitRunnable(){
            public void run() {
                Particle effect = Particle.FALLING_OBSIDIAN_TEAR;
                Vector offsets = new Vector(0.2, 0.75, 0.2);
                voidTouchedSet.forEach((attacker, victimSet) -> {
                    //entry -> victim UUID, time
                    victimSet.entrySet().removeIf(entry -> {
                        Entity victimEntity = Bukkit.getEntity(entry.getKey());
                        Player attackerEntity = Bukkit.getPlayer(attacker);
                        if (entry.getValue() < getTickTime()) return true;
                        if (victimEntity == null) return true;
                        if (attackerEntity != null) attackerEntity.spawnParticle(effect, victimEntity.getLocation().add(0, 1.25, 0), 10, offsets.getX(), offsets.getY(), offsets.getZ());
                        if (victimEntity instanceof Player victimPlayer) {
                            victimPlayer.spawnParticle(effect, victimPlayer.getLocation().add(0, 1.25, 0), 10, offsets.getX(), offsets.getY(), offsets.getZ());
                        }
                        return false;
                    });
                });
            }
        }.runTaskTimer(EnchantsSquared.getPlugin(), 10L, 10L);
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
        Map<UUID, Long> playerEntry = voidTouchedSet.get(realAttacker.getUniqueId());
        double maxLength = time_base + ((level - 1) * time_lv);
        if (playerEntry != null) {
            
            Long logTime = playerEntry.get(victim.getUniqueId());
            long currentTime = getTickTime();
            if((logTime != null) && (logTime > currentTime)){
                double ratio = (currentTime - logTime + maxLength) / maxLength;
                double multiplier = (damage_base + ((level - 1) * damage_lv)) * ratio;
                if(e.getDamager() instanceof Projectile) multiplier *= (1 - ranged_damage_penalty);
                e.setDamage(e.getDamage() * (1 + multiplier));
                return;
            }
            playerEntry.put(victim.getUniqueId(), getTickTime() + Math.round(maxLength));
            return;
        }
        playerEntry = new HashMap<>();
        playerEntry.put(victim.getUniqueId(), getTickTime() + Math.round(maxLength));
        voidTouchedSet.put(realAttacker.getUniqueId(), playerEntry);

        /*plan:
        check if the attack UUID exists in voidTouchedSet
        +-check if the victim is present on the list
        +-check if it's still under timer
        if all are true, calculate damage
        if any is false, add new entry and remove previous if applicable
        would be best if I could get some particles per second :3
        */

        /*
        LivingEntity victim = (LivingEntity) e.getEntity();
        if (ignoreArrows.contains(e.getDamager().getUniqueId()) || shouldEnchantmentCancel(level, realAttacker, victim.getLocation())
        || e.getDamager() instanceof LivingEntity) return;

        double finalRadius = this.radius_base + ((level - 1) * radius_lv);
        double finalDamage = this.aoe_damage_base + ((level - 1) * aoe_damage_lv);
        double damage = e.getDamage();
        if (explosion){
            realAttacker.getWorld().spawnParticle(Particle.valueOf(MinecraftVersion.currentVersionNewerThan(MinecraftVersion.MINECRAFT_1_20_5) ? "EXPLOSION" : "EXPLOSION_NORMAL"), victim.getLocation(), 0);
            victim.getWorld().playSound(victim.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 1F, 1F);
        }
        Collection<Entity> surroundingEntities = victim.getWorld().getNearbyEntities(victim.getLocation(), finalRadius, finalRadius, finalRadius);
        surroundingEntities.remove(victim);
        surroundingEntities.remove(e.getDamager());
        surroundingEntities.remove(realAttacker);
        ignoreArrows.add(e.getDamager().getUniqueId());
        for (Entity entity : surroundingEntities){
            if (entity instanceof LivingEntity && !EntityClassificationType.isMatchingClassification(entity.getType(), EntityClassificationType.UNALIVE)){
                ((LivingEntity) entity).damage(damage * finalDamage, e.getDamager());
            }
        }
        ignoreArrows.remove(e.getDamager().getUniqueId());
        */
    }

    private long getTickTime() {
        return getTickTime(null);
    }

    private long getTickTime(@Nullable Long time) {
        if(time == null) time = System.currentTimeMillis();
        return Math.round(time / 50D);
    } 
    @Override
    public String getDisplayEnchantment() {
        return config.getString("enchantment_configuration.void_touch.enchant_name", getType())
                .replace(" %lv_roman%", "")
                .replace(" %lv_number%", "");
    }

    @Override
    public String getDescription() {
        return config.getString("enchantment_configuration.void_touch.description");
    }

    @Override
    public boolean isEnabled() {
        return config.getBoolean("enchantment_configuration.void_touch.enabled");
    }

    @Override
    public String getRequiredPermission() {
        return "es.enchant.void_touch";
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
        return config.getInt("enchantment_configuration.void_touch.weight");
    }

    @Override
    public int getMaxLevel() {
        return config.getInt("enchantment_configuration.void_touch.max_level");
    }

    @Override
    public int getMaxTableLevel() {
        return config.getInt("enchantment_configuration.void_touch.max_level_table");
    }

    @Override
    public boolean isTreasure() {
        return config.getBoolean("enchantment_configuration.void_touch.is_treasure");
    }

    @Override
    public boolean isBookOnly() {
        return config.getBoolean("enchantment_configuration.void_touch.book_only");
    }

    @Override
    public boolean isTradingEnabled() {
        return config.getBoolean("enchantment_configuration.void_touch.trade_enabled");
    }

    @Override
    public int getTradingMinBasePrice() {
        return config.getInt("enchantment_configuration.void_touch.trade_cost_base_lower");
    }

    @Override
    public int getTradingMaxBasePrice() {
        return config.getInt("enchantment_configuration.void_touch.trade_cost_base_upper");
    }

    @Override
    public int getTradingMinLeveledPrice() {
        return config.getInt("enchantment_configuration.void_touch.trade_cost_lv_lower");
    }

    @Override
    public int getTradingMaxLeveledPrice() {
        return config.getInt("enchantment_configuration.void_touch.trade_cost_base_upper");
    }

    private final ItemStack icon;
    @Override
    public ItemStack getIcon() {
        return icon;
    }

    @Override
    public String getWorldGuardFlagName() {
        return "es-deny-aoe-arrows";
    }

    @Override
    public Collection<String> getCompatibleItems() {
        return naturallyCompatibleWith;
    }
}
