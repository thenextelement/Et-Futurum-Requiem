package ganymedes01.etfuturum.mixins.early.shield.client;

import ganymedes01.etfuturum.items.equipment.ItemShield;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ModelBiped.class)
public class MixinModelBiped {

	@Shadow
	public ModelRenderer bipedRightArm;

	@Shadow
	public int heldItemRight;

	/**
	 * Modern's shield block pose also turns the raised arm 30 degrees in; 1.7.10's block pose only raises it.
	 */
	@Inject(method = "setRotationAngles", at = @At("TAIL"))
	private void etfuturum$shieldBlockArm(float f, float f1, float f2, float f3, float f4, float f5, Entity entity, CallbackInfo ci) {
		if (heldItemRight == 3 && entity instanceof EntityPlayer player && player.getItemInUse() != null
				&& player.getItemInUse().getItem() instanceof ItemShield && player.getItemInUse() == player.getCurrentEquippedItem()) {
			bipedRightArm.rotateAngleY = -(float) Math.PI / 6.0F;
		}
	}
}
