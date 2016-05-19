package ethanjones.mc.inventorybook;

import net.minecraftforge.common.config.Configuration;

import java.io.File;

public class ConfigHandler {
  // BOOK
  public static int LINES_PER_PAGE;
  public static int CHAR_PER_LINE;
  // NBT
//  public static int NBT_PER_ITEM;
//  public static boolean NBT_EXTRA_PAGE;

  public static void init(File file) {
    Configuration config = new Configuration(file);
    config.load();

    LINES_PER_PAGE = config.get("BOOK", "LINES_PER_PAGE", 14).getInt();
    CHAR_PER_LINE = config.get("BOOK", "CHAR_PER_LINE", 18).getInt();

//    NBT_PER_ITEM = config.get("NBT", "ALLOWED_LENGTH_PER_ITEM", 32767 / 20).getInt();
//    NBT_EXTRA_PAGE = config.get("NBT", "EXTRA_PAGE_FOR_ITEM", true, "Will create a page just for an item if the NBT is too long").getBoolean();

    config.save();
  }
}
