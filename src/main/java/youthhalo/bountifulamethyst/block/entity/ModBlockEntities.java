package youthhalo.bountifulamethyst.block.entity;

import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import youthhalo.bountifulamethyst.BountifulAmethyst;
import youthhalo.bountifulamethyst.block.ModBlocks;

public class ModBlockEntities {
	
	public static final BlockEntityType<EnhancedEnchantingTableBlockEntity> ENHANCED_ENCHANTING_TABLE = 
		Registry.register(
			Registries.BLOCK_ENTITY_TYPE,
			Identifier.of(BountifulAmethyst.MOD_ID, "enhanced_enchanting_table"),
			FabricBlockEntityTypeBuilder.<EnhancedEnchantingTableBlockEntity>create(
				EnhancedEnchantingTableBlockEntity::new, 
				ModBlocks.ENHANCED_ENCHANTING_TABLE
			).build()
		);

	public static void initialize() {
		BountifulAmethyst.LOGGER.info("Registering Block Entities for " + BountifulAmethyst.MOD_ID);
	}
}
