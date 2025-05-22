package trinary.stars.journies;

import org.bukkit.plugin.Plugin;
import org.bukkit.event.Listener;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.entity.Player;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.entity.Animals;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ExperienceOrb;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityBreedEvent;
import org.bukkit.event.entity.EntityInteractEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;

import java.util.Collection;


public class ClassListener implements Listener {
    private final ClassManager classManager;
    private final MoneyManager moneyManager;
    private final Plugin plugin;

    public ClassListener(ClassManager manager, MoneyManager manager2, Plugin plug) {
        this.classManager = manager;
        this.moneyManager = manager2;
        this.plugin = plug;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e) {
        Player p = e.getPlayer();

        if (!p.hasPlayedBefore()) {
            // Give starting balance or other first-time setup
            moneyManager.setBalance(p, 20*1000); // Starting money
        }
        if (classManager.isClass(p, "warrior")) {
            p.getAttribute(Attribute.MAX_ABSORPTION).setBaseValue(24); // +2 hearts
            p.getAttribute(Attribute.MOVEMENT_SPEED).setBaseValue(p.getAttribute(Attribute.MOVEMENT_SPEED).getValue()*0.9);
            p.getAttribute(Attribute.ATTACK_SPEED).setBaseValue(p.getAttribute(Attribute.ATTACK_SPEED).getValue()*0.9);
            p.getAttribute(Attribute.ATTACK_DAMAGE).setBaseValue(p.getAttribute(Attribute.ATTACK_DAMAGE).getValue()*1.25);
        }
    }

    @EventHandler
    public void onPlayerKill(PlayerDeathEvent event) {
        Player dead = event.getEntity();
        Player killer = dead.getKiller();

        if (killer != null) {
            // Reward money
            double deadBal = moneyManager.getBalance(dead);
            double returnPercent = 0.35;
            String killerClass = classManager.getPlayerClass(killer);
            String deadClass = classManager.getPlayerClass(dead);
            if (killerClass.equals("Rogue")) {
                returnPercent += 0.15;
            }
            if (deadClass.equals("Entrepreneur")) {
                returnPercent -= 0.15;
            }

            double kept = deadBal * (1 - returnPercent);
            double lost = deadBal * returnPercent;
            killer.sendMessage("§aYou earned §e$" + lost +" $ §afor killing another player.");
            dead.sendMessage("§cYou lost §e$" + lost + "$ §cfrom being killed by another player.\nYou still have §e" + kept + "$ §cleft.");
        }
    }

    @EventHandler
    public void onMinerBreakOre(BlockBreakEvent event) {
        Player player = event.getPlayer();
        if (!classManager.isClass(player,"Miner")) return;

        Block block = event.getBlock();
        Material type = block.getType();

        // Only boost ore blocks
        if (isOre(type)) {
            // Drop extra ore
            block.getWorld().dropItemNaturally(block.getLocation(), new ItemStack(type));
            // Extra XP
            block.getWorld().spawn(block.getLocation(), ExperienceOrb.class).setExperience(5);
            player.sendMessage("§bMiner bonus! Extra ore and XP.");
        }
    }

    private boolean isOre(Material material) {
        return switch (material) {
            case COAL_ORE, DEEPSLATE_COAL_ORE,
                 IRON_ORE, DEEPSLATE_IRON_ORE,
                 GOLD_ORE, DEEPSLATE_GOLD_ORE,
                 COPPER_ORE, DEEPSLATE_COPPER_ORE,
                 REDSTONE_ORE, DEEPSLATE_REDSTONE_ORE,
                 LAPIS_ORE, DEEPSLATE_LAPIS_ORE,
                 DIAMOND_ORE, DEEPSLATE_DIAMOND_ORE,
                 EMERALD_ORE, DEEPSLATE_EMERALD_ORE -> true;
            default -> false;
        };
    }

    // ----- FARMER -----

