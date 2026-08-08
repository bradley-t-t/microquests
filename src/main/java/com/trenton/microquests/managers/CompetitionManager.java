package com.trenton.microquests.managers;

import com.trenton.microquests.MicroQuests;
import com.trenton.microquests.competition.Competition;
import com.trenton.microquests.competition.QuestGenerator;
import com.trenton.coreapi.annotations.CoreManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

@CoreManager(
   name = "CompetitionManager"
)
public class CompetitionManager {
   private MicroQuests plugin;
   private Competition activeCompetition;
   private ConfigManager configManager;

   public void init(MicroQuests plugin) {
      this.plugin = plugin;
      this.configManager = (ConfigManager)plugin.getCoreAPI().getManager("ConfigManager");
      this.startInterval();
   }

   public void shutdown() {
      if (this.activeCompetition != null && this.activeCompetition.isActive()) {
         this.activeCompetition.end((Player)null);
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
               int onlinePlayers = Bukkit.getOnlinePlayers().size();
               int minPlayers = localConfigManager.getConfig().getInt("min-players");
               if (onlinePlayers >= minPlayers) {
                  CompetitionManager.this.activeCompetition = new Competition(CompetitionManager.this.plugin, (new QuestGenerator(CompetitionManager.this.plugin)).generateQuest());
                  CompetitionManager.this.activeCompetition.start();
               }
            }

         }
      }).runTaskTimer(this.plugin, 60L, 1200L);
   }

   public Competition getActiveCompetition() {
      return this.activeCompetition;
   }
}
