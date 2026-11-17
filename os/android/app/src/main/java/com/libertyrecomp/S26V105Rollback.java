package com.libertyrecomp;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import java.io.File;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

/** Reverts only the unsuccessful v105 experiment without resetting graphics, saves or MOD cheats. */
final class S26V105Rollback {
    private static final String PREFS = "s26_v106_rollback";
    private static final String DONE = "applied";

    /** Do not change hand-tuned presets or non-sustained configurations. */
    static boolean matchesV105(Map<String,String> saved, Map<String,String> pending) {
        String queue = pending.getOrDefault("audio_maxqframes", saved.get("audio_maxqframes"));
        String frame = pending.getOrDefault("gta4_frame_limit", saved.get("gta4_frame_limit"));
        String scaler = pending.getOrDefault("gta4_native_upscaler", saved.get("gta4_native_upscaler"));
        return "40".equals(queue) && "60".equals(frame) && "fsr1".equals(scaler);
    }

    static Map<String,String> audioRollback(Map<String,String> saved) {
        Map<String,String> update = new LinkedHashMap<>();
        if ("40".equals(saved.get("audio_maxqframes"))) update.put("audio_maxqframes", "32");
        return update;
    }

    /** Called by the launcher before new game launch, only once per app data installation. */
    static void restoreIfNeeded(Activity activity, File data) {
        SharedPreferences flags = activity.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        if (flags.getBoolean(DONE, false)) return;
        try {
            File config = MobileConfig.file(data);
            Map<String,String> saved = MobileConfig.read(config);
            Map<String,String> pending = PendingSettings.read(data);
            if (matchesV105(saved, pending)) {
                // v105 automatically requested both display and Android sustained modes
                // when the player selected 60 FPS. Return to the pre-v105 UI defaults.
                UiPreferences.set(activity, "battery_refresh", false);
                UiPreferences.set(activity, "sustained", false);
                Map<String,String> previous = audioRollback(saved);
                if (!previous.isEmpty()) MobileConfig.update(config, previous);
                Map<String,String> queued = audioRollback(pending);
                if (!queued.isEmpty()) PendingSettings.save(data, queued);
                Log.i("LibertyRecomp", "S26 v106 reverted v105 audio/power experiment; graphics and game data untouched");
            }
            flags.edit().putBoolean(DONE,true).apply();
        } catch (IOException | RuntimeException failure) {
            Log.w("LibertyRecomp", "S26 v106 deferred safe audio rollback", failure);
        }
    }

    private S26V105Rollback() {}
}
