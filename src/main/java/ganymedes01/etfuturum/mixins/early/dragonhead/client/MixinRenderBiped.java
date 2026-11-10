package ganymedes01.etfuturum.mixins.early.dragonhead.client;

import ganymedes01.etfuturum.client.renderer.tileentity.DragonHeadRenderer;
import net.minecraft.entity.EntityLiving;
import net.minecraft.client.renderer.entity.RenderBiped;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(RenderBiped.class)
public class MixinRenderBiped {

	/**
	 * A worn dragon head's mouth moves with the wearer's walking, like vanilla.
	 */
	@Inject(method = "renderEquippedItems(Lnet/minecraft/entity/EntityLiving;F)V", at = @At("HEAD"))
	private void etfuturum$dragonHeadWalk(EntityLiving entity, float partialTicks, CallbackInfo ci) {
		DragonHeadRenderer.wornAnimation = entity.limbSwing - entity.limbSwingAmount * (1.0F - partialTicks);
	}
}
