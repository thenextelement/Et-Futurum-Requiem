package ganymedes01.etfuturum.blocks;

import ganymedes01.etfuturum.ModBlocks;
import ganymedes01.etfuturum.entities.EntityFallingDripstone;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.util.MathHelper;
import java.util.Random;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import ganymedes01.etfuturum.EtFuturum;
import ganymedes01.etfuturum.client.sound.ModSounds;
import ganymedes01.etfuturum.core.utils.Utils;
import ganymedes01.etfuturum.lib.RenderIDs;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.DamageSource;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

public class BlockPointedDripstone extends Block {

	private IIcon[] downIcons;
	private IIcon[] upIcons;
	private static final int states = 5;

	public static final DamageSource STALACTITE_DAMAGE = new DamageSource("stalactite");
	public static final DamageSource STALAGMITE_DAMAGE = new DamageSource("stalagmite").setDamageBypassesArmor();

	private static final int MAX_STALACTITE_LENGTH = 7;
	private static final int MAX_CAULDRON_SEARCH = 11;
	private static final int MAX_STALAGMITE_SEARCH = 10;
	private static final float WATER_DRIP_CHANCE = 0.17578125F;
	private static final float LAVA_DRIP_CHANCE = 0.05859375F;
	private static final float GROW_CHANCE = 0.011377778F;

