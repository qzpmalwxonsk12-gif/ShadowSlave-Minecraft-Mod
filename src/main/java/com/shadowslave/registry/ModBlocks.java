package com.shadowslave.registry;

import com.shadowslave.ShadowSlave;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.function.Supplier;

/** Custom blocks of the Forgotten Shore. */
public class ModBlocks {

    public static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(ForgeRegistries.BLOCKS, ShadowSlave.MODID);
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, ShadowSlave.MODID);

    /** Grey, ashen shore sand. */
    public static final RegistryObject<Block> ASHEN_SAND = register("ashen_sand",
            () -> new Block(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.STONE).strength(0.6F).sound(SoundType.GRAVEL)));

    /** A petrified Sleeper — the stone the dreamers wake from, and most never leave. */
    public static final RegistryObject<Block> PETRIFIED_SLEEPER = register("petrified_sleeper",
            () -> new Block(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.STONE).strength(1.6F, 6.0F)
                    .requiresCorrectToolForDrops().sound(SoundType.STONE)));

    /** Dark crimson stone that forms the looming Spires. Emits a faint glow. */
    public static final RegistryObject<Block> CRIMSON_SPIRE_BLOCK = register("crimson_spire_block",
            () -> new Block(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.NETHER).strength(2.0F, 8.0F)
                    .requiresCorrectToolForDrops().lightLevel(s -> 4).sound(SoundType.STONE)));

    /** Living crimson coral that tangles the sea floor into a labyrinth. */
    public static final RegistryObject<Block> CRIMSON_CORAL = register("crimson_coral",
            () -> new Block(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_RED).strength(1.0F).sound(SoundType.CORAL_BLOCK)));

    private static <T extends Block> RegistryObject<T> register(String name, Supplier<T> supplier) {
        RegistryObject<T> block = BLOCKS.register(name, supplier);
        ITEMS.register(name, () -> new BlockItem(block.get(), new Item.Properties()));
        return block;
    }

    private ModBlocks() {}
}
