package trinary.stars.journies;

import org.bukkit.plugin.java.JavaPlugin;

public final class Journies extends JavaPlugin {
    private ClassManager classManager;
    private MoneyManager moneyManager;
    @Override
    public void onEnable() {
        classManager = new ClassManager(this);
        this.getCommand("classinfo").setExecutor(new ClassInfoCommand(classManager));
        this.getCommand("setclass").setExecutor(new SetClassCommand(classManager));
        moneyManager = new MoneyManager(this);
        this.getCommand("pay").setExecutor(new PayCommand(moneyManager));
        this.getCommand("bal").setExecutor(new BalCommand(moneyManager));
        getServer().getPluginManager().registerEvents(new ClassListener(classManager,moneyManager,this), this);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