	public BlockPointedDripstone() {
		super(Material.rock);
		Utils.setBlockSound(this, ModSounds.soundPointedDripstone);
		this.setHardness(1.5F);
		this.setResistance(3F);
		this.setHarvestLevel("pickaxe", 0);
		this.setTickRandomly(true);
		this.setBlockName(Utils.getUnlocalisedName("pointed_dripstone"));
		this.setBlockTextureName("pointed_dripstone");
		this.setCreativeTab(EtFuturum.creativeTabBlocks);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerBlockIcons(IIconRegister reg) {
		downIcons = new IIcon[5];
		downIcons[0] = reg.registerIcon(getTextureName() + "_down_tip");
		downIcons[1] = reg.registerIcon(getTextureName() + "_down_frustum");
		downIcons[2] = reg.registerIcon(getTextureName() + "_down_middle");
		downIcons[3] = reg.registerIcon(getTextureName() + "_down_base");
		downIcons[4] = reg.registerIcon(getTextureName() + "_down_tip_merge");

		upIcons = new IIcon[5];
		upIcons[0] = reg.registerIcon(getTextureName() + "_up_tip");
		upIcons[1] = reg.registerIcon(getTextureName() + "_up_frustum");
		upIcons[2] = reg.registerIcon(getTextureName() + "_up_middle");
		upIcons[3] = reg.registerIcon(getTextureName() + "_up_base");
		upIcons[4] = reg.registerIcon(getTextureName() + "_up_tip_merge");

		this.blockIcon = downIcons[3];
	}

	@Override
	public int onBlockPlaced(World world, int x, int y, int z, int side, float subX, float subY, float subZ, int meta) {
		if (side < 2) {
			return side * states;
		}
		if (canDripstoneStayHere(world, x, y, z, -1)) {
			return states;
		}
		return 0;
	}

	@Override
	public boolean canPlaceBlockOnSide(World world, int x, int y, int z, int side) {
		int checkY = side == 0 || world.isSideSolid(x, y + 1, z, ForgeDirection.DOWN) ? 1 : -1;
		return canDripstoneStayHere(world, x, y, z, checkY);
	}

	@Override
	public void onNeighborBlockChange(World world, int x, int y, int z, Block block) {
		if (!canBlockStay(world, x, y, z)) {
			int meta = world.getBlockMetadata(x, y, z);
			world.setBlockToAir(x, y, z);
			if (isStalactite(meta) && !world.isRemote) {
				// Unsupported stalactites fall instead of popping off
				world.spawnEntityInWorld(new EntityFallingDripstone(world, x + 0.5D, y, z + 0.5D, meta));
			} else {
				this.dropBlockAsItem(world, x, y, z, 0, 0);
			}
			return;
		}
		setState(world, x, y, z);
	}

	private static boolean isStalactite(int meta) {
		return meta < states;
	}

	private static boolean isTip(int meta) {
		return meta % states == 0;
	}

	/**
	 * @return the Y of the block the stalactite ending at (x, tipY, z) hangs from, or -1 if it isn't a stalactite
	 */
	private int findStalactiteRoot(World world, int x, int tipY, int z) {
		int y = tipY;
		for (int i = 0; i <= MAX_STALACTITE_LENGTH + 4 && y < world.getHeight(); i++, y++) {
			if (world.getBlock(x, y, z) != this) return y;
			if (!isStalactite(world.getBlockMetadata(x, y, z))) return -1;
		}
		return -1;
	}

	/**
	 * The fluid source sitting on top of the stalactite's root block, if any.
	 */
	private Block getFluidAbove(World world, int x, int rootY, int z) {
		Block fluid = world.getBlock(x, rootY + 1, z);
		if (world.getBlockMetadata(x, rootY + 1, z) != 0) return null;
		if (fluid.getMaterial() == Material.water) return Blocks.water;
		if (fluid.getMaterial() == Material.lava) return Blocks.lava;
		return null;
	}

	@Override
	public void updateTick(World world, int x, int y, int z, Random rand) {
		int meta = world.getBlockMetadata(x, y, z);
		if (!isStalactite(meta) || !isTip(meta)) return;
		int rootY = findStalactiteRoot(world, x, y, z);
		if (rootY < 0) return;
		Block fluid = getFluidAbove(world, x, rootY, z);
		if (fluid == null) return;

		fillCauldronBelow(world, x, y, z, fluid, rand);

		if (fluid == Blocks.water && world.getBlock(x, rootY, z) == ModBlocks.DRIPSTONE_BLOCK.get() && rand.nextFloat() < GROW_CHANCE) {
			grow(world, x, y, z, rootY);
		}
	}

	private void fillCauldronBelow(World world, int x, int tipY, int z, Block fluid, Random rand) {
		for (int y = tipY - 1; y > tipY - MAX_CAULDRON_SEARCH && y > 0; y--) {
			Block below = world.getBlock(x, y, z);
			if (below.isAir(world, x, y, z)) continue;
			if (below == Blocks.cauldron) {
				int level = world.getBlockMetadata(x, y, z);
				if (fluid == Blocks.water && level < 3 && rand.nextFloat() < WATER_DRIP_CHANCE) {
					world.setBlockMetadataWithNotify(x, y, z, level + 1, 3);
				} else if (fluid == Blocks.lava && level == 0 && ModBlocks.LAVA_CAULDRON.isEnabled() && rand.nextFloat() < LAVA_DRIP_CHANCE) {
					world.setBlock(x, y, z, ModBlocks.LAVA_CAULDRON.get());
				}
			}
			return;
		}
	}

	private void grow(World world, int x, int tipY, int z, int rootY) {
		// Lengthen the stalactite first, then build up a stalagmite under it
		if (rootY - tipY < MAX_STALACTITE_LENGTH && world.isAirBlock(x, tipY - 1, z) && world.isAirBlock(x, tipY - 2, z)) {
			world.setBlock(x, tipY - 1, z, this, 0, 3);
			return;
		}
		for (int y = tipY - 1; y > tipY - MAX_STALAGMITE_SEARCH && y > 0; y--) {
			if (world.isAirBlock(x, y, z)) continue;
			Block floor = world.getBlock(x, y, z);
			if (floor == this) {
				int meta = world.getBlockMetadata(x, y, z);
				if (!isStalactite(meta) && isTip(meta) && world.isAirBlock(x, y + 1, z)) {
					world.setBlock(x, y + 1, z, this, states, 3);
				}
			} else if (world.isSideSolid(x, y, z, ForgeDirection.UP) && world.isAirBlock(x, y + 1, z)) {
				world.setBlock(x, y + 1, z, this, states, 3);
			}
			return;
		}
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void randomDisplayTick(World world, int x, int y, int z, Random rand) {
		int meta = world.getBlockMetadata(x, y, z);
		if (!isStalactite(meta) || !isTip(meta) || rand.nextInt(5) != 0) return;
		int rootY = findStalactiteRoot(world, x, y, z);
		if (rootY < 0) return;
		Block fluid = getFluidAbove(world, x, rootY, z);
		if (fluid != null) {
			world.spawnParticle(fluid == Blocks.lava ? "dripLava" : "dripWater", x + 0.5D, y + 0.0625D, z + 0.5D, 0, 0, 0);
		}
	}

	@Override
	public void onFallenUpon(World world, int x, int y, int z, Entity entity, float fallDistance) {
		int meta = world.getBlockMetadata(x, y, z);
		if (!isStalactite(meta) && isTip(meta)) {
			// Stalagmites double fall damage, like 1.17+
			int damage = MathHelper.ceiling_float_int((fallDistance + 2.0F - 3.0F) * 2.0F);
			if (damage > 0) {
				entity.attackEntityFrom(STALAGMITE_DAMAGE, damage);
				entity.fallDistance = 0;
			}
		}
	}

	@Override
	public AxisAlignedBB getCollisionBoundingBoxFromPool(World world, int x, int y, int z) {
		int meta = world.getBlockMetadata(x, y, z);
		if (meta % 5 == 4) meta = 0;
		float offset = 0.0625F + ((float) (2 - meta % states) * 0.125F);

		return AxisAlignedBB.getBoundingBox(x + offset, y, z + offset, x + (1 - offset), y + 1.0F, z + (1 - offset));
	}

	@Override
	public void setBlockBoundsBasedOnState(IBlockAccess access, int x, int y, int z) {
		int meta = access.getBlockMetadata(x, y, z);
		if (meta % 5 == 4) meta = 0;
		float offset = 0.0625F + ((float) (2 - meta % states) * 0.0625F);

		this.setBlockBounds(offset, 0, offset, 1 - offset, 1.0F, 1 - offset);
	}

	private void setState(World world, int x, int y, int z) {
		int meta = 0;
		boolean up = world.getBlockMetadata(x, y, z) > 4;
		for (meta = 0; meta < 2; meta++) {
			if (world.getBlockMetadata(x, y + (up ? 1 : -1) + (up ? meta : -meta), z) > 4 != up || world.getBlock(x, y + (up ? 1 : -1) + (up ? meta : -meta), z) != this) {
				break;
			}
		}
		if (meta % 5 == 0 && world.getBlock(x, y + (up ? 1 : -1), z) == this && world.getBlockMetadata(x, y + (up ? 1 : -1), z) > 4 != up) {
			meta = 4;
		}
		if (meta % 5 == 2 && world.getBlock(x, y + (up ? 1 : -1), z) == this && world.getBlockMetadata(x, y + (up ? 1 : -1), z) % 5 == 2
				&& world.getBlock(x, y + (up ? -1 : 1), z).isSideSolid(world, x, y + (up ? -1 : 1), z, up ? ForgeDirection.DOWN : ForgeDirection.UP)) {
			meta = 3;
		}
		if (up) meta += states;
		world.setBlockMetadataWithNotify(x, y, z, meta, 3);
	}

	@Override
	public boolean canBlockStay(World world, int x, int y, int z) {
		int checkY = world.getBlockMetadata(x, y, z) < states ? 1 : -1;
		return canDripstoneStayHere(world, x, y, z, checkY);
	}

	private boolean canDripstoneStayHere(World world, int x, int y, int z, int offset) {
		return world.isBlockNormalCubeDefault(x, y + offset, z, false) || (offset == (world.getBlockMetadata(x, y + offset, z) < states ? 1 : -1) && world.getBlock(x, y + offset, z) == this);
	}

	@Override
	public IIcon getIcon(int side, int meta) {
		return meta < states ? downIcons[meta % states] : upIcons[meta % states];
	}

	@Override
	public int getRenderType() {
		return RenderIDs.POINTED_DRIPSTONE;
	}

	@Override
	public boolean isOpaqueCube() {
		return false;
	}

	@Override
	public boolean renderAsNormalBlock() {
		return false;
	}

	@Override
	public String getItemIconName() {
		return "pointed_dripstone";
	}

}
