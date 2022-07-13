package zip.firecrates;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import redempt.redlib.inventorygui.InventoryGUI;
import redempt.redlib.inventorygui.ItemButton;
import redempt.redlib.itemutils.ItemBuilder;

import java.util.Random;

public final class FireCrates extends JavaPlugin implements Listener {
    @Override
    public void onEnable() {
        this.saveDefaultConfig();
        getServer().getPluginManager().registerEvents(this, this);
    }

    @Override
    public void onDisable() {
    }
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (label.equalsIgnoreCase("cratereload")) {
            reloadConfig();
        }
        return false;
    }
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Block eblock = event.getClickedBlock();
        String crate_block = "";
        String crate_name = "";
        for(String crate : getConfig().getConfigurationSection("Crates").getKeys(false)){
            String block = getConfig().getConfigurationSection("Crates."+crate).getString("Block");
            if (eblock.getType().name().toLowerCase().equals(block)) {
                crate_block = block;
                crate_name = crate;
            }
        }
        if (crate_block != "") {
            if(event.getAction() == Action.LEFT_CLICK_BLOCK || event.getAction() == Action.LEFT_CLICK_AIR){
                InventoryGUI gui = new InventoryGUI(Bukkit.createInventory(null, 27, crate_name + " Preview"));
                int count = 0;
                for(String item : getConfig().getConfigurationSection("Crates."+crate_name+".Items").getKeys(false)) {
                    String Name = getConfig().getConfigurationSection("Crates." + crate_name + ".Items." + item).getString("Name");
                    Material block = Material.getMaterial(item.toUpperCase());
                    ItemButton button = ItemButton.create(new ItemBuilder(block).setName(Name), e -> {});
                    gui.addButton(button, count);
                    count += 1;
                }
                for(int i = count; i < 27; i++) {
                    String type = getConfig().getString("options.preview_filler");
                    Material block = Material.getMaterial(type.toUpperCase());
                    ItemButton button = ItemButton.create(new ItemBuilder(block).setName(""), e -> {});
                    gui.addButton(button, i);
                }
                gui.open(event.getPlayer());
                event.setCancelled(true);
                return;
            }
            if (event.getPlayer().getInventory().getItemInMainHand().getType().equals(Material.AIR)) {
                event.getPlayer().spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(ChatColor.RED + "You Need " + crate_name + " key!"));
                event.setCancelled(true);
                return;
            }
            if (!event.getPlayer().getInventory().getItemInMainHand().getItemMeta().getDisplayName().toLowerCase().contains(crate_name.toLowerCase() + " key")) {
                event.getPlayer().spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(ChatColor.RED + "You Need " + crate_name + " key!"));
                event.setCancelled(true);
                return;
            }
            Material type = Material.AIR;
            String name = "";
            for(String item : getConfig().getConfigurationSection("Crates."+crate_name+".Items").getKeys(false)) {
                Integer Chance = getConfig().getConfigurationSection("Crates." + crate_name + ".Items." + item).getInt("Chance");
                String Name = getConfig().getConfigurationSection("Crates." + crate_name + ".Items." + item).getString("Name");
                boolean test = new Random().nextInt(Chance) == 0;
                if (test) {
                    type = Material.getMaterial(item.toUpperCase());
                    name = Name;
                    break;
                }
            }
            if (type == null) {
                type = Material.AIR;
            }
            if (type == Material.AIR){
                event.getPlayer().sendMessage("You got: Nothing!");
                event.setCancelled(true);
                return;
            }
            event.getPlayer().sendMessage("You got: " + name);
            ItemStack is = new ItemStack(type, 1);
            ItemMeta im = is.getItemMeta();
            if (im != null) {
                im.setDisplayName(name);
            }
            is.setItemMeta(im);
            event.getPlayer().getInventory().addItem(is);
            event.setCancelled(true);
        }
    }
}