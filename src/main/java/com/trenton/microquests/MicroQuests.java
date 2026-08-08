package com.trenton.microquests;

import org.bstats.bukkit.Metrics;
import com.trenton.coreapi.api.CoreAPI;
import com.trenton.updater.api.UpdaterImpl;
import com.trenton.updater.api.UpdaterService;
import java.io.File;
import org.bukkit.plugin.java.JavaPlugin;

public final class MicroQuests extends JavaPlugin {
   private UpdaterService updater;
   private CoreAPI coreAPI;

   public void onEnable() {
      this.saveDefaultConfig();
      this.setupFiles();
      this.initialize();
      this.setupUpdater();
      this.setupMetrics();
   }

   public void initialize() {
      String packageName = this.getClass().getPackageName();
      this.coreAPI = new CoreAPI(this, packageName);
      this.coreAPI.initialize();
   }

   private void setupFiles() {
      File messagesFile = new File(this.getDataFolder(), "messages.yml");
      if (!messagesFile.exists()) {
         this.saveResource("messages.yml", false);
      }

      File questsFile = new File(this.getDataFolder(), "quests.yml");
      if (!questsFile.exists()) {
         this.saveResource("quests.yml", false);
      }

   }

   public void setupUpdater() {
      this.updater = new UpdaterImpl(this, 124181);
      boolean autoUpdate = this.getConfig().getBoolean("auto_updater.enabled", true);
      this.updater.checkForUpdates(autoUpdate);
   }

   public void setupMetrics() {
      new Metrics(this, 25514);
   }

   public void onDisable() {
      if (this.coreAPI != null) {
         this.coreAPI.shutdown();
      }

      if (this.updater != null) {
         this.updater.handleUpdateOnShutdown();
      }

   }

   public CoreAPI getCoreAPI() {
      return this.coreAPI;
   }

   public UpdaterService getUpdater() {
      return this.updater;
   }
}
