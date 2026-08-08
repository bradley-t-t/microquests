package com.trenton.microquests.commands;

import com.trenton.microquests.MicroQuests;
import com.trenton.microquests.competition.Competition;
import com.trenton.microquests.managers.CompetitionManager;
import com.trenton.microquests.managers.ConfigManager;
import com.trenton.coreapi.annotations.CoreCommand;
import com.trenton.coreapi.api.CoreCommandHandler;
import com.trenton.coreapi.util.MessageUtils;
import java.util.Set;
import java.util.UUID;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

@CoreCommand(
   name = "quest"
)
public class QuestCommand implements CoreCommandHandler {
   private MicroQuests plugin;
   private ConfigManager configManager;
   private Set<UUID> optOut;

   public void init(MicroQuests plugin) {
      this.plugin = plugin;
      this.configManager = (ConfigManager)plugin.getCoreAPI().getManager("ConfigManager");
      if (this.configManager == null) {
         plugin.getLogger().severe("ConfigManager is null in QuestCommand.init");
      } else {
         this.optOut = this.configManager.getOptOut();
      }
   }

   public boolean execute(CommandSender sender, String label, String[] args) {
      if (sender instanceof Player player) {
         if (this.configManager == null) {
            this.plugin.getLogger().severe("ConfigManager is null in QuestCommand.execute");
            return true;
         } else if (args.length == 0) {
            MessageUtils.sendMessage(this.configManager.getMessages(), player, "command-invalid-usage");
            return true;
         } else {
            switch (args[0].toLowerCase()) {
               case "optout":
                  if (this.optOut.contains(player.getUniqueId())) {
                     this.optOut.remove(player.getUniqueId());
                     MessageUtils.sendMessage(this.configManager.getMessages(), player, "optout-disabled");
                  } else {
                     this.optOut.add(player.getUniqueId());
                     MessageUtils.sendMessage(this.configManager.getMessages(), player, "optout-enabled");
                  }

                  this.configManager.saveOptOut();
                  return true;
               case "status":
                  Competition comp = ((CompetitionManager)this.plugin.getCoreAPI().getManager("CompetitionManager")).getActiveCompetition();
                  if (comp != null && comp.isActive()) {
                     long timeLeft = this.plugin.getConfig().getLong("max-quest-time") - (System.currentTimeMillis() - comp.getStartTime()) / 1000L;
                     MessageUtils.sendMessage(this.configManager.getMessages(), player, "status-active", comp.getQuest().getObjective(), (int)timeLeft);
                  } else {
                     MessageUtils.sendMessage(this.configManager.getMessages(), player, "status-no-competition");
                  }

                  return true;
               default:
                  MessageUtils.sendMessage(this.configManager.getMessages(), player, "command-invalid-usage");
                  return true;
            }
         }
      } else {
         if (this.configManager != null) {
            MessageUtils.sendMessage(this.configManager.getMessages(), sender, "command-player-only");
         }

         return true;
      }
   }
}
