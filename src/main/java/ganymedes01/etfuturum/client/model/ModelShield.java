package ganymedes01.etfuturum.client.model;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;

/**
 * Vanilla's shield model: a 12x22 plate with a handle on the back, on a 64x64 texture.
 */
public class ModelShield extends ModelBase {

	private final ModelRenderer plate;
	private final ModelRenderer handle;

	public ModelShield() {
		textureWidth = 64;
		textureHeight = 64;
		plate = new ModelRenderer(this, 0, 0);
		plate.addBox(-6.0F, -11.0F, -2.0F, 12, 22, 1, 0.0F);
		handle = new ModelRenderer(this, 26, 0);
		handle.addBox(-1.0F, -3.0F, -1.0F, 2, 6, 6, 0.0F);
	}

	public void render() {
		handle.render(0.0625F);
		plate.render(0.0625F);
	}
}
