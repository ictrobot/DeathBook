package ethanjones.mc.inventorybook;

import net.minecraftforge.common.config.Configuration;

import java.io.File;

public class ConfigHandler {
  // BOOK
  public static int LINES_PER_PAGE;
  public static int CHAR_PER_LINE;
  public static boolean DISPLAY_SLOT_NUMBERS;
  public static String TEXT_BEFORE_SLOT_NUMBER;
  public static String TEXT_AFTER_SLOT_NUMBER;
  public static String TEXT_AFTER_STACK_SIZE;
  // NBT
  public static int NBT_PER_ITEM;
  public static boolean NBT_EXTRA_PAGE;
  // DEATH
  public static boolean DEATH_BOOK;
  public static boolean DEATH_REQUIRE_BOOK_AND_QUILL;
  // OTHER
  public static boolean RIGHT_CLICK_PLAYER_INVENTORY;
  public static boolean RIGHT_CLICK_BLOCK_INVENTORY;

  public static void init(File file) {
    Configuration config = new Configuration(file);
    config.load();

    LINES_PER_PAGE = config.get("BOOK", "LINES_PER_PAGE", 14).getInt();
    CHAR_PER_LINE = config.get("BOOK", "CHAR_PER_LINE", 18).getInt();
    DISPLAY_SLOT_NUMBERS = config.get("BOOK", "DISPLAY_SLOT_NUMBERS", true).getBoolean();
    TEXT_BEFORE_SLOT_NUMBER = config.get("BOOK", "TEXT_BEFORE_SLOT_NUMBER", "[").getString();
    TEXT_AFTER_SLOT_NUMBER = config.get("BOOK", "TEXT_AFTER_SLOT_NUMBER", "] ").getString();
    TEXT_AFTER_STACK_SIZE = config.get("BOOK", "TEXT_AFTER_STACK_SIZE", " ").getString();

    NBT_PER_ITEM = config.get("NBT", "ALLOWED_LENGTH_PER_ITEM", 32767 / 20).getInt();
    NBT_EXTRA_PAGE = config.get("NBT", "EXTRA_PAGE_FOR_ITEM", true, "Will create a page just for an item if the NBT is too long").getBoolean();

    DEATH_BOOK = config.get("DEATH", "DEATH_BOOK", true).getBoolean();
    DEATH_REQUIRE_BOOK_AND_QUILL = config.get("DEATH", "DEATH_REQUIRE_BOOK_AND_QUILL", false).getBoolean();

    RIGHT_CLICK_PLAYER_INVENTORY = config.get("OTHER", "RIGHT_CLICK_PLAYER_INVENTORY", true, "Enable shift right clicking book and quill to receive inventory book").getBoolean();
    RIGHT_CLICK_BLOCK_INVENTORY = config.get("OTHER", "RIGHT_CLICK_BLOCK_INVENTORY", true, "Enable shift right clicking book and quill on a block to receive inventory book").getBoolean();

    config.save();
  }
}
