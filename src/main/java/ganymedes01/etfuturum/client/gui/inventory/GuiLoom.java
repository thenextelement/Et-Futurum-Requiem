package ganymedes01.etfuturum.client.gui.inventory;

import ganymedes01.etfuturum.client.gui.modern.GuiPanel;
import ganymedes01.etfuturum.client.gui.modern.ModernGuiTextures;
import ganymedes01.etfuturum.inventory.ContainerLoom;
import ganymedes01.etfuturum.lib.EnumColor;
import ganymedes01.etfuturum.tileentities.TileEntityBanner;
import ganymedes01.etfuturum.tileentities.TileEntityBanner.EnumBannerPattern;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.resources.I18n;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import java.util.List;

public class GuiLoom extends GuiContainer {

	private static final ResourceLocation TEXTURE = ModernGuiTextures.container("loom");
	private static final ResourceLocation BANNER_BASE = new ResourceLocation("textures/entity/banner_base.png");
	private static final int GRID_X = 60, GRID_Y = 13, COLUMNS = 4, ROWS = 4, TILE = 14;
	private static final int SCROLL_X = 119, SCROLL_Y = 13, SCROLL_TRAVEL = 41, SCROLLER_H = 15;

	private final ContainerLoom container;
	private boolean textured;
	private int startRow;
	private float scrollOffs;
	private boolean scrolling;

	public GuiLoom(ContainerLoom container) {
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

	private int hiddenRows() {
		return Math.max(0, (container.getPatterns().size() + COLUMNS - 1) / COLUMNS - ROWS);
	}

	@Override
	protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
		fontRendererObj.drawString(I18n.format("container.etfuturum.loom"), 8, 4, 0x404040);
		fontRendererObj.drawString(I18n.format("container.inventory"), 8, ySize - 94, 0x404040);
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		int left = guiLeft, top = guiTop;
		boolean showPatterns = !container.getPatterns().isEmpty() && !container.hasMaxPatterns();
		int scrollerY = top + SCROLL_Y + (int) (SCROLL_TRAVEL * scrollOffs);

		if (textured) {
			mc.getTextureManager().bindTexture(TEXTURE);
			drawTexturedModalRect(left, top, 0, 0, xSize, ySize);
			// Empty slot hints: banner, dye, pattern
			for (int i = 0; i < 3; i++) {
				Slot slot = container.getSlot(i);
				if (!slot.getHasStack()) {
					drawTexturedModalRect(left + slot.xDisplayPosition, top + slot.yDisplayPosition, xSize + 16 * i, 0, 16, 16);
				}
			}
			drawTexturedModalRect(left + SCROLL_X, scrollerY, 232 + (hiddenRows() > 0 ? 0 : 12), 0, 12, SCROLLER_H);
			if (container.hasMaxPatterns()) {
				Slot result = container.getSlot(3);
				drawTexturedModalRect(left + result.xDisplayPosition - 2, top + result.yDisplayPosition - 2, xSize, 17, 17, 16);
			}
		} else {
			GuiPanel.panel(left, top, xSize, ySize);
			GuiPanel.allSlots(container, left, top);
			GuiPanel.sunken(left + GRID_X - 1, top + GRID_Y - 1, COLUMNS * TILE + 2, ROWS * TILE + 2);
			GuiPanel.sunken(left + SCROLL_X - 1, top + SCROLL_Y - 1, 14, SCROLL_TRAVEL + SCROLLER_H + 2);
			GuiPanel.raised(left + SCROLL_X, scrollerY, 12, SCROLLER_H, false, hiddenRows() == 0);
		}

		if (!showPatterns) return;

		List<EnumBannerPattern> patterns = container.getPatterns();
		ItemStack banner = container.getSlot(0).getStack();
		int baseRGB = banner == null ? 0xFFFFFF : EnumColor.fromDamage(TileEntityBanner.getBaseColor(banner)).getRGB();
		int dye = ContainerLoom.getDyeColor(container.getSlot(1).getStack());
		int patternRGB = dye < 0 ? 0xFFFFFF : EnumColor.fromDamage(dye).getRGB();

		int first = startRow * COLUMNS;
		for (int i = first; i < Math.min(patterns.size(), first + ROWS * COLUMNS); i++) {
			int tx = left + GRID_X + (i - first) % COLUMNS * TILE;
			int ty = top + GRID_Y + (i - first) / COLUMNS * TILE;
			boolean hovered = mouseX >= tx && mouseY >= ty && mouseX < tx + TILE && mouseY < ty + TILE;
			boolean selected = i == container.getSelectedPattern();
			GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
			if (textured) {
				mc.getTextureManager().bindTexture(TEXTURE);
				drawTexturedModalRect(tx, ty, 0, ySize + (selected ? TILE : hovered ? TILE * 2 : 0), TILE, TILE);
			} else {
				GuiPanel.raised(tx, ty, TILE, TILE, hovered, selected);
			}
			drawPatternPreview(tx + 4.5D, ty + 2, patterns.get(i), baseRGB, patternRGB);
		}
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
	}

