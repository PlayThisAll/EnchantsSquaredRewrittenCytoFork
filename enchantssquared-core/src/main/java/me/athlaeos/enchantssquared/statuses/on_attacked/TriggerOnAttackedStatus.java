package me.athlaeos.enchantssquared.statuses.on_attacked;

import me.athlaeos.enchantssquared.domain.EntityClassificationType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import me.athlaeos.enchantssquared.statuses.statusData;

public interface TriggerOnAttackedStatus {
    /**
     * Trigger on an EntityDamageByEntityEvent. Statuses of this type trigger when an entity affected by the
     * statys effect is attacked by another entity.
     * The victim is asserted to be a LivingEntity that's not of {@link EntityClassificationType}.UNALIVE
     * @param e the event
     * @param realAttacker the real attacker in the event, usually representing the shooter of a projectile if they are an entity
     */
    public void onAttacked(EntityDamageByEntityEvent e, LivingEntity realAttacker);
}