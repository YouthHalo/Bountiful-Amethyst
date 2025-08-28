package youthhalo.bountifulamethyst.client.gui.screen;

import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.render.entity.model.BookModel;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerListener;
import net.minecraft.text.StringVisitable;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.ColorHelper;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.random.Random;
import youthhalo.bountifulamethyst.screen.EnhancedEnchantingTableScreenHandler;

public class EnhancedEnchantingTableScreen extends HandledScreen<EnhancedEnchantingTableScreenHandler> {
	 private static final Identifier[] LEVEL_TEXTURES = new Identifier[]{Identifier.ofVanilla("container/enchanting_table/level_1"), Identifier.ofVanilla("container/enchanting_table/level_2"), Identifier.ofVanilla("container/enchanting_table/level_3")};
	 private static final Identifier[] LEVEL_DISABLED_TEXTURES = new Identifier[]{Identifier.ofVanilla("container/enchanting_table/level_1_disabled"), Identifier.ofVanilla("container/enchanting_table/level_2_disabled"), Identifier.ofVanilla("container/enchanting_table/level_3_disabled")};
	 private static final Identifier ENCHANTMENT_SLOT_DISABLED_TEXTURE = Identifier.ofVanilla("container/enchanting_table/enchantment_slot_disabled");
	 private static final Identifier ENCHANTMENT_SLOT_HIGHLIGHTED_TEXTURE = Identifier.ofVanilla("container/enchanting_table/enchantment_slot_highlighted");
	 private static final Identifier ENCHANTMENT_SLOT_TEXTURE = Identifier.ofVanilla("container/enchanting_table/enchantment_slot");
	 private static final Identifier TEXTURE = Identifier.ofVanilla("textures/gui/container/enchanting_table.png");
	 private static final Identifier BOOK_TEXTURE = Identifier.ofVanilla("textures/entity/enchanting_table_book.png");
   private final Random random = Random.create();
   private BookModel BOOK_MODEL;
   public float nextPageAngle;
   public float pageAngle;
   public float approximatePageAngle;
   public float pageRotationSpeed;
   public float nextPageTurningSpeed;
   public float pageTurningSpeed;
   private net.minecraft.item.ItemStack stack;
   private boolean revealMode = false;   public EnhancedEnchantingTableScreen(EnhancedEnchantingTableScreenHandler handler, PlayerInventory inventory, Text title) {
      super(handler, inventory, title);
      this.stack = net.minecraft.item.ItemStack.EMPTY;
      
      // Listen for property changes to sync reveal mode
      handler.addListener(new ScreenHandlerListener() {
         @Override
         public void onSlotUpdate(ScreenHandler handler, int slotId, ItemStack stack) {
            // Handle slot updates if needed
         }
         
         @Override
         public void onPropertyUpdate(ScreenHandler handler, int property, int value) {
            // Property 0 is reveal mode
            if (property == 0) {
               revealMode = (value == 1);
            }
         }
      });
   }	 protected void init() {
			super.init();
			this.BOOK_MODEL = new BookModel(this.client.getLoadedEntityModels().getModelPart(EntityModelLayers.BOOK));
	 }

	 public void handledScreenTick() {
			super.handledScreenTick();
			this.client.player.experienceBarDisplayStartTime = this.client.player.age;
			this.doTick();
	 }

	public boolean mouseClicked(double mouseX, double mouseY, int button) {
			int i = (this.width - this.backgroundWidth) / 2;
			int j = (this.height - this.backgroundHeight) / 2;

			for(int k = 0; k < 3; ++k) {
				 double d = mouseX - (double)(i + 60);
				 double e = mouseY - (double)(j + 14 + 19 * k);
				 if (d >= 0.0 && e >= 0.0 && d < 108.0 && e < 19.0) {
						// Send button click to server for all buttons
						this.client.interactionManager.clickButton(((EnhancedEnchantingTableScreenHandler)this.handler).syncId, k);
						return true;
				 }
			}

			return super.mouseClicked(mouseX, mouseY, button);
	 }

