package youthhalo.bountifulamethyst.client.gui.screen;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.Text;
import youthhalo.bountifulamethyst.screen.EnhancedEnchantingTableScreenHandler;

public class EnhancedEnchantingTableScreen extends HandledScreen<EnhancedEnchantingTableScreenHandler> {
    private ButtonWidget enchantButton1;
    private ButtonWidget enchantButton2;
    private ButtonWidget enchantButton3;

    public EnhancedEnchantingTableScreen(EnhancedEnchantingTableScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
        this.backgroundHeight = 166;
        this.playerInventoryTitleY = this.backgroundHeight - 94;
    }

    @Override
    protected void init() {
        super.init();
        // Center the title
        titleX = (backgroundWidth - textRenderer.getWidth(title)) / 2;
        
        // Add enchantment buttons positioned like vanilla enchanting table
        int x = (width - backgroundWidth) / 2;
        int y = (height - backgroundHeight) / 2;
        
        // Get dynamic costs from the handler
        int cost1 = handler.getEnchantCost1();
        int cost2 = handler.getEnchantCost2();
        int revealCost = handler.getRevealCost();
        
        // Two enchantment option buttons (level 2 and 3 equivalents)
        enchantButton1 = ButtonWidget.builder(Text.literal("II (" + cost1 + " XP)"), button -> {
            enchantItem(1);
        }).dimensions(x + 99, y + 7, 108, 19).build();
        
        enchantButton2 = ButtonWidget.builder(Text.literal("III (" + cost2 + " XP)"), button -> {
            enchantItem(2);
        }).dimensions(x + 99, y + 27, 108, 19).build();
        
        // Reveal button to show what enchantments will be applied
        enchantButton3 = ButtonWidget.builder(Text.literal("Reveal (" + revealCost + " XP)"), button -> {
            revealEnchantments();
        }).dimensions(x + 99, y + 47, 108, 19).build();
        
        addDrawableChild(enchantButton1);
        addDrawableChild(enchantButton2);
        addDrawableChild(enchantButton3);
    }
    
    private void enchantItem(int option) {
        // TODO: Implement enchanting logic
        // This will enchant the item with the selected option
    }
    
    private void revealEnchantments() {
        // TODO: Implement reveal logic
        // This will show what enchantments would be applied
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
        
        // Draw enchantment level indicators and preview text
        context.drawText(textRenderer, "II", x + 105, y + 15, 0xC8C8C8, false);  // Level 2
        context.drawText(textRenderer, "III", x + 105, y + 35, 0xC8C8C8, false); // Level 3
        context.drawText(textRenderer, "?", x + 105, y + 55, 0xC8C8C8, false);   // Reveal button
        
        // Draw placeholder enchantment text (normally would be blurred/encrypted)
        context.drawText(textRenderer, "??? ??? ???", x + 120, y + 15, 0x666666, false);
        context.drawText(textRenderer, "??? ??? ???", x + 120, y + 35, 0x666666, false);
        context.drawText(textRenderer, "Show enchants", x + 120, y + 55, 0x666666, false);
        
        // Draw XP cost on the right side of each option
        int cost1 = handler.getEnchantCost1();
        int cost2 = handler.getEnchantCost2(); 
        int revealCost = handler.getRevealCost();
        
        context.drawText(textRenderer, cost1 + "", x + 190, y + 15, 0x80FF20, false);
        context.drawText(textRenderer, cost2 + "", x + 190, y + 35, 0x80FF20, false);
        context.drawText(textRenderer, revealCost + "", x + 190, y + 55, 0x80FF20, false);
        
        drawMouseoverTooltip(context, mouseX, mouseY);
    }

    @Override
    protected void drawBackground(DrawContext context, float delta, int mouseX, int mouseY) {
        int x = (width - backgroundWidth) / 2;
        int y = (height - backgroundHeight) / 2;
        
        // Draw main background similar to vanilla enchanting table
        context.fill(x, y, x + backgroundWidth, y + backgroundHeight, 0xFF373737);
        context.drawBorder(x, y, backgroundWidth, backgroundHeight, 0xFF8B8B8B);
        
        // Draw the left panel (book and slots area) - lighter background
        context.fill(x + 7, y + 7, x + 90, y + 72, 0xFF4A4A4A);
        context.drawBorder(x + 7, y + 7, 83, 65, 0xFF6B6B6B);
        
        // Draw the right panel (enchantment options area) - darker background  
        context.fill(x + 99, y + 7, x + 207, y + 72, 0xFF2A2A2A);
        context.drawBorder(x + 99, y + 7, 108, 65, 0xFF5A5A5A);
        
        // Draw individual enchantment option backgrounds
        context.fill(x + 102, y + 10, x + 204, y + 25, 0xFF1A1A1A);  // Option 1
        context.fill(x + 102, y + 30, x + 204, y + 45, 0xFF1A1A1A);  // Option 2
        context.fill(x + 102, y + 50, x + 204, y + 65, 0xFF1A1A1A);  // Option 3
        
        // Draw slot backgrounds for item and lapis slots (positioned like vanilla)
        // Item slot (15, 47)
        context.fill(x + 14, y + 46, x + 32, y + 64, 0xFF8B8B8B);
        context.fill(x + 15, y + 47, x + 31, y + 63, 0xFF2A2A2A);
        
        // Lapis slot (35, 47)  
        context.fill(x + 34, y + 46, x + 52, y + 64, 0xFF8B8B8B);
        context.fill(x + 35, y + 47, x + 51, y + 63, 0xFF2A2A2A);
    }
}
