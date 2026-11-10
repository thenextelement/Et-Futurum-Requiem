package ganymedes01.etfuturum.client.gui.modern;

import net.minecraft.client.gui.Gui;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;

/**
 * Draws vanilla-styled GUI pieces with plain rectangles, for when modern textures are unavailable.
 */
public final class GuiPanel {

	private static final int BACKGROUND = 0xFFC6C6C6;
	private static final int LIGHT = 0xFFFFFFFF;
	private static final int SHADOW = 0xFF555555;
	private static final int OUTLINE = 0xFF000000;
	private static final int SLOT = 0xFF8B8B8B;
	private static final int SLOT_SHADOW = 0xFF373737;

	private GuiPanel() {
	}

	public static void panel(int x, int y, int w, int h) {
		Gui.drawRect(x + 1, y, x + w - 1, y + h, OUTLINE);
		Gui.drawRect(x, y + 1, x + w, y + h - 1, OUTLINE);
		Gui.drawRect(x + 1, y + 1, x + w - 1, y + h - 1, LIGHT);
		Gui.drawRect(x + 3, y + 3, x + w - 1, y + h - 1, SHADOW);
		Gui.drawRect(x + 3, y + 3, x + w - 3, y + h - 3, BACKGROUND);
	}

	/**
	 * A sunken 18x18 slot frame around a slot whose item sits at (x, y).
	 */
	public static void slot(int x, int y) {
		sunken(x - 1, y - 1, 18, 18);
	}

	/**
	 * A bigger result slot like the crafting table output.
	 */
	public static void bigSlot(int x, int y) {
		sunken(x - 5, y - 5, 26, 26);
	}

	public static void sunken(int x, int y, int w, int h) {
		Gui.drawRect(x, y, x + w, y + h, LIGHT);
		Gui.drawRect(x, y, x + w - 1, y + h - 1, SLOT_SHADOW);
		Gui.drawRect(x + 1, y + 1, x + w - 1, y + h - 1, SLOT);
	}

	public static void raised(int x, int y, int w, int h, boolean highlighted, boolean pressed) {
		int face = pressed ? 0xFF7F9FCF : highlighted ? 0xFFDADADA : 0xFFB4B4B4;
		Gui.drawRect(x, y, x + w, y + h, OUTLINE);
		Gui.drawRect(x + 1, y + 1, x + w - 1, y + h - 1, pressed ? SHADOW : LIGHT);
		Gui.drawRect(x + 2, y + 2, x + w - 1, y + h - 1, SHADOW);
		Gui.drawRect(x + 2, y + 2, x + w - 2, y + h - 2, face);
	}

	/**
	 * Draws a frame for every slot of the container, offset by the GUI position.
	 */
	public static void allSlots(Container container, int guiLeft, int guiTop) {
		for (Object o : container.inventorySlots) {
			Slot slot = (Slot) o;
			slot(guiLeft + slot.xDisplayPosition, guiTop + slot.yDisplayPosition);
		}
	}

	/**
	 * A right-pointing progress arrow made of rectangles.
	 */
	public static void arrow(int x, int y, int color) {
		Gui.drawRect(x, y + 6, x + 16, y + 10, color);
		for (int i = 0; i < 7; i++) {
			Gui.drawRect(x + 16 + i, y + 1 + i, x + 17 + i, y + 15 - i, color);
		}
	}
}