	@Override
	public void render(DrawContext context, int mouseX, int mouseY, float delta) {
		renderBackground(context, mouseX, mouseY, delta);
		super.render(context, mouseX, mouseY, delta);

		int x = (width - backgroundWidth) / 2;
		int y = (height - backgroundHeight) / 2;

		// Draw amethyst count info in the left panel
		int amethystCount = handler.getAmethystBlockCount();
		String amethystText = "Amethyst: " + amethystCount + "/9";
		context.drawText(textRenderer, amethystText, x + 10, y + 10, 0xC8C8C8, false);

		drawMouseoverTooltip(context, mouseX, mouseY);
	}

	protected void drawMouseoverTooltip(DrawContext context, int mouseX, int mouseY) {
		super.drawMouseoverTooltip(context, mouseX, mouseY);
		
		int x = (width - backgroundWidth) / 2;
		int y = (height - backgroundHeight) / 2;
		
		// Check if hovering over enchantment buttons (all three)
		for(int slot = 0; slot < 3; ++slot) {
			int slotX = x + 60;
			int slotY = y + 14 + 19 * slot;
			
			if (mouseX >= slotX && mouseX < slotX + 108 && mouseY >= slotY && mouseY < slotY + 19) {
				if (slot == 2) {
					// Show reveal tooltip for third button
					var revealTooltip = generateRevealTooltip();
					if (!revealTooltip.isEmpty()) {
						context.drawTooltip(textRenderer, revealTooltip, mouseX, mouseY);
					}
				} else {
					// Check if there's an enchantable item for first two buttons
					net.minecraft.item.ItemStack itemStack = this.handler.getSlot(0).getStack();
					if (!itemStack.isEmpty() && isEnchantableItem(itemStack)) {
						// Generate tooltip showing first enchantment
						int enchantLevel = (slot == 0) ? 2 : 3;
						var tooltip = generateEnchantmentTooltip(itemStack, enchantLevel);
						if (!tooltip.isEmpty()) {
							context.drawTooltip(textRenderer, tooltip, mouseX, mouseY);
						}
					}
				}
				break; // Only show tooltip for one button at a time
			}
		}
	}

	/**
	 * Check if an item can be enchanted (tools, weapons, armor, etc.)
	 */
	private static boolean isEnchantableItem(net.minecraft.item.ItemStack itemStack) {
		if (itemStack.isEmpty()) {
			return false;
		}
		
		// Check if item already has enchantments - if so, don't allow enchanting
		var existingEnchantments = net.minecraft.enchantment.EnchantmentHelper.getEnchantments(itemStack);
		if (!existingEnchantments.isEmpty()) {
			return false;
		}
		
		var item = itemStack.getItem();
		
		// Check for common enchantable items by their string ID
		String itemId = net.minecraft.registry.Registries.ITEM.getId(item).toString();
		
		return itemId.contains("sword") ||
				 itemId.contains("pickaxe") ||
				 itemId.contains("axe") ||
				 itemId.contains("shovel") ||
				 itemId.contains("hoe") ||
				 itemId.contains("helmet") ||
				 itemId.contains("chestplate") ||
				 itemId.contains("leggings") ||
				 itemId.contains("boots") ||
				 itemId.contains("bow") ||
				 itemId.contains("crossbow") ||
				 itemId.contains("trident") ||
				 itemId.contains("fishing_rod") ||
				 itemId.contains("flint_and_steel") ||
				 item == net.minecraft.item.Items.BOOK ||
				 itemId.contains("shield") ||
				 itemId.contains("elytra");
	}

