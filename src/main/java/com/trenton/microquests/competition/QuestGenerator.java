package com.trenton.microquests.competition;

import com.trenton.microquests.MicroQuests;
import com.trenton.microquests.competition.quests.CraftQuest;
import com.trenton.microquests.competition.quests.GatherQuest;
import com.trenton.microquests.competition.quests.KillQuest;
import com.trenton.microquests.competition.quests.Quest;
import com.trenton.microquests.managers.ConfigManager;
import com.trenton.coreapi.util.MessageUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

/**
 * Picks a random quest from the types that have valid entries in
 * {@code quests.yml}, weighting kill quests when any player is underground
 * and gather quests otherwise. Returns null when nothing valid is
 * configured.
 */
public class QuestGenerator {
   private final MicroQuests plugin;
   private final ConfigManager configManager;
   private final Random random;

   public QuestGenerator(MicroQuests plugin) {
      this.plugin = plugin;
      this.configManager = (ConfigManager)plugin.getCoreAPI().getManager("ConfigManager");
      this.random = new Random();
   }

   public Quest generateQuest() {
      List<Player> players = new ArrayList<>(Bukkit.getOnlinePlayers());
      boolean underground = players.stream().anyMatch((p) -> p.getLocation().getY() < 50.0);
      List<String> possibleTypes = new ArrayList<>();
      if (!this.configManager.getValidKillMobs().isEmpty()) {
         possibleTypes.add("kill");
      }

      if (!this.configManager.getValidGatherItems().isEmpty()) {
         possibleTypes.add("gather");
      }

      if (!this.configManager.getValidCraftItems().isEmpty()) {
         possibleTypes.add("craft");
      }

      if (possibleTypes.isEmpty()) {
         this.plugin.getLogger().warning("No valid quest configurations found. Cannot generate quest.");
         return null;
      } else {
         List<String> weightedTypes = new ArrayList<>();
         if (underground) {
            if (possibleTypes.contains("kill")) {
               weightedTypes.add("kill");
               weightedTypes.add("kill");
            }

            if (possibleTypes.contains("gather")) {
               weightedTypes.add("gather");
            }

            if (possibleTypes.contains("craft")) {
               weightedTypes.add("craft");
            }
         } else {
            if (possibleTypes.contains("gather")) {
               weightedTypes.add("gather");
               weightedTypes.add("gather");
            }

            if (possibleTypes.contains("craft")) {
               weightedTypes.add("craft");
            }

            if (possibleTypes.contains("kill")) {
               weightedTypes.add("kill");
            }
         }

         if (weightedTypes.isEmpty()) {
            this.plugin.getLogger().warning("No quests available for the current context.");
            return null;
         } else {
            switch (weightedTypes.get(this.random.nextInt(weightedTypes.size()))) {
               case "kill" -> {
                  return this.generateKillQuest();
               }
               case "gather" -> {
                  return this.generateGatherQuest();
               }
               case "craft" -> {
                  return this.generateCraftQuest();
               }
               default -> {
                  return null;
               }
            }
         }
      }
   }

   private Quest generateKillQuest() {
      List<EntityType> mobs = this.configManager.getValidKillMobs();
      if (mobs.isEmpty()) {
         return null;
      } else {
         EntityType mob = mobs.get(this.random.nextInt(mobs.size()));
         int minAmount = this.configManager.getKillMinAmount();
         int maxAmount = this.configManager.getKillMaxAmount();
         int amount = minAmount + this.random.nextInt(maxAmount - minAmount + 1);
         String name = MessageUtils.formatEnumName(mob.name());
         String plural = amount > 1 ? "s" : "";
         String objective = "Kill " + amount + " " + name + plural;
         return new KillQuest(mob, amount, objective);
      }
   }

   private Quest generateGatherQuest() {
      List<Material> items = this.configManager.getValidGatherItems();
      if (items.isEmpty()) {
         return null;
      } else {
         Material item = items.get(this.random.nextInt(items.size()));
         int minAmount = this.configManager.getGatherMinAmount();
         int maxAmount = this.configManager.getGatherMaxAmount();
         int amount = minAmount + this.random.nextInt(maxAmount - minAmount + 1);
         String name = MessageUtils.formatEnumName(item.name());
         String plural = amount > 1 ? "s" : "";
         String objective = "Gather " + amount + " " + name + plural;
         return new GatherQuest(item, amount, objective);
      }
   }

   private Quest generateCraftQuest() {
      List<Material> items = this.configManager.getValidCraftItems();
      if (items.isEmpty()) {
         return null;
      } else {
         Material item = items.get(this.random.nextInt(items.size()));
         int minAmount = this.configManager.getCraftMinAmount();
         int maxAmount = this.configManager.getCraftMaxAmount();
         int amount = minAmount + this.random.nextInt(maxAmount - minAmount + 1);
         String name = MessageUtils.formatEnumName(item.name());
         String plural = amount > 1 ? "s" : "";
         String objective = "Craft " + amount + " " + name + plural;
         return new CraftQuest(item, amount, objective);
      }
   }
}
