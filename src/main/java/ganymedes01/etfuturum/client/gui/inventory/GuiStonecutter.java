package ganymedes01.etfuturum.client.gui.inventory;

import ganymedes01.etfuturum.client.gui.modern.GuiPanel;
import ganymedes01.etfuturum.client.gui.modern.ModernGuiTextures;
import ganymedes01.etfuturum.inventory.ContainerStonecutter;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import java.util.List;

public class GuiStonecutter extends GuiContainer {

	private static final ResourceLocation TEXTURE = ModernGuiTextures.container("stonecutter");
	private static final int LIST_X = 52, LIST_Y = 14, COLUMNS = 4, ROWS = 3, BUTTON_W = 16, BUTTON_H = 18;
	private static final int SCROLL_X = 119, SCROLL_Y = 15, SCROLL_TRACK = 54, SCROLLER_H = 15;

	private final ContainerStonecutter container;
	private boolean textured;
	private float scrollOffs;
	private int startRow;
	private boolean scrolling;

	public GuiStonecutter(ContainerStonecutter container) {
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
		return Math.max(0, (container.getRecipes().size() + COLUMNS - 1) / COLUMNS - ROWS);
	}

	@Override
	protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
		fontRendererObj.drawString(I18n.format("container.etfuturum.stonecutter"), 8, 4, 0x404040);
		fontRendererObj.drawString(I18n.format("container.inventory"), 8, ySize - 94, 0x404040);
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		int left = guiLeft, top = guiTop;
		int scrollerY = top + SCROLL_Y + (int) ((SCROLL_TRACK - SCROLLER_H - 0) * scrollOffs);
		boolean scrollActive = hiddenRows() > 0;

		if (textured) {
			mc.getTextureManager().bindTexture(TEXTURE);
			drawTexturedModalRect(left, top, 0, 0, xSize, ySize);
			drawTexturedModalRect(left + SCROLL_X, scrollerY, 176 + (scrollActive ? 0 : 12), 0, 12, SCROLLER_H);
		} else {
			GuiPanel.panel(left, top, xSize, ySize);
			GuiPanel.allSlots(container, left, top);
			GuiPanel.sunken(left + LIST_X - 1, top + LIST_Y - 1, COLUMNS * BUTTON_W + 2, ROWS * BUTTON_H + 2);
			GuiPanel.sunken(left + SCROLL_X - 1, top + SCROLL_Y - 1, 14, SCROLL_TRACK);
			GuiPanel.raised(left + SCROLL_X, scrollerY, 12, SCROLLER_H, false, !scrollActive);
		}

		List<ItemStack> recipes = container.getRecipes();
		int first = startRow * COLUMNS;
		int last = Math.min(recipes.size(), first + ROWS * COLUMNS);
		for (int i = first; i < last; i++) {
			int bx = left + LIST_X + (i - first) % COLUMNS * BUTTON_W;
			int by = top + LIST_Y + (i - first) / COLUMNS * BUTTON_H;
			boolean hovered = mouseX >= bx && mouseY >= by && mouseX < bx + BUTTON_W && mouseY < by + BUTTON_H;
			boolean selected = i == container.getSelectedRecipe();
			if (textured) {
				mc.getTextureManager().bindTexture(TEXTURE);
				drawTexturedModalRect(bx, by, 0, ySize + (selected ? BUTTON_H : hovered ? BUTTON_H * 2 : 0), BUTTON_W, BUTTON_H);
			} else {
				GuiPanel.raised(bx, by, BUTTON_W, BUTTON_H, hovered, selected);
			}
		}

		RenderHelper.enableGUIStandardItemLighting();
		GL11.glEnable(GL12.GL_RESCALE_NORMAL);
		for (int i = first; i < last; i++) {
			int bx = left + LIST_X + (i - first) % COLUMNS * BUTTON_W;
			int by = top + LIST_Y + (i - first) / COLUMNS * BUTTON_H + 1;
			itemRender.renderItemAndEffectIntoGUI(fontRendererObj, mc.getTextureManager(), recipes.get(i), bx, by);
			itemRender.renderItemOverlayIntoGUI(fontRendererObj, mc.getTextureManager(), recipes.get(i), bx, by);
		}
		RenderHelper.disableStandardItemLighting();
		GL11.glDisable(GL12.GL_RESCALE_NORMAL);
		GL11.glDisable(GL11.GL_LIGHTING);
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		super.drawScreen(mouseX, mouseY, partialTicks);
		List<ItemStack> recipes = container.getRecipes();
		int first = startRow * COLUMNS;
		for (int i = first; i < Math.min(recipes.size(), first + ROWS * COLUMNS); i++) {
			int bx = guiLeft + LIST_X + (i - first) % COLUMNS * BUTTON_W;
			int by = guiTop + LIST_Y + (i - first) / COLUMNS * BUTTON_H;
			if (mouseX >= bx && mouseY >= by && mouseX < bx + BUTTON_W && mouseY < by + BUTTON_H) {
				renderToolTip(recipes.get(i), mouseX, mouseY);
			}
		}
	}

	@Override
	protected void mouseClicked(int mouseX, int mouseY, int button) {
		scrolling = false;
		if (container.hasInput()) {
			int first = startRow * COLUMNS;
			for (int i = first; i < Math.min(container.getRecipes().size(), first + ROWS * COLUMNS); i++) {
				int bx = guiLeft + LIST_X + (i - first) % COLUMNS * BUTTON_W;
				int by = guiTop + LIST_Y + (i - first) / COLUMNS * BUTTON_H;
				if (mouseX >= bx && mouseY >= by && mouseX < bx + BUTTON_W && mouseY < by + BUTTON_H && container.enchantItem(mc.thePlayer, i)) {
					mc.getSoundHandler().playSound(PositionedSoundRecord.func_147674_a(new ResourceLocation("gui.button.press"), 1.0F));
					mc.playerController.sendEnchantPacket(container.windowId, i);
					return;
				}
			}
			int sx = guiLeft + SCROLL_X, sy = guiTop + SCROLL_Y;
			if (mouseX >= sx && mouseX < sx + 12 && mouseY >= sy && mouseY < sy + SCROLL_TRACK) {
				scrolling = true;
			}
		}
		super.mouseClicked(mouseX, mouseY, button);
	}

	@Override
	protected void mouseClickMove(int mouseX, int mouseY, int button, long time) {
		if (scrolling && hiddenRows() > 0) {
			int top = guiTop + SCROLL_Y;
			scrollOffs = Math.max(0, Math.min(1, (mouseY - top - SCROLLER_H / 2.0F) / (SCROLL_TRACK - SCROLLER_H)));
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