	private java.util.List<Text> generateEnchantmentTooltip(net.minecraft.item.ItemStack itemStack, int enchantLevel) {
		var tooltip = new java.util.ArrayList<Text>();
		
		// If in reveal mode, show all selected enchantments
		if (revealMode) {
			// For now, show a message that reveal mode is active
			tooltip.add(Text.literal("Reveal Mode Active").formatted(Formatting.GOLD));
			tooltip.add(Text.literal("Enchantments will be shown here").formatted(Formatting.GRAY));
		} else {
			// Show one random enchantment as preview
			var world = this.client.world;
			if (world != null) {
				var enchantmentRegistry = world.getRegistryManager().getOrThrow(net.minecraft.registry.RegistryKeys.ENCHANTMENT);
				
				// Get all possible enchantments for this item
				var possibleEnchantments = enchantmentRegistry.streamEntries()
					.filter(entry -> entry.value().isAcceptableItem(itemStack))
					.filter(entry -> entry.value().getMinLevel() <= enchantLevel && entry.value().getMaxLevel() >= enchantLevel)
					.toList();
				
				if (!possibleEnchantments.isEmpty()) {
					// Use a fixed seed for consistent preview display
					var random = net.minecraft.util.math.random.Random.create();
					random.setSeed(42L); // Fixed seed for consistent preview display
					
					// Pick first enchantment as preview
					var firstEnchantment = possibleEnchantments.get(random.nextInt(possibleEnchantments.size()));
					var enchantmentName = net.minecraft.enchantment.Enchantment.getName(firstEnchantment, enchantLevel);
					
					// Add enchantment name with "...?" to indicate it's a preview
					var combinedText = Text.literal("").append(enchantmentName).append(Text.literal("...?").formatted(Formatting.GRAY));
					tooltip.add(combinedText);
				}
			}
		}
		
		// Add requirements for both modes
		if (!tooltip.isEmpty()) {
			// Empty line
			tooltip.add(Text.literal(""));
			
			// Lapis requirement
			int requiredLapis = (enchantLevel == 2) ? 2 : 3;
			int currentLapis = this.handler.getSlot(1).getStack().getCount();
			var lapisText = Text.literal(requiredLapis + " Lapis Lazuli");
			if (currentLapis < requiredLapis) {
				lapisText = lapisText.formatted(Formatting.RED);
			}
			tooltip.add(lapisText);
			
			// XP level requirement
			int xpCost = (enchantLevel == 2) ? this.handler.getEnchantCost1() : this.handler.getEnchantCost2();
			var xpText = Text.literal(xpCost + " Enchantment Levels");
			if (this.client.player.experienceLevel < xpCost) {
				xpText = xpText.formatted(Formatting.RED);
			}
			tooltip.add(xpText);
		}
		
		return tooltip;
	}

	private java.util.List<Text> generateRevealTooltip() {
		var tooltip = new java.util.ArrayList<Text>();
		
		// XP cost (left side)
		int xpCost = 5;
		var xpText = Text.literal(xpCost + " levels").formatted(Formatting.GREEN);
		tooltip.add(xpText);
		
		// Empty line
		tooltip.add(Text.literal(""));
		
		// Level requirement (right side)
		int levelRequirement = 30;
		var levelText = Text.literal("Requires " + levelRequirement + " levels").formatted(Formatting.YELLOW);
		tooltip.add(levelText);
		
		return tooltip;
	}