    @EventHandler
    public void onFarmerHarvest(BlockBreakEvent event) {
        Player player = event.getPlayer();
        if (!classManager.getPlayerClass(player).equalsIgnoreCase("Farmer")) return;

        Material type = event.getBlock().getType();

        // Boost certain crop drops
        if (type == Material.WHEAT || type == Material.CARROTS || type == Material.POTATOES || type == Material.BEETROOTS) {
            event.getBlock().getDrops().forEach(drop -> {
                ItemStack bonus = drop.clone();
                bonus.setAmount(drop.getAmount()); // duplicate amount
                event.getBlock().getWorld().dropItemNaturally(event.getBlock().getLocation(), bonus);
            });
            player.sendMessage("§aFarmer bonus! Extra crops harvested.");
        }
    }

    @EventHandler
    public void onFarmerBreed(EntityBreedEvent event) {
        if (!(event.getBreeder() instanceof Player player)) return;
        if (!classManager.getPlayerClass(player).equalsIgnoreCase("Farmer")) return;

        Entity baby = event.getEntity();
        if (baby instanceof Animals animal) {
            animal.setAge((int) (animal.getAge() * 0.5)); // Grows up twice as fast
            player.sendMessage("§eFarmer bonus! Animal will grow faster.");
        }
    }

    @EventHandler
    public void onSuperfoodCraft(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (!classManager.getPlayerClass(player).equalsIgnoreCase("Farmer")) return;

        ItemStack hand = player.getInventory().getItemInMainHand();
        if (hand.getType() == Material.GOLDEN_CARROT) {
            ItemStack superFood = new ItemStack(Material.GOLDEN_CARROT);
            ItemMeta meta = superFood.getItemMeta();
            meta.setDisplayName("§6Superfood");
            meta.getPersistentDataContainer().set(new NamespacedKey(plugin, "superfood"), PersistentDataType.BYTE, (byte) 1);
            superFood.setItemMeta(meta);

            player.getInventory().setItemInMainHand(superFood);
            player.sendMessage("§dYou infused your food into §lSuperfood§d!");
        }
    }

    @EventHandler
    public void onEatSuperfood(PlayerItemConsumeEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItem();
        if (item.hasItemMeta() && item.getItemMeta().getPersistentDataContainer().has(new NamespacedKey(plugin, "superfood"), PersistentDataType.BYTE)) {
            // Apply potion effects, buffs, etc.
            player.sendMessage("§dYou feel ultra nourished!");
            // Example: maybe give speed or regen here
        }
    }

    @EventHandler
    public void onBrew(BrewEvent event) {
        // Loop through results and boost effect levels if Alchemist
        for (int i = 0; i < event.getContents().getSize(); i++) {
            ItemStack item = event.getContents().getItem(i);
            if (item == null || item.getType() != Material.POTION) continue;

            ItemMeta meta = item.getItemMeta();
            if (!(meta instanceof PotionMeta potionMeta)) continue;

            PotionData data = potionMeta.getBasePotionData();
            PotionType type = data.getType();

            // Boost common potions (not extended or splash)
            switch (type) {
                case SWIFTNESS, STRENGTH, REGENERATION, POISON, HARMING -> {
                    potionMeta.addCustomEffect(new PotionEffect(getEffectFromType(type), 20 * 60, 1), true); // Amplifier 1 = Level 2
                    item.setItemMeta(potionMeta);
                }
                default -> {} // Don't alter other types
            }
        }
    }

    private PotionEffectType getEffectFromType(PotionType type) {
        return switch (type) {
            case SWIFTNESS -> PotionEffectType.SPEED;
            case STRENGTH -> PotionEffectType.STRENGTH;
            case REGENERATION -> PotionEffectType.REGENERATION;
            case POISON -> PotionEffectType.POISON;
            case HARMING -> PotionEffectType.INSTANT_DAMAGE;
            default -> null;
        };
    }

}

