package me.athlaeos.enchantssquared.enchantments.on_attack;

import me.athlaeos.enchantssquared.EnchantsSquared;
import me.athlaeos.enchantssquared.config.ConfigManager;
import me.athlaeos.enchantssquared.animations.Animation;
import me.athlaeos.enchantssquared.domain.MaterialClassType;
import me.athlaeos.enchantssquared.enchantments.*;
import me.athlaeos.enchantssquared.managers.AnimationRegistry;
import me.athlaeos.enchantssquared.utility.EntityUtils;
import me.athlaeos.enchantssquared.utility.ItemUtils;
import me.athlaeos.enchantssquared.utility.PotionEffectMappings;
import me.athlaeos.enchantssquared.utility.Utils;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Collection;
import java.util.HashSet;
import java.util.UUID;

public class Stunning extends CustomEnchant implements TriggerOnAttackEnchantment, Listener {

    private final double chanceBase;
    private final double chanceLv;
    private final int durationBase;
    private final int durationLv;
    private final double axeStunMultiplier;
    private final String particleAnimation;
    private final Sound sound;
    private final YamlConfiguration config;
    private final Collection<String> incompatibleVanillaEnchantments;
    private final Collection<String> incompatibleCustomEnchantments;

    private final Collection<UUID> stunnedPlayers;

    public Stunning(int id, String type) {
        super(id, type);
        this.config = ConfigManager.getInstance().getConfig("config.yml").get();
        this.chanceBase = config.getDouble("enchantment_configuration.stunning.apply_chance");
        this.chanceLv = config.getDouble("enchantment_configuration.stunning.apply_chance_lv");
        this.axeStunMultiplier = config.getDouble("enchantment_configuration.stunning.buffed_axe_potency");
        this.durationBase = config.getInt("enchantment_configuration.stunning.duration");
        this.durationLv = config.getInt("enchantment_configuration.stunning.duration_lv");
        this.naturallyCompatibleWith = new HashSet<>(config.getStringList("enchantment_configuration.stunning.compatible_with"));
        this.incompatibleVanillaEnchantments = new HashSet<>(config.getStringList("enchantment_configuration.stunning.incompatible_vanilla_enchantments"));
        this.incompatibleCustomEnchantments = new HashSet<>(config.getStringList("enchantment_configuration.stunning.incompatible_custom_enchantments"));
        this.sound = Utils.soundFromString(config.getString("enchantment_configuration.stunning.sound"), Sound.ENCHANT_THORNS_HIT);
        this.particleAnimation = config.getString("enchantment_configuration.stunning.animation");

        this.stunnedPlayers = new HashSet<>();

        this.icon = ItemUtils.getIconFromConfig(config, "enchantment_configuration.stunning.icon", createIcon(Material.ANVIL));
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
        ItemStack item = null;
        if (realAttacker.getEquipment() != null){
            item = realAttacker.getEquipment().getItemInMainHand();
            if (ItemUtils.isAirOrNull(item)) item = realAttacker.getEquipment().getItemInOffHand();
        }

        double chance = chanceBase + ((level - 1) * chanceLv);
        if (!ItemUtils.isAirOrNull(item) && item.getType().toString().contains("_AXE")) {
            chance *= axeStunMultiplier;
        }
        if (Utils.getRandom().nextDouble() < chance){
            int duration = durationBase + ((level - 1) * durationLv);

            if(victim instanceof Player playerVictim) {
                stunnedPlayers.add(playerVictim.getUniqueId());
                new BukkitRunnable(){
                    public void run() {
                        stunnedPlayers.remove(playerVictim.getUniqueId());
                    }
                }.runTaskLater(EnchantsSquared.getPlugin(), duration);
            }

            EntityUtils.applyPotionEffectIfStronger(victim,
                    new PotionEffect(PotionEffectMappings.SLOWNESS.getPotionEffectType(), duration, 15, true, false, true)
            );
            EntityUtils.applyPotionEffectIfStronger(victim,
                    new PotionEffect(PotionEffectMappings.MINING_FATIGUE.getPotionEffectType(), duration, 15, true, false, true)
            );
            EntityUtils.applyPotionEffectIfStronger(victim,
                    new PotionEffect(PotionEffectMappings.WEAKNESS.getPotionEffectType(), duration, 15, true, false, true)
            );
            EntityUtils.applyPotionEffectIfStronger(victim,
                    new PotionEffect(PotionEffectMappings.BLINDNESS.getPotionEffectType(), duration, 15, true, false, true)
            );
            EntityUtils.applyPotionEffectIfStronger(victim,
                    new PotionEffect(PotionEffectMappings.JUMP_BOOST.getPotionEffectType(), duration, 250, true, false, true)
            );

            if (sound != null) victim.getWorld().playSound(victim.getLocation(), sound, 0.5f, 1f);
            Animation animation = AnimationRegistry.get(particleAnimation);
            if (animation != null) animation.play(victim.getEyeLocation());
        }
    }

    @EventHandler
    public void onMove(PlayerMoveEvent e){
        if (stunnedPlayers.contains(e.getPlayer().getUniqueId())) {
            Location from = e.getFrom();
            Location to = e.getTo();
            to.setX(from.getX());
            to.setZ(from.getZ());
            if(to.getY() > from.getY() ) {to.setY(from.getY());}
            e.setTo(to);
        }
        
    }

    @Override
    public String getDisplayEnchantment() {
        return config.getString("enchantment_configuration.stunning.enchant_name", getType())
                .replace(" %lv_roman%", "")
                .replace(" %lv_number%", "");
    }

    @Override
    public String getDescription() {
        return config.getString("enchantment_configuration.stunning.description");
    }

    @Override
    public boolean isEnabled() {
        return config.getBoolean("enchantment_configuration.stunning.enabled");
    }

    @Override
    public String getRequiredPermission() {
        return "es.enchant.stunning";
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
        return config.getInt("enchantment_configuration.stunning.weight");
    }

    @Override
    public int getMaxLevel() {
        return config.getInt("enchantment_configuration.stunning.max_level");
    }

    @Override
    public int getMaxTableLevel() {
        return config.getInt("enchantment_configuration.stunning.max_level_table");
    }

    @Override
    public boolean isTreasure() {
        return config.getBoolean("enchantment_configuration.stunning.is_treasure");
    }

    @Override
    public boolean isBookOnly() {
        return config.getBoolean("enchantment_configuration.stunning.book_only");
    }

    @Override
    public boolean isTradingEnabled() {
        return config.getBoolean("enchantment_configuration.stunning.trade_enabled");
    }

    @Override
    public int getTradingMinBasePrice() {
        return config.getInt("enchantment_configuration.stunning.trade_cost_base_lower");
    }

    @Override
    public int getTradingMaxBasePrice() {
        return config.getInt("enchantment_configuration.stunning.trade_cost_base_upper");
    }

    @Override
    public int getTradingMinLeveledPrice() {
        return config.getInt("enchantment_configuration.stunning.trade_cost_lv_lower");
    }

    @Override
    public int getTradingMaxLeveledPrice() {
        return config.getInt("enchantment_configuration.stunning.trade_cost_base_upper");
    }

    private final ItemStack icon;
    @Override
    public ItemStack getIcon() {
        return icon;
    }

    @Override
    public String getWorldGuardFlagName() {
        return "es-deny-stunning";
    }

    @Override
    public Collection<String> getCompatibleItems() {
        return naturallyCompatibleWith;
    }
}
