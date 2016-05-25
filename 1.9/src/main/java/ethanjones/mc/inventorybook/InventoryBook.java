package ethanjones.mc.inventorybook;

import ethanjones.mc.inventorybook.handler.IInventoryTEHandler;
import ethanjones.mc.inventorybook.handler.ModInventoryHandlers;
import ethanjones.mc.inventorybook.handler.PageHandler;
import ethanjones.mc.inventorybook.handler.PageHandler.ItemStackCallback;
import ethanjones.mc.inventorybook.handler.PlayerInventoryHandlers;

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
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerRespawnEvent;
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
    if (Loader.isModLoaded("Baubles")) {
      log.info("Activating Baubles Integration");
      handlers.add(new ModInventoryHandlers.BaublesHandler());
    }
    if (Loader.isModLoaded("bagginses")) {
      log.info("Activating Bagginses Integration");
      itemStackHandlers.add(new ModInventoryHandlers.BagginsesHandler());
    }
  }

  @SubscribeEvent
  public void itemTooltip(ItemTooltipEvent event) {
    if (!ITEM_TOOLTIP) return;
    ItemStack itemStack = event.getItemStack();
    if (itemStack.getItem() == Items.written_book && itemStack.hasTagCompound() && itemStack.getTagCompound().hasKey("inventorybook", 8)) {
      String tag = itemStack.getTagCompound().getString("inventorybook");
      if (tag.isEmpty()) return;
      String type = tag.substring(0, 1).toUpperCase() + tag.substring(1);
      event.getToolTip().add(1, type + " InventoryBook");
    }
  }

  @SubscribeEvent
  public void rightClick(PlayerInteractEvent.RightClickItem event) {
    if (!RIGHT_CLICK_PLAYER_INVENTORY) return;
    if (event.getItemStack() == null  || event.getItemStack().getItem() != Items.writable_book || !event.getEntityPlayer().isSneaking()) return;
    if (event.isCanceled() || event.getEntityPlayer().worldObj.isRemote || event.getEntityPlayer() instanceof FakePlayer) return;
    ItemStack book = getBook(event.getEntityPlayer(), "Inventory");
    if (book == null || !decrementBook(event.getEntityPlayer())) return;
    event.getEntityPlayer().inventory.addItemStackToInventory(book);
    event.setCanceled(true);
  }

  @SubscribeEvent
  public void rightClick(PlayerInteractEvent.RightClickBlock event) {
    if (!RIGHT_CLICK_BLOCK_INVENTORY) return;
    if (event.getItemStack() == null  ||  event.getItemStack().getItem() != Items.writable_book || !event.getEntityPlayer().isSneaking()) return;
    if (event.isCanceled() || event.getEntityPlayer().worldObj.isRemote || event.getEntityPlayer() instanceof FakePlayer) return;
    TileEntity tileEntity = event.getEntityPlayer().worldObj.getTileEntity(event.getPos());
    if (tileEntity == null || !(tileEntity instanceof IInventory)) return;
    ItemStack book = getBook(((IInventory) tileEntity), event.getEntityPlayer(), "Block");
    if (book == null || !decrementBook(event.getEntityPlayer())) return;
    event.getEntityPlayer().inventory.addItemStackToInventory(book);
    event.setCanceled(true);
  }
  
  @SubscribeEvent(priority = EventPriority.HIGHEST)
  public void death(LivingDeathEvent event) {
    Entity entity = event.getEntity();
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
    return entityPlayer.inventory.clearMatchingItems(Items.writable_book, 0, 1, null) == 1;
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
      final ArrayList<ItemStack> extraPages = new ArrayList<ItemStack>();
      final ArrayDeque<ItemStack> toProcess = new ArrayDeque<ItemStack>();
      ItemStackCallback callback = new ItemStackCallback() {
        @Override
        public void itemStack(ItemStack itemStack) {
          toProcess.add(itemStack);
        }

        @Override
        public int extraPage(ItemStack itemStack) {
          extraPages.add(itemStack);
          return extraPages.size();
        }
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
      for (int i = 0; i < extraPages.size(); i++) {
        PageHandler.createExtra(pages, extraPages.get(i), i + 1);
      }
      if (pages.hasNoTags()) return null;

      String date = new SimpleDateFormat("HH:mm:ss dd-MM-yyyy").format(new Date());
      NBTTagCompound compound = new NBTTagCompound();
      compound.setTag("pages", pages);
      compound.setString("author", player.getName());
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

        @Override
        public int extraPage(ItemStack itemStack) {
          extraPages.add(itemStack);
          return extraPages.size();
        }
      };
      if (iInventoryHandler.valid(inv)) iInventoryHandler.addPages(pages, inv, callback);
      while (!toProcess.isEmpty()) {
        ItemStack itemStack = toProcess.pop();
        for (PageHandler<ItemStack> handler : itemStackHandlers) {
          if (handler.valid(itemStack)) handler.addPages(pages, itemStack, callback);
        }
      }
      for (int i = 0; i < extraPages.size(); i++) {
        PageHandler.createExtra(pages, extraPages.get(i), i + 1);
      }
      if (pages.hasNoTags()) return null;

      String date = new SimpleDateFormat("HH:mm:ss dd-MM-yyyy").format(new Date());
      NBTTagCompound compound = new NBTTagCompound();
      compound.setTag("pages", pages);
      compound.setString("author", entityPlayer.getName());
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
