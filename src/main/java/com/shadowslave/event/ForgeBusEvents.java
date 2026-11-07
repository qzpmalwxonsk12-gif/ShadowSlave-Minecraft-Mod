package com.shadowslave.event;

import com.shadowslave.ShadowSlave;
import com.shadowslave.network.ModMessages;
import com.shadowslave.soul.Rank;
import com.shadowslave.soul.RankBuffs;
import com.shadowslave.soul.SoulCapability;
import com.shadowslave.soul.SoulData;
import com.shadowslave.soul.SoulProvider;
import com.shadowslave.world.ForgottenShore;
import com.shadowslave.world.TideManager;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.warden.Warden;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.entity.player.PlayerWakeUpEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/** Server-side gameplay handlers for the Soul Core system. */
@Mod.EventBusSubscriber(modid = ShadowSlave.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ForgeBusEvents {

    /* ------------------------------------------------------------------ */
    /* Capability attachment                                               */
    /* ------------------------------------------------------------------ */

    @SubscribeEvent
    public static void onAttachCapabilities(AttachCapabilitiesEvent<Entity> event) {
        if (event.getObject() instanceof Player) {
            if (!event.getObject().getCapability(SoulCapability.SOUL).isPresent()) {
                SoulProvider provider = new SoulProvider();
                event.addCapability(new ResourceLocation(ShadowSlave.MODID, "soul"), provider);
                event.addListener(provider::invalidate);
            }
        }
    }

    /** Persist Soul data across death/respawn (and other clone events). */
    @SubscribeEvent
    public static void onClone(PlayerEvent.Clone event) {
        event.getOriginal().reviveCaps();
        event.getOriginal().getCapability(SoulCapability.SOUL).ifPresent(oldData ->
                event.getEntity().getCapability(SoulCapability.SOUL).ifPresent(newData -> newData.copyFrom(oldData)));
        event.getOriginal().invalidateCaps();
    }

    /* ------------------------------------------------------------------ */
    /* Kills feed the Soul Core with Essence                               */
    /* ------------------------------------------------------------------ */

    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {
        if (!(event.getSource().getEntity() instanceof ServerPlayer player)) return;
        LivingEntity victim = event.getEntity();
        if (victim instanceof Player) return; // no Essence from players

        ModMessages.update(player, data -> {
            double percent = essenceFor(victim) * data.getEssenceMultiplier();
            if (data.addEssencePercent(percent)) {
                player.displayClientMessage(
                        Component.literal(String.format("+%.2f%% Soul Core", percent))
                                .withStyle(ChatFormatting.AQUA), true);
            }
        });
    }

    /** Essence reward (as a percentage of Soul Core capacity) for a given victim. */
    private static double essenceFor(LivingEntity victim) {
        if (victim instanceof WitherBoss) return 40.0;
        if (victim instanceof EnderDragon) return 30.0;
        if (victim instanceof Warden) return 20.0;
        if (victim instanceof IronGolem) return 3.0;
        if (victim instanceof Monster) return 0.5;   // hostile
        return 0.25;                                  // peaceful / everything else
    }

    /* ------------------------------------------------------------------ */
    /* Nether Star expands the Soul Core                                   */
    /* ------------------------------------------------------------------ */

    @SubscribeEvent
    public static void onUseNetherStar(PlayerInteractEvent.RightClickItem event) {
        if (event.getLevel().isClientSide()) return;
        if (!(event.getEntity() instanceof ServerPlayer player)) return;

        ItemStack stack = event.getItemStack();
        if (!stack.is(Items.NETHER_STAR)) return;

        stack.shrink(1);
        ModMessages.update(player, SoulData::applyNetherStar);
        player.displayClientMessage(
                Component.literal("Your Soul Core expands — capacity increased ×1.")
                        .withStyle(ChatFormatting.LIGHT_PURPLE), false);
        player.level().playSound(null, player.blockPosition(),
                SoundEvents.BEACON_ACTIVATE, SoundSource.PLAYERS, 1.0F, 1.4F);

        event.setCanceled(true);
        event.setCancellationResult(InteractionResult.SUCCESS);
    }

    /* ------------------------------------------------------------------ */
    /* Aether regenerates over time; periodic client sync                  */
    /* ------------------------------------------------------------------ */

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (!(event.player instanceof ServerPlayer player)) return;

        player.getCapability(SoulCapability.SOUL).ifPresent(data -> {
            boolean changed = data.regenAether();
            if (changed && player.tickCount % 20 == 0) {
                ModMessages.syncToClient(player);
            }
        });
    }

    /* ------------------------------------------------------------------ */
    /* Login / respawn: apply rank buffs and sync                          */
    /* ------------------------------------------------------------------ */

    @SubscribeEvent
    public static void onLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        player.getCapability(SoulCapability.SOUL).ifPresent(data -> RankBuffs.apply(player, data.getRank()));
        ModMessages.syncToClient(player);
    }

    @SubscribeEvent
    public static void onRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        player.getCapability(SoulCapability.SOUL).ifPresent(data -> RankBuffs.apply(player, data.getRank()));
        ModMessages.syncToClient(player);
    }

    /* ------------------------------------------------------------------ */
    /* Sleep: 10% drowsy roll, then transport on the next sleep            */
    /* ------------------------------------------------------------------ */

    @SubscribeEvent
    public static void onWakeUp(PlayerWakeUpEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;

        player.getCapability(SoulCapability.SOUL).ifPresent(data -> {
            if (data.isDrowsy()) {
                data.setDrowsy(false);
                ModMessages.syncToClient(player);
                player.displayClientMessage(
                        Component.literal("The world dissolves into cold grey mist...")
                                .withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.ITALIC), false);
                // Defer the dimension change one tick so it happens cleanly after waking.
                player.getServer().execute(() -> ForgottenShore.transport(player));
            } else if (player.getRandom().nextFloat() < 0.10F) {
                data.setDrowsy(true);
                ModMessages.syncToClient(player);
                player.displayClientMessage(
                        Component.literal("You wake feeling extremely drowsy. Sleep again, and the Nightmare may claim you...")
                                .withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC), false);
            }
        });
    }

    /* ------------------------------------------------------------------ */
    /* Dimension change grants Sleeper / Awakened ranks                    */
    /* ------------------------------------------------------------------ */

    @SubscribeEvent
    public static void onChangeDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;

        ResourceKey<Level> from = event.getFrom();
        ResourceKey<Level> to = event.getTo();

        player.getCapability(SoulCapability.SOUL).ifPresent(data -> {
            boolean changed = false;

            if (to.equals(ForgottenShore.FORGOTTEN_SHORE) && data.getRank() == Rank.MORTAL) {
                data.setRank(Rank.SLEEPER);
                player.displayClientMessage(
                        Component.literal("You wash ashore upon the Forgotten Shore. You are now a SLEEPER.")
                                .withStyle(ChatFormatting.LIGHT_PURPLE), false);
                changed = true;
            } else if (to.equals(Level.OVERWORLD) && from.equals(ForgottenShore.FORGOTTEN_SHORE)
                    && data.getRank() == Rank.SLEEPER) {
                data.setRank(Rank.AWAKENED);
                player.displayClientMessage(
                        Component.literal("You claw your way back to the waking world. You are now AWAKENED.")
                                .withStyle(ChatFormatting.AQUA), false);
                changed = true;
            }

            if (changed) {
                RankBuffs.apply(player, data.getRank());
                ModMessages.syncToClient(player);
            }
        });

        // Every arrival on the Shore wakes amid a cluster of petrified Sleepers.
        if (to.equals(ForgottenShore.FORGOTTEN_SHORE)) {
            ForgottenShore.buildAwakeningStatue(player);
        }
    }

    /* ------------------------------------------------------------------ */
    /* The Tide — drives the Forgotten Shore's rising/receding sea         */
    /* ------------------------------------------------------------------ */

    @SubscribeEvent
    public static void onLevelTick(TickEvent.LevelTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (!(event.level instanceof ServerLevel serverLevel)) return;
        if (!serverLevel.dimension().equals(ForgottenShore.FORGOTTEN_SHORE)) return;
        TideManager.tick(serverLevel);
    }
}
