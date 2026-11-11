package ganymedes01.etfuturum.client;

import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 * Shield face with banner layers, from the official 1.21 shield textures. Their pattern masks are grayscale with
 * black keyed out (tRNS), which older Java ImageIO doesn't apply, so black is treated as transparent here. Each mask
 * is tinted with the modern dye colours, like vanilla's banner shader.
 */
public class ShieldPatternTexture extends AbstractTexture {

	/**
	 * Modern dye texture colours, in wool meta order.
	 */
	public static final int[] DYE_COLORS = {0xF9FFFE, 0xF9801D, 0xC74EBD, 0x3AB3DA, 0xFED83D, 0x80C71F, 0xF38BAA, 0x474F52,
			0x9D9D97, 0x169C9C, 0x8932B8, 0x3C44AA, 0x835432, 0x5E7C16, 0xB02E26, 0x1D1D21};

	private final ResourceLocation base;
	private final List<ResourceLocation> masks;
	private final List<Integer> colors;

	public ShieldPatternTexture(ResourceLocation base, List<ResourceLocation> masks, List<Integer> woolColors) {
		this.base = base;
		this.masks = masks;
		this.colors = woolColors;
	}

	@Override
	public void loadTexture(IResourceManager manager) throws IOException {
		deleteGlTexture();
		BufferedImage out = copy(read(manager, base));
		for (int i = 0; i < masks.size() && i < colors.size(); i++) {
			BufferedImage mask = read(manager, masks.get(i));
			if (mask.getWidth() != out.getWidth() || mask.getHeight() != out.getHeight()) continue;
			int dye = DYE_COLORS[colors.get(i) & 15];
			for (int y = 0; y < out.getHeight(); y++) {
				for (int x = 0; x < out.getWidth(); x++) {
					int m = mask.getRGB(x, y);
					int gray = m & 0xFF;
					int alpha = gray == 0 ? 0 : m >>> 24;
					if (alpha == 0) continue;
					int r = ((dye >> 16) & 0xFF) * gray / 255, g = ((dye >> 8) & 0xFF) * gray / 255, b = (dye & 0xFF) * gray / 255;
					int under = out.getRGB(x, y);
					float a = alpha / 255.0F;
					r = (int) (r * a + ((under >> 16) & 0xFF) * (1 - a));
					g = (int) (g * a + ((under >> 8) & 0xFF) * (1 - a));
					b = (int) (b * a + (under & 0xFF) * (1 - a));
					int outA = Math.max(under >>> 24, alpha);
					out.setRGB(x, y, outA << 24 | r << 16 | g << 8 | b);
				}
			}
		}
		TextureUtil.uploadTextureImage(getGlTextureId(), out);
	}

	private static BufferedImage read(IResourceManager manager, ResourceLocation location) throws IOException {
		try (InputStream in = manager.getResource(location).getInputStream()) {
			return ImageIO.read(in);
		}
	}

	private static BufferedImage copy(BufferedImage src) {
		BufferedImage img = new BufferedImage(src.getWidth(), src.getHeight(), BufferedImage.TYPE_INT_ARGB);
		img.getGraphics().drawImage(src, 0, 0, null);
		return img;
	}
}
