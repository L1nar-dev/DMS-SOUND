package ru.portal.worldmusic;

import com.sedmelluq.discord.lavaplayer.format.AudioDataFormat;
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
        
        // Настраиваем Lavaplayer на отдачу PCM 48000Hz Mono Signed Big-Endian (идеально для SVC)
        playerManager.getConfiguration().setOutputFormat(
            new AudioDataFormat(1, 48000, 960, AudioDataFormat.Codec.PCM_S16BE)
        );

        // Включаем ТОЛЬКО локальные источники (блокировка внешних сетей)
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
