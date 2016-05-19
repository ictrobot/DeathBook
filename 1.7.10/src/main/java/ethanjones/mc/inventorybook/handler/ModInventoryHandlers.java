package ethanjones.mc.inventorybook.handler;

import baubles.api.BaublesApi;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;

public class ModInventoryHandlers {
  public static class BaublesHandler extends PageHandler.IInventoryHandler {
    @Override
    public IInventory getIInventory(EntityPlayer entityPlayer) {
      return BaublesApi.getBaubles(entityPlayer);
    }

    @Override
    public String title(EntityPlayer obj) {
      return "Baubles";
    }
  }

  public static class BagginsesHandler extends PageHandler<ItemStack> {

    @Override
    public boolean valid(ItemStack obj) {
      String n = Item.itemRegistry.getNameForObject(obj.getItem()).toLowerCase();
      if (!n.startsWith("bagginses:") || n.equals("bagginses:void") || n.equals("bagginses:ender")) return false;
      return obj.getTagCompound() != null && obj.getTagCompound().hasKey("Items");
    }

    @Override
    public ItemStack itemStack(ItemStack obj, int i) {
      NBTTagList items = obj.getTagCompound().getTagList("Items", 10);
      return ItemStack.loadItemStackFromNBT(items.getCompoundTagAt(i));
    }

    @Override
    public int itemStacksLength(ItemStack obj) {
      NBTTagList items = obj.getTagCompound().getTagList("Items", 10);
      return items.tagCount();
    }

    @Override
    public String title(ItemStack obj) {
      return obj.getDisplayName();
    }
  }

  public static class ExtraUtilitiesGoldenBagHandler extends PageHandler<ItemStack> {

    @Override
    public boolean valid(ItemStack obj) {
      String n = Item.itemRegistry.getNameForObject(obj.getItem()).toLowerCase();
      if (!n.equals("extrautilities:golden_bag")) return false;
      return obj.getTagCompound() != null && !obj.getTagCompound().hasNoTags();
    }

    @Override
    public ItemStack itemStack(ItemStack obj, int i) {
      NBTTagCompound tag = obj.getTagCompound().getCompoundTag("items_" + i);
      if (tag == null || tag.hasNoTags()) return null;
      return ItemStack.loadItemStackFromNBT(tag);
    }

    @Override
    public int itemStacksLength(ItemStack obj) {
      return 54;
    }

    @Override
    public String title(ItemStack obj) {
      return obj.getDisplayName();
    }
  }
}
