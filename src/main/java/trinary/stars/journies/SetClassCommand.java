package trinary.stars.journies;

import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.Command;
import org.bukkit.entity.Player;
import org.bukkit.Bukkit;
import org.bukkit.command.ConsoleCommandSender;

public class SetClassCommand implements CommandExecutor {
    private final ClassManager classManager;

    public SetClassCommand(ClassManager manager) {
        this.classManager = manager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (!(sender instanceof Player) && !(sender instanceof ConsoleCommandSender)) {
            sender.sendMessage("Only players or console can use this command.");
            return true;
        }

        if (sender instanceof Player player && !player.hasPermission("classplugin.admin.setclass")) {
            player.sendMessage("§cYou do not have permission to use this command.");
            return true;
        }

        if (args.length != 2) {
            sender.sendMessage("§cUsage: /setclass <player> <class>");
            return true;
        }

        Player target = Bukkit.getPlayerExact(args[0]);
        if (target == null) {
            sender.sendMessage("§cThat player is not online.");
            return true;
        }

        String className = args[1].toLowerCase();

        // You can validate class names here
        if (!classManager.isValidClass(className)) {
            sender.sendMessage("§cInvalid class name: " + className);
            return true;
        }

        classManager.setClass(target, className);
        sender.sendMessage("§aSet " + target.getName() + "'s class to " + className);
        target.sendMessage("§eYour class has been set to " + className + " by an admin.");

        return true;
    }
}