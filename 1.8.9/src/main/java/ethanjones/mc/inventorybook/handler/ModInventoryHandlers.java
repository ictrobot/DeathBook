package ethanjones.mc.inventorybook.handler;

import baubles.api.BaublesApi;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.IChatComponent;

public class ModInventoryHandlers {
  public static class BaublesHandler extends PageHandler.IInventoryHandler {
    @Override
    public IInventory getIInventory(EntityPlayer entityPlayer) {
      return BaublesApi.getBaubles(entityPlayer);
    }

    @Override
    public IChatComponent title(EntityPlayer obj) {
      return new ChatComponentText("Baubles");
    }
  }

  public static class BagginsesHandler extends PageHandler<ItemStack> {

    @Override
    public boolean valid(ItemStack obj) {
      String n = Item.itemRegistry.getNameForObject(obj.getItem()).toString().toLowerCase();
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
    public IChatComponent title(ItemStack obj) {
      return getItemStackComponent(obj, null, false, false);
    }
  }
}
