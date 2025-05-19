package trinary.stars.journies;

import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.Command;
import org.bukkit.entity.Player;

public abstract class ClassInfoCommand implements CommandExecutor {
    private final ClassManager classManager;

    public ClassInfoCommand(ClassManager manager) {
        this.classManager = manager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (sender instanceof Player p) {
            String className = classManager.getPlayerClass(p);
            p.sendMessage("§6You are the " + className + "!");
        }
        return true;
    }
}
