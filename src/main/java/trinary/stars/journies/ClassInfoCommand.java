package trinary.stars.journies;

import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.Command;
import org.bukkit.entity.Player;

public class ClassInfoCommand implements CommandExecutor {
    private final ClassManager classManager;

    public ClassInfoCommand(ClassManager manager) {
        this.classManager = manager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (sender instanceof Player p) {
            String className = classManager.getPlayerClass(p);
            if (!className.equals("None")) {
                p.sendMessage("§6You are the " + className + "!");
            } else {
                p.sendMessage("§cYou Don't Have a Class Yet...");
            }
        }
        return true;
    }
}
