package ganymedes01.etfuturum.entities;

import ganymedes01.etfuturum.ModBlocks;
import ganymedes01.etfuturum.blocks.BlockPointedDripstone;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityFallingBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;

import java.util.ArrayList;

/**
 * A falling stalactite. The block is removed by whoever spawns this; it breaks into an item when it lands.
 */
public class EntityFallingDripstone extends EntityFallingBlock {

	private static final int META_WATCHER = 20;

	private int fallHurtMax = 40;
	private float fallHurtAmount = 6.0F;
	private boolean hurtEntities = true;

	public EntityFallingDripstone(World world) {
		super(world);
	}

	public EntityFallingDripstone(World world, double x, double y, double z, int meta) {
		super(world, x, y, z, ModBlocks.POINTED_DRIPSTONE.get(), meta);
		dataWatcher.updateObject(META_WATCHER, meta);
	}

	@Override
	protected void entityInit() {
		super.entityInit();
		dataWatcher.addObject(META_WATCHER, 0);
	}

	public int getDripstoneMeta() {
		return dataWatcher.getWatchableObjectInt(META_WATCHER);
	}

	@Override
	public void onUpdate() {
		Block block = ModBlocks.POINTED_DRIPSTONE.get();

		prevPosX = posX;
		prevPosY = posY;
		prevPosZ = posZ;
		++field_145812_b; // fallTime
		motionY -= 0.04D;
		moveEntity(motionX, motionY, motionZ);
		motionX *= 0.98D;
		motionY *= 0.98D;
		motionZ *= 0.98D;

		if (!worldObj.isRemote) {
			int j = MathHelper.floor_double(posY);
			if (onGround) {
				setDead();
				if (field_145813_c) { // shouldDropItem
					entityDropItem(new ItemStack(block, 1, 0), 0.0F);
				}
			} else if (field_145812_b > 100 && (j < 1 || j > 256) || field_145812_b > 600) {
				if (field_145813_c) {
					entityDropItem(new ItemStack(block, 1, 0), 0.0F);
				}
				setDead();
			}
		}
	}

	@Override
	protected void fall(float distance) {
		if (hurtEntities) {
			int i = MathHelper.ceiling_float_int(distance - 1.0F);
			if (i > 0) {
				float damage = Math.min(MathHelper.floor_float(i * fallHurtAmount), fallHurtMax);
				for (Entity entity : new ArrayList<Entity>(worldObj.getEntitiesWithinAABBExcludingEntity(this, boundingBox))) {
					entity.attackEntityFrom(BlockPointedDripstone.STALACTITE_DAMAGE, damage);
				}
			}
		}
	}

	@Override
	protected void writeEntityToNBT(NBTTagCompound tag) {
		super.writeEntityToNBT(tag);
		tag.setBoolean("HurtEntities", hurtEntities);
		tag.setFloat("FallHurtAmount", fallHurtAmount);
		tag.setInteger("FallHurtMax", fallHurtMax);
	}

	@Override
	protected void readEntityFromNBT(NBTTagCompound tag) {
		super.readEntityFromNBT(tag);
		hurtEntities = tag.getBoolean("HurtEntities");
		fallHurtAmount = tag.getFloat("FallHurtAmount");
		fallHurtMax = tag.getInteger("FallHurtMax");
		dataWatcher.updateObject(META_WATCHER, field_145814_a);
	}
}
