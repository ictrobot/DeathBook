package ethanjones.mc.inventorybook.handler;

import ethanjones.mc.inventorybook.ConfigHandler;
import ethanjones.mc.inventorybook.InventoryBook;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.event.HoverEvent;

public abstract class PageHandler<T> {
  public abstract boolean valid(T obj);

  public abstract ItemStack itemStack(T obj, int i);

  public abstract int itemStacksLength(T obj);

  public int convertItemStackIndex(int i) {
    return i;
  }

  public abstract ITextComponent title(T obj);

  public boolean displaySlotNumber() {
    return ConfigHandler.DISPLAY_SLOT_NUMBERS;
  }

  public boolean displayStackSize() {
    return true;
  }

  public void addPages(NBTTagList pages, T obj, ItemStackCallback callback) {
    ITextComponent text = title(obj);
    int lines = text.getUnformattedText().isEmpty() ? 0 : 1;

    for (int i = 0; i < itemStacksLength(obj); i++) {
      ItemStack itemStack = itemStack(obj, i);
      if (itemStack == null) continue;
      callback.itemStack(itemStack);
      if (lines >= ConfigHandler.LINES_PER_PAGE) {
        pages.appendTag(new NBTTagString(ITextComponent.Serializer.componentToJson(text)));
        text = new TextComponentString("");
        lines = 0;
      }

      StringBuilder stringBuilder = new StringBuilder();
      if (lines != 0) stringBuilder.append("\n");
      if (displaySlotNumber()) stringBuilder.append("[").append(convertItemStackIndex(i)).append("] ");
      if (displayStackSize() && itemStack.stackSize > 1) stringBuilder.append(itemStack.stackSize).append(" ");
      text.appendText(stringBuilder.toString());
      text.appendSibling(getItemStackComponent(itemStack, callback, false, true));

      String[] split = text.getUnformattedText().split("\n");
      if (split.length == 0) split = new String[]{text.getUnformattedText()};
      int charLen = split[split.length - 1].length();
      lines += 1 + (charLen / ConfigHandler.CHAR_PER_LINE);
    }

    pages.appendTag(new NBTTagString(ITextComponent.Serializer.componentToJson(text)));
  }

  public static void createExtra(NBTTagList pages, ItemStack itemStack, int num) {
    ITextComponent text = new TextComponentString("Extra #" + num + "\n");
    text.appendSibling(getItemStackComponent(itemStack, null, true, false));
    pages.appendTag(new NBTTagString(ITextComponent.Serializer.componentToJson(text)));
  }

  public static ITextComponent getItemStackComponent(ItemStack itemStack, ItemStackCallback callback, boolean isExtraPage, boolean createExtraPage) {
    TextComponentString x = new TextComponentString(itemStack.getDisplayName());
    if (itemStack.hasDisplayName()) x.getStyle().setItalic(Boolean.valueOf(true));

    if (itemStack.getItem() == Items.WRITTEN_BOOK && itemStack.getTagCompound() != null && itemStack.getTagCompound().hasKey("inventorybook")) {
      itemStack = ItemStack.copyItemStack(itemStack);
      NBTTagList pages = new NBTTagList();
      pages.appendTag(new NBTTagString(""));
      itemStack.getTagCompound().setTag("pages", pages);
    }

    if (itemStack.getItem() != null) {
      NBTTagCompound nbttagcompound = new NBTTagCompound();
      itemStack.writeToNBT(nbttagcompound);

      String s;
      int tagLength;
      try {
        s = nbttagcompound.toString();
        tagLength = s.length();
      } catch (StackOverflowError error) {
        s = "";
        tagLength = Integer.MAX_VALUE;
      }
      if (isExtraPage ? tagLength > 32000 : tagLength > ConfigHandler.NBT_PER_ITEM) {
        nbttagcompound.removeTag("tag");
        nbttagcompound.removeTag("ForgeCaps");

        NBTTagCompound tag = new NBTTagCompound();
        nbttagcompound.setTag("tag", tag);
        NBTTagCompound display = new NBTTagCompound();
        tag.setTag("display", display);

        String lore;
        if (!ConfigHandler.NBT_EXTRA_PAGE || !createExtraPage || callback == null || tagLength > 32000) {
          lore = "[InventoryBook] NBT Removed - too long";
        } else {
          int i = callback.extraPage(itemStack);
          lore = "[InventoryBook] See extra page #" + i + " - NBT too long";
        }
        NBTTagList list = new NBTTagList();
        list.appendTag(new NBTTagString(lore));
        display.setTag("Lore", list);

        s = nbttagcompound.toString();

        InventoryBook.log.info(lore + " " + itemStack.toString());
      }

      x.getStyle().setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_ITEM, new TextComponentString(s)));
    }

    return x;
  }

  // convince methods to determine if itemstack[] is empty
  public static boolean empty(ItemStack[] itemStacks) {
    return empty(itemStacks, 0, itemStacks.length - 1);
  }

  public static boolean empty(ItemStack[] itemStacks, int minIndex, int maxIndex) {
    for (int i = minIndex; i <= maxIndex; i++) {
      if (itemStacks[i] != null) return false;
    }
    return true;
  }

  public static abstract class IInventoryHandler extends PageHandler<EntityPlayer> {

    @Override
    public boolean valid(EntityPlayer entityPlayer) {
      IInventory iInventory = getIInventory(entityPlayer);
      for (int i = 0; i< iInventory.getSizeInventory(); i++) {
        if (iInventory.getStackInSlot(i) != null) return true;
      }
      return false;
    }

    @Override
    public ItemStack itemStack(EntityPlayer entityPlayer, int i) {
      return getIInventory(entityPlayer).getStackInSlot(i);
    }

    @Override
    public int itemStacksLength(EntityPlayer entityPlayer) {
      return getIInventory(entityPlayer).getSizeInventory();
    }

    public abstract IInventory getIInventory(EntityPlayer entityPlayer);
  }

  public static interface ItemStackCallback {
    public void itemStack(ItemStack itemStack);

    public int extraPage(ItemStack itemStack);
  }
}