	/**
	 * Draws the front of a small banner (5x10) with the pattern on it.
	 */
	private void drawPatternPreview(double x, double y, EnumBannerPattern pattern, int baseRGB, int patternRGB) {
		GL11.glEnable(GL11.GL_BLEND);
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		drawBannerFront(BANNER_BASE, x, y, baseRGB);
		drawBannerFront(new ResourceLocation("textures/entity/banner/" + pattern.getPatternName() + ".png"), x, y, patternRGB);
		GL11.glDisable(GL11.GL_BLEND);
	}

	private void drawBannerFront(ResourceLocation texture, double x, double y, int rgb) {
		mc.getTextureManager().bindTexture(texture);
		GL11.glColor4f((rgb >> 16 & 255) / 255.0F, (rgb >> 8 & 255) / 255.0F, (rgb & 255) / 255.0F, 1.0F);
		// Front face of the 64x64 banner texture: (1, 1) to (21, 41)
		double u0 = 1 / 64.0D, v0 = 1 / 64.0D, u1 = 21 / 64.0D, v1 = 41 / 64.0D;
		Tessellator t = Tessellator.instance;
		t.startDrawingQuads();
		t.addVertexWithUV(x, y + 10, zLevel, u0, v1);
		t.addVertexWithUV(x + 5, y + 10, zLevel, u1, v1);
		t.addVertexWithUV(x + 5, y, zLevel, u1, v0);
		t.addVertexWithUV(x, y, zLevel, u0, v0);
		t.draw();
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		super.drawScreen(mouseX, mouseY, partialTicks);
		List<EnumBannerPattern> patterns = container.getPatterns();
		if (container.hasMaxPatterns()) return;
		int first = startRow * COLUMNS;
		for (int i = first; i < Math.min(patterns.size(), first + ROWS * COLUMNS); i++) {
			int tx = guiLeft + GRID_X + (i - first) % COLUMNS * TILE;
			int ty = guiTop + GRID_Y + (i - first) / COLUMNS * TILE;
			if (mouseX >= tx && mouseY >= ty && mouseX < tx + TILE && mouseY < ty + TILE) {
				drawCreativeTabHoveringText(I18n.format("item.banner." + patterns.get(i).getPatternName() + ".white").replaceFirst("^\\S+\\s", ""), mouseX, mouseY);
			}
		}
	}

	@Override
	protected void mouseClicked(int mouseX, int mouseY, int button) {
		scrolling = false;
		if (!container.getPatterns().isEmpty() && !container.hasMaxPatterns()) {
			int first = startRow * COLUMNS;
			for (int i = first; i < Math.min(container.getPatterns().size(), first + ROWS * COLUMNS); i++) {
				int tx = guiLeft + GRID_X + (i - first) % COLUMNS * TILE;
				int ty = guiTop + GRID_Y + (i - first) / COLUMNS * TILE;
				if (mouseX >= tx && mouseY >= ty && mouseX < tx + TILE && mouseY < ty + TILE && container.enchantItem(mc.thePlayer, i)) {
					mc.getSoundHandler().playSound(PositionedSoundRecord.func_147674_a(new ResourceLocation("gui.button.press"), 1.0F));
					mc.playerController.sendEnchantPacket(container.windowId, i);
					return;
				}
			}
			int sx = guiLeft + SCROLL_X, sy = guiTop + SCROLL_Y;
			if (mouseX >= sx && mouseX < sx + 12 && mouseY >= sy && mouseY < sy + SCROLL_TRAVEL + SCROLLER_H) {
				scrolling = true;
			}
		}
		super.mouseClicked(mouseX, mouseY, button);
	}

	@Override
	protected void mouseClickMove(int mouseX, int mouseY, int button, long time) {
		if (scrolling && hiddenRows() > 0) {
			scrollOffs = Math.max(0, Math.min(1, (mouseY - guiTop - SCROLL_Y - SCROLLER_H / 2.0F) / SCROLL_TRAVEL));
			startRow = (int) (scrollOffs * hiddenRows() + 0.5D);
			return;
		}
		super.mouseClickMove(mouseX, mouseY, button, time);
	}

	@Override
	public void handleMouseInput() {
		super.handleMouseInput();
		int wheel = Mouse.getEventDWheel();
		int hidden = hiddenRows();
		if (wheel != 0 && hidden > 0) {
			startRow = Math.max(0, Math.min(hidden, startRow - Integer.signum(wheel)));
			scrollOffs = (float) startRow / hidden;
		}
	}

	@Override
	public void updateScreen() {
		super.updateScreen();
		int hidden = hiddenRows();
		if (startRow > hidden) {
			startRow = hidden;
			scrollOffs = hidden == 0 ? 0 : 1;
		}
	}
}
