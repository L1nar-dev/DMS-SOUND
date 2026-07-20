package ru.portal.worldmusic;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.playback.AudioFrame;
import de.maxhenkel.voicechat.api.VoicechatConnection;
import de.maxhenkel.voicechat.api.VoicechatServerApi;
import de.maxhenkel.voicechat.api.audio.channels.StaticAudioChannel;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import java.io.File;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.*;

public class WorldAudioManager {
    private final World world;
    private final AudioPlayer player;
    private final Map<UUID, StaticAudioChannel> channels = new ConcurrentHashMap<>();
    private ScheduledExecutorService executor;
    private ScheduledFuture<?> ticker;

    public WorldAudioManager(World world, AudioPlayerManager manager) {
        this.world = world;
        this.player = manager.createPlayer();
    }

    public void play(File file, float volume) {
        stop();
        player.setVolume((int) (volume * 100));

        WorldMusic.getInstance().getMusicManager().getPlayerManager().loadItem(file.getAbsolutePath(), new com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler() {
            @Override
            public void trackLoaded(AudioTrack track) {
                player.playTrack(track);
                startTicker();
            }

            @Override
            public void playlistLoaded(com.sedmelluq.discord.lavaplayer.track.AudioPlaylist playlist) {}

            @Override
            public void noMatches() {
                WorldMusic.getInstance().getLogger().warning("Файл не найден для проигрывания: " + file.getName());
            }

            @Override
            public void loadFailed(com.sedmelluq.discord.lavaplayer.tools.FriendlyException exception) {
                WorldMusic.getInstance().getLogger().severe("Ошибка загрузки файла трека: " + exception.getMessage());
            }
        });
    }

    private synchronized void startTicker() {
        executor = Executors.newSingleThreadScheduledExecutor();
        ticker = executor.scheduleAtFixedRate(this::tick, 0, 20, TimeUnit.MILLISECONDS);
    }

    private void tick() {
        try {
            if (player.getPlayingTrack() == null) {
                stop();
                return;
            }

            AudioFrame frame = player.provide();
            if (frame == null) return;

            byte[] data = frame.getData();
            short[] samples = new short[data.length / 2];
            for (int i = 0; i < samples.length; i++) {
                samples[i] = (short) (((data[i * 2] & 0xFF) << 8) | (data[i * 2 + 1] & 0xFF));
            }

            VoicechatServerApi api = VoicechatPluginImpl.getApi();
            if (api == null) return;

            // Динамическое отключение
            channels.keySet().removeIf(uuid -> {
                Player p = Bukkit.getPlayer(uuid);
                return p == null || !p.getWorld().equals(world);
            });

            // Динамическое подключение
            for (Player p : world.getPlayers()) {
                VoicechatConnection conn = api.getConnectionOf(p.getUniqueId());
                if (conn != null && conn.isInstalled()) {
                    // Новое SVC API: Передаем ID, ServerLevel и Соединение игрока
                    StaticAudioChannel channel = channels.computeIfAbsent(p.getUniqueId(), uuid -> 
                        api.createStaticAudioChannel(UUID.randomUUID(), api.fromServerLevel(world), conn)
                    );
                    channel.send(samples);
                }
            }
        } catch (Exception e) {
            WorldMusic.getInstance().getLogger().severe("Критическая ошибка в аудио-потоке мира " + world.getName() + ": " + e.getMessage());
        }
    }

    public synchronized void stop() {
        player.stopTrack();
        if (ticker != null) {
            ticker.cancel(false);
            ticker = null;
        }
        if (executor != null) {
            executor.shutdown();
            executor = null;
        }
        channels.clear();
    }
}
