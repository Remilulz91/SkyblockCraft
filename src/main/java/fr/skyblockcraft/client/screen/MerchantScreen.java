package fr.skyblockcraft.client.screen;

import fr.skyblockcraft.merchant.screen.MerchantScreenHandler;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

/**
 * Client-side rendering for the Sky Merchant shop. Uses the vanilla 6-row
 * chest texture as the background — no custom textures required for the MVP.
 */
public class MerchantScreen extends HandledScreen<MerchantScreenHandler> {

    private static final Identifier TEXTURE = Identifier.ofVanilla("textures/gui/container/generic_54.png");

    public MerchantScreen(MerchantScreenHandler handler, PlayerInventory inv, Text title) {
        super(handler, inv, title);
        this.backgroundHeight = 222;
        this.playerInventoryTitleY = this.backgroundHeight - 94;
    }

    @Override
    protected void drawBackground(DrawContext ctx, float delta, int mouseX, int mouseY) {
        int x = (this.width - this.backgroundWidth) / 2;
        int y = (this.height - this.backgroundHeight) / 2;
        // Top part (title bar + 6 rows of slots): 17 + 6*18 = 125 px
        ctx.drawTexture(TEXTURE, x, y, 0, 0, this.backgroundWidth, 17 + MerchantScreenHandler.SHOP_ROWS * 18);
        // Bottom part (player inventory + hotbar): sampled from the vanilla texture at v=126
        ctx.drawTexture(TEXTURE, x, y + 17 + MerchantScreenHandler.SHOP_ROWS * 18,
                0, 126, this.backgroundWidth, 96);
    }

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        this.renderBackground(ctx, mouseX, mouseY, delta);
        super.render(ctx, mouseX, mouseY, delta);
        this.drawMouseoverTooltip(ctx, mouseX, mouseY);
    }
}
