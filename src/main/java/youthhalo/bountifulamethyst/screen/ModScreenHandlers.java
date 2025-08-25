package youthhalo.bountifulamethyst.screen;

import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.util.Identifier;
import youthhalo.bountifulamethyst.BountifulAmethyst;

public class ModScreenHandlers {
    
    public static final ScreenHandlerType<EnhancedEnchantingTableScreenHandler> ENHANCED_ENCHANTING_TABLE =
        Registry.register(Registries.SCREEN_HANDLER, 
            Identifier.of(BountifulAmethyst.MOD_ID, "enhanced_enchanting_table"),
            new ScreenHandlerType<>(EnhancedEnchantingTableScreenHandler::new, null));

    public static void initialize() {
        BountifulAmethyst.LOGGER.info("Registering Screen Handlers for " + BountifulAmethyst.MOD_ID);
    }
}
