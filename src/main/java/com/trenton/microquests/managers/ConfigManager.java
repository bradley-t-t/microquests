package com.trenton.microquests.managers;

import com.trenton.microquests.MicroQuests;
import com.trenton.coreapi.annotations.CoreManager;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.EntityType;

@CoreManager(
   name = "ConfigManager"
)
public class ConfigManager {
   private MicroQuests plugin;
   private FileConfiguration config;
   private FileConfiguration messages;
   private FileConfiguration questsConfig;
   private File optOutFile;
   private FileConfiguration optOutConfig;
   private Set<UUID> optOut;
   private List<EntityType> validKillMobs;
   private List<Material> validGatherItems;
   private List<Material> validCraftItems;

   public void init(MicroQuests plugin) {
      this.plugin = plugin;
      this.config = plugin.getConfig();
      this.messages = plugin.getCoreAPI().getMessages();
      this.questsConfig = YamlConfiguration.loadConfiguration(new File(plugin.getDataFolder(), "quests.yml"));
      this.optOutFile = new File(plugin.getDataFolder(), "optout.yml");
      this.optOutConfig = YamlConfiguration.loadConfiguration(this.optOutFile);
      this.optOut = new HashSet();
      if (this.optOutConfig.contains("optout")) {
         for(String uuid : this.optOutConfig.getStringList("optout")) {
            this.optOut.add(UUID.fromString(uuid));
         }
      }

      this.validateQuestConfigs();
   }

   public void shutdown() {
      this.saveOptOut();
   }

   public FileConfiguration getConfig() {
      return this.config;
   }

   public FileConfiguration getMessages() {
      return this.messages;
   }

   public FileConfiguration getQuestsConfig() {
      return this.questsConfig;
   }

   public Set<UUID> getOptOut() {
      return this.optOut;
   }

   public void saveOptOut() {
      this.optOutConfig.set("optout", this.optOut.stream().map(UUID::toString).toList());

      try {
         this.optOutConfig.save(this.optOutFile);
      } catch (IOException e) {
         this.plugin.getLogger().severe("Failed to save optout.yml: " + e.getMessage());
      }

   }

   private void validateQuestConfigs() {
      this.validateMobs();
      this.validateGatherItems();
      this.validateCraftItems();
   }

   private void validateMobs() {
      List<String> mobs = this.questsConfig.getStringList("kill_quests.mobs");
      this.validKillMobs = new ArrayList();

      for(int i = 0; i < mobs.size(); ++i) {
         String mobName = (String)mobs.get(i);

         try {
            EntityType mob = EntityType.valueOf(mobName.toUpperCase());
            this.validKillMobs.add(mob);
         } catch (IllegalArgumentException var5) {
            this.plugin.getLogger().warning("Invalid mob type in quests.yml: " + mobName + ". Replacing with INVALID_TYPE.");
            mobs.set(i, "INVALID_TYPE");
         }
      }

      this.questsConfig.set("kill_quests.mobs", mobs);
   }

   private void validateGatherItems() {
      List<String> items = this.questsConfig.getStringList("gather_quests.items");
      this.validGatherItems = new ArrayList();

      for(int i = 0; i < items.size(); ++i) {
         String itemName = (String)items.get(i);

         try {
            Material item = Material.valueOf(itemName.toUpperCase());
            this.validGatherItems.add(item);
         } catch (IllegalArgumentException var5) {
            this.plugin.getLogger().warning("Invalid item type in quests.yml (gather): " + itemName + ". Replacing with INVALID_TYPE.");
            items.set(i, "INVALID_TYPE");
         }
      }

      this.questsConfig.set("gather_quests.items", items);
   }

   private void validateCraftItems() {
      List<String> items = this.questsConfig.getStringList("craft_quests.items");
      this.validCraftItems = new ArrayList();

      for(int i = 0; i < items.size(); ++i) {
         String itemName = (String)items.get(i);

         try {
            Material item = Material.valueOf(itemName.toUpperCase());
            this.validCraftItems.add(item);
         } catch (IllegalArgumentException var5) {
            this.plugin.getLogger().warning("Invalid item type in quests.yml (craft): " + itemName + ". Replacing with INVALID_TYPE.");
            items.set(i, "INVALID_TYPE");
         }
      }

      this.questsConfig.set("craft_quests.items", items);
   }

   public List<EntityType> getValidKillMobs() {
      return this.validKillMobs;
   }

   public List<Material> getValidGatherItems() {
      return this.validGatherItems;
   }

   public List<Material> getValidCraftItems() {
      return this.validCraftItems;
   }

   public int getKillMinAmount() {
      return this.questsConfig.getInt("kill_quests.min_amount", 3);
   }

   public int getKillMaxAmount() {
      return this.questsConfig.getInt("kill_quests.max_amount", 7);
   }

   public int getGatherMinAmount() {
      return this.questsConfig.getInt("gather_quests.min_amount", 5);
   }

   public int getGatherMaxAmount() {
      return this.questsConfig.getInt("gather_quests.max_amount", 10);
   }

   public int getCraftMinAmount() {
      return this.questsConfig.getInt("craft_quests.min_amount", 1);
   }

   public int getCraftMaxAmount() {
      return this.questsConfig.getInt("craft_quests.max_amount", 5);
   }
}
