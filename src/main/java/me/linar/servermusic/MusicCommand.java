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

import java.util.List;

public class MusicCommand implements CommandExecutor {
    private final ServerMusicPlugin plugin;

    public MusicCommand(ServerMusicPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        // Синтаксис: /smusic <play|stop|pause> <селектор> [название_трека]
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

        // Парсим селекторы через Bukkit API
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
                sendMusicPacket(player, action, trackName);
                sentCount++;
            }
        }

        sender.sendMessage("§aПакет " + action + " успешно отправлен для " + sentCount + " игроков.");
        return true;
    }

    private void sendMusicPacket(Player player, String action, String trackName) {
        // Формируем поток байт для отправки через Plugin Messaging
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF(action);    // Отправляем строку действия ("PLAY", "STOP", "PAUSE")
        out.writeUTF(trackName); // Отправляем имя файла (например, "chapter1_boss")

        player.sendPluginMessage(plugin, ServerMusicPlugin.CHANNEL, out.toByteArray());
    }
}
