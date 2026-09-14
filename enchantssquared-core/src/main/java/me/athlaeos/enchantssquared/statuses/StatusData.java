package me.athlaeos.enchantssquared.statuses;
public class StatusData {
    private final UUID victim;
    private final UUID attacker;
    private final Long startTime;
    private final Long endTime;
    private final int enchantLevel;

    //those should always be present
    public StatusData(UUID argVictim, UUID argAttacker, Long argStartTime, Long argEndTime, int argEnchantLevel) {
        this.victim = argVictim;
        this.attacker = argAttacker;
        this.startTime = argStartTime;
        this.endTime = argEndTime;
        this.enchantLevel = argEnchantLevel;
    }

    public UUID getVictim() {
        return this.victim;
    }

    public UUID getAttacker() {
        return this.attacker;
    }

    public Long getStartTime() {
        return this.startTime;
    }

    public Long getEndTime() {
        return this.endTime;
    }

    public int getEnchantLevel() {
        return this.enchantLevel;
    }
}