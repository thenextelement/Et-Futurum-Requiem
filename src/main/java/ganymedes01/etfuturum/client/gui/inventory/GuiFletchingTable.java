package ganymedes01.etfuturum.client.gui.inventory;

import ganymedes01.etfuturum.client.gui.modern.GuiPanel;
import ganymedes01.etfuturum.inventory.ContainerFletchingTable;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.resources.I18n;
import net.minecraft.inventory.Slot;
import org.lwjgl.opengl.GL11;

/**
 * Vanilla never gave the fletching table a GUI, so this one is drawn in code in the vanilla style.
 */
public class GuiFletchingTable extends GuiContainer {

	private final ContainerFletchingTable container;

	public GuiFletchingTable(ContainerFletchingTable container) {
		super(container);
		this.container = container;
		xSize = 176;
		ySize = 166;
	}

	@Override
	protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
		fontRendererObj.drawString(I18n.format("container.etfuturum.fletching_table"), 8, 4, 0x404040);
		fontRendererObj.drawString(I18n.format("container.inventory"), 8, ySize - 94, 0x404040);
		if (!container.getSlot(0).getHasStack()) {
			fontRendererObj.drawString(I18n.format("container.etfuturum.fletching_table.arrows"), 10, 26, 0x707070);
		}
		if (!container.getSlot(1).getHasStack()) {
			fontRendererObj.drawString(I18n.format("container.etfuturum.fletching_table.potion"), 10, 52, 0x707070);
		}
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		int left = guiLeft, top = guiTop;
		GuiPanel.panel(left, top, xSize, ySize);
		for (Object o : container.inventorySlots) {
			Slot slot = (Slot) o;
			if (container.getSlot(2) == slot) {
				GuiPanel.bigSlot(left + slot.xDisplayPosition, top + slot.yDisplayPosition);
			} else {
				GuiPanel.slot(left + slot.xDisplayPosition, top + slot.yDisplayPosition);
			}
		}
		boolean ready = container.getSlot(2).getHasStack();
		GuiPanel.arrow(left + 90, top + 35, ready ? 0xFFFFFFFF : 0xFF8B8B8B);
	}
}
