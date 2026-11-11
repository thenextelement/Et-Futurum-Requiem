package ganymedes01.etfuturum.mixins.early.shield;

import ganymedes01.etfuturum.items.equipment.ItemShield;
import net.minecraft.entity.player.EntityPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(EntityPlayer.class)
public class MixinEntityPlayer {

	/**
	 * Shields use the block action for their pose, but must not get the sword's half damage: what a shield blocks is
	 * cancelled outright, and hits from behind go through in full.
	 */
	@Redirect(method = "damageEntity", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/EntityPlayer;isBlocking()Z"))
	private boolean etfuturum$noSwordBlockForShields(EntityPlayer player) {
		return player.isBlocking() && !(player.getItemInUse().getItem() instanceof ItemShield);
	}
}
