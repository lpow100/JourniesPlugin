package trinary.stars.journies;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class MoneyManager {
    private final JavaPlugin plugin;
    private final FileConfiguration config;

    public MoneyManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.config = plugin.getConfig();
    }

    private String getKey(Player player) {
        return "money." + player.getUniqueId();
    }

    public double getBalance(Player player) {
        return config.getDouble(getKey(player), 0.0);
    }

    public void setBalance(Player player, double amount) {
        config.set(getKey(player), amount);
        save();
    }

    public void addMoney(Player player, double amount) {
        double current = getBalance(player);
        setBalance(player, current + amount);
    }

    public boolean takeMoney(Player player, double amount) {
        double current = getBalance(player);
        if (current < amount) return false;
        setBalance(player, current - amount);
        return true;
    }

    private void save() {
        plugin.saveConfig();
    }
}
