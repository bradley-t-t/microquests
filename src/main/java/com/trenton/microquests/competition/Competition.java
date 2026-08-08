package com.trenton.microquests.competition;

import com.trenton.microquests.MicroQuests;
import com.trenton.microquests.competition.quests.Quest;
import com.trenton.microquests.managers.RewardManager;
import com.trenton.coreapi.util.MessageUtils;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * One server-wide quest race: every participating player works on the same
 * objective, and the first to reach the target amount wins and is rewarded.
 * A competition that reaches its configured time limit ends with no winner.
 */
public class Competition {
   private final MicroQuests plugin;
   private final Quest quest;
   private final Map<UUID, Integer> progress;
   private final long startTime;
   private boolean active;

   public Competition(MicroQuests plugin, Quest quest) {
      this.plugin = plugin;
      this.quest = quest;
      this.progress = new HashMap<>();
      this.startTime = System.currentTimeMillis();
      this.active = false;
   }

   public void start() {
      this.active = true;
      MessageUtils.broadcast(this.plugin, this.plugin.getCoreAPI().getMessages(), "competition-start", this.quest.getObjective());
      MessageUtils.sendTitle(this.plugin, this.plugin.getCoreAPI().getMessages(), "competition-start-title", "competition-start-subtitle", this.quest.getObjective());
      (new BukkitRunnable() {
         public void run() {
            if (Competition.this.active) {
               Competition.this.end(null);
            }

         }
      }).runTaskLater(this.plugin, this.plugin.getConfig().getLong("max-quest-time") * 20L);
   }

   /**
    * Ends the competition. A null winner means it expired unclaimed.
    */
   public void end(Player winner) {
      if (this.active) {
         this.active = false;
         if (winner != null) {
            MessageUtils.broadcast(this.plugin, this.plugin.getCoreAPI().getMessages(), "competition-win", winner.getName(), this.quest.getObjective());
            MessageUtils.sendTitle(this.plugin, this.plugin.getCoreAPI().getMessages(), "competition-win-title", "competition-win-subtitle", winner.getName(), this.quest.getObjective());
            ((RewardManager)this.plugin.getCoreAPI().getManager("RewardManager")).rewardWinner(winner, this.quest);
         } else {
            MessageUtils.broadcast(this.plugin, this.plugin.getCoreAPI().getMessages(), "competition-expired");
         }

         this.progress.clear();
      }
   }

   public Quest getQuest() {
      return this.quest;
   }

   public long getStartTime() {
      return this.startTime;
   }

   public void incrementProgress(UUID uuid) {
      if (this.active) {
         Player player = Bukkit.getPlayer(uuid);
         if (player != null) {
            this.progress.merge(uuid, 1, Integer::sum);
            MessageUtils.sendActionBar(this.plugin.getCoreAPI().getMessages(), player, "progress-update", this.quest.getObjective(), this.progress.get(uuid), this.quest.getAmount());
            if (this.progress.get(uuid) >= this.quest.getAmount()) {
               this.end(player);
            }

         }
      }
   }

   public int getProgress(UUID uuid) {
      return this.progress.getOrDefault(uuid, 0);
   }

   public boolean isActive() {
      return this.active;
   }
}
