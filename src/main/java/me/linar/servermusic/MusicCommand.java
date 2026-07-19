package me.linar.servermusic;

import com.google.common.io.ByteStreams;
import com.google.common.io.ByteArrayDataOutput;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

public class MusicCommand implements CommandExecutor {
    private final ServerMusicPlugin plugin;

    public MusicCommand(ServerMusicPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length < 2) {
            sender.sendMessage("§cИспользование: /smusic <play|stop|pause> <игрок/селектор> [трек]");
            return true;
        }

        String action = args[0].toUpperCase();
        String targetSelector = args[1];
        String trackName = args.length > 2 ? args[2] : "";

        if (!action.equals("PLAY") && !action.equals("STOP") && !action.equals("PAUSE")) {
            sender.sendMessage("§cНеверное действие! Доступны: play, stop, pause");
            return true;
        }

        if (action.equals("PLAY") && trackName.isEmpty()) {
            sender.sendMessage("§cУкажите название аудиофайла для воспроизведения!");
            return true;
        }

        List<Entity> targets;
        try {
            targets = Bukkit.selectEntities(sender, targetSelector);
        } catch (IllegalArgumentException e) {
            sender.sendMessage("§cОшибка синтаксиса селектора!");
            return true;
        }

        int sentCount = 0;
        for (Entity entity : targets) {
            if (entity instanceof Player player) {
                // Вызываем метод отправки. Если при PLAY файла нет, метод вернет false
                if (sendMusicPacket(player, action, trackName)) {
                    sentCount++;
                } else {
                    sender.sendMessage("§cНе удалось отправить пакет для " + player.getName() + " (проверьте наличие файла).");
                }
            }
        }

        sender.sendMessage("§aПакет " + action + " успешно обработан для " + sentCount + " игроков.");
        return true;
    }

    private boolean sendMusicPacket(Player player, String action, String trackName) {
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF(action);    
        out.writeUTF(trackName); 

        // Если команда PLAY, считываем файл с сервера и пихаем его байты в этот же пакет
        if (action.equals("PLAY")) {
            // Файлы будут лежать в паапке: plugins/ServerMusic/tracks/имя_трека.wav
            File tracksDir = new File(plugin.getDataFolder(), "tracks");
            if (!tracksDir.exists()) {
                tracksDir.mkdirs();
            }
            
            File trackFile = new File(tracksDir, trackName + ".wav");
            
            if (!trackFile.exists()) {
                plugin.getLogger().warning("Файл трека не найден на сервере: " + trackFile.getAbsolutePath());
                return false;
            }

            try {
                // Читаем аудиофайл в массив байт
                byte[] audioBytes = Files.readAllBytes(trackFile.toPath());
                
                // Записываем сначала размер массива, чтобы клиент знал, сколько байт читать
                out.writeInt(audioBytes.length);
                // Записываем сами байты звука
                out.write(audioBytes);
                
            } catch (IOException e) {
                plugin.getLogger().severe("Ошибка чтения аудиофайла " + trackName + ": " + e.getMessage());
                return false;
            }
        } else {
            // Для STOP и PAUSE музыка не нужна, пишем размер 0
            out.writeInt(0);
        }

        // Отправляем массив байт через плагин-канал
        player.sendPluginMessage(plugin, ServerMusicPlugin.CHANNEL, out.toByteArray());
        return true;
    }
}
