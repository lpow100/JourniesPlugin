package trinary.stars.journies;

import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.entity.Player;

import java.util.Vector;

public class ClassManager {
    private final JavaPlugin plugin;

    public ClassManager(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public boolean isClass(Player player, String className) {
        String uuid = plugin.getConfig().getString("classes." + className);
        return uuid != null && uuid.equalsIgnoreCase(player.getUniqueId().toString());
    }

    public boolean isValidClass(String target){
        for (String className : plugin.getConfig().getConfigurationSection("classes").getKeys(false )) {
            if (target.equals(className)){
                return true;
            }
        }
        return false;
    }

    public void setClass(Player player, String newClass){
        plugin.getConfig().getConfigurationSection("classes").set(newClass,player.getUniqueId());
    }

    public String getPlayerClass(Player player) {
        for (String className : plugin.getConfig().getConfigurationSection("classes").getKeys(false)) {
            if (isClass(player, className)) {
                return className;
            }
        }
        return "None";
    }
}
