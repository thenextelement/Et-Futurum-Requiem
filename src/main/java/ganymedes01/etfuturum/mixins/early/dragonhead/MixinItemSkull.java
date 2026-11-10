package ganymedes01.etfuturum.mixins.early.dragonhead;

import net.minecraft.item.ItemSkull;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemSkull.class)
public class MixinItemSkull {

	@Shadow
	@Final
	@Mutable
	private static String[] skullTypes;

	@Shadow
	@Final
	@Mutable
	public static String[] field_94587_a;

	/**
	 * Adds the dragon head as skull type 5, its ID in modern versions. It has no flat icon, so the icon array points
	 * at the wither skull's; the item always renders as a model.
	 */
	@Inject(method = "<clinit>", at = @At("TAIL"))
	private static void etfuturum$addDragonHead(CallbackInfo ci) {
		skullTypes = new String[]{"skeleton", "wither", "zombie", "char", "creeper", "dragon"};
		field_94587_a = new String[]{"skeleton", "wither", "zombie", "steve", "creeper", "wither"};
	}
}
