package com.shadowslave.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.shadowslave.ShadowSlave;
import com.shadowslave.network.ModMessages;
import com.shadowslave.soul.Rank;
import com.shadowslave.soul.RankBuffs;
import com.shadowslave.soul.SoulCapability;
import com.shadowslave.soul.SoulData;
import com.shadowslave.world.ForgottenShore;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Arrays;
import java.util.Locale;

/**
 * Quality-of-life playtesting commands, all under {@code /soulcore} (requires permission level 2).
 *
 * Every value-changing sub-command takes an optional trailing [player] target (defaults to the
 * command sender). Changes are immediately synced to the affected player's client/UI.
 */
@Mod.EventBusSubscriber(modid = ShadowSlave.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class ModCommands {

    private static final SuggestionProvider<CommandSourceStack> RANK_SUGGESTIONS = (ctx, builder) ->
            SharedSuggestionProvider.suggest(
                    Arrays.stream(Rank.values()).map(r -> r.name().toLowerCase(Locale.ROOT)), builder);

    private ModCommands() {}

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        register(event.getDispatcher());
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("soulcore")
                .requires(src -> src.hasPermission(2))
                .then(leaf("info", ModCommands::info))
                .then(Commands.literal("essence")
                        .then(doubleArg("add", ModCommands::essenceAdd))
                        .then(doubleArg("set", ModCommands::essenceSet))
                        .then(doubleArg("percent", ModCommands::essencePercent))
                        .then(leaf("full", ModCommands::essenceFull)))
                .then(Commands.literal("aether")
                        .then(doubleArg("set", ModCommands::aetherSet))
                        .then(leaf("full", ModCommands::aetherFull)))
                .then(Commands.literal("multiplier")
                        .then(doubleArg("set", ModCommands::multiplierSet)))
                .then(Commands.literal("rank")
                        .then(Commands.literal("set")
                                .then(Commands.argument("rank", StringArgumentType.word()).suggests(RANK_SUGGESTIONS)
                                        .executes(ModCommands::rankSet)
                                        .then(Commands.argument("player", EntityArgument.player())
                                                .executes(ModCommands::rankSet))))
                        .then(leaf("next", ModCommands::rankNext)))
                .then(Commands.literal("upgrades")
                        .then(intArg("set", ModCommands::upgradesSet)))
                .then(intArg("netherstar", ModCommands::netherStar))
                .then(Commands.literal("drowsy")
                        .then(Commands.argument("value", BoolArgumentType.bool())
                                .executes(ModCommands::drowsySet)
                                .then(Commands.argument("player", EntityArgument.player())
                                        .executes(ModCommands::drowsySet))))
                .then(leaf("shore", ModCommands::shore))
                .then(stringArg("flaw", ModCommands::flawSet))
                .then(stringArg("aspect", ModCommands::aspectSet))
                .then(leaf("reset", ModCommands::reset)));
    }

    /* ------------------------------------------------------------------ */
    /* Node builders                                                       */
    /* ------------------------------------------------------------------ */

    private static LiteralArgumentBuilder<CommandSourceStack> leaf(String name, Command<CommandSourceStack> exec) {
        return Commands.literal(name)
                .executes(exec)
                .then(Commands.argument("player", EntityArgument.player()).executes(exec));
    }

    private static LiteralArgumentBuilder<CommandSourceStack> doubleArg(String name, Command<CommandSourceStack> exec) {
        return Commands.literal(name)
                .then(Commands.argument("amount", DoubleArgumentType.doubleArg())
                        .executes(exec)
                        .then(Commands.argument("player", EntityArgument.player()).executes(exec)));
    }

    private static LiteralArgumentBuilder<CommandSourceStack> intArg(String name, Command<CommandSourceStack> exec) {
        return Commands.literal(name)
                .then(Commands.argument("count", IntegerArgumentType.integer(0))
                        .executes(exec)
                        .then(Commands.argument("player", EntityArgument.player()).executes(exec)));
    }

    private static LiteralArgumentBuilder<CommandSourceStack> stringArg(String name, Command<CommandSourceStack> exec) {
        return Commands.literal(name)
                .then(Commands.argument("text", StringArgumentType.string())
                        .executes(exec)
                        .then(Commands.argument("player", EntityArgument.player()).executes(exec)));
    }

    /* ------------------------------------------------------------------ */
    /* Command bodies                                                      */
    /* ------------------------------------------------------------------ */

    private static int info(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer t = target(ctx);
        t.getCapability(SoulCapability.SOUL).ifPresent(d -> {
            CommandSourceStack s = ctx.getSource();
            s.sendSuccess(() -> Component.literal("§d— Soul Core · " + t.getName().getString() + " —"), false);
            s.sendSuccess(() -> Component.literal(String.format("§7Rank: §f%s §7(×%.2f hp/dmg)",
                    d.getRank().getDisplayName(), d.getRank().getMultiplier())), false);
            s.sendSuccess(() -> Component.literal(String.format("§7Essence: §f%.1f / %.1f §7(%.1f%%)",
                    d.getEssence(), d.getCoreCapacity(), d.getCoreFillPercent())), false);
            s.sendSuccess(() -> Component.literal(String.format("§7Aether: §f%.1f / %.1f",
                    d.getAether(), d.getMaxAether())), false);
            s.sendSuccess(() -> Component.literal(String.format("§7Upgrades: §f%d §7(cap ×%d)",
                    d.getCoreUpgrades(), 1 + d.getCoreUpgrades())), false);
            s.sendSuccess(() -> Component.literal(String.format("§7Essence gain: §f×%.2f", d.getEssenceMultiplier())), false);
            s.sendSuccess(() -> Component.literal(String.format("§7Flaw: §f%s  §7Aspect: §f%s",
                    d.getFlaw(), d.getAspect())), false);
            s.sendSuccess(() -> Component.literal("§7Drowsy: §f" + d.isDrowsy()), false);
        });
        return 1;
    }

    private static int essenceAdd(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer t = target(ctx);
        double amt = DoubleArgumentType.getDouble(ctx, "amount");
        edit(t, d -> d.setEssence(d.getEssence() + amt));
        return reply(ctx, String.format("Added §f%.1f§r essence to §f%s", amt, name(t)));
    }

    private static int essenceSet(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer t = target(ctx);
        double amt = DoubleArgumentType.getDouble(ctx, "amount");
        edit(t, d -> d.setEssence(Math.min(amt, d.getCoreCapacity())));
        return reply(ctx, String.format("Set §f%s§r essence to §f%.1f", name(t), amt));
    }

    private static int essencePercent(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer t = target(ctx);
        double pct = DoubleArgumentType.getDouble(ctx, "amount");
        edit(t, d -> d.addEssencePercent(pct));
        return reply(ctx, String.format("Added §f%.2f%%§r of capacity to §f%s§r's Soul Core", pct, name(t)));
    }

    private static int essenceFull(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer t = target(ctx);
        edit(t, d -> d.setEssence(d.getCoreCapacity()));
        return reply(ctx, String.format("Filled §f%s§r's Soul Core", name(t)));
    }

    private static int aetherSet(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer t = target(ctx);
        double amt = DoubleArgumentType.getDouble(ctx, "amount");
        edit(t, d -> d.setAether(Math.min(amt, d.getMaxAether())));
        return reply(ctx, String.format("Set §f%s§r's Aether to §f%.1f", name(t), amt));
    }

    private static int aetherFull(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer t = target(ctx);
        edit(t, d -> d.setAether(d.getMaxAether()));
        return reply(ctx, String.format("Refilled §f%s§r's Aether", name(t)));
    }

    private static int multiplierSet(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer t = target(ctx);
        double amt = DoubleArgumentType.getDouble(ctx, "amount");
        edit(t, d -> d.setEssenceMultiplier(amt));
        return reply(ctx, String.format("Set §f%s§r's Essence-gain multiplier to §f×%.2f", name(t), amt));
    }

    private static int rankSet(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer t = target(ctx);
        String raw = StringArgumentType.getString(ctx, "rank");
        Rank rank = parseRank(raw);
        if (rank == null) {
            ctx.getSource().sendFailure(Component.literal("§cUnknown rank: " + raw));
            return 0;
        }
        edit(t, d -> d.setRank(rank));
        RankBuffs.apply(t, rank);
        return reply(ctx, String.format("Set §f%s§r's rank to §f%s", name(t), rank.getDisplayName()));
    }

    private static int rankNext(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer t = target(ctx);
        Rank[] holder = new Rank[1];
        edit(t, d -> { holder[0] = d.getRank().next(); d.setRank(holder[0]); });
        RankBuffs.apply(t, holder[0]);
        return reply(ctx, String.format("Advanced §f%s§r to §f%s", name(t), holder[0].getDisplayName()));
    }

    private static int upgradesSet(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer t = target(ctx);
        int n = IntegerArgumentType.getInteger(ctx, "count");
        edit(t, d -> d.setCoreUpgrades(n));
        return reply(ctx, String.format("Set §f%s§r's upgrades to §f%d §7(cap ×%d)", name(t), n, 1 + n));
    }

    private static int netherStar(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer t = target(ctx);
        int n = IntegerArgumentType.getInteger(ctx, "count");
        edit(t, d -> d.setCoreUpgrades(d.getCoreUpgrades() + n));
        return reply(ctx, String.format("Applied §f%d§r Nether Star upgrade(s) to §f%s", n, name(t)));
    }

    private static int drowsySet(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer t = target(ctx);
        boolean v = BoolArgumentType.getBool(ctx, "value");
        edit(t, d -> d.setDrowsy(v));
        return reply(ctx, String.format("Set §f%s§r's drowsy flag to §f%s", name(t), v));
    }

    private static int shore(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer t = target(ctx);
        ForgottenShore.transport(t);
        return reply(ctx, String.format("Dragged §f%s§r across to the Forgotten Shore (or back)", name(t)));
    }

    private static int flawSet(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer t = target(ctx);
        String text = StringArgumentType.getString(ctx, "text");
        edit(t, d -> d.setFlaw(text));
        return reply(ctx, String.format("Set §f%s§r's Flaw to §f%s", name(t), text));
    }

    private static int aspectSet(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer t = target(ctx);
        String text = StringArgumentType.getString(ctx, "text");
        edit(t, d -> d.setAspect(text));
        return reply(ctx, String.format("Set §f%s§r's Aspect to §f%s", name(t), text));
    }

    private static int reset(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer t = target(ctx);
        edit(t, d -> d.copyFrom(new SoulData()));
        RankBuffs.apply(t, Rank.MORTAL);
        return reply(ctx, String.format("Reset §f%s§r's Soul Core to defaults", name(t)));
    }

    /* ------------------------------------------------------------------ */
    /* Helpers                                                             */
    /* ------------------------------------------------------------------ */

    /** Resolve the optional [player] argument, defaulting to the command sender. */
    private static ServerPlayer target(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        try {
            return EntityArgument.getPlayer(ctx, "player");
        } catch (IllegalArgumentException noPlayerArg) {
            return ctx.getSource().getPlayerOrException();
        }
    }

    private static void edit(ServerPlayer player, java.util.function.Consumer<SoulData> action) {
        player.getCapability(SoulCapability.SOUL).ifPresent(data -> {
            action.accept(data);
            ModMessages.syncToClient(player);
        });
    }

    private static String name(ServerPlayer player) {
        return player.getName().getString();
    }

    private static int reply(CommandContext<CommandSourceStack> ctx, String message) {
        ctx.getSource().sendSuccess(() -> Component.literal("§d[Soul Core] §r" + message), true);
        return 1;
    }

    private static Rank parseRank(String raw) {
        for (Rank r : Rank.values()) {
            if (r.name().equalsIgnoreCase(raw) || r.getDisplayName().equalsIgnoreCase(raw)) {
                return r;
            }
        }
        return null;
    }
}
