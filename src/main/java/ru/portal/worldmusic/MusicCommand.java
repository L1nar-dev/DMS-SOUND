package ru.portal.worldmusic;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import java.io.File;

public class MusicCommand implements CommandExecutor {
    private final WorldMusic plugin;

    public MusicCommand(WorldMusic plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("worldmusic.admin")) {
            sender.sendMessage("§cУ вас недостаточно прав.");
            return true;
        }

        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "play":
                if (args.length < 3) {
                    sender.sendMessage("§cИспользование: /worldmusic play <мир> <файл> [громкость]");
                    return true;
                }
                World world = Bukkit.getWorld(args[1]);
                if (world == null) {
                    sender.sendMessage("§cУказанный мир не найден.");
                    return true;
                }
                File file = new File(plugin.getDataFolder(), "tracks/" + args[2]);
                if (!file.exists()) {
                    sender.sendMessage("§cФайл '" + args[2] + "' не найден в папке plugins/WorldMusic/tracks/");
                    return true;
                }
                float volume = (float) plugin.getConfig().getDouble("default-volume", 0.5);
                if (args.length >= 4) {
                    try {
                        volume = Float.parseFloat(args[3]);
                        if (volume < 0.0f || volume > 1.0f) throw new NumberFormatException();
                    } catch (NumberFormatException e) {
                        sender.sendMessage("§cГромкость должна быть числом от 0.0 до 1.0");
                        return true;
                    }
                }
                plugin.getMusicManager().getOrCreateManager(world).play(file, volume);
                sender.sendMessage("§aВ мире " + world.getName() + " запущена музыка: " + file.getName());
                break;

            case "stop":
                if (args.length < 2) {
                    sender.sendMessage("§cИспользование: /worldmusic stop <мир>");
                    return true;
                }
                World targetWorld = Bukkit.getWorld(args[1]);
                if (targetWorld == null) {
                    sender.sendMessage("§cУказанный мир не найден.");
                    return true;
                }
                plugin.getMusicManager().getOrCreateManager(targetWorld).stop();
                sender.sendMessage("§aМузыка в мире " + targetWorld.getName() + " остановлена.");
                break;

            case "stopall":
                plugin.getMusicManager().stopAll();
                sender.sendMessage("§aСтриминг музыки экстренно остановлен во всех мирах.");
                break;

            case "reload":
                plugin.reloadConfig();
                File tracksDir = new File(plugin.getDataFolder(), "tracks");
                if (!tracksDir.exists()) tracksDir.mkdirs();
                sender.sendMessage("§aКонфигурация перезагружена, папка треков проверена.");
                break;

            default:
                sendHelp(sender);
                break;
        }
        return true;
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage("§6--- Управление WorldMusic ---");
        sender.sendMessage("§e/worldmusic play <мир> <файл> [0.0-1.0] §7- Включить трек");
        sender.sendMessage("§e/worldmusic stop <мир> §7- Выключить трек в мире");
        sender.sendMessage("§e/worldmusic stopall §7- Выключить всё везде");
        sender.sendMessage("§e/worldmusic reload §7- Перезагрузить конфиг");
    }
}
