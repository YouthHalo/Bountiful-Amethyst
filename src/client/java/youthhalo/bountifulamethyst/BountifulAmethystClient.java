package youthhalo.bountifulamethyst;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.rendering.v1.BlockRenderLayerMap;
import net.minecraft.client.gui.screen.ingame.HandledScreens;
import net.minecraft.client.render.BlockRenderLayer;
import youthhalo.bountifulamethyst.block.ModBlocks;
import youthhalo.bountifulamethyst.client.gui.screen.EnhancedEnchantingTableScreen;
import youthhalo.bountifulamethyst.screen.ModScreenHandlers;

@Environment(EnvType.CLIENT)
public class BountifulAmethystClient implements ClientModInitializer {
	public void onInitializeClient() {
		// To make some parts of the block transparent (like glass, saplings and doors):
		BlockRenderLayerMap.putBlock(ModBlocks.ENHANCED_ENCHANTING_TABLE, BlockRenderLayer.CUTOUT);

		// Register the screen for our container
		HandledScreens.register(ModScreenHandlers.ENHANCED_ENCHANTING_TABLE, EnhancedEnchantingTableScreen::new);

		// To make some parts of the block translucent (like ice, stained glass and
		// portal)
		// BlockRenderLayerMap.putBlock(ModBlocks.ENHANCED_ENCHANTING_TABLE,
		// BlockRenderLayer.TRANSLUCENT);
	}
}