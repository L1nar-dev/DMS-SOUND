package ru.portal.worldmusic;

import de.maxhenkel.voicechat.api.VoicechatApi;
import de.maxhenkel.voicechat.api.VoicechatPlugin;
import de.maxhenkel.voicechat.api.VoicechatServerApi;

public class VoicechatPluginImpl implements VoicechatPlugin {
    private static VoicechatServerApi api;

    @Override
    public String getPluginId() {
        return "world_music";
    }

    @Override
    public void initialize(VoicechatApi voicechatApi) {
        if (voicechatApi instanceof VoicechatServerApi) {
            api = (VoicechatServerApi) voicechatApi;
        }
    }

    public static VoicechatServerApi getApi() {
        return api;
    }
}
