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
    static final String ULTRA_DRIVING = "ultra_driving";
    static final String SUSTAINED_60 = "sustained_60";
    static final String SUSTAINED_60_CITY = "sustained_60_city";

    static void apply(Map<String,String> values, String profile) {
        if (!BALANCED.equals(profile) && !HIGH.equals(profile) && !MAX_EXPERIMENTAL.equals(profile) && !ULTRA_VISUAL.equals(profile) && !POLICE_LIGHTS_TRAFFIC.equals(profile) && !ULTRA_DRIVING.equals(profile) && !SUSTAINED_60.equals(profile) && !SUSTAINED_60_CITY.equals(profile))
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
            case ULTRA_DRIVING:
                // Large streaming workload while driving at speed; keep spatial
                // sharpness at 1080p and all near-scene shadows and anti-aliasing.
                // The same frame cap chosen by the user remains in force.
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
                values.put("gta4_reflection_capture_distance", "original");
                values.put("gta4_native_water_reflection_interval", "2");
                values.put("gta4_native_environment_reflection_interval", "2");
                // 1.2 instead of 1.4 reduces traffic/geometry work and asset
                // streaming pressure, without lowering output resolution.
                values.put("gta4_draw_distance_scale", "1.2");
                values.put("gta4_dynamic_draw_distance", "true");
                values.put("gta4_motion_blur", "false");
                values.put("gta4_native_pipeline_workers", "2");
                values.put("gta4_native_pipeline_prewarm", "true");
                values.put("gta4_native_pipeline_lookahead", "true");
                values.put("gta4_native_startup_prepare_seconds", "10");
                values.put("gta4_native_converted_texture_disk_cache", "true");
                values.put("gta4_native_texture_reuse_extended", "false");
                values.put("android_gpu_turbo", "false");
                values.put("android_thermal_guard", "true");
                break;
            case SUSTAINED_60:
                // Favour cooler sustained play instead of peak benchmark quality.
                // 1080p is the OUTPUT size; FSR1 renders fewer internal pixels.
                // 60 FPS is the target frame limit, not a guarantee under throttling.
                values.put("gta4_frame_limit", "60");
                values.put("resolution", "1080p");
                values.put("gta4_native_upscaler", "fsr1");
                values.put("gta4_fsr1_quality", "ultra_quality");
                values.put("gta4_fsr1_sharpness_reduction", "0.2");
                values.put("gta4_native_anti_aliasing", "smaa");
                values.put("gta4_native_smaa_quality", "high");
                values.put("gta4_shadow_map_base_size", "512");
                values.put("gta4_shadow_distance_scale", "1.0");
                values.put("gta4_native_shadow_interval", "1");
                values.put("gta4_reflection_resolution", "original");
                values.put("gta4_water_reflection_resolution", "original");
                values.put("gta4_mirror_reflection_resolution", "original");
                values.put("gta4_reflection_aa", "off");
                values.put("gta4_reflection_capture_distance", "near");
                values.put("gta4_native_water_reflection_interval", "3");
                values.put("gta4_native_environment_reflection_interval", "3");
                values.put("gta4_draw_distance_scale", "0.9");
                values.put("gta4_dynamic_draw_distance", "true");
                values.put("gta4_motion_blur", "false");
                values.put("gta4_native_pipeline_workers", "1");
                values.put("gta4_native_pipeline_prewarm", "true");
                values.put("gta4_native_pipeline_lookahead", "true");
                values.put("gta4_native_startup_prepare_seconds", "10");
                values.put("gta4_native_converted_texture_disk_cache", "true");
                values.put("gta4_native_texture_reuse_extended", "false");
                values.put("gta4_native_max_queued_frames", "3");
                values.put("android_gpu_turbo", "false");
                values.put("android_thermal_guard", "true");
                // Roll back v105's unsuccessful 40-block sound experiment.
                // This does not fix CPU/GPU starvation but avoids extra latency.
                values.put("audio_maxqframes", "32");
                values.put("audio_adaptive_buffer", "true");
                values.put("audio_low_latency", "false");
                break;
            case SUSTAINED_60_CITY:
                // Keep the proven cooler 60-FPS settings of v101, with a small
                // improvement to distant building visibility during fast driving.
                // The adaptive draw distance may still reduce it under load.
                apply(values, SUSTAINED_60);
                values.put("gta4_draw_distance_scale", "1.05");
                break;
            default: throw new AssertionError(profile);
        }
    }

    /**
     * Explicit user-selected sustained-mode tuning. Never change drivers or
     * graphics at application start. Existing RADV trial remains opt-in.
     */
    static void applyForDriver(Map<String,String> values, String profile, String driverMode) {
        apply(values,profile);
        if (!SUSTAINED_60.equals(profile) && !SUSTAINED_60_CITY.equals(profile)) return;

        // v108: prioritize legible distant geometry instead of exchanging it
        // for a higher headline FPS. Automatic uses the system Vulkan driver
        // on the S26's Xclipse GPU; never force a driver switch.
        if ("system".equalsIgnoreCase(driverMode) || "auto".equalsIgnoreCase(driverMode)) {
            // Undo invalid v107 enum values: "Sun shadows" supports 0/1,
            // reflection intervals 0..3 in the current Android settings.
            values.put("gta4_native_shadow_interval", "1");
            values.put("gta4_native_water_reflection_interval", "3");
            values.put("gta4_native_environment_reflection_interval", "3");
            values.put("gta4_native_max_queued_frames", "2");
            // Keep the original 1080p OUTPUT/FSR Ultra Quality ratio to avoid
            // a native 1080p heat spike, but reduce FSR softness.
            values.put("gta4_fsr1_sharpness_reduction", "0.05");
            // Draw objects further out and do not shrink the scene merely
            // because frame delivery falls below 60 fps. Android thermal
            // protection stays on and may still reduce detail when required.
            values.put("gta4_draw_distance_scale",
                    SUSTAINED_60_CITY.equals(profile) ? "1.15" : "1.10");
            values.put("gta4_dynamic_draw_distance", "false");
            // Reuse converted textures while memory is available; reclamation
            // remains controlled by the native renderer's memory pressure.
            values.put("gta4_native_texture_reuse_extended", "true");
            // Compensate only with edge pass quality, not with textures,
            // geometry, near-field shadows or output resolution.
            values.put("gta4_native_smaa_quality", "medium");
            return;
        }
        if (!usesExperimentalRadv(driverMode)) return;

        // RADV experiment: prioritize recognisable distant buildings/vehicles.
        // FSR1 still works below native output resolution, but less softening
        // makes its upscaled output sharper without increasing render pixels.
        values.put("gta4_fsr1_sharpness_reduction", "0.05");
        // The v105 dynamic-distance loop repeatedly decreased distant geometry
        // under load. A fixed near-retail distance is a controlled comparison
        // to assess pop-in; may draw more scenery and increase CPU/battery use.
        values.put("gta4_draw_distance_scale", "1.05");
        values.put("gta4_dynamic_draw_distance", "false");
        // Offset part of the larger scene cost with less expensive effects.
        // Do not change resolution, native game engine or cap above 60 FPS.
        values.put("gta4_shadow_map_base_size", "256");
        values.put("gta4_native_smaa_quality", "medium");
    }

    static boolean usesExperimentalRadv(String mode) {
        if (mode==null) return false;
        String lower=mode.trim().toLowerCase(java.util.Locale.ROOT);
        // The ZIP import uses custom:<meta-name-or-zip-name>, not 'radv18'.
        // Explicitly exclude the bundled 1.8 driver and all system modes.
        return lower.startsWith("custom:") && lower.contains("radv") &&
                lower.contains("xclipse");
    }

    private S26GraphicsProfiles() {}
}
