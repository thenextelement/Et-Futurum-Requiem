package ganymedes01.etfuturum.client.renderer.tileentity;

import ganymedes01.etfuturum.client.model.ModelDragonHead;
import net.minecraft.client.Minecraft;
import net.minecraft.tileentity.TileEntitySkull;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import java.util.Map;
import java.util.WeakHashMap;

/**
 * Dragon heads (skull type 5), placed, held or worn. Like vanilla, a placed head's mouth moves while it is powered and
 * stays where it stopped otherwise; a worn one moves with the wearer's walking.
 */
public final class DragonHeadRenderer {

	public static final int TYPE = 5;
	private static final ResourceLocation TEXTURE = new ResourceLocation("textures/entity/enderdragon/dragon.png");
	private static final ModelDragonHead MODEL = new ModelDragonHead();
	private static final Map<TileEntitySkull, double[]> MOUTH = new WeakHashMap<>();

	/**
	 * Walk animation of the entity whose worn head is being drawn, set around RenderBiped/RenderPlayer's head pass.
	 */
	public static float wornAnimation;

	private DragonHeadRenderer() {
	}

	/**
	 * Same placement rules as the other heads: meta 1 on the floor, 2-5 on a wall.
	 */
	public static void render(float x, float y, float z, int meta, float rotation, float animation) {
		Minecraft.getMinecraft().getTextureManager().bindTexture(TEXTURE);
		GL11.glPushMatrix();
		GL11.glDisable(GL11.GL_CULL_FACE);
		if (meta != 1) {
			switch (meta) {
				case 2 -> GL11.glTranslatef(x + 0.5F, y + 0.25F, z + 0.74F);
				case 3 -> {
					GL11.glTranslatef(x + 0.5F, y + 0.25F, z + 0.26F);
					rotation = 180.0F;
				}
				case 4 -> {
					GL11.glTranslatef(x + 0.74F, y + 0.25F, z + 0.5F);
					rotation = 270.0F;
				}
				default -> {
					GL11.glTranslatef(x + 0.26F, y + 0.25F, z + 0.5F);
					rotation = 90.0F;
				}
			}
		} else {
			GL11.glTranslatef(x + 0.5F, y, z + 0.5F);
		}
		GL11.glEnable(GL12.GL_RESCALE_NORMAL);
		GL11.glScalef(-1.0F, -1.0F, 1.0F);
		GL11.glEnable(GL11.GL_ALPHA_TEST);
		MODEL.render(rotation, animation);
		GL11.glPopMatrix();
	}

	/**
	 * Advances a placed head's mouth while it is powered.
	 */
	public static float animation(TileEntitySkull skull, float partialTicks) {
		if (skull.getWorldObj() == null) return 0;
		double[] state = MOUTH.computeIfAbsent(skull, k -> new double[]{0, skull.getWorldObj().getTotalWorldTime() + partialTicks});
		double now = skull.getWorldObj().getTotalWorldTime() + partialTicks;
		if (skull.getWorldObj().isBlockIndirectlyGettingPowered(skull.xCoord, skull.yCoord, skull.zCoord)) {
			state[0] += Math.max(0, now - state[1]);
		}
		state[1] = now;
		return (float) state[0];
	}
}
