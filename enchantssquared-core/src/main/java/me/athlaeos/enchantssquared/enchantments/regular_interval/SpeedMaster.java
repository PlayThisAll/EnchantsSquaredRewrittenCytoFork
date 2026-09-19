package me.athlaeos.enchantssquared.enchantments.regular_interval;

import me.athlaeos.enchantssquared.AttributeEnchantment;
import me.athlaeos.enchantssquared.config.ConfigManager;
import me.athlaeos.enchantssquared.domain.MaterialClassType;
import me.athlaeos.enchantssquared.enchantments.CustomEnchant;
import me.athlaeos.enchantssquared.enchantments.LevelService;
import me.athlaeos.enchantssquared.enchantments.LevelsFromMainHandAndEquipment;
import me.athlaeos.enchantssquared.enchantments.LevelsFromOffHandAndEquipment;
import me.athlaeos.enchantssquared.utility.EntityUtils;
import me.athlaeos.enchantssquared.utility.ItemUtils;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.EntityDismountEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

import java.util.Collection;
import java.util.HashSet;
import java.util.UUID;

public class SpeedMaster extends CustomEnchant implements TriggerOnRegularIntervalsEnchantment, AttributeEnchantment, Listener {
    private final YamlConfiguration config;
    private final Collection<String> incompatibleVanillaEnchantments;
    private final Collection<String> incompatibleCustomEnchantments;
    /**
     * Constructor for a Custom Enchant. The type and id must be unique and the type will automatically be uppercased
     * by convention.
     * The id will be used on the item to store the enchantment and thus must be consistent, or it will risk
     * changing existing enchantments on item or simply invalidate the enchantment entirely.
     *
     * @param id   the identifying id of this custom enchant.
     * @param type the identifying type of this custom enchant.
     */
    public SpeedMaster(int id, String type) {
        super(id, type);
        this.config = ConfigManager.getInstance().getConfig("config.yml").get();
        this.naturallyCompatibleWith = new HashSet<>(config.getStringList("enchantment_configuration.speed_master.compatible_with"));
        this.incompatibleVanillaEnchantments = new HashSet<>(config.getStringList("enchantment_configuration.speed_master.incompatible_vanilla_enchantments"));
        this.incompatibleCustomEnchantments = new HashSet<>(config.getStringList("enchantment_configuration.speed_master.incompatible_custom_enchantments"));

        this.speedBase = config.getDouble("enchantment_configuration.speed_master.speed_base");
        this.speedLv = config.getDouble("enchantment_configuration.speed_master.speed_lv");

        this.icon = ItemUtils.getIconFromConfig(config, "enchantment_configuration.speed_master.icon", createIcon(Material.DIAMOND_SPEAR));
    }

    private final LevelService mainHandLevels = new LevelsFromMainHandAndEquipment(this);
    private final LevelService offHandLevels = new LevelsFromOffHandAndEquipment(this);
    @Override
    public LevelService getLevelService(boolean offHand, LivingEntity entity) {
        return offHand ? offHandLevels : mainHandLevels;
    }

    @Override
    public String getDisplayEnchantment() {
        return config.getString("enchantment_configuration.speed_master.enchant_name", getType())
                .replace(" %lv_roman%", "")
                .replace(" %lv_number%", "");
    }

    @Override
    public String getDescription() {
        return config.getString("enchantment_configuration.speed_master.description");
    }

    @Override
    public boolean isEnabled() {
        return config.getBoolean("enchantment_configuration.speed_master.enabled");
    }

    @Override
    public String getRequiredPermission() {
        return "es.enchant.speed_master";
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
        return config.getInt("enchantment_configuration.speed_master.weight");
    }

    @Override
    public int getMaxLevel() {
        return config.getInt("enchantment_configuration.speed_master.max_level");
    }

    @Override
    public int getMaxTableLevel() {
        return config.getInt("enchantment_configuration.speed_master.max_level_table");
    }

    @Override
    public boolean isTreasure() {
        return config.getBoolean("enchantment_configuration.speed_master.is_treasure");
    }

    @Override
    public boolean isBookOnly() {
        return config.getBoolean("enchantment_configuration.speed_master.book_only");
    }

    @Override
    public boolean isTradingEnabled() {
        return config.getBoolean("enchantment_configuration.speed_master.trade_enabled");
    }

    @Override
    public int getTradingMinBasePrice() {
        return config.getInt("enchantment_configuration.speed_master.trade_cost_base_lower");
    }

    @Override
    public int getTradingMaxBasePrice() {
        return config.getInt("enchantment_configuration.speed_master.trade_cost_base_upper");
    }

    @Override
    public int getTradingMinLeveledPrice() {
        return config.getInt("enchantment_configuration.speed_master.trade_cost_lv_lower");
    }

    @Override
    public int getTradingMaxLeveledPrice() {
        return config.getInt("enchantment_configuration.speed_master.trade_cost_base_upper");
    }

    private final ItemStack icon;
    @Override
    public ItemStack getIcon() {
        return icon;
    }


    @Override
    public String getWorldGuardFlagName() {
        return "es-deny-speed-master";
    }

    @Override
    public Collection<String> getCompatibleItems() {
        return naturallyCompatibleWith;
    }

    private final double speedBase;
    private final double speedLv;

    @Override
    public long getInterval() {
        return 20;
    }

    @Override
    public void execute(Entity e, int level) {
        if (!(e instanceof LivingEntity)) return;
        if (shouldEnchantmentCancel(level, (LivingEntity) e, e.getLocation())) {
            EntityUtils.removeUniqueAttribute((LivingEntity) e, "es_speed_master", Attribute.MOVEMENT_SPEED);
            if(e.getVehicle() instanceof LivingEntity mount) {
                EntityUtils.removeUniqueAttribute((LivingEntity) mount, "es_speed_master", Attribute.MOVEMENT_SPEED);
            }
            return;
        }

        double speedBoost = speedBase + ((level - 1) * speedLv);

        EntityUtils.addUniqueAttribute((LivingEntity) e, SPEED_MASTER_UUID, "es_speed_master", Attribute.MOVEMENT_SPEED, speedBoost, AttributeModifier.Operation.ADD_SCALAR);
        if(e.getVehicle() instanceof LivingEntity mount) {
            EntityUtils.addUniqueAttribute((LivingEntity) mount, SPEED_MASTER_UUID, "es_speed_master", Attribute.MOVEMENT_SPEED, speedBoost, AttributeModifier.Operation.ADD_SCALAR);
        }
    }

    public static final UUID SPEED_MASTER_UUID = UUID.fromString("a178b8ad-b642-4f2f-9ffd-730ea891467b");

    @Override
    public void cleanAttribute(LivingEntity e) {
        EntityUtils.removeUniqueAttribute(e, "es_speed_master", Attribute.MOVEMENT_SPEED);
        if(e.getVehicle() instanceof LivingEntity mount) {
            EntityUtils.removeUniqueAttribute((LivingEntity) mount, "es_speed_master", Attribute.MOVEMENT_SPEED);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onDismount(EntityDismountEvent event) {
        if(event.getDismounted() instanceof LivingEntity mount) {
            EntityUtils.removeUniqueAttribute((LivingEntity) mount, "es_speed_master", Attribute.MOVEMENT_SPEED);
        }
    }
}