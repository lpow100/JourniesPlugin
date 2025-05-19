package trinary.stars.journies;

import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.entity.Player;

public class ClassManager {
    private final JavaPlugin plugin;

    public ClassManager(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public boolean isClass(Player player, String className) {
        String uuid = plugin.getConfig().getString("classes." + className);
        return uuid != null && uuid.equalsIgnoreCase(player.getUniqueId().toString());
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
