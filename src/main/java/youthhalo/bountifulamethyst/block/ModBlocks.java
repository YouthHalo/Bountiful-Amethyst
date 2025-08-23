package youthhalo.bountifulamethyst.block;

import java.util.function.Function;

import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import youthhalo.bountifulamethyst.BountifulAmethyst;
import youthhalo.bountifulamethyst.item.ModItems;

public class ModBlocks {

	public static final Block ENHANCED_ENCHANTING_TABLE = register("enhanced_enchanting_table",
			settings -> new Block(settings) {
				@Override
				public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
					return VoxelShapes.cuboid(0.0, 0.0, 0.0, 1.0, 0.75, 1.0);
				}
				
				@Override
				public VoxelShape getCollisionShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
					return VoxelShapes.cuboid(0.0, 0.0, 0.0, 1.0, 0.75, 1.0);
				}
			},
			AbstractBlock.Settings.create().sounds(BlockSoundGroup.STONE), true);

	public static void initialize() {
		BountifulAmethyst.LOGGER.info("Registering Mod Blocks for " + BountifulAmethyst.MOD_ID);

		// Add blocks to the custom item group
		ItemGroupEvents.modifyEntriesEvent(ModItems.BOUNTIFUL_AMETHYST_GROUP_KEY).register(entries -> {
			entries.add(ENHANCED_ENCHANTING_TABLE);

		});
	}

	private static Block register(String name, Function<AbstractBlock.Settings, Block> blockFactory,
			AbstractBlock.Settings settings, boolean shouldRegisterItem) {
		// Create a registry key for the block
		RegistryKey<Block> blockKey = keyOfBlock(name);
		// Create the block instance
		Block block = blockFactory.apply(settings.registryKey(blockKey));

		// Sometimes, you may not want to register an item for the block.
		// Eg: if it's a technical block like `minecraft:moving_piston` or
		// `minecraft:end_gateway`
		if (shouldRegisterItem) {
			// Items need to be registered with a different type of registry key, but the ID
			// can be the same.
			RegistryKey<Item> itemKey = keyOfItem(name);

			BlockItem blockItem = new BlockItem(block, new Item.Settings().registryKey(itemKey));
			Registry.register(Registries.ITEM, itemKey, blockItem);
		}

		return Registry.register(Registries.BLOCK, blockKey, block);
	}

	private static RegistryKey<Block> keyOfBlock(String name) {
		return RegistryKey.of(RegistryKeys.BLOCK, Identifier.of(BountifulAmethyst.MOD_ID, name));
	}

	private static RegistryKey<Item> keyOfItem(String name) {
		return RegistryKey.of(RegistryKeys.ITEM, Identifier.of(BountifulAmethyst.MOD_ID, name));
	}

}
