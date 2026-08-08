package com.trenton.microquests.listeners;

import com.trenton.microquests.MicroQuests;
import com.trenton.microquests.competition.Competition;
import com.trenton.microquests.competition.quests.CraftQuest;
import com.trenton.microquests.competition.quests.GatherQuest;
import com.trenton.microquests.competition.quests.KillQuest;
import com.trenton.microquests.managers.CompetitionManager;
import com.trenton.microquests.managers.ConfigManager;
import com.trenton.coreapi.annotations.CoreListener;
import com.trenton.coreapi.api.CoreListenerInterface;
import java.util.Set;
import java.util.UUID;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.inventory.CraftItemEvent;

@CoreListener(
   name = "CompetitionListener"
)
public class CompetitionListener implements CoreListenerInterface {
   private MicroQuests plugin;
   private ConfigManager configManager;
   private Set<UUID> optOut;

   public void init(MicroQuests plugin) {
      this.plugin = plugin;
      this.configManager = (ConfigManager) plugin.getCoreAPI().getManager("ConfigManager");
      if (this.configManager != null) {
         this.optOut = this.configManager.getOptOut();
      }
   }

   public void handleEvent(Event event) {
      if (this.configManager != null && this.optOut != null) {
         Competition comp = ((CompetitionManager) this.plugin.getCoreAPI().getManager("CompetitionManager")).getActiveCompetition();
         if (comp != null && comp.isActive()) {
            if (event instanceof EntityDeathEvent deathEvent) {
               Player player = deathEvent.getEntity().getKiller();
               if (player != null && !this.optOut.contains(player.getUniqueId())
                     && comp.getQuest() instanceof KillQuest quest
                     && quest.getMob() == deathEvent.getEntityType()) {
                  comp.incrementProgress(player.getUniqueId());
               }
            } else if (event instanceof BlockBreakEvent breakEvent) {
               Player player = breakEvent.getPlayer();
               if (!this.optOut.contains(player.getUniqueId())
                     && comp.getQuest() instanceof GatherQuest quest
                     && quest.getItem() == breakEvent.getBlock().getType()) {
                  comp.incrementProgress(player.getUniqueId());
               }
            } else if (event instanceof EntityPickupItemEvent pickupEvent) {
               if (pickupEvent.getEntity() instanceof Player player
                     && !this.optOut.contains(player.getUniqueId())
                     && comp.getQuest() instanceof GatherQuest quest
                     && quest.getItem() == pickupEvent.getItem().getItemStack().getType()) {
                  comp.incrementProgress(player.getUniqueId());
               }
            } else if (event instanceof CraftItemEvent craftEvent) {
               if (craftEvent.getWhoClicked() instanceof Player player
                     && !this.optOut.contains(player.getUniqueId())
                     && comp.getQuest() instanceof CraftQuest quest
                     && craftEvent.getCurrentItem() != null
                     && quest.getItem() == craftEvent.getCurrentItem().getType()) {
                  int amountCrafted = craftEvent.getCurrentItem().getAmount();

                  for (int i = 0; i < amountCrafted; ++i) {
                     comp.incrementProgress(player.getUniqueId());
                  }
               }
            }
         }
      }
   }

   public Class<? extends Event>[] getHandledEvents() {
      return new Class[]{EntityDeathEvent.class, BlockBreakEvent.class, EntityPickupItemEvent.class, CraftItemEvent.class};
   }
}
