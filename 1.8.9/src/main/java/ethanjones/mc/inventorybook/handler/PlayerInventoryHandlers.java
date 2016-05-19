package ethanjones.mc.inventorybook.handler;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.IChatComponent;

public class PlayerInventoryHandlers {
  public static class HotbarHandler extends PageHandler<EntityPlayer> {

    @Override
    public boolean valid(EntityPlayer entityPlayer) { //change from 1.9: no offHand
      return !empty(entityPlayer.inventory.mainInventory, 0, 8);
    }

    @Override
    public ItemStack itemStack(EntityPlayer entityPlayer, int i) {
      return entityPlayer.inventory.mainInventory[i];
    }

    @Override
    public int itemStacksLength(EntityPlayer entityPlayer) {
      return 9;
    }

    @Override
    public IChatComponent title(EntityPlayer obj) {
      return new ChatComponentText("Hotbar");
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
    public IChatComponent title(EntityPlayer obj) {
      return new ChatComponentText("Main Inventory");
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
    public ChatComponentText title(EntityPlayer obj) {
      return new ChatComponentText("Armour");
    }
  }
}
