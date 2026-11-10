package ganymedes01.etfuturum.inventory;

import ganymedes01.etfuturum.Tags;
import ganymedes01.etfuturum.core.utils.MapLocking;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.inventory.Slot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraft.world.storage.MapData;

/**
 * Filled map + paper zooms out, + empty map clones, + glass pane locks.
 */
public class ContainerCartographyTable extends ContainerWorkbenchBase {

	public final InventoryCrafting inputs = new InventoryCrafting(this, 2, 1);

	public ContainerCartographyTable(InventoryPlayer inv, World world, int x, int y, int z) {
		super(world, x, y, z);
		addSlotToContainer(new Slot(inputs, 0, 15, 15) {
			@Override
			public boolean isItemValid(ItemStack stack) {
				return stack.getItem() == Items.filled_map;
			}
		});
		addSlotToContainer(new Slot(inputs, 1, 15, 52) {
			@Override
			public boolean isItemValid(ItemStack stack) {
				return isModifier(stack);
			}
		});
		addResultSlot(145, 39);
		addPlayerInventory(inv);
	}

	public static boolean isModifier(ItemStack stack) {
		Item item = stack.getItem();
		return item == Items.paper || item == Items.map || item == Item.getItemFromBlock(Blocks.glass_pane);
	}

	public MapData getMapData() {
		ItemStack map = inputs.getStackInSlot(0);
		return map == null ? null : (MapData) world.loadItemData(MapData.class, "map_" + map.getItemDamage());
	}

	@Override
	public void onCraftMatrixChanged(IInventory inv) {
		resultInventory.setInventorySlotContents(0, computeResult());
		super.onCraftMatrixChanged(inv);
	}

	private ItemStack computeResult() {
		ItemStack map = inputs.getStackInSlot(0);
		ItemStack modifier = inputs.getStackInSlot(1);
		if (map == null || modifier == null) return null;
		boolean locked = MapLocking.isLocked(map);
		Item item = modifier.getItem();

		if (item == Items.map) {
			ItemStack result = map.copy();
			result.stackSize = 2;
			return result;
		}
		if (locked) return null;
		if (item == Items.paper) {
			MapData data = getMapData();
			if (data != null && data.scale >= MapLocking.MAX_SCALE) return null;
			return withTag(map, MapLocking.PENDING_ZOOM);
		}
		if (item == Item.getItemFromBlock(Blocks.glass_pane)) {
			return withTag(map, MapLocking.PENDING_LOCK);
		}
		return null;
	}

	private static ItemStack withTag(ItemStack map, String tag) {
		ItemStack result = map.copy();
		result.stackSize = 1;
		if (!result.hasTagCompound()) result.setTagCompound(new NBTTagCompound());
		result.getTagCompound().setBoolean(tag, true);
		return result;
	}

	@Override
	protected void finalizeResult(EntityPlayer player, ItemStack result) {
		MapLocking.finalizeMap(result, world);
	}

	@Override
	protected IInventory getInputInventory() {
		return inputs;
	}

	@Override
	protected void onTakeResult(EntityPlayer player, ItemStack result) {
		inputs.decrStackSize(0, 1);
		inputs.decrStackSize(1, 1);
		playSound(Tags.MC_ASSET_VER + ":ui.cartography_table.take_result");
	}

	@Override
	protected boolean mergeIntoInputs(ItemStack stack) {
		return mergeIntoSlot(stack, 0) || mergeIntoSlot(stack, 1);
	}
}
