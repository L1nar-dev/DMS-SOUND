package me.linar.servermusic;

import org.bukkit.plugin.java.JavaPlugin;

public class ServerMusicPlugin extends JavaPlugin {
    // Имя канала должно быть строго в формате "namespace:key" в нижнем регистре
    public static final String CHANNEL = "servermusic:control";

    @Override
    public void onEnable() {
        // Регистрируем исходящий канал для отправки данных моду
        this.getServer().getMessenger().registerOutgoingPluginChannel(this, CHANNEL);
        
        // Регистрируем команду выполнения
        if (this.getCommand("servermusic") != null) {
            this.getCommand("servermusic").setExecutor(new MusicCommand(this));
        }
        
        getLogger().info("ServerMusic (Серверная часть) активирована.");
    }

    @Override
    public void onDisable() {
        // Не забываем закрыть канал при выключении
        this.getServer().getMessenger().unregisterOutgoingPluginChannel(this, CHANNEL);
    }
}
