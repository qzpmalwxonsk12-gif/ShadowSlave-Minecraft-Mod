package com.shadowslave.soul;

import net.minecraft.nbt.CompoundTag;

/**
 * Per-player Soul Core state.
 *
 * Two distinct energies, as described in the design:
 *  - {@code aether}  : regenerates naturally over time, spent on skills. Current value tracked here;
 *                      the maximum scales with Soul Core upgrades.
 *  - {@code essence} : harvested by killing creatures, accumulates inside the Soul Core and is used to
 *                      advance the quantity (capacity) of your energies. Tracked as an absolute amount;
 *                      the UI shows it as a fill percentage of the core's capacity.
 *
 * Nether Stars increase {@code coreUpgrades}; each one adds 1x to both the Soul Core capacity and the
 * Aether capacity (i.e. capacity = base * (1 + coreUpgrades)).
 */
public class SoulData {

    // Base capacities at zero upgrades.
    public static final double BASE_CORE_CAPACITY = 100.0;
    public static final double BASE_AETHER_CAPACITY = 100.0;

    // Aether regenerated per server tick.
    public static final double AETHER_REGEN_PER_TICK = 0.25;

    private double essence = 0.0;     // absolute essence currently stored in the Soul Core
    private double aether = BASE_AETHER_CAPACITY; // current skill energy, starts full
    private int coreUpgrades = 0;     // number of Nether Stars consumed
    private int rankIndex = 0;        // index into Rank
    private double essenceMultiplier = 1.0; // multiplies Essence gained from kills (playtest/QoL)

    // Flaw / Aspect — placeholders for now, surfaced in the UI.
    private String flaw = "Locked";
    private String aspect = "Dormant";

    // True after a 10% "drowsy" roll on waking; the next sleep transports the player.
    private boolean drowsy = false;

    /* ------------------------------------------------------------------ */
    /* Derived capacities                                                  */
    /* ------------------------------------------------------------------ */

    public double getCoreCapacity() {
        return BASE_CORE_CAPACITY * (1 + coreUpgrades);
    }

    public double getMaxAether() {
        return BASE_AETHER_CAPACITY * (1 + coreUpgrades);
    }

    /** Soul Core fill as a percentage (0–100) of its current capacity. */
    public double getCoreFillPercent() {
        double cap = getCoreCapacity();
        return cap <= 0 ? 0 : (essence / cap) * 100.0;
    }

    /** Aether fill fraction (0–1) of its current capacity. */
    public double getAetherFraction() {
        double max = getMaxAether();
        return max <= 0 ? 0 : Math.min(1.0, aether / max);
    }

    /* ------------------------------------------------------------------ */
    /* Mutators                                                            */
    /* ------------------------------------------------------------------ */

    /**
     * Add essence as a percentage of the current core capacity (e.g. a hostile mob adds 0.5%).
     * @return true if the core changed (i.e. it was not already full).
     */
    public boolean addEssencePercent(double percent) {
        double cap = getCoreCapacity();
        if (essence >= cap) return false;
        essence = Math.min(cap, essence + cap * (percent / 100.0));
        return true;
    }

    /** Regenerate aether by one tick's worth. @return true if the value changed. */
    public boolean regenAether() {
        double max = getMaxAether();
        if (aether >= max) return false;
        aether = Math.min(max, aether + AETHER_REGEN_PER_TICK);
        return true;
    }

    /** Consume one Nether Star: +1x to both Soul Core and Aether capacity. */
    public void applyNetherStar() {
        coreUpgrades++;
        // Current values are preserved (absolute), so the fill percentage naturally drops
        // to reflect the larger core. Aether keeps its current value under the higher cap.
    }

    /* ------------------------------------------------------------------ */
    /* Simple getters / setters                                            */
    /* ------------------------------------------------------------------ */

    public double getEssence() { return essence; }
    public void setEssence(double v) { this.essence = Math.max(0, v); }

    public double getAether() { return aether; }
    public void setAether(double v) { this.aether = Math.max(0, v); }

    public int getCoreUpgrades() { return coreUpgrades; }
    public void setCoreUpgrades(int v) { this.coreUpgrades = Math.max(0, v); }

    public double getEssenceMultiplier() { return essenceMultiplier; }
    public void setEssenceMultiplier(double v) { this.essenceMultiplier = Math.max(0, v); }

    public Rank getRank() { return Rank.byIndex(rankIndex); }
    public void setRank(Rank rank) { this.rankIndex = rank.ordinal(); }
    public int getRankIndex() { return rankIndex; }

    public String getFlaw() { return flaw; }
    public void setFlaw(String flaw) { this.flaw = flaw; }

    public String getAspect() { return aspect; }
    public void setAspect(String aspect) { this.aspect = aspect; }

    public boolean isDrowsy() { return drowsy; }
    public void setDrowsy(boolean drowsy) { this.drowsy = drowsy; }

    /* ------------------------------------------------------------------ */
    /* Persistence / sync                                                  */
    /* ------------------------------------------------------------------ */

    public void copyFrom(SoulData other) {
        this.essence = other.essence;
        this.aether = other.aether;
        this.coreUpgrades = other.coreUpgrades;
        this.rankIndex = other.rankIndex;
        this.essenceMultiplier = other.essenceMultiplier;
        this.flaw = other.flaw;
        this.aspect = other.aspect;
        this.drowsy = other.drowsy;
    }

    public CompoundTag toNBT() {
        CompoundTag tag = new CompoundTag();
        tag.putDouble("essence", essence);
        tag.putDouble("aether", aether);
        tag.putInt("coreUpgrades", coreUpgrades);
        tag.putInt("rankIndex", rankIndex);
        tag.putDouble("essenceMultiplier", essenceMultiplier);
        tag.putString("flaw", flaw);
        tag.putString("aspect", aspect);
        tag.putBoolean("drowsy", drowsy);
        return tag;
    }

    public void fromNBT(CompoundTag tag) {
        essence = tag.getDouble("essence");
        aether = tag.getDouble("aether");
        coreUpgrades = tag.getInt("coreUpgrades");
        rankIndex = tag.getInt("rankIndex");
        essenceMultiplier = tag.contains("essenceMultiplier") ? tag.getDouble("essenceMultiplier") : 1.0;
        flaw = tag.contains("flaw") ? tag.getString("flaw") : "Locked";
        aspect = tag.contains("aspect") ? tag.getString("aspect") : "Dormant";
        drowsy = tag.getBoolean("drowsy");
    }
}
