package youthhalo.bountifulamethyst.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.screen.EnchantmentScreenHandler;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import youthhalo.bountifulamethyst.block.EnhancedEnchantingTableBlock;
import youthhalo.bountifulamethyst.util.EnchantingUtils;

@Mixin(EnchantmentScreenHandler.class)
public class EnchantmentScreenHandlerMixin {

	@Inject(method = "getPower(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;)I", at = @At("HEAD"), cancellable = true)
	private static void getEnhancedPower(World world, BlockPos pos, CallbackInfoReturnable<Integer> cir) {
		// Check if this is our enhanced enchanting table
		if (world.getBlockState(pos).getBlock() instanceof EnhancedEnchantingTableBlock) {
			int enhancedPower = EnchantingUtils.getEnhancedPower(world, pos);
			cir.setReturnValue(enhancedPower);
		}
	}
}
