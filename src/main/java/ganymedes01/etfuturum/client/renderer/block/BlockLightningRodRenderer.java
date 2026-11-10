package ganymedes01.etfuturum.client.renderer.block;

import com.gtnewhorizons.angelica.api.ThreadSafeISBRH;
import cpw.mods.fml.client.registry.ISimpleBlockRenderingHandler;
import ganymedes01.etfuturum.blocks.BlockLightningRod;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.common.util.ForgeDirection;

@ThreadSafeISBRH(perThread = false)
public class BlockLightningRodRenderer implements ISimpleBlockRenderingHandler {

	private static final double[] TIP_UV = {0, 0, 4, 4};
	private static final double[] SHAFT_SIDE_UV = {0, 4, 2, 16};
	private static final double[] SHAFT_END_UV = {0, 4, 2, 6};

	private final int renderID;

	public BlockLightningRodRenderer(int renderID) {
		this.renderID = renderID;
	}

	private static void draw(OrientedBoxRenderer boxes) {
		boxes.box(6, 12, 6, 10, 16, 10, TIP_UV, TIP_UV);
		boxes.box(7, 0, 7, 9, 12, 9, SHAFT_SIDE_UV, SHAFT_END_UV);
	}

	@Override
	public void renderInventoryBlock(Block block, int metadata, int modelId, RenderBlocks renderer) {
		IIcon icon = renderer.getBlockIconFromSideAndMetadata(block, 0, 0);
		Tessellator t = Tessellator.instance;
		t.startDrawingQuads();
		draw(new OrientedBoxRenderer().begin(-0.5, -0.5, -0.5, ForgeDirection.UP, icon, false));
		t.draw();
	}

	@Override
	public boolean renderWorldBlock(IBlockAccess world, int x, int y, int z, Block block, int modelId, RenderBlocks renderer) {
		int meta = world.getBlockMetadata(x, y, z);
		IIcon icon = renderer.hasOverrideBlockTexture() ? renderer.overrideBlockTexture : renderer.getBlockIconFromSideAndMetadata(block, 0, meta);
		Tessellator.instance.setBrightness(BlockLightningRod.isPowered(meta) ? 0xF000F0 : block.getMixedBrightnessForBlock(world, x, y, z));
		draw(new OrientedBoxRenderer().begin(x, y, z, BlockLightningRod.getFacing(meta), icon, true));
		return true;
	}

	@Override
	public boolean shouldRender3DInInventory(int modelId) {
		return true;
	}

	@Override
	public int getRenderId() {
		return renderID;
	}
}
