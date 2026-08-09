package com.trenton.microquests.managers;

import com.trenton.microquests.MicroQuests;
import com.trenton.microquests.competition.Competition;
import com.trenton.microquests.competition.QuestGenerator;
import com.trenton.microquests.competition.quests.Quest;
import com.trenton.coreapi.annotations.CoreManager;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * Starts a new competition on a fixed interval whenever none is running and
 * enough players are online.
 */
@CoreManager(
   name = "CompetitionManager"
)
public class CompetitionManager {
   private MicroQuests plugin;
   private Competition activeCompetition;
   private ConfigManager configManager;
   private long lastStartMillis;

   public void init(MicroQuests plugin) {
      this.plugin = plugin;
      this.configManager = (ConfigManager)plugin.getCoreAPI().getManager("ConfigManager");
      // A fresh boot counts as a start, so the first competition arrives one
      // interval in rather than the moment enough players are on.
      this.lastStartMillis = System.currentTimeMillis();
      this.startInterval();
   }

   public void shutdown() {
      if (this.activeCompetition != null && this.activeCompetition.isActive()) {
         this.activeCompetition.end(null);
      }
   }

   private void startInterval() {
      (new BukkitRunnable() {
         public void run() {
            ConfigManager localConfigManager = CompetitionManager.this.configManager;
            if (localConfigManager == null) {
               localConfigManager = (ConfigManager)CompetitionManager.this.plugin.getCoreAPI().getManager("ConfigManager");
               if (localConfigManager == null) {
                  CompetitionManager.this.plugin.getLogger().warning("ConfigManager is null in CompetitionManager task. Skipping execution.");
                  return;
               }

               CompetitionManager.this.configManager = localConfigManager;
            }

            if (CompetitionManager.this.activeCompetition == null || !CompetitionManager.this.activeCompetition.isActive()) {
               long intervalMillis = localConfigManager.getConfig().getInt("interval", 900) * 1000L;
               if (System.currentTimeMillis() - CompetitionManager.this.lastStartMillis < intervalMillis) {
                  return;
               }

               int onlinePlayers = Bukkit.getOnlinePlayers().size();
               int minPlayers = localConfigManager.getConfig().getInt("min-players");
               if (onlinePlayers >= minPlayers) {
                  Quest quest = (new QuestGenerator(CompetitionManager.this.plugin)).generateQuest();
                  if (quest != null) {
                     CompetitionManager.this.activeCompetition = new Competition(CompetitionManager.this.plugin, quest);
                     CompetitionManager.this.activeCompetition.start();
                     CompetitionManager.this.lastStartMillis = System.currentTimeMillis();
                  }
               }
            }

         }
      }).runTaskTimer(this.plugin, 60L, 1200L);
   }

   public Competition getActiveCompetition() {
      return this.activeCompetition;
   }
}
