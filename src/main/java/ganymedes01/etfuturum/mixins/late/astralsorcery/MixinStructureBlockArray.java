package ganymedes01.etfuturum.mixins.late.astralsorcery;

import cpw.mods.fml.common.registry.GameRegistry;
import ganymedes01.etfuturum.compat.PrismarineCompat;
import net.minecraft.block.Block;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Shrine lights use Botania's sea lamp when Botania's prismarine wins, instead of EFR's sea lantern. */
@Pseudo
@Mixin(targets = "hellfirepvp.astralsorcery.common.util.struct.StructureBlockArray", remap = false)
public abstract class MixinStructureBlockArray {

	@Inject(method = "getShrineLightBlock", at = @At("HEAD"), cancellable = true, remap = false)
	private static void etfu$botaniaSeaLamp(CallbackInfoReturnable<Block> cir) {
		if (PrismarineCompat.botaniaPrismarine()) {
			Block lamp = GameRegistry.findBlock("Botania", "seaLamp");
			if (lamp != null) {
				cir.setReturnValue(lamp);
			}
		}
	}
}
