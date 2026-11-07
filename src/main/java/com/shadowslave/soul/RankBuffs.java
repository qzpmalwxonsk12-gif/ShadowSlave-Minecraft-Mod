package com.shadowslave.soul;

import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;

import java.util.UUID;

/** Applies a rank's multiplier to the player's max health and attack damage. */
public final class RankBuffs {

    private static final UUID HP_UUID = UUID.fromString("a1f3c9e2-0b6d-4a7c-9e21-2f3a1b4c5d6e");
    private static final UUID DMG_UUID = UUID.fromString("b2e4d8f3-1c7e-4b8d-8f32-3a4b2c5d6e7f");

    private RankBuffs() {}

    public static void apply(Player player, Rank rank) {
        applyOne(player, Attributes.MAX_HEALTH, HP_UUID, "shadowslave_rank_hp", rank);
        applyOne(player, Attributes.ATTACK_DAMAGE, DMG_UUID, "shadowslave_rank_dmg", rank);
    }

    private static void applyOne(Player player, Attribute attribute, UUID uuid, String name, Rank rank) {
        AttributeInstance instance = player.getAttribute(attribute);
        if (instance == null) return;

        instance.removeModifier(uuid); // clear any previous rank modifier
        double amount = rank.getMultiplier() - 1.0;
        if (amount > 0) {
            instance.addPermanentModifier(
                    new AttributeModifier(uuid, name, amount, AttributeModifier.Operation.MULTIPLY_TOTAL));
        }
    }
}
