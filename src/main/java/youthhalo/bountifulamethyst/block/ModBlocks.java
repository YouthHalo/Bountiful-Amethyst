package youthhalo.bountifulamethyst.block;

import java.util.function.Function;

import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.util.Identifier;
import youthhalo.bountifulamethyst.BountifulAmethyst;
import youthhalo.bountifulamethyst.item.ModItems;

public class ModBlocks {

	public static final Block ENHANCED_ENCHANTING_TABLE = register("enhanced_enchanting_table",
			settings -> new EnhancedEnchantingTableBlock(settings),
			AbstractBlock.Settings.create()
					.strength(7.5f, 1200.0f) // Increased hardness by 50% (from 5.0 to 7.5), same resistance
					.sounds(BlockSoundGroup.STONE) // mining sound
					.requiresTool(), // Requires a tool to break efficiently
			true);

	public static final Block DEPLETED_AMETHYST_BLOCK = register("depleted_amethyst_block",
			settings -> new Block(settings),
			AbstractBlock.Settings.create()
					.strength(3.0f, 9.0f) // Hardness and resistance similar to iron ore
					.sounds(BlockSoundGroup.STONE)
					.requiresTool(), // mining sound
			true); 

	public static void initialize() {
		BountifulAmethyst.LOGGER.info("Registering Mod Blocks for " + BountifulAmethyst.MOD_ID);

		// Add blocks to the custom item group
		ItemGroupEvents.modifyEntriesEvent(ModItems.BOUNTIFUL_AMETHYST_GROUP_KEY).register(entries -> {
			entries.add(ENHANCED_ENCHANTING_TABLE);
			entries.add(DEPLETED_AMETHYST_BLOCK);

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

			// Register as a regular BlockItem
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
