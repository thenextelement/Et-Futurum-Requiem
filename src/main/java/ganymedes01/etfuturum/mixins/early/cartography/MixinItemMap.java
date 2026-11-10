package ganymedes01.etfuturum.mixins.early.cartography;

import ganymedes01.etfuturum.core.utils.MapLocking;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemMap;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemMap.class)
public class MixinItemMap {

	/**
	 * Locked maps (from the cartography table) keep tracking players but are never redrawn.
	 */
	@Inject(method = "onUpdate", at = @At("HEAD"), cancellable = true)
	private void etfuturum$skipLockedMapUpdate(ItemStack stack, World world, Entity entity, int slot, boolean held, CallbackInfo ci) {
		if (MapLocking.isLocked(stack)) {
			if (!world.isRemote && entity instanceof EntityPlayer player) {
				((ItemMap) (Object) this).getMapData(stack, world).updateVisiblePlayers(player, stack);
			}
			ci.cancel();
		}
	}
}
