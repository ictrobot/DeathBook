package ethanjones.mc.inventorybook.handler;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;

public class PlayerInventoryHandlers {
  public static class HotbarHandler extends PageHandler<EntityPlayer> {

    @Override
    public boolean valid(EntityPlayer entityPlayer) {
      return !empty(entityPlayer.inventory.mainInventory, 0, 8) || !entityPlayer.inventory.offHandInventory.get(0).isEmpty();
    }

    @Override
    public ItemStack itemStack(EntityPlayer entityPlayer, int i) {
      if (i == 9) return entityPlayer.inventory.offHandInventory.get(0);
      return entityPlayer.inventory.mainInventory.get(i);
    }

    @Override
    public int itemStacksLength(EntityPlayer entityPlayer) {
      return 10;
    }

    @Override
    public ITextComponent title(EntityPlayer obj) {
      return new TextComponentString("Hotbar");
    }
  }

  public static class MainInvHandler extends PageHandler<EntityPlayer> {

    @Override
    public boolean valid(EntityPlayer entityPlayer) {
      return !empty(entityPlayer.inventory.mainInventory, 9, entityPlayer.inventory.mainInventory.size() - 1);
    }

    @Override
    public ItemStack itemStack(EntityPlayer entityPlayer, int i) {
      return entityPlayer.inventory.mainInventory.get(i + 9);
    }

    @Override
    public int itemStacksLength(EntityPlayer entityPlayer) {
      return entityPlayer.inventory.mainInventory.size() - 9;
    }

    @Override
    public ITextComponent title(EntityPlayer obj) {
      return new TextComponentString("Main Inventory");
    }
  }

  public static class ArmourHandler extends PageHandler<EntityPlayer> {

    @Override
    public boolean valid(EntityPlayer entityPlayer) {
      return !empty(entityPlayer.inventory.armorInventory);
    }

    @Override
    public ItemStack itemStack(EntityPlayer entityPlayer, int i) {
      return entityPlayer.inventory.armorInventory.get(i);
    }

    @Override
    public int itemStacksLength(EntityPlayer entityPlayer) {
      return entityPlayer.inventory.armorInventory.size();
    }

    @Override
    public boolean displaySlotNumber() {
      return false;
    }

    @Override
    public boolean displayStackSize() {
      return false;
    }

    @Override
    public ITextComponent title(EntityPlayer obj) {
      return new TextComponentString("Armour");
    }
  }
}
