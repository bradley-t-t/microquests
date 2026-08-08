package com.trenton.microquests.managers;

import com.trenton.microquests.MicroQuests;
import com.trenton.microquests.competition.quests.Quest;
import com.trenton.coreapi.annotations.CoreManager;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.logging.Logger;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

@CoreManager(
   name = "RewardManager"
)
public class RewardManager {
   private MicroQuests plugin;
   private List<String> victoryCommands;
   private Random random;

   public void init(MicroQuests plugin) {
      this.plugin = plugin;
      this.victoryCommands = plugin.getConfig().getStringList("rewards.on-victory");
      this.random = new Random();
   }

   public void shutdown() {
   }

   public void rewardWinner(Player player, Quest quest) {
      boolean commandsExecuted = false;

      for(String cmd : this.victoryCommands) {
         try {
            if (cmd.startsWith("/")) {
               cmd = cmd.substring(1);
            }

            cmd = cmd.replace("{player}", player.getName()).replace("{quest}", quest.getObjective()).replace("{amount}", String.valueOf(quest.getAmount()));
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd);
            commandsExecuted = true;
         } catch (Exception e) {
            Logger var10000 = this.plugin.getLogger();
            String var10001 = player.getName();
            var10000.warning("Failed to execute reward command for " + var10001 + ": " + cmd + " (" + e.getMessage() + ")");
         }
      }

      if (!commandsExecuted) {
         String xpRange = this.plugin.getConfig().getString("rewards.fallback.xp", "0");
         int xp = this.parseRange(xpRange);
         if (xp > 0) {
            player.giveExp(xp);
         }

         for(Object obj : this.plugin.getConfig().getList("rewards.fallback.items", List.of())) {
            if (obj instanceof Map) {
               Map<String, Object> itemMap = (Map)obj;
               Material material = Material.getMaterial((String)itemMap.get("material"));
               if (material != null) {
                  int amount = this.parseRange((String)itemMap.get("amount"));
                  player.getInventory().addItem(new ItemStack[]{new ItemStack(material, amount)});
               }
            }
         }

         for(Object obj : this.plugin.getConfig().getList("rewards.fallback.buffs", List.of())) {
            if (obj instanceof Map) {
               Map<String, Object> buffMap = (Map)obj;
               PotionEffectType effect = PotionEffectType.getByName((String)buffMap.get("effect"));
               if (effect != null) {
                  int duration = ((Number)buffMap.get("duration")).intValue() * 20;
                  int amplifier = ((Number)buffMap.get("amplifier")).intValue();
                  player.addPotionEffect(new PotionEffect(effect, duration, amplifier));
               }
            }
         }
      }

   }

   private int parseRange(String range) {
      try {
         if (range.contains("-")) {
            String[] parts = range.split("-");
            int min = Integer.parseInt(parts[0]);
            int max = Integer.parseInt(parts[1]);
            return min + this.random.nextInt(max - min + 1);
         } else {
            return Integer.parseInt(range);
         }
      } catch (NumberFormatException var5) {
         return 0;
      }
   }
}
