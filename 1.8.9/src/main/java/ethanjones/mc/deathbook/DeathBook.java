package ethanjones.mc.deathbook;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.event.HoverEvent;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.IChatComponent;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerRespawnEvent;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.UUID;

@Mod(modid = DeathBook.MODID, version = DeathBook.VERSION)
public class DeathBook {
  public static final String MODID = "deathbook";
  public static final String VERSION = "0.0.1";
  HashMap<UUID, ItemStack> map = new HashMap<UUID, ItemStack>();
  
  @EventHandler
  public void init(FMLInitializationEvent event) {
    MinecraftForge.EVENT_BUS.register(this);
  }
  
  @SubscribeEvent(priority = EventPriority.HIGHEST)
  public void death(LivingDeathEvent event) {
    Entity entity = event.entity;
    World world = entity.worldObj;
    if (event.isCanceled() || world.isRemote || !(entity instanceof EntityPlayer) || entity instanceof FakePlayer)
      return;
    EntityPlayer entityPlayer = ((EntityPlayer) entity);
    ItemStack book = getBook(entityPlayer);
    map.put(entityPlayer.getPersistentID(), book);
  }
  
  @SubscribeEvent
  public void respawn(PlayerRespawnEvent event) {
    ItemStack itemStack = map.remove(event.player.getPersistentID());
    if (itemStack != null)
      event.player.inventory.addItemStackToInventory(itemStack);
  }
  
  public ItemStack getBook(EntityPlayer player) {
    NBTTagList pages = new NBTTagList();
    hotbarInv(pages, player);
    mainInv(pages, player);
    armourInv(pages, player);
    
    String date = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss").format(new Date());
    NBTTagCompound compound = new NBTTagCompound();
    compound.setTag("pages", pages);
    compound.setString("author", player.getName());
    compound.setString("title", date);
    
    ItemStack itemStack = new ItemStack(Items.written_book, 1, 0);
    itemStack.setTagCompound(compound);
    return itemStack;
  }
  
  public void hotbarInv(NBTTagList pages, EntityPlayer player) {
    ChatComponentText text = new ChatComponentText("Hotbar");
    for (int i = 0; i < 9; i++) {
      ItemStack is = player.inventory.getStackInSlot(i);
      if (is == null) continue;
      text.appendSibling(new ChatComponentText("\n[" + (i +1) + "] " + (is.stackSize > 1 ? is.stackSize  + "x "  : "")));
      text.appendSibling(getChatComponent(is));
    }
    pages.appendTag(new NBTTagString(IChatComponent.Serializer.componentToJson(text)));
  }

  public void mainInv(NBTTagList pages, EntityPlayer player) {
    ChatComponentText text = new ChatComponentText("Main");
    int lines = 1;
    for (int i = 9; i < player.inventory.mainInventory.length; i++) {
      ItemStack is = player.inventory.getStackInSlot(i);
      if (is == null) continue;
      if (lines == 10) {
        pages.appendTag(new NBTTagString(IChatComponent.Serializer.componentToJson(text)));
        text = new ChatComponentText("");
        lines = 0;
      }
      text.appendSibling(new ChatComponentText("\n[" + ( i - 8 ) + "] " + (is.stackSize > 1 ? is.stackSize + "x " : "")));
      text.appendSibling(getChatComponent(is));
      lines++;
    }
    pages.appendTag(new NBTTagString(IChatComponent.Serializer.componentToJson(text)));
  }

  public void armourInv(NBTTagList pages, EntityPlayer player) {
    ChatComponentText text = new ChatComponentText("Armour");
    for (int i = 0; i < player.inventory.armorInventory.length; i++) {
      ItemStack is = player.inventory.armorItemInSlot(i);
      if (is == null) continue;
      text.appendSibling(new ChatComponentText("\n"));
      text.appendSibling(getChatComponent(is));
    }
    pages.appendTag(new NBTTagString(ChatComponentText.Serializer.componentToJson(text)));
  }
  
  public ChatComponentText getChatComponent(ItemStack itemStack) {
    ChatComponentText itextcomponent = new ChatComponentText(itemStack.getDisplayName());
    if (itemStack.hasDisplayName()) itextcomponent.getChatStyle().setItalic(true);
    
    if (itemStack.getItem() != null) {
      NBTTagCompound nbttagcompound = new NBTTagCompound();
      itemStack.writeToNBT(nbttagcompound);
      itextcomponent.getChatStyle().setChatHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_ITEM, new ChatComponentText(nbttagcompound.toString())));
    }
    
    return itextcomponent;
  }
}
