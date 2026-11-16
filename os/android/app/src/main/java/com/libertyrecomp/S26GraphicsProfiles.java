package com.libertyrecomp;

import java.util.Map;

/**
 * Conservative starting points for manual Galaxy S26 quality testing.
 * These are user-selected presets, NOT device capability detection.
 */
final class S26GraphicsProfiles {
    static final String BALANCED = "balanced";
    static final String HIGH = "high";
    static final String MAX_EXPERIMENTAL = "max_experimental";

    static void apply(Map<String,String> values, String profile) {
        if (!BALANCED.equals(profile) && !HIGH.equals(profile) && !MAX_EXPERIMENTAL.equals(profile))
            throw new IllegalArgumentException("Unknown S26 graphics profile: " + profile);

        // Always stay on a bounded frame cap until the runtime crash is resolved.
        values.put("gta4_frame_limit", "30");
        values.put("android_thermal_guard", "true");
        values.put("gta4_native_upscaler", "native");
        values.put("gta4_native_shadow_interval", "1");
        values.put("gta4_reflection_capture_distance", "near");
        values.put("gta4_native_water_reflection_interval", "0");
        values.put("gta4_native_environment_reflection_interval", "0");

        switch (profile) {
            case BALANCED:
                values.put("resolution", "720p");
                values.put("gta4_native_anti_aliasing", "fxaa");
                values.put("gta4_shadow_map_base_size", "256");
                values.put("gta4_shadow_distance_scale", "1.0");
                values.put("gta4_reflection_resolution", "original");
                values.put("gta4_reflection_aa", "off");
                values.put("gta4_draw_distance_scale", "0.85");
                break;
            case HIGH:
                values.put("resolution", "900p");
                values.put("gta4_native_anti_aliasing", "fxaa");
                values.put("gta4_shadow_map_base_size", "512");
                values.put("gta4_shadow_distance_scale", "1.0");
                values.put("gta4_reflection_resolution", "original");
                values.put("gta4_reflection_aa", "off");
                values.put("gta4_draw_distance_scale", "1.0");
                break;
            case MAX_EXPERIMENTAL:
                // Optional manual test only after a stable baseline has been confirmed.
                values.put("resolution", "1080p");
                values.put("gta4_native_anti_aliasing", "smaa");
                values.put("gta4_native_smaa_quality", "high");
                values.put("gta4_shadow_map_base_size", "1024");
                values.put("gta4_shadow_distance_scale", "1.0");
                values.put("gta4_reflection_resolution", "full");
                values.put("gta4_reflection_aa", "2x");
                values.put("gta4_draw_distance_scale", "1.2");
                break;
            default: throw new AssertionError(profile);
        }
    }

    private S26GraphicsProfiles() {}
}
