package ganymedes01.etfuturum.client.renderer.item;

import ganymedes01.etfuturum.client.renderer.tileentity.DragonHeadRenderer;

import com.mojang.authlib.GameProfile;
import ganymedes01.etfuturum.client.OpenGLHelper;
import ganymedes01.etfuturum.client.renderer.tileentity.TileEntityFancySkullRenderer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTUtil;
import net.minecraftforge.client.IItemRenderer;
import net.minecraftforge.common.util.Constants;

public class ItemSkullRenderer implements IItemRenderer {

	@Override
	public boolean handleRenderType(ItemStack item, ItemRenderType type) {
		return type != ItemRenderType.FIRST_PERSON_MAP;
	}

	@Override
	public boolean shouldUseRenderHelper(ItemRenderType type, ItemStack item, ItemRendererHelper helper) {
		return true;
	}

	@Override
	public void renderItem(ItemRenderType type, ItemStack stack, Object... data) {
		GameProfile profile = stack.hasTagCompound() ? getGameProfile(stack) : null;

		switch (type) {
			case ENTITY:
				renderSkull(-0.25F, -0.5F, -0.5F, stack.getItemDamage(), profile);
				break;
			case EQUIPPED:
				renderSkull(0.5F, 0.0F, 0.0F, stack.getItemDamage(), profile);
				break;
			case EQUIPPED_FIRST_PERSON:
				renderSkull(0.5F, 0.35F, 0.25F, stack.getItemDamage(), profile);
				break;
			case INVENTORY:
				// The dragon head model is bigger than a mob head; keep it inside the slot
				float scale = stack.getItemDamage() == DragonHeadRenderer.TYPE ? 1.25F : 1.5F;
				OpenGLHelper.scale(scale, scale, scale);
				renderSkull(0.75F, 0.30F, 0.5F, stack.getItemDamage(), profile);
				break;
			default:
				break;
		}
	}

	private void renderSkull(float x, float y, float z, int meta, GameProfile name) {
		OpenGLHelper.pushMatrix();
		OpenGLHelper.translate(x, y, z);
		TileEntityFancySkullRenderer.instance.renderSkull(0, 0, 0, 0, 0, meta, name);
		OpenGLHelper.popMatrix();
	}

	private GameProfile getGameProfile(ItemStack stack) {
		GameProfile profile = null;

		if (stack.hasTagCompound()) {
			NBTTagCompound nbt = stack.getTagCompound();
			if (nbt.hasKey("SkullOwner", Constants.NBT.TAG_COMPOUND))
				profile = NBTUtil.func_152459_a(nbt.getCompoundTag("SkullOwner")); // readGameProfileFromNBT
			else if (nbt.hasKey("SkullOwner", Constants.NBT.TAG_STRING))
				profile = new GameProfile(null, nbt.getString("SkullOwner"));
		}

		return profile;
	}
}