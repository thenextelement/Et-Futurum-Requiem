package ganymedes01.etfuturum.client.gui.inventory;

import ganymedes01.etfuturum.client.gui.modern.GuiPanel;
import ganymedes01.etfuturum.client.gui.modern.ModernGuiTextures;
import ganymedes01.etfuturum.core.utils.MapLocking;
import ganymedes01.etfuturum.inventory.ContainerCartographyTable;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.resources.I18n;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.storage.MapData;
import org.lwjgl.opengl.GL11;

public class GuiCartographyTable extends GuiContainer {

	private static final ResourceLocation TEXTURE = ModernGuiTextures.container("cartography_table");

	private final ContainerCartographyTable container;
	private boolean textured;

	public GuiCartographyTable(ContainerCartographyTable container) {
		super(container);
		this.container = container;
		xSize = 176;
		ySize = 166;
	}

	@Override
	public void initGui() {
		super.initGui();
		textured = ModernGuiTextures.isAvailable(TEXTURE);
	}

	@Override
	protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
		fontRendererObj.drawString(I18n.format("container.etfuturum.cartography_table"), 8, 4, 0x404040);
		fontRendererObj.drawString(I18n.format("container.inventory"), 8, ySize - 94, 0x404040);
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		int left = guiLeft, top = guiTop;

		ItemStack map = container.getSlot(0).getStack();
		ItemStack modifier = container.getSlot(1).getStack();
		Item mod = modifier == null ? null : modifier.getItem();
		boolean cloning = mod == Items.map;
		boolean zooming = mod == Items.paper;
		boolean locking = mod == Item.getItemFromBlock(Blocks.glass_pane);
		MapData data = container.getMapData();
		boolean locked = MapLocking.isLocked(map);
		boolean blocked = data != null && (locked && (zooming || locking) || zooming && data.scale >= MapLocking.MAX_SCALE);

		if (textured) {
			mc.getTextureManager().bindTexture(TEXTURE);
			drawTexturedModalRect(left, top, 0, 0, xSize, ySize);
			if (blocked) {
				drawTexturedModalRect(left + 35, top + 31, xSize + 50, 132, 28, 21);
			}
		} else {
			GuiPanel.panel(left, top, xSize, ySize);
			GuiPanel.allSlots(container, left, top);
			GuiPanel.sunken(left + 67, top + 13, 66, 66);
			if (blocked) {
				fontRendererObj.drawString("X", left + 45, top + 37, 0xFFAA0000);
			}
		}

		if (map == null || data == null) return;

		// Previews follow the 1.20.1 cartography table screen
		if (zooming && !blocked) {
			frame(left + 67, top + 13, xSize, 66, 66, 66);
			drawMap(data, left + 85, top + 31, 0.226F);
		} else if (cloning) {
			frame(left + 67 + 16, top + 13, xSize, 132, 50, 66);
			drawMap(data, left + 86, top + 16, 0.34F);
			GL11.glTranslatef(0, 0, 1);
			frame(left + 67, top + 13 + 16, xSize, 132, 50, 66);
			drawMap(data, left + 70, top + 32, 0.34F);
			GL11.glTranslatef(0, 0, -1);
		} else {
			frame(left + 67, top + 13, xSize, 0, 66, 66);
			drawMap(data, left + 71, top + 17, 0.45F);
			if (locking && !blocked || locked) {
				GL11.glTranslatef(0, 0, 1);
				frame(left + 66, top + 12, 0, ySize, 66, 66);
				GL11.glTranslatef(0, 0, -1);
			}
		}
	}

	private void frame(int x, int y, int u, int v, int w, int h) {
		if (!textured) return;
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		mc.getTextureManager().bindTexture(TEXTURE);
		drawTexturedModalRect(x, y, u, v, w, h);
	}

	private void drawMap(MapData data, int x, int y, float scale) {
		GL11.glPushMatrix();
		GL11.glTranslatef(x, y, 1.0F);
		GL11.glScalef(scale, scale, 1.0F);
		mc.entityRenderer.getMapItemRenderer().func_148250_a(data, true);
		GL11.glPopMatrix();
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
	}
}
