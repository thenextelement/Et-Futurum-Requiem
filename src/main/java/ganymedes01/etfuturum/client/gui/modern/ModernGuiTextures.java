package ganymedes01.etfuturum.client.gui.modern;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.util.ResourceLocation;

import java.io.IOException;

/**
 * Container textures read straight out of Mojang's 1.20.1 client jar, which AssetDirector downloads from Mojang
 * at runtime (nothing is redistributed). 1.20.1 is the last version where each container GUI is one texture sheet.
 * When the jar could not be fetched (offline, blocked download) the GUIs fall back to {@link GuiPanel} drawing.
 */
public final class ModernGuiTextures {

	public static final String ASSET_VERSION = "1.20.1";
	public static final String DOMAIN = "minecraft_" + ASSET_VERSION;

	private ModernGuiTextures() {
	}

	public static ResourceLocation container(String name) {
		return new ResourceLocation(DOMAIN, "textures/gui/container/" + name + ".png");
	}

	/**
	 * Checks whether the texture actually resolves. Call once when a GUI opens rather than every frame.
	 */
	public static boolean isAvailable(ResourceLocation texture) {
		try {
			Minecraft.getMinecraft().getResourceManager().getResource(texture);
			return true;
		} catch (IOException | RuntimeException e) {
			return false;
		}
	}

	/**
	 * Draws a texture region from a 256x256 sheet.
	 */
	public static void blit(Gui gui, int x, int y, int u, int v, int w, int h) {
		gui.drawTexturedModalRect(x, y, u, v, w, h);
	}
}
