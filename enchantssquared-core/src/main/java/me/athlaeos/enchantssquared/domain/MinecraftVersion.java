package me.athlaeos.enchantssquared.domain;

import me.athlaeos.enchantssquared.EnchantsSquared;

public enum MinecraftVersion {
    MINECRAFT_26_3_4(17.4F, "26_3_4", "26.3.4", "v1_21_R3"),
    MINECRAFT_26_3_3(17.3F, "26_3_3", "26.3.3", "v1_21_R3"),
    MINECRAFT_26_3_2(17.2F, "26_3_2", "26.3.2", "v1_21_R3"),
    MINECRAFT_26_3_1(17.1F, "26_3_1", "26.3.1", "v1_21_R3"),
    MINECRAFT_26_3(17.0F, "26_3", "26.3", "v1_21_R3"),
    MINECRAFT_26_2_12(16.93F, "26_2_12", "26.2.12", "v1_21_R3"),
    MINECRAFT_26_2_11(16.92F, "26_2_11", "26.2.11", "v1_21_R3"),
    MINECRAFT_26_2_10(16.91F, "26_2_10", "26.2.10", "v1_21_R3"),
    MINECRAFT_26_2_9(16.9F, "26_2_9", "26.2.9", "v1_21_R3"),
    MINECRAFT_26_2_8(16.8F, "26_2_8", "26.2.8", "v1_21_R3"),
    MINECRAFT_26_2_7(16.7F, "26_2_7", "26.2.7", "v1_21_R3"),
    MINECRAFT_26_2_6(16.6F, "26_2_6", "26.2.6", "v1_21_R3"),
    MINECRAFT_26_2_5(16.5F, "26_2_5", "26.2.5", "v1_21_R3"),
    MINECRAFT_26_2_4(16.4F, "26_2_4", "26.2.4", "v1_21_R3"),
    MINECRAFT_26_2_3(16.3F, "26_2_3", "26.2.3", "v1_21_R3"),
    MINECRAFT_26_2_2(16.2F, "26_2_2", "26.2.2", "v1_21_R3"),
    MINECRAFT_26_2_1(16.1F, "26_2_1", "26.2.1", "v1_21_R3"),
    MINECRAFT_26_2(16.0F, "26_2", "26.2", "v1_21_R3"),
    MINECRAFT_26_1_12(15.93F, "26_1_12", "26.1.12", "v1_21_R3"),
    MINECRAFT_26_1_11(15.92F, "26_1_11", "26.1.11", "v1_21_R3"),
    MINECRAFT_26_1_10(15.91F, "26_1_10", "26.1.10", "v1_21_R3"),
    MINECRAFT_26_1_9(15.9F, "26_1_9", "26.1.9", "v1_21_R3"),
    MINECRAFT_26_1_8(15.8F, "26_1_8", "26.1.8", "v1_21_R3"),
    MINECRAFT_26_1_7(15.7F, "26_1_7", "26.1.7", "v1_21_R3"),
    MINECRAFT_26_1_6(15.6F, "26_1_6", "26.1.6", "v1_21_R3"),
    MINECRAFT_26_1_5(15.5F, "26_1_5", "26.1.5", "v1_21_R3"),
    MINECRAFT_26_1_4(15.4F, "26_1_4", "26.1.4", "v1_21_R3"),
    MINECRAFT_26_1_3(15.3F, "26_1_3", "26.1.3", "v1_21_R3"),
    MINECRAFT_26_1_2(15.2F, "26_1_2", "26.1.2", "v1_21_R3"),
    MINECRAFT_26_1_1(15.1F, "26_1_1", "26.1.1", "v1_21_R3"),
    MINECRAFT_26_1(15.0F, "26_1", "26.1", "v1_21_R3"),
    MINECRAFT_1_21_11(14.92F, "1_21_11", "1.21.11", "v1_21_R3"),
    MINECRAFT_1_21_10(14.91F, "1_21_10", "1.21.10", "v1_21_R3"),
    MINECRAFT_1_21_9(14.9F, "1_21_9", "1.21.9", "v1_21_R3"),
    MINECRAFT_1_21_8(14.8F, "1_21_8", "1.21.8", "v1_21_R3"),
    MINECRAFT_1_21_7(14.7F, "1_21_7", "1.21.7", "v1_21_R3"),
    MINECRAFT_1_21_6(14.6F, "1_21_6", "1.21.6", "v1_21_R3"),
    MINECRAFT_1_21_5(14.5F, "1_21_5", "1.21.5", "v1_21_R3"),
    MINECRAFT_1_21_4(14.4F, "1_21_4", "1.21.4", "v1_21_R3"),
    MINECRAFT_1_21_3(14.3F, "1_21_3", "1.21.3", "v1_21_R2"),
    MINECRAFT_1_21_2(14.2F, "1_21_2", "1.21.2", "v1_21_R2"),
    MINECRAFT_1_21_1(14.1F, "1_21_1", "1.21.1", "v1_21_R1"),
    MINECRAFT_1_21(14, "1_21", "1.21", "v1_21_R1"),
    MINECRAFT_1_20_6(13.6F, "1_20_6", "1.20.6", "default"),
    MINECRAFT_1_20_5(13.5F, "1_20_5", "1.20.5", "default"),
    MINECRAFT_1_20_4(13.4F, "1_20_4", "1.20.4", "default"),
    MINECRAFT_1_20_3(13.3F, "1_20_3", "1.20.3", "default"),
    MINECRAFT_1_20_2(13.2F, "1_20_2", "1.20.2", "default"),
    MINECRAFT_1_20_1(13.1F, "1_20_1", "1.20.1", "default"),
    MINECRAFT_1_20(13, "1_20", "1.20", "default"),
    MINECRAFT_1_19_4(12.4F, "1_19_4", "1.19.4", "default"),
    MINECRAFT_1_19_3(12.3F, "1_19_3", "1.19.3", "default"),
    MINECRAFT_1_19_2(12.2F, "1_19_2", "1.19.2", "default"),
    MINECRAFT_1_19_1(12.1F, "1_19_1", "1.19.1", "default"),
    MINECRAFT_1_19(12, "1_19", "1.19", "default"),
    MINECRAFT_1_18(11, "1_18", "1.18", "default"),
    MINECRAFT_1_17(10, "1_17", "1.17", "default"),
    MINECRAFT_1_16(9, "1_16", "1.16", "default"),
    MINECRAFT_1_15(8, "1_15", "1.15", "default"),
    MINECRAFT_1_14(7, "1_14", "1.14", "default"),
    MINECRAFT_1_13(6, "1_13", "1.13", "default"),
    MINECRAFT_1_12(5, "1_12", "1.12", "default"),
    MINECRAFT_1_11(4, "1_11", "1.11", "default"),
    MINECRAFT_1_10(3, "1_10", "1.10", "default"),
    MINECRAFT_1_9(2, "1_9", "1.9", "default"),
    MINECRAFT_1_8(1, "1_8", "1.8", "default"),
    INCOMPATIBLE(-1, null, null, null);
    private final float v;
    private final String versionString1;
    private final String versionString2;
    private final String nmsVersion;
    private static final MinecraftVersion serverVersion = init();

