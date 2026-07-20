package ru.portal.worldmusic;

import com.sedmelluq.discord.lavaplayer.format.PcmAudioDataFormat;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import org.bukkit.World;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class MusicManager {
    private final AudioPlayerManager playerManager;
    private final Map<String, WorldAudioManager> managers = new ConcurrentHashMap<>();

    public MusicManager() {
        this.playerManager = new DefaultAudioPlayerManager();
        
        // Lavaplayer 2.x Fix: используем PcmAudioDataFormat вместо абстрактного AudioDataFormat
        playerManager.getConfiguration().setOutputFormat(
            new PcmAudioDataFormat(1, 48000, 960, true)
        );

        // Включаем ТОЛЬКО локальные источники
        AudioSourceManagers.registerLocalSource(playerManager);
    }

    public WorldAudioManager getOrCreateManager(World world) {
        return managers.computeIfAbsent(world.getName(), name -> new WorldAudioManager(world, playerManager));
    }

    public void stopAll() {
        managers.values().forEach(WorldAudioManager::stop);
        managers.clear();
    }

    public void shutdown() {
        stopAll();
        playerManager.shutdown();
    }

    public AudioPlayerManager getPlayerManager() {
        return playerManager;
    }
}
