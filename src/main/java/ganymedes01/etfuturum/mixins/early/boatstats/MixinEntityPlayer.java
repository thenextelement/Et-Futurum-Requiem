package ganymedes01.etfuturum.mixins.early.boatstats;

import ganymedes01.etfuturum.entities.EntityNewBoat;
import ganymedes01.etfuturum.entities.EntityNewBoatSeat;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.stats.StatList;
import net.minecraft.util.MathHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityPlayer.class)
public class MixinEntityPlayer {

	/**
	 * Vanilla only counts "Distance by Boat" when riding an EntityBoat. Our boats (and the passenger seat) extend
	 * Entity, so count them here. ServerUtilities leaderboards read this same vanilla stat.
	 */
	@Inject(method = "addMountedMovementStat", at = @At("HEAD"), cancellable = true)
	private void etfuturum$countNewBoatDistance(double dx, double dy, double dz, CallbackInfo ci) {
		EntityPlayer player = (EntityPlayer) (Object) this;
		if (player.ridingEntity instanceof EntityNewBoat || player.ridingEntity instanceof EntityNewBoatSeat) {
			int distance = Math.round(MathHelper.sqrt_double(dx * dx + dy * dy + dz * dz) * 100.0F);
			if (distance > 0) {
				player.addStat(StatList.distanceByBoatStat, distance);
			}
			ci.cancel();
		}
	}
}
