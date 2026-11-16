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
    static final String ULTRA_VISUAL = "ultra_visual";
    static final String POLICE_LIGHTS_TRAFFIC = "police_lights_traffic";

    static void apply(Map<String,String> values, String profile) {
        if (!BALANCED.equals(profile) && !HIGH.equals(profile) && !MAX_EXPERIMENTAL.equals(profile) && !ULTRA_VISUAL.equals(profile) && !POLICE_LIGHTS_TRAFFIC.equals(profile))
            throw new IllegalArgumentException("Unknown S26 graphics profile: " + profile);

        // Never overwrite a frame cap the player has already tested successfully.
        values.putIfAbsent("gta4_frame_limit", "30");
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
            case ULTRA_VISUAL:
                // Opt-in visual test: the original game assets are unchanged.
                // More work per frame; keep Thermal Guard and the existing FPS cap.
                values.put("resolution", "1080p");
                values.put("gta4_native_upscaler", "native");
                values.put("gta4_native_anti_aliasing", "smaa");
                values.put("gta4_native_smaa_quality", "ultra");
                values.put("gta4_shadow_map_base_size", "1024");
                values.put("gta4_shadow_distance_scale", "1.25");
                values.put("gta4_reflection_resolution", "full");
                values.put("gta4_water_reflection_resolution", "full");
                values.put("gta4_mirror_reflection_resolution", "full");
                values.put("gta4_reflection_aa", "4x");
                values.put("gta4_reflection_capture_distance", "extended");
                values.put("gta4_native_water_reflection_interval", "1");
                values.put("gta4_native_environment_reflection_interval", "1");
                values.put("gta4_draw_distance_scale", "1.4");
                values.put("gta4_dynamic_draw_distance", "true");
                values.put("gta4_motion_blur", "false");
                break;
            case POLICE_LIGHTS_TRAFFIC:
                // Heavy police pursuits: preserve Ultra geometry, sharpness and resolution.
                // Only reflection update frequency trades a little temporal smoothness
                // for lower GPU capture cost when flashing police lights fill the scene.
                values.put("resolution", "1080p");
                values.put("gta4_native_upscaler", "native");
                values.put("gta4_native_anti_aliasing", "smaa");
                values.put("gta4_native_smaa_quality", "ultra");
                values.put("gta4_shadow_map_base_size", "1024");
                values.put("gta4_shadow_distance_scale", "1.25");
                values.put("gta4_reflection_resolution", "full");
                values.put("gta4_water_reflection_resolution", "full");
                values.put("gta4_mirror_reflection_resolution", "full");
                values.put("gta4_reflection_aa", "4x");
                values.put("gta4_reflection_capture_distance", "extended");
                values.put("gta4_native_water_reflection_interval", "1");
                values.put("gta4_native_environment_reflection_interval", "2");
                values.put("gta4_draw_distance_scale", "1.4");
                values.put("gta4_dynamic_draw_distance", "true");
                values.put("gta4_motion_blur", "false");
                // Precompile/reuse expensive effects where the runtime supports it.
                values.put("gta4_native_pipeline_workers", "2");
                values.put("gta4_native_pipeline_prewarm", "true");
                values.put("gta4_native_pipeline_lookahead", "true");
                values.put("gta4_native_startup_prepare_seconds", "10");
                values.put("gta4_native_converted_texture_disk_cache", "true");
                // Do not force GPU Turbo: heavy police lights can already raise heat.
                // Keep the frame limit the player already chose (including 60 FPS).
                break;
            default: throw new AssertionError(profile);
        }
    }

    private S26GraphicsProfiles() {}
}
