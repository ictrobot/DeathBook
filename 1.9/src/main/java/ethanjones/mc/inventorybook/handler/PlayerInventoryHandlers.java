package ethanjones.mc.inventorybook.handler;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;

public class PlayerInventoryHandlers {
  public static class HotbarHandler extends PageHandler<EntityPlayer> {

    @Override
    public boolean valid(EntityPlayer entityPlayer) {
      return !empty(entityPlayer.inventory.mainInventory, 0, 8) || entityPlayer.inventory.offHandInventory[0] != null;
    }

    @Override
    public ItemStack itemStack(EntityPlayer entityPlayer, int i) {
      if (i == 9) return entityPlayer.inventory.offHandInventory[0];
      return entityPlayer.inventory.mainInventory[i];
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
      return !empty(entityPlayer.inventory.mainInventory, 9, entityPlayer.inventory.mainInventory.length - 1);
    }

    @Override
    public ItemStack itemStack(EntityPlayer entityPlayer, int i) {
      return entityPlayer.inventory.mainInventory[i + 9];
    }

    @Override
    public int itemStacksLength(EntityPlayer entityPlayer) {
      return entityPlayer.inventory.mainInventory.length - 9;
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
      return entityPlayer.inventory.armorInventory[i];
    }

    @Override
    public int itemStacksLength(EntityPlayer entityPlayer) {
      return entityPlayer.inventory.armorInventory.length;
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
