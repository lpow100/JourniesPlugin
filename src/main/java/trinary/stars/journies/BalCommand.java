package trinary.stars.journies;

import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.Command;
import org.bukkit.entity.Player;
import org.bukkit.Bukkit;

public class BalCommand implements CommandExecutor {
    private final MoneyManager moneyManager;

    public BalCommand(MoneyManager moneyManager) {
        this.moneyManager = moneyManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length != 2) return false;

        Player target = Bukkit.getPlayer(sender.getName());
        double money = moneyManager.getBalance(target);
        if (money < 0){
            sender.sendMessage("§cYou have §e$" + money + " §cmoney!");
            sender.sendMessage("§cYou are in debt");
        } else if (money == 0){
            sender.sendMessage("You have §e$" + money + " §fmoney!");
            sender.sendMessage("§cYou are broke");
        } else if (money > 0){
            sender.sendMessage("§aYou have §e$" + money + " §amoney!");
        }
        return true;
    }
}