    private static MinecraftVersion init(){
        String v = EnchantsSquared.getPlugin().getServer().getVersion();
        for (MinecraftVersion version : MinecraftVersion.values()){
            if (version.versionString1 == null || version.versionString2 == null) continue;
            if (v.contains(version.versionString1) || v.contains(version.versionString2)) return version;
        }
        return INCOMPATIBLE;
    }

    MinecraftVersion(float v, String v1, String v2, String nmsVersion){
        this.v = v;
        this.versionString1 = v1;
        this.versionString2 = v2;
        this.nmsVersion = nmsVersion;
    }

    public String getVersionString() {
        return versionString1;
    }

    /**
     * @return True if the version is the same or older than the version given
     */
    public static boolean currentVersionOlderThan(MinecraftVersion version){
        if (serverVersion == MinecraftVersion.INCOMPATIBLE) return false;
        return serverVersion.v <= version.v;
    }

    public String getNmsVersion() {
        return nmsVersion;
    }

    /**
     * @return True if the version is the same or newer than the version given
     */
    public static boolean currentVersionNewerThan(MinecraftVersion version){
        if (serverVersion == MinecraftVersion.INCOMPATIBLE) return false;
        return serverVersion.v >= version.v;
    }

    public static MinecraftVersion getServerVersion() {
        return serverVersion;
    }
}
