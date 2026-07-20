package ru.portal.worldmusic;

import de.maxhenkel.voicechat.api.VoicechatPlugin;
import de.maxhenkel.voicechat.api.VoicechatServerApi;

public class VoicechatPluginImpl implements VoicechatPlugin {
    private static VoicechatServerApi api;

    @Override
    public String getPluginId() {
        return "world_music";
    }

    @Override
    public void initialize(VoicechatServerApi serverApi) {
        api = serverApi;
    }

    public static VoicechatServerApi getApi() {
        return api;
    }
}
