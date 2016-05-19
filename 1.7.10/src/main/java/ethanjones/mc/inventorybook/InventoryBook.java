package ethanjones.mc.inventorybook;

import ethanjones.mc.inventorybook.handler.ModInventoryHandlers;
import ethanjones.mc.inventorybook.handler.PageHandler;
import ethanjones.mc.inventorybook.handler.PageHandler.ItemStackCallback;
import ethanjones.mc.inventorybook.handler.PlayerInventoryHandlers;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.eventhandler.EventPriority;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.PlayerEvent.PlayerRespawnEvent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import org.apache.logging.log4j.Logger;

import java.text.SimpleDateFormat;
import java.util.*;

@Mod(modid = "inventorybook", version = "0.0.1")
public class InventoryBook {
  public static final HashMap<UUID, ItemStack> books = new HashMap<UUID, ItemStack>();
  public static final ArrayList<PageHandler<EntityPlayer>> handlers = new ArrayList<PageHandler<EntityPlayer>>() {{
    add(new PlayerInventoryHandlers.HotbarHandler());
    add(new PlayerInventoryHandlers.MainInvHandler());
    add(new PlayerInventoryHandlers.ArmourHandler());
  }};
  public static final ArrayList<PageHandler<ItemStack>> itemStackHandlers = new ArrayList<PageHandler<ItemStack>>();
  public static Logger log;

  @EventHandler
  public void preInit(FMLPreInitializationEvent event) {
    log = event.getModLog();
    ConfigHandler.init(event.getSuggestedConfigurationFile());
  }

  @EventHandler
  public void init(FMLInitializationEvent event) {
    MinecraftForge.EVENT_BUS.register(this);
    FMLCommonHandler.instance().bus().register(this); // change
    if (Loader.isModLoaded("Baubles")) {
      log.info("Activating Baubles Integration");
      handlers.add(new ModInventoryHandlers.BaublesHandler());
    }
    if (Loader.isModLoaded("bagginses")) {
      log.info("Activating Bagginses Integration");
      itemStackHandlers.add(new ModInventoryHandlers.BagginsesHandler());
    }
    if (Loader.isModLoaded("ExtraUtilities")) { // EXTRA
      log.info("Activating ExtraUtilities Integration");
      itemStackHandlers.add(new ModInventoryHandlers.ExtraUtilitiesGoldenBagHandler());
    }
  }
  
  @SubscribeEvent(priority = EventPriority.HIGHEST)
  public void death(LivingDeathEvent event) {
    Entity entity = event.entity; // change
    World world = entity.worldObj;
    if (event.isCanceled() || world.isRemote || !(entity instanceof EntityPlayer) || entity instanceof FakePlayer)
      return;
    EntityPlayer entityPlayer = ((EntityPlayer) entity);
    ItemStack book = getBook(entityPlayer, "Death");
    books.put(entityPlayer.getPersistentID(), book);
  }
  
  @SubscribeEvent
  public void respawn(PlayerRespawnEvent event) {
    ItemStack itemStack = books.remove(event.player.getPersistentID());
    if (itemStack != null)
      event.player.inventory.addItemStackToInventory(itemStack);
  }
  
  public ItemStack getBook(EntityPlayer player, String title) {
    try {
      NBTTagList pages = new NBTTagList();
//      final ArrayList<ItemStack> extraPages = new ArrayList<ItemStack>();
      final ArrayDeque<ItemStack> toProcess = new ArrayDeque<ItemStack>();
      ItemStackCallback callback = new ItemStackCallback() {
        @Override
        public void itemStack(ItemStack itemStack) {
          toProcess.add(itemStack);
        }

//        @Override
//        public int extraPage(ItemStack itemStack) {
//          extraPages.add(itemStack);
//          return extraPages.size();
//        }
      };

      for (PageHandler<EntityPlayer> handler : handlers) {
        if (handler.valid(player)) handler.addPages(pages, player, callback);
      }
      while (!toProcess.isEmpty()) {
        ItemStack itemStack = toProcess.pop();
        for (PageHandler<ItemStack> handler : itemStackHandlers) {
          if (handler.valid(itemStack)) handler.addPages(pages, itemStack, callback);
        }
      }
//      for (int i = 0; i < extraPages.size(); i++) {
//        PageHandler.createExtra(pages, extraPages.get(i), i + 1);
//      }
      if (pages.tagCount() == 0) return null; // change

      String date = new SimpleDateFormat("HH:mm:ss dd-MM-yyyy").format(new Date());
      NBTTagCompound compound = new NBTTagCompound();
      compound.setTag("pages", pages);
      compound.setString("author", player.getDisplayName()); // change
      compound.setString("title", title + " " + date);
      compound.setBoolean("inventorybook", true);

      ItemStack itemStack = new ItemStack(Items.written_book, 1, 0);
      itemStack.setTagCompound(compound);
      return itemStack;
    } catch (Throwable t) {
      log.error("FAILED TO CREATE INVENTORY BOOK", t);
      return null;
    }
  }
}
