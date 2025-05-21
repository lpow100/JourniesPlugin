package trinary.stars.journies;

import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.Command;
import org.bukkit.entity.Player;
import org.bukkit.Bukkit;

public class PayCommand implements CommandExecutor {
    private final MoneyManager moneyManager;

    public PayCommand(MoneyManager moneyManager) {
        this.moneyManager = moneyManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length != 2) return false;

        Player target = Bukkit.getPlayer(args[0]);
        double amount;
        try {
            amount = Double.parseDouble(args[1]);
        } catch (NumberFormatException e) {
            sender.sendMessage("§cInvalid amount.");
            return true;
        }

        if(moneyManager.getBalance(Bukkit.getPlayer(sender.getName())) < amount){
            sender.sendMessage("§cYou don't enough money");
            return true;
        }

        if (target == null) {
            sender.sendMessage("§cPlayer not found.");
            return true;
        }

        moneyManager.addMoney(target, amount);
        moneyManager.takeMoney(Bukkit.getPlayer(sender.getName()), amount);
        sender.sendMessage("§aGave §e$" + amount + " §ato §f" + target.getName());
        target.sendMessage("§aYou received §e$" + amount + " §amoney!");
        return true;
    }
}

