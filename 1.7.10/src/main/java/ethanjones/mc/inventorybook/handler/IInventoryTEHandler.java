package ethanjones.mc.inventorybook.handler;

import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IChatComponent;

public class IInventoryTEHandler extends PageHandler<IInventory> {
  @Override
  public boolean valid(IInventory obj) {
    return true;
  }

  @Override
  public ItemStack itemStack(IInventory obj, int i) {
    return obj.getStackInSlot(i);
  }

  @Override
  public int itemStacksLength(IInventory obj) {
    return obj.getSizeInventory();
  }

  @Override
  public String title(IInventory obj) {
    return obj.getInventoryName();
  }
}
