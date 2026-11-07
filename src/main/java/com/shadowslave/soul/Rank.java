package com.shadowslave.soul;

/**
 * The progression ranks of a Sleeper.
 *
 * MORTAL is the default state before a player is ever pulled into the Forgotten Shore.
 * The first transport grants SLEEPER; returning to the waking world grants AWAKENED.
 * Higher ranks are placeholders for future progression and currently use scaling buffs.
 *
 * Each rank carries a multiplier that is applied to BOTH max health and attack damage.
 */
public enum Rank {
    MORTAL("Mortal", 0xFFAAAAAA, 1.00f),
    SLEEPER("Sleeper", 0xFF6E6E9B, 1.20f),
    AWAKENED("Awakened", 0xFF4FA3F7, 1.75f),
    MASTER("Master", 0xFF36C275, 2.50f),
    SAINT("Saint", 0xFFF2C14E, 3.50f),
    SUPREME("Supreme", 0xFFF28C28, 5.00f),
    SACRED("Sacred", 0xFFE5484D, 7.00f),
    DIVINE("Divine", 0xFFB14EF2, 10.00f);

    private final String displayName;
    private final int color;       // ARGB, used by the UI
    private final float multiplier; // applied to health & damage

    Rank(String displayName, int color, float multiplier) {
        this.displayName = displayName;
        this.color = color;
        this.multiplier = multiplier;
    }

    public String getDisplayName() {
        return displayName;
    }

    public int getColor() {
        return color;
    }

    public float getMultiplier() {
        return multiplier;
    }

    public Rank next() {
        Rank[] values = values();
        return this.ordinal() < values.length - 1 ? values[this.ordinal() + 1] : this;
    }

    public static Rank byIndex(int index) {
        Rank[] values = values();
        if (index < 0) index = 0;
        if (index >= values.length) index = values.length - 1;
        return values[index];
    }
}
