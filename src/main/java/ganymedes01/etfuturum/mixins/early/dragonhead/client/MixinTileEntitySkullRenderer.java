package ganymedes01.etfuturum.mixins.early.dragonhead.client;

import com.mojang.authlib.GameProfile;
import ganymedes01.etfuturum.client.renderer.tileentity.DragonHeadRenderer;
import net.minecraft.client.renderer.tileentity.TileEntitySkullRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TileEntitySkullRenderer.class)
public class MixinTileEntitySkullRenderer {

	/**
	 * Worn heads go through vanilla's renderer, which only knows types 0-4.
	 */
	@Inject(method = "func_152674_a", at = @At("HEAD"), cancellable = true, remap = false)
	private void etfuturum$renderDragonHead(float x, float y, float z, int meta, float rotation, int type, GameProfile profile, CallbackInfo ci) {
		if (type == DragonHeadRenderer.TYPE) {
			DragonHeadRenderer.render(x, y, z, meta, rotation, DragonHeadRenderer.wornAnimation);
			ci.cancel();
		}
	}
}
