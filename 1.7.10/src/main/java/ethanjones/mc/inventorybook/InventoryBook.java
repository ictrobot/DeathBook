package ethanjones.mc.inventorybook;

import ethanjones.mc.inventorybook.handler.IInventoryTEHandler;
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
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.Action;
import org.apache.logging.log4j.Logger;

import java.text.SimpleDateFormat;
import java.util.*;

import static ethanjones.mc.inventorybook.ConfigHandler.*;

@Mod(modid = "inventorybook", version = "0.0.2")
public class InventoryBook {
  public static final HashMap<UUID, ItemStack> books = new HashMap<UUID, ItemStack>();
  public static final ArrayList<PageHandler<EntityPlayer>> handlers = new ArrayList<PageHandler<EntityPlayer>>() {{
    add(new PlayerInventoryHandlers.HotbarHandler());
    add(new PlayerInventoryHandlers.MainInvHandler());
    add(new PlayerInventoryHandlers.ArmourHandler());
  }};
  public static final ArrayList<PageHandler<ItemStack>> itemStackHandlers = new ArrayList<PageHandler<ItemStack>>();
  public static final IInventoryTEHandler iInventoryHandler = new IInventoryTEHandler();
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

  @SubscribeEvent
  public void itemTooltip(ItemTooltipEvent event) {
    if (!ITEM_TOOLTIP) return;
    ItemStack itemStack = event.itemStack;
    if (itemStack.getItem() == Items.written_book && itemStack.hasTagCompound() && itemStack.getTagCompound().hasKey("inventorybook", 8)) {
      String tag = itemStack.getTagCompound().getString("inventorybook");
      if (tag.isEmpty()) return;
      String type = tag.substring(0, 1).toUpperCase() + tag.substring(1);
      event.toolTip.add(1, type + " InventoryBook");
    }
  }

  @SubscribeEvent
  public void rightClick(PlayerInteractEvent event) {
    ItemStack itemStack = event.entityPlayer.getHeldItem();
    if (itemStack == null  ||  itemStack.getItem() != Items.writable_book || !event.entityPlayer.isSneaking()) return;
    if (event.isCanceled() || event.entityPlayer.worldObj.isRemote || event.entityPlayer instanceof FakePlayer) return;

    if (event.action == Action.RIGHT_CLICK_AIR) {
      if (!RIGHT_CLICK_PLAYER_INVENTORY) return;
      ItemStack book = getBook(event.entityPlayer, "Inventory");
      if (book == null || !decrementBook(event.entityPlayer)) return;
      event.entityPlayer.inventory.addItemStackToInventory(book);
      event.setCanceled(true);
    } else if (event.action == Action.RIGHT_CLICK_BLOCK) {
      if (!RIGHT_CLICK_BLOCK_INVENTORY) return;
      TileEntity tileEntity = event.entityPlayer.worldObj.getTileEntity(event.x, event.y, event.z);
      if (tileEntity == null || !(tileEntity instanceof IInventory)) return;
      ItemStack book = getBook(((IInventory) tileEntity), event.entityPlayer, "Block");
      if (book == null || !decrementBook(event.entityPlayer)) return;
      event.entityPlayer.inventory.addItemStackToInventory(book);
      event.setCanceled(true);
    }
  }
  
  @SubscribeEvent(priority = EventPriority.HIGHEST)
  public void death(LivingDeathEvent event) {
    Entity entity = event.entity; // change
    World world = entity.worldObj;
    if (event.isCanceled() || world.isRemote || !(entity instanceof EntityPlayer) || entity instanceof FakePlayer)
      return;
    if (!DEATH_BOOK) return;
    EntityPlayer entityPlayer = ((EntityPlayer) entity);
    if (DEATH_REQUIRE_BOOK_AND_QUILL && !decrementBook(entityPlayer)) return;
    ItemStack book = getBook(entityPlayer, "Death");
    books.put(entityPlayer.getPersistentID(), book);
  }

  public boolean decrementBook(EntityPlayer entityPlayer) {
    return entityPlayer.inventory.consumeInventoryItem(Items.writable_book);
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
      compound.setString("inventorybook", title.toLowerCase());

      ItemStack itemStack = new ItemStack(Items.written_book, 1, 0);
      itemStack.setTagCompound(compound);
      return itemStack;
    } catch (Throwable t) {
      log.error("FAILED TO CREATE INVENTORY BOOK", t);
      return null;
    }
  }

  public ItemStack getBook(IInventory inv, EntityPlayer entityPlayer, String title) {
    try {
      NBTTagList pages = new NBTTagList();
      final ArrayList<ItemStack> extraPages = new ArrayList<ItemStack>();
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
      if (iInventoryHandler.valid(inv)) iInventoryHandler.addPages(pages, inv, callback);
      while (!toProcess.isEmpty()) {
        ItemStack itemStack = toProcess.pop();
        for (PageHandler<ItemStack> handler : itemStackHandlers) {
          if (handler.valid(itemStack)) handler.addPages(pages, itemStack, callback);
        }
      }
//      for (int i = 0; i < extraPages.size(); i++) {
//        PageHandler.createExtra(pages, extraPages.get(i), i + 1);
//      }
      if (pages.tagCount() == 0) return null;

      String date = new SimpleDateFormat("HH:mm:ss dd-MM-yyyy").format(new Date());
      NBTTagCompound compound = new NBTTagCompound();
      compound.setTag("pages", pages);
      compound.setString("author", entityPlayer.getDisplayName()); // change
      compound.setString("title", title + " " + date);
      compound.setString("inventorybook", title.toLowerCase());

      ItemStack itemStack = new ItemStack(Items.written_book, 1, 0);
      itemStack.setTagCompound(compound);
      return itemStack;
    } catch (Throwable t) {
      log.error("FAILED TO CREATE INVENTORY BOOK", t);
      return null;
    }
  }
}