	 protected void drawBackground(DrawContext context, float deltaTicks, int mouseX, int mouseY) {
			int i = (this.width - this.backgroundWidth) / 2;
			int j = (this.height - this.backgroundHeight) / 2;
			context.drawTexture(RenderPipelines.GUI_TEXTURED, TEXTURE, i, j, 0.0F, 0.0F, this.backgroundWidth, this.backgroundHeight, 256, 256);
			
			// Check if there's an enchantable item in the enchanting slot
			net.minecraft.item.ItemStack itemStack = this.handler.getSlot(0).getStack();
			boolean hasEnchantableItem = !itemStack.isEmpty() && isEnchantableItem(itemStack);
			
			// Only draw and animate book if there's an enchantable item
			if (hasEnchantableItem) {
				 this.drawBook(context, i, j);
			}
			
			// Use a fixed seed for consistent enchanting phrases
			this.random.setSeed(42L);
			
			int lapisCount = this.handler.getSlot(1).getStack().getCount();

			for(int slot = 0; slot < 3; ++slot) {
				 int slotX = i + 60;
				 int slotY = j + 14 + 19 * slot;
				 
				 // Get the XP cost for this slot
				 int xpCost = (slot == 0) ? this.handler.getEnchantCost1() : 
										 (slot == 1) ? this.handler.getEnchantCost2() : 
										 this.handler.getRevealCost();
				 
				 // Only show costs if there's an enchantable item
				 if (!hasEnchantableItem) {
						xpCost = 0;
				 }
				 
				 // Check lapis requirements based on slot
				 int requiredLapis = (slot == 0) ? 2 : (slot == 1) ? 3 : 1;
				 
				 // Check if player can afford
				 boolean canAfford = this.client.player.experienceLevel >= xpCost && 
													 lapisCount >= requiredLapis && 
													 !this.client.player.isInCreativeMode() &&
													 hasEnchantableItem;
				 
				 // For third slot, always show as available if there's an item (different color)
				 if (slot == 2 && hasEnchantableItem) {
						canAfford = true;
				 }
				 
				 if (xpCost == 0) {
						context.drawGuiTexture(RenderPipelines.GUI_TEXTURED, ENCHANTMENT_SLOT_DISABLED_TEXTURE, slotX, slotY, 108, 19);
				 } else {
						// Handle reveal button differently
						if (slot == 2) {
							 // For reveal button, don't show level texture or galactic text
							 int relativeMouseX = mouseX - (i + 60);
							 int relativeMouseY = mouseY - (j + 14 + 19 * slot);
							 
							 if (relativeMouseX >= 0 && relativeMouseY >= 0 && relativeMouseX < 108 && relativeMouseY < 19) {
									context.drawGuiTexture(RenderPipelines.GUI_TEXTURED, ENCHANTMENT_SLOT_HIGHLIGHTED_TEXTURE, slotX, slotY, 108, 19);
							 } else {
									context.drawGuiTexture(RenderPipelines.GUI_TEXTURED, ENCHANTMENT_SLOT_TEXTURE, slotX, slotY, 108, 19);
							 }
							 
							 // Draw "REVEAL" text on the left
							 context.drawTextWithShadow(this.textRenderer, "REVEAL", slotX + 20, slotY + 2 + 7, canAfford ? -8323296 : -12550384);
							 
							 // Draw XP cost on the right
							 String costText = xpCost + " XP";
							 int costTextWidth = this.textRenderer.getWidth(costText);
							 context.drawTextWithShadow(this.textRenderer, costText, slotX + 108 - costTextWidth - 5, slotY + 2 + 7, canAfford ? -8323296 : -12550384);
						} else {
							 // For regular enchantment buttons, use the original logic
							 String levelString = "" + xpCost;
							 int textWidth = 86 - this.textRenderer.getWidth(levelString);
							 
							 // Generate enchanting phrase in galactic alphabet
							 String[] galacticPhrases = {
										"ᔑᓵᘎᘬᓭ ᔑᓵᘎᘬᓭ ᔑᓵᘎᘬᓭ",
										"ᓭᓪᓸᓪᓭ ᓭᓪᓸᓪᓭ ᓭᓪᓸᓪᓭ",
										"ᘬᓭᓪᓸ ᘬᓭᓪᓸ ᘬᓭᓪᓸ",
										"ᔑᓵᘎᘬ ᔑᓵᘎᘬ ᔑᓵᘎᘬ",
										"ᓭᓪᓸᓪ ᓭᓪᓸᓪ ᓭᓪᓸᓪ",
										"ᘎᘬᓭᓪ ᘎᘬᓭᓪ ᘎᘬᓭᓪ",
										"ᓵᘎᘬᓭ ᓵᘎᘬᓭ ᓵᘎᘬᓭ",
										"ᔑᓵᘎᘬᓭᓪᓸ ᔑᓵᘎᘬᓭᓪᓸ",
										"ᓭᓪᓸᓪᓭᓪ ᓭᓪᓸᓪᓭᓪ",
										"ᘬᓭᓪᓸᘎᘬ ᘬᓭᓪᓸᘎᘬ"
							 };
							 
							 String phrase = galacticPhrases[this.random.nextInt(galacticPhrases.length)];
							 
							 int textColor = -9937334;
							 
							 if (!canAfford) {
									context.drawGuiTexture(RenderPipelines.GUI_TEXTURED, ENCHANTMENT_SLOT_DISABLED_TEXTURE, slotX, slotY, 108, 19);
									// Use correct disabled level texture for each slot
									Identifier disabledLevelTexture = LEVEL_DISABLED_TEXTURES[slot + 1];
									context.drawGuiTexture(RenderPipelines.GUI_TEXTURED, disabledLevelTexture, slotX + 1, slotY + 1, 16, 16);
									StringVisitable phraseVisitable = this.textRenderer.getTextHandler().trimToWidth(
											Text.literal(phrase), textWidth, net.minecraft.text.Style.EMPTY);
									context.drawWrappedText(this.textRenderer, phraseVisitable, slotX + 20, slotY + 2, textWidth, 
											ColorHelper.fullAlpha((textColor & 16711422) >> 1), false);
									textColor = -12550384;
							 } else {
									int relativeMouseX = mouseX - (i + 60);
									int relativeMouseY = mouseY - (j + 14 + 19 * slot);
									
									if (relativeMouseX >= 0 && relativeMouseY >= 0 && relativeMouseX < 108 && relativeMouseY < 19) {
										 context.drawGuiTexture(RenderPipelines.GUI_TEXTURED, ENCHANTMENT_SLOT_HIGHLIGHTED_TEXTURE, slotX, slotY, 108, 19);
										 textColor = -128;
									} else {
										 context.drawGuiTexture(RenderPipelines.GUI_TEXTURED, ENCHANTMENT_SLOT_TEXTURE, slotX, slotY, 108, 19);
									}

									// Use level texture for regular enchantment buttons
									Identifier levelTexture = LEVEL_TEXTURES[slot + 1];
									context.drawGuiTexture(RenderPipelines.GUI_TEXTURED, levelTexture, slotX + 1, slotY + 1, 16, 16);
									context.drawWrappedText(this.textRenderer, Text.literal(phrase), slotX + 20, slotY + 2, textWidth, textColor, false);
									textColor = -8323296;
							 }

							 context.drawTextWithShadow(this.textRenderer, levelString, slotX + 20 + 86 - this.textRenderer.getWidth(levelString), 
									slotY + 2 + 7, textColor);
						}
				 }
			}
	 }

