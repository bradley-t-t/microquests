package com.trenton.microquests.listeners;

import com.trenton.microquests.MicroQuests;
import com.trenton.microquests.competition.Competition;
import com.trenton.microquests.competition.quests.CraftQuest;
import com.trenton.microquests.competition.quests.GatherQuest;
import com.trenton.microquests.competition.quests.KillQuest;
import com.trenton.microquests.competition.quests.Quest;
import com.trenton.microquests.managers.CompetitionManager;
import com.trenton.microquests.managers.ConfigManager;
import com.trenton.coreapi.annotations.CoreListener;
import com.trenton.coreapi.api.CoreListenerInterface;
import java.util.Set;
import java.util.UUID;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;

@CoreListener(
   name = "CompetitionListener"
)
public class CompetitionListener implements CoreListenerInterface {
   private MicroQuests plugin;
   private ConfigManager configManager;
   private Set<UUID> optOut;

   public void init(MicroQuests plugin) {
      this.plugin = plugin;
      this.configManager = (ConfigManager)plugin.getCoreAPI().getManager("ConfigManager");
      if (this.configManager != null) {
         this.optOut = this.configManager.getOptOut();
      }

   }

   public void handleEvent(Event event) {
      if (this.configManager != null && this.optOut != null) {
         Player player = null;
         Competition comp = ((CompetitionManager)this.plugin.getCoreAPI().getManager("CompetitionManager")).getActiveCompetition();
         if (comp != null && comp.isActive()) {
            if (event instanceof EntityDeathEvent) {
               EntityDeathEvent deathEvent = (EntityDeathEvent)event;
               player = deathEvent.getEntity().getKiller();
               if (player != null && !this.optOut.contains(player.getUniqueId())) {
                  Quest var9 = comp.getQuest();
                  if (var9 instanceof KillQuest) {
                     KillQuest quest = (KillQuest)var9;
                     if (quest.getMob() == deathEvent.getEntityType()) {
                        comp.incrementProgress(player.getUniqueId());
                     }
                  }
               }
            } else if (event instanceof BlockBreakEvent) {
               BlockBreakEvent breakEvent = (BlockBreakEvent)event;
               player = breakEvent.getPlayer();
               if (player != null && !this.optOut.contains(player.getUniqueId())) {
                  Quest var19 = comp.getQuest();
                  if (var19 instanceof GatherQuest) {
                     GatherQuest quest = (GatherQuest)var19;
                     if (quest.getItem() == breakEvent.getBlock().getType()) {
                        comp.incrementProgress(player.getUniqueId());
                     }
                  }
               }
            } else if (event instanceof PlayerPickupItemEvent) {
               PlayerPickupItemEvent pickupEvent = (PlayerPickupItemEvent)event;
               player = pickupEvent.getPlayer();
               if (player != null && !this.optOut.contains(player.getUniqueId())) {
                  Quest var20 = comp.getQuest();
                  if (var20 instanceof GatherQuest) {
                     GatherQuest quest = (GatherQuest)var20;
                     if (quest.getItem() == pickupEvent.getItem().getItemStack().getType()) {
                        comp.incrementProgress(player.getUniqueId());
                     }
                  }
               }
            } else if (event instanceof CraftItemEvent) {
               CraftItemEvent craftEvent = (CraftItemEvent)event;
               HumanEntity var21 = craftEvent.getWhoClicked();
               if (var21 instanceof Player) {
                  Player craftPlayer = (Player)var21;
                  player = craftPlayer;
                  if (craftPlayer != null && !this.optOut.contains(craftPlayer.getUniqueId())) {
                     Quest var10 = comp.getQuest();
                     if (var10 instanceof CraftQuest) {
                        CraftQuest quest = (CraftQuest)var10;
                        if (craftEvent.getCurrentItem() != null && quest.getItem() == craftEvent.getCurrentItem().getType()) {
                           int amountCrafted = craftEvent.getCurrentItem().getAmount();

                           for(int i = 0; i < amountCrafted; ++i) {
                              comp.incrementProgress(player.getUniqueId());
                           }
                        }
                     }
                  }
               }
            }

         }
      }
   }

   public Class<? extends Event>[] getHandledEvents() {
      return new Class[]{EntityDeathEvent.class, BlockBreakEvent.class, PlayerPickupItemEvent.class, CraftItemEvent.class};
   }
}
