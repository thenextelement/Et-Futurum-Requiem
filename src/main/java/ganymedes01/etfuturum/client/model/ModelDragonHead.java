package ganymedes01.etfuturum.client.model;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import org.lwjgl.opengl.GL11;

/**
 * Vanilla's dragon head model (1.9+), on the ender dragon texture.
 */
public class ModelDragonHead extends ModelBase {

	private final ModelRenderer head;
	private final ModelRenderer jaw;

	public ModelDragonHead() {
		textureWidth = 256;
		textureHeight = 256;
		setTextureOffset("mouth.jaw", 176, 65);
		setTextureOffset("head.upperlip", 176, 44);
		setTextureOffset("head.upperhead", 112, 30);
		setTextureOffset("head.scale", 0, 0);
		setTextureOffset("head.nostril", 112, 0);
		float f = -16.0F;
		head = new ModelRenderer(this, "head");
		head.addBox("upperlip", -6.0F, -1.0F, -8.0F + f, 12, 5, 16);
		head.addBox("upperhead", -8.0F, -8.0F, 6.0F + f, 16, 16, 16);
		head.mirror = true;
		head.addBox("scale", -5.0F, -12.0F, 12.0F + f, 2, 4, 6);
		head.addBox("nostril", -5.0F, -3.0F, -6.0F + f, 2, 2, 4);
		head.mirror = false;
		head.addBox("scale", 3.0F, -12.0F, 12.0F + f, 2, 4, 6);
		head.addBox("nostril", 3.0F, -3.0F, -6.0F + f, 2, 2, 4);
		jaw = new ModelRenderer(this, "mouth");
		jaw.setRotationPoint(0.0F, 4.0F, 8.0F + f);
		jaw.addBox("jaw", -6.0F, 0.0F, -16.0F, 12, 4, 16);
		head.addChild(jaw);
	}

	/**
	 * @param animation mouth animation ticks: the jaw opens and closes as this advances
	 */
	public void render(float rotationY, float animation) {
		jaw.rotateAngleX = (float) (Math.sin(animation * Math.PI * 0.2F) + 1.0D) * 0.2F;
		head.rotateAngleY = rotationY * ((float) Math.PI / 180F);
		head.rotateAngleX = 0;
		GL11.glTranslatef(0.0F, -0.374375F, 0.0F);
		GL11.glScalef(0.75F, 0.75F, 0.75F);
		head.render(0.0625F);
	}
}