	 private void drawBook(DrawContext context, int x, int y) {
			float f = this.client.getRenderTickCounter().getTickProgress(false);
			float g = MathHelper.lerp(f, this.pageTurningSpeed, this.nextPageTurningSpeed);
			float h = MathHelper.lerp(f, this.pageAngle, this.nextPageAngle);
			int i = x + 14;
			int j = y + 14;
			int k = i + 38;
			int l = j + 31;
			
			// Draw the normal book
			context.addBookModel(this.BOOK_MODEL, BOOK_TEXTURE, 40.0F, g, h, i, j, k, l);
			
			// Add enchantment glint if in reveal mode
			if (revealMode) {
				// Use enchantment glint texture
				Identifier glintTexture = Identifier.ofVanilla("textures/misc/enchanted_item_glint.png");
				context.addBookModel(this.BOOK_MODEL, glintTexture, 40.0F, g, h, i, j, k, l);
			}
	 }

	 public void doTick() {
			net.minecraft.item.ItemStack itemStack = ((EnhancedEnchantingTableScreenHandler)this.handler).getSlot(0).getStack();
			if (!net.minecraft.item.ItemStack.areEqual(itemStack, this.stack)) {
				 this.stack = itemStack;

				 do {
						this.approximatePageAngle += (float)(this.random.nextInt(4) - this.random.nextInt(4));
				 } while(this.nextPageAngle <= this.approximatePageAngle + 1.0F && this.nextPageAngle >= this.approximatePageAngle - 1.0F);
			}

			this.pageAngle = this.nextPageAngle;
			this.pageTurningSpeed = this.nextPageTurningSpeed;
			
			// Check if there's an enchantable item in the slot
			boolean hasEnchantableItem = !itemStack.isEmpty() && isEnchantableItem(itemStack);
			boolean hasEnchantments = false;

			if (hasEnchantableItem) {
				 for(int i = 0; i < 3; ++i) {
						int cost = (i == 0) ? this.handler.getEnchantCost1() : 
											(i == 1) ? this.handler.getEnchantCost2() : 
											this.handler.getRevealCost();
						if (cost != 0) {
							 hasEnchantments = true;
						}
				 }
			}

			if (hasEnchantments) {
				 this.nextPageTurningSpeed += 0.2F;
			} else {
				 this.nextPageTurningSpeed -= 0.2F;
			}

			this.nextPageTurningSpeed = MathHelper.clamp(this.nextPageTurningSpeed, 0.0F, 1.0F);
			float f = (this.approximatePageAngle - this.nextPageAngle) * 0.4F;
			f = MathHelper.clamp(f, -0.2F, 0.2F);
			this.pageRotationSpeed += (f - this.pageRotationSpeed) * 0.9F;
			this.nextPageAngle += this.pageRotationSpeed;
	 }
}
