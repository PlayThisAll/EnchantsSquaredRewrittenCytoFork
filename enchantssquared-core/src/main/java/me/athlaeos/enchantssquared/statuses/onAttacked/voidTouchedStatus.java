package me.athlaeos.enchantssquared.statuses.on_attacked;

public class voidTouchedStatus extends CustomStatus implements TriggerOnAttackedStatus {
    private final YamlConfiguration config;
    private final double damageBase;
    private final double damageLevel;

    public voidTouchedStatus(int id, String type) {
        super(id, type);
        this.config = ConfigManager.getInstance().getConfig("config.yml").get();
    }

    public void onAttacked(EntityDamageByEntityEvent e, LivingEntity realAttacker) {
        LivingEntity victim = (LivingEntity) e.getEntity();
        StatusData data = getStatusData(victim.getUniqueId());
        if(data.getEndTime() < CustomStatusManager.getInstance().getTickCount()) {
            removeStatus(data);
            return;
        }
        double damageMulti = damageBase + ((data.getEnchantLevel() - 1) * damageLevel);
        e.setDamage(e.getDamage() * damageMulti);
    }
}