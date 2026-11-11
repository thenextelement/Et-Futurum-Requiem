package ganymedes01.etfuturum.core.handlers;

import ganymedes01.etfuturum.lib.Reference;
import cpw.mods.fml.common.eventhandler.EventPriority;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import ganymedes01.etfuturum.compat.CompatBackhand;
import ganymedes01.etfuturum.items.equipment.ItemShield;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemAxe;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;
import net.minecraftforge.event.entity.living.LivingAttackEvent;

/**
 * Shield blocking. Works for a shield in either hand: with Backhand, a raised offhand shield is the item in use.
 */
public class ShieldHandler {

	public static final ShieldHandler INSTANCE = new ShieldHandler();
	public static final int AXE_DISABLE_TICKS = 100;

	/**
	 * The raised shield of this player, or null when they aren't blocking with one.
	 */
	public static ItemStack getActiveShield(EntityPlayer player) {
		ItemStack stack = player.getItemInUse();
		if (stack == null || !(stack.getItem() instanceof ItemShield)) return null;
		if (stack.getMaxItemUseDuration() - player.getItemInUseCount() < ItemShield.RAISE_TICKS) return null;
		return stack;
	}

	/**
	 * Vanilla's rule: blockable damage whose source is in front of the player.
	 */
	public static boolean canBlock(EntityPlayer player, DamageSource source) {
		if (source.isUnblockable() || source.isFireDamage() || source.isMagicDamage() || getActiveShield(player) == null) return false;
		Entity from = source.getSourceOfDamage();
		if (from == null) return false;
		Vec3 look = player.getLook(1.0F);
		double dx = from.posX - player.posX, dz = from.posZ - player.posZ;
		return dx * look.xCoord + dz * look.zCoord > 0.0D;
	}

	@SubscribeEvent(priority = EventPriority.HIGH)
	public void onLivingAttack(LivingAttackEvent event) {
		if (!(event.entityLiving instanceof EntityPlayer player) || player.worldObj.isRemote || event.ammount <= 0) return;
		if (!canBlock(player, event.source)) return;
		event.setCanceled(true);

		ItemStack shield = getActiveShield(player);
		player.worldObj.playSoundAtEntity(player, Reference.MCAssetVer + ":item.shield.block", 1.0F, 0.8F + player.worldObj.rand.nextFloat() * 0.4F);

		Entity direct = event.source.getSourceOfDamage();
		if (direct instanceof EntityLivingBase attacker) {
			attacker.knockBack(player, 0.5F, player.posX - attacker.posX, player.posZ - attacker.posZ);
			ItemStack weapon = attacker.getHeldItem();
			if (weapon != null && weapon.getItem() instanceof ItemAxe) {
				ItemShield.disable(shield, player.worldObj, AXE_DISABLE_TICKS);
				player.stopUsingItem();
				player.worldObj.playSoundAtEntity(player, Reference.MCAssetVer + ":item.shield.break", 0.8F, 0.8F + player.worldObj.rand.nextFloat() * 0.4F);
			}
		}

		if (event.ammount >= 3.0F) {
			shield.damageItem(1 + MathHelper.floor_float(event.ammount), player);
			if (shield.stackSize <= 0) {
				removeBrokenShield(player, shield);
				player.worldObj.playSoundAtEntity(player, Reference.MCAssetVer + ":item.shield.break", 0.8F, 0.8F + player.worldObj.rand.nextFloat() * 0.4F);
			}
		}
	}

	private static void removeBrokenShield(EntityPlayer player, ItemStack shield) {
		player.clearItemInUse();
		if (player.getCurrentEquippedItem() == shield) {
			player.destroyCurrentEquippedItem();
		} else if (CompatBackhand.isLoaded() && CompatBackhand.getOffhandItem(player) == shield) {
			CompatBackhand.setOffhandItem(player, null);
		}
	}
}
