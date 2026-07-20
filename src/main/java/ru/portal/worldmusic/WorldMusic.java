package ru.portal.worldmusic;

import de.maxhenkel.voicechat.api.BukkitVoicechatService;
import org.bukkit.plugin.java.JavaPlugin;
import java.io.File;

public class WorldMusic extends JavaPlugin {
    private static WorldMusic instance;
    private MusicManager musicManager;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();

        File tracksDir = new File(getDataFolder(), "tracks");
        if (!tracksDir.exists()) {
            tracksDir.mkdirs();
        }

        musicManager = new MusicManager();

        // Регистрируем плагин в SVC API через новый BukkitVoicechatService
        BukkitVoicechatService service = getServer().getServicesManager().load(BukkitVoicechatService.class);
        if (service != null) {
            service.registerPlugin(new VoicechatPluginImpl());
            getLogger().info("Успешное подключение к Simple Voice Chat API!");
        } else {
            getLogger().severe("Simple Voice Chat не найден! Плагин выключается.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        getCommand("worldmusic").setExecutor(new MusicCommand(this));
    }

    @Override
    public void onDisable() {
        if (musicManager != null) {
            musicManager.shutdown();
        }
    }

    public static WorldMusic getInstance() {
        return instance;
    }

    public MusicManager getMusicManager() {
        return musicManager;
    }
}
