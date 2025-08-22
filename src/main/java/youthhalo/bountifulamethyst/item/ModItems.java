package youthhalo.bountifulamethyst.item;

import java.util.function.Function;

import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import youthhalo.bountifulamethyst.BountifulAmethyst;

public class ModItems {

	public static final Item AMETHYST_STAFF = register("amethyst_staff", AmethystStaffItem::new, new Item.Settings());
	public static final Item AMETHYST_TOME = register("amethyst_tome", AmethystTomeItem::new, new Item.Settings());

	// Custom Item Group
	public static final RegistryKey<ItemGroup> BOUNTIFUL_AMETHYST_GROUP_KEY = RegistryKey.of(
			RegistryKeys.ITEM_GROUP,
			Identifier.of(BountifulAmethyst.MOD_ID, "bountiful_amethyst_group"));

	public static final ItemGroup BOUNTIFUL_AMETHYST_GROUP = FabricItemGroup.builder()
			.icon(() -> new ItemStack(net.minecraft.item.Items.AMETHYST_CLUSTER))
			.displayName(Text.translatable("itemGroup.bountiful-amethyst.bountiful_amethyst_group"))
			.build();

	public static void initialize() {
		BountifulAmethyst.LOGGER.info("Registering Mod Items for " + BountifulAmethyst.MOD_ID);

		// Register the custom item group
		Registry.register(Registries.ITEM_GROUP, BOUNTIFUL_AMETHYST_GROUP_KEY, BOUNTIFUL_AMETHYST_GROUP);

		// Add items to the custom item group
		ItemGroupEvents.modifyEntriesEvent(BOUNTIFUL_AMETHYST_GROUP_KEY).register(entries -> {
			// Add items
			entries.add(AMETHYST_STAFF);
			entries.add(AMETHYST_TOME);
		});
	}

	public static Item register(String name, Function<Item.Settings, Item> itemFactory, Item.Settings settings) {
		// Create the item key.
		RegistryKey<Item> itemKey = RegistryKey.of(RegistryKeys.ITEM, Identifier.of(BountifulAmethyst.MOD_ID, name));

		// Create the item instance.
		Item item = itemFactory.apply(settings.registryKey(itemKey));

		// Register the item.
		Registry.register(Registries.ITEM, itemKey, item);

		return item;
	}
}
