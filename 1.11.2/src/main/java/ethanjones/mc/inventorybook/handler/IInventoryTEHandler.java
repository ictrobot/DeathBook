package ethanjones.mc.inventorybook.handler;

import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;

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
  public ITextComponent title(IInventory obj) {
    return obj.getDisplayName();
  }
}
