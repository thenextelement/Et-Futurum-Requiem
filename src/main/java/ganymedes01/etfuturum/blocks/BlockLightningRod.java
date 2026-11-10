package ganymedes01.etfuturum.blocks;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import ganymedes01.etfuturum.client.sound.ModSounds;
import ganymedes01.etfuturum.lib.RenderIDs;
import ganymedes01.etfuturum.tileentities.TileEntityLightningRod;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

import java.util.Random;

/**
 * Metadata: bits 0-2 are the facing (ForgeDirection ordinal), bit 3 is set while powered by a lightning strike.
 */
public class BlockLightningRod extends BaseBlock {

	public static final int POWERED = 8;
	private static final int POWER_TICKS = 8;

	@SideOnly(Side.CLIENT)
	private IIcon poweredIcon;

	public BlockLightningRod() {
		super(Material.iron);
		setHardness(3);
		setResistance(6);
		setHarvestLevel("pickaxe", 1);
		setNames("lightning_rod");
		setBlockSound(ModSounds.soundCopper);
	}

	public static ForgeDirection getFacing(int meta) {
		return ForgeDirection.getOrientation(meta & 7);
	}

	public static boolean isPowered(int meta) {
		return (meta & POWERED) != 0;
	}

	@Override
	public boolean hasTileEntity(int metadata) {
		return true;
	}

	@Override
	public TileEntity createTileEntity(World world, int metadata) {
		return new TileEntityLightningRod();
	}

	@Override
	public int onBlockPlaced(World world, int x, int y, int z, int side, float hitX, float hitY, float hitZ, int meta) {
		ForgeDirection dir = ForgeDirection.getOrientation(side);
		int bx = x - dir.offsetX, by = y - dir.offsetY, bz = z - dir.offsetZ;
		if (world.getBlock(bx, by, bz) == this && (world.getBlockMetadata(bx, by, bz) & 7) == side) {
			dir = dir.getOpposite();
		}
		return dir.ordinal();
	}

	/**
	 * Called when lightning strikes this rod.
	 */
	public void onLightningStrike(World world, int x, int y, int z) {
		int meta = world.getBlockMetadata(x, y, z);
		world.setBlockMetadataWithNotify(x, y, z, meta | POWERED, 3);
		notifySupport(world, x, y, z, meta);
		world.scheduleBlockUpdate(x, y, z, this, POWER_TICKS);
	}

	@Override
	public void updateTick(World world, int x, int y, int z, Random rand) {
		int meta = world.getBlockMetadata(x, y, z);
		if (isPowered(meta)) {
			world.setBlockMetadataWithNotify(x, y, z, meta & ~POWERED, 3);
			notifySupport(world, x, y, z, meta);
		}
	}

	private void notifySupport(World world, int x, int y, int z, int meta) {
		ForgeDirection dir = getFacing(meta);
		world.notifyBlocksOfNeighborChange(x, y, z, this);
		world.notifyBlocksOfNeighborChange(x - dir.offsetX, y - dir.offsetY, z - dir.offsetZ, this);
	}

	@Override
	public boolean canProvidePower() {
		return true;
	}

	@Override
	public int isProvidingWeakPower(IBlockAccess world, int x, int y, int z, int side) {
		return isPowered(world.getBlockMetadata(x, y, z)) ? 15 : 0;
	}

	@Override
	public int isProvidingStrongPower(IBlockAccess world, int x, int y, int z, int side) {
		int meta = world.getBlockMetadata(x, y, z);
		return isPowered(meta) && (meta & 7) == side ? 15 : 0;
	}

	@Override
	public int damageDropped(int meta) {
		return 0;
	}

	@Override
	public void setBlockBoundsBasedOnState(IBlockAccess world, int x, int y, int z) {
		switch (getFacing(world.getBlockMetadata(x, y, z))) {
			case EAST, WEST -> setBlockBounds(0.0F, 0.375F, 0.375F, 1.0F, 0.625F, 0.625F);
			case NORTH, SOUTH -> setBlockBounds(0.375F, 0.375F, 0.0F, 0.625F, 0.625F, 1.0F);
			default -> setBlockBounds(0.375F, 0.0F, 0.375F, 0.625F, 1.0F, 0.625F);
		}
	}

	@Override
	public void setBlockBoundsForItemRender() {
		setBlockBounds(0.375F, 0.0F, 0.375F, 0.625F, 1.0F, 0.625F);
	}

	@Override
	public AxisAlignedBB getCollisionBoundingBoxFromPool(World world, int x, int y, int z) {
		setBlockBoundsBasedOnState(world, x, y, z);
		return super.getCollisionBoundingBoxFromPool(world, x, y, z);
	}

	@Override
	public AxisAlignedBB getSelectedBoundingBoxFromPool(World world, int x, int y, int z) {
		setBlockBoundsBasedOnState(world, x, y, z);
		return super.getSelectedBoundingBoxFromPool(world, x, y, z);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerBlockIcons(IIconRegister reg) {
		blockIcon = reg.registerIcon(getTextureName());
		poweredIcon = reg.registerIcon(getTextureName() + "_on");
	}

	@Override
	@SideOnly(Side.CLIENT)
	public IIcon getIcon(int side, int meta) {
		return isPowered(meta) ? poweredIcon : blockIcon;
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
	public int getRenderType() {
		return RenderIDs.LIGHTNING_ROD;
	}
}
