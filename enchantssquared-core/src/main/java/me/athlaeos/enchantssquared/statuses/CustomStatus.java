package me.athlaeos.enchantssquared.statuses;

import me.athlaeos.enchantssquared.EnchantsSquared;
import me.athlaeos.enchantssquared.config.ConfigManager;
import me.athlaeos.enchantssquared.domain.ExecutionPriority;
import me.athlaeos.enchantssquared.domain.MinecraftVersion;
import me.athlaeos.enchantssquared.managers.CustomEnchantManager;
import me.athlaeos.enchantssquared.utility.ChatUtils;
import me.athlaeos.enchantssquared.statuses.statusData;

public abstract class CustomStatus {
    protected String type;
    protected int id;
    protected ExecutionPriority priority = ExecutionPriority.NORMAL;

    public Collection<StatusData> affectedPlayers = new HashSet<>();

    public CustomStatus(int id, String type){
        this.id = id;
        this.type = type.toUpperCase();
    }

    public String getType() {
        return type;
    }

    public int getId() {
        return id;
    }

    public ExecutionPriority getPriority() {
        return priority;
    }

    public StatusData addStatus(UUID argVictim, UUID argAttacker, Long tickLength, int argEnchantLevel) {
        Long startTime = CustomStatusManager.getInstance().getTickCount();
        Long endTime = startTime + tickLength;
        StatusData data = new StatusData(argVictim, argAttacker, startTime, endTime, argEnchantLevel);
        this.affectedPlayers.add(data);
        return data;
    }

    public StatusData getStatusData(UUID victim, UUID attacker) {
        for(StatusData entry : affectedPlayers) {
            if(entry.getVictim().equals(victim) && entry.getAttacker().equals(attacker)) return entry;
        }
        return null;
    }

    public StatusData getStatusData(UUID victim) {
        for(StatusData entry : affectedPlayers) {
            if(entry.getVictim().equals(victim)) return entry;
        }
        return null;
    }

    public void removeStatus(StatusData statusData) {
        this.affectedPlayers.remove(statusData);
    }

    public void cleanStatusList() {
        for(StatusData entry : affectedPlayers) {
            if(entry.getEndTime() < CustomStatusManager.getInstance().getTickCount()) removeStatus(entry);
        }
    }
}