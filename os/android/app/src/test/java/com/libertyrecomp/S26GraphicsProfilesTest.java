package com.libertyrecomp;

import static org.junit.Assert.*;
import java.util.LinkedHashMap;
import java.util.Map;
import org.junit.Test;

public class S26GraphicsProfilesTest {
    @Test public void defaultGameLanguageIsSpanish() {
        assertEquals("5", MobileConfig.DEFAULTS.get("user_language"));
    }

    @Test public void qualityProfilesStayWithinDeclaredOptions() {
        for (String profile : new String[] {
                S26GraphicsProfiles.BALANCED,
                S26GraphicsProfiles.HIGH,
                S26GraphicsProfiles.MAX_EXPERIMENTAL,
                S26GraphicsProfiles.ULTRA_VISUAL,
                S26GraphicsProfiles.POLICE_LIGHTS_TRAFFIC,
                S26GraphicsProfiles.ULTRA_DRIVING}) {
            Map<String,String> settings = new LinkedHashMap<>();
            settings.put("unrelated_setting","keep_me");
            S26GraphicsProfiles.apply(settings,profile);
            assertEquals("keep_me",settings.get("unrelated_setting"));
            assertEquals("30",settings.get("gta4_frame_limit"));
            assertEquals("true",settings.get("android_thermal_guard"));
            assertEquals("native",settings.get("gta4_native_upscaler"));
            assertTrue(settings.containsKey("gta4_shadow_map_base_size"));
            assertTrue(settings.containsKey("gta4_reflection_resolution"));
        }
    }

    @Test public void presetsPreservePreviouslySelected60Fps() {
        Map<String,String> settings=new LinkedHashMap<>();
        settings.put("gta4_frame_limit","60");
        for(String profile:new String[]{S26GraphicsProfiles.BALANCED,S26GraphicsProfiles.HIGH,S26GraphicsProfiles.MAX_EXPERIMENTAL,S26GraphicsProfiles.ULTRA_VISUAL,S26GraphicsProfiles.POLICE_LIGHTS_TRAFFIC,S26GraphicsProfiles.ULTRA_DRIVING}){
            S26GraphicsProfiles.apply(settings,profile);
            assertEquals("60",settings.get("gta4_frame_limit"));
        }
    }

    @Test public void sustained60PresetKeepsOutput1080pAnd60FpsWithoutDisablingThermalGuard() {
        Map<String,String> settings=new LinkedHashMap<>();
        settings.put("gta4_frame_limit","30");
        settings.put("unrelated_setting","keep_me");
        S26GraphicsProfiles.apply(settings,S26GraphicsProfiles.SUSTAINED_60);
        assertEquals("60",settings.get("gta4_frame_limit"));
        assertEquals("1080p",settings.get("resolution"));
        assertEquals("fsr1",settings.get("gta4_native_upscaler"));
        assertEquals("ultra_quality",settings.get("gta4_fsr1_quality"));
        assertEquals("smaa",settings.get("gta4_native_anti_aliasing"));
        assertEquals("high",settings.get("gta4_native_smaa_quality"));
        assertEquals("512",settings.get("gta4_shadow_map_base_size"));
        assertEquals("0.9",settings.get("gta4_draw_distance_scale"));
        assertEquals("3",settings.get("gta4_native_environment_reflection_interval"));
        assertEquals("3",settings.get("gta4_native_water_reflection_interval"));
        assertEquals("true",settings.get("gta4_dynamic_draw_distance"));
        assertEquals("true",settings.get("gta4_native_converted_texture_disk_cache"));
        assertEquals("false",settings.get("android_gpu_turbo"));
        assertEquals("true",settings.get("android_thermal_guard"));
        assertEquals("32",settings.get("audio_maxqframes"));
        assertEquals("true",settings.get("audio_adaptive_buffer"));
        assertEquals("false",settings.get("audio_low_latency"));
        assertFalse(settings.containsKey("ui_battery_refresh"));
        assertFalse(settings.containsKey("ui_sustained"));
        assertEquals("keep_me",settings.get("unrelated_setting"));
    }

    @Test public void cityVisibleProfileRetainsProvenV101SettingsExceptDrawDistance() {
        Map<String,String> stable=new LinkedHashMap<>(), city=new LinkedHashMap<>();
        stable.put("other_key", "keep_me");
        city.put("other_key", "keep_me");
        S26GraphicsProfiles.apply(stable,S26GraphicsProfiles.SUSTAINED_60);
        S26GraphicsProfiles.apply(city,S26GraphicsProfiles.SUSTAINED_60_CITY);
        assertEquals("0.9",stable.get("gta4_draw_distance_scale"));
        assertEquals("1.05",city.get("gta4_draw_distance_scale"));
        stable.remove("gta4_draw_distance_scale");
        city.remove("gta4_draw_distance_scale");
        assertEquals(stable,city);
        assertEquals("60",city.get("gta4_frame_limit"));
        assertEquals("true",city.get("gta4_dynamic_draw_distance"));
        assertEquals("true",city.get("android_thermal_guard"));
    }

    @Test public void cityVisibleInheritsSustainedAudioProtection() {
        Map<String,String> city=new LinkedHashMap<>();
        S26GraphicsProfiles.apply(city,S26GraphicsProfiles.SUSTAINED_60_CITY);
        assertEquals("1.05",city.get("gta4_draw_distance_scale"));
        assertEquals("32",city.get("audio_maxqframes"));
        assertEquals("true",city.get("audio_adaptive_buffer"));
        assertEquals("false",city.get("audio_low_latency"));
        assertFalse(city.containsKey("ui_battery_refresh"));
        assertEquals("false",city.get("android_gpu_turbo"));
    }

    @Test public void otherProfilesDoNotReceiveSustainedAudioChanges() {
        for (String profile:new String[]{
                S26GraphicsProfiles.BALANCED,
                S26GraphicsProfiles.HIGH,
                S26GraphicsProfiles.ULTRA_DRIVING,
                S26GraphicsProfiles.POLICE_LIGHTS_TRAFFIC}) {
            Map<String,String> values=new LinkedHashMap<>();
            S26GraphicsProfiles.apply(values,profile);
            assertFalse(profile,values.containsKey("audio_maxqframes"));
            assertFalse(profile,values.containsKey("ui_battery_refresh"));
        }
    }

    @Test public void radvImportGetsSharperAndMoreStableVisibilityOnSameSustainedMode() {
        Map<String,String> tuned=new LinkedHashMap<>();
        tuned.put("custom_setting","keep");
        S26GraphicsProfiles.applyForDriver(tuned,S26GraphicsProfiles.SUSTAINED_60,
                "custom:RADV_Xclipse_27.0.0_devel_cc87fdf");
        assertEquals("60",tuned.get("gta4_frame_limit"));
        assertEquals("1080p",tuned.get("resolution"));
        assertEquals("fsr1",tuned.get("gta4_native_upscaler"));
        assertEquals("ultra_quality",tuned.get("gta4_fsr1_quality"));
        assertEquals("0.05",tuned.get("gta4_fsr1_sharpness_reduction"));
        assertEquals("1.05",tuned.get("gta4_draw_distance_scale"));
        assertEquals("false",tuned.get("gta4_dynamic_draw_distance"));
        assertEquals("256",tuned.get("gta4_shadow_map_base_size"));
        assertEquals("medium",tuned.get("gta4_native_smaa_quality"));
        assertEquals("32",tuned.get("audio_maxqframes"));
        assertEquals("true",tuned.get("android_thermal_guard"));
        assertEquals("false",tuned.get("android_gpu_turbo"));
        assertEquals("keep",tuned.get("custom_setting"));
        assertFalse(tuned.containsKey("ui_battery_refresh"));
        assertFalse(tuned.containsKey("ui_sustained"));
    }

    @Test public void radvCityVisibleUsesSameTuningAsSustained() {
        Map<String,String> tuned=new LinkedHashMap<>();
        S26GraphicsProfiles.applyForDriver(tuned,S26GraphicsProfiles.SUSTAINED_60_CITY,
                "custom:radv_xclipse_1.9");
        assertEquals("1.05",tuned.get("gta4_draw_distance_scale"));
        assertEquals("false",tuned.get("gta4_dynamic_draw_distance"));
        assertEquals("0.05",tuned.get("gta4_fsr1_sharpness_reduction"));
    }

    @Test public void nonSystemNonRadvDriversStayAtExistingVisualDefaults() {
        for(String mode:new String[]{"turnip","radv18",
                "custom:turnip_r6","custom:other_vulkan"}) {
            Map<String,String> current=new LinkedHashMap<>(),tuned=new LinkedHashMap<>();
            S26GraphicsProfiles.apply(current,S26GraphicsProfiles.SUSTAINED_60);
            S26GraphicsProfiles.applyForDriver(tuned,S26GraphicsProfiles.SUSTAINED_60,mode);
            assertEquals(mode,current,tuned);
        }
        assertFalse(S26GraphicsProfiles.usesExperimentalRadv("custom:turnip_19"));
        assertTrue(S26GraphicsProfiles.usesExperimentalRadv("custom:radv-xclipse-v1.9"));
    }

    @Test public void systemOrAutoAppliesPr10ThermalTestOnlyWhenSelectingSustained() {
        for (String mode:new String[]{"auto","system"}) {
            Map<String,String> baseline=new LinkedHashMap<>(),tuned=new LinkedHashMap<>();
            S26GraphicsProfiles.apply(baseline,S26GraphicsProfiles.SUSTAINED_60);
            S26GraphicsProfiles.applyForDriver(tuned,S26GraphicsProfiles.SUSTAINED_60,mode);
            assertEquals("60",tuned.get("gta4_frame_limit"));
            assertEquals("1080p",tuned.get("resolution"));
            assertEquals("fsr1",tuned.get("gta4_native_upscaler"));
            assertEquals("0.9",tuned.get("gta4_draw_distance_scale"));
            assertEquals("true",tuned.get("gta4_dynamic_draw_distance"));
            assertEquals("2",tuned.get("gta4_native_shadow_interval"));
            assertEquals("4",tuned.get("gta4_native_water_reflection_interval"));
            assertEquals("4",tuned.get("gta4_native_environment_reflection_interval"));
            assertEquals("2",tuned.get("gta4_native_max_queued_frames"));
            assertEquals("32",tuned.get("audio_maxqframes"));
            assertEquals("true",tuned.get("android_thermal_guard"));
            assertEquals("false",tuned.get("android_gpu_turbo"));
            assertFalse(tuned.containsKey("ui_sustained"));
            assertFalse(tuned.containsKey("ui_battery_refresh"));
            baseline.remove("gta4_native_shadow_interval");
            baseline.remove("gta4_native_water_reflection_interval");
            baseline.remove("gta4_native_environment_reflection_interval");
            baseline.remove("gta4_native_max_queued_frames");
            tuned.remove("gta4_native_shadow_interval");
            tuned.remove("gta4_native_water_reflection_interval");
            tuned.remove("gta4_native_environment_reflection_interval");
            tuned.remove("gta4_native_max_queued_frames");
            assertEquals(baseline,tuned);
        }
    }

    @Test public void systemCityVisibleAppliesPr10DistanceWithoutChangingSoundOrFsr() {
        Map<String,String> tuned=new LinkedHashMap<>();
        S26GraphicsProfiles.applyForDriver(tuned,S26GraphicsProfiles.SUSTAINED_60_CITY,"system");
        assertEquals("1.0",tuned.get("gta4_draw_distance_scale"));
        assertEquals("60",tuned.get("gta4_frame_limit"));
        assertEquals("ultra_quality",tuned.get("gta4_fsr1_quality"));
        assertEquals("true",tuned.get("gta4_dynamic_draw_distance"));
        assertEquals("2",tuned.get("gta4_native_max_queued_frames"));
        assertEquals("32",tuned.get("audio_maxqframes"));
    }

    @Test public void highAndUltraRemainUnchangedForSystemDriver() {
        for (String profile:new String[]{S26GraphicsProfiles.HIGH,S26GraphicsProfiles.ULTRA_DRIVING}) {
            Map<String,String> old=new LinkedHashMap<>(),current=new LinkedHashMap<>();
            S26GraphicsProfiles.apply(old,profile);
            S26GraphicsProfiles.applyForDriver(current,profile,"system");
            assertEquals(old,current);
        }
    }

    @Test public void radvDoesNotOverrideOtherProfilesOrTurnOnUnrequestedPowerModes() {
        Map<String,String> baseline=new LinkedHashMap<>(),tuned=new LinkedHashMap<>();
        S26GraphicsProfiles.apply(baseline,S26GraphicsProfiles.ULTRA_DRIVING);
        S26GraphicsProfiles.applyForDriver(tuned,S26GraphicsProfiles.ULTRA_DRIVING,
                "custom:RADV_Xclipse_1.9");
        assertEquals(baseline,tuned);
        assertFalse(tuned.containsKey("ui_sustained"));
        assertFalse(tuned.containsKey("ui_battery_refresh"));
    }

    @Test public void sustained60RemainsOptInAndOldUltraStaysNative() {
        Map<String,String> ultra=new LinkedHashMap<>();
        S26GraphicsProfiles.apply(ultra,S26GraphicsProfiles.ULTRA_DRIVING);
        assertEquals("native",ultra.get("gta4_native_upscaler"));
        assertEquals("1024",ultra.get("gta4_shadow_map_base_size"));
        assertEquals("1.2",ultra.get("gta4_draw_distance_scale"));
    }

    @Test public void ultraDrivingPreservesNearSceneAndExisting60Fps() {
        Map<String,String> previous=new LinkedHashMap<>(),driving=new LinkedHashMap<>();
        previous.put("gta4_frame_limit","60");
        driving.put("gta4_frame_limit","60");
        S26GraphicsProfiles.apply(previous,S26GraphicsProfiles.ULTRA_VISUAL);
        S26GraphicsProfiles.apply(driving,S26GraphicsProfiles.ULTRA_DRIVING);
        for(String key:new String[]{"resolution","gta4_native_upscaler","gta4_native_anti_aliasing",
                "gta4_native_smaa_quality","gta4_shadow_map_base_size",
                "gta4_shadow_distance_scale","gta4_reflection_resolution","gta4_reflection_aa"})
            assertEquals(key,previous.get(key),driving.get(key));
        assertEquals("1.2",driving.get("gta4_draw_distance_scale"));
        assertEquals("true",driving.get("gta4_dynamic_draw_distance"));
        assertEquals("2",driving.get("gta4_native_environment_reflection_interval"));
        assertEquals("2",driving.get("gta4_native_water_reflection_interval"));
        assertEquals("2",driving.get("gta4_native_pipeline_workers"));
        assertEquals("true",driving.get("gta4_native_converted_texture_disk_cache"));
        assertEquals("false",driving.get("android_gpu_turbo"));
        assertEquals("true",driving.get("android_thermal_guard"));
        assertEquals("60",driving.get("gta4_frame_limit"));
    }

    @Test public void policeLightTrafficUsesUltraSpatialQualityWithFewerReflectionUpdates() {
        Map<String,String> ultra=new LinkedHashMap<>(), pursuit=new LinkedHashMap<>();
        ultra.put("gta4_frame_limit","60");
        pursuit.put("gta4_frame_limit","60");
        S26GraphicsProfiles.apply(ultra,S26GraphicsProfiles.ULTRA_VISUAL);
        S26GraphicsProfiles.apply(pursuit,S26GraphicsProfiles.POLICE_LIGHTS_TRAFFIC);
        for(String key:new String[]{
                "resolution","gta4_native_upscaler","gta4_native_anti_aliasing",
                "gta4_native_smaa_quality","gta4_shadow_map_base_size",
                "gta4_shadow_distance_scale","gta4_reflection_resolution",
                "gta4_water_reflection_resolution","gta4_mirror_reflection_resolution",
                "gta4_reflection_aa","gta4_reflection_capture_distance",
                "gta4_draw_distance_scale"})
            assertEquals(key,ultra.get(key),pursuit.get(key));
        assertEquals("60",pursuit.get("gta4_frame_limit"));
        assertEquals("2",pursuit.get("gta4_native_environment_reflection_interval"));
        assertEquals("1",pursuit.get("gta4_native_water_reflection_interval"));
        assertEquals("2",pursuit.get("gta4_native_pipeline_workers"));
        assertEquals("10",pursuit.get("gta4_native_startup_prepare_seconds"));
        assertEquals("true",pursuit.get("gta4_native_converted_texture_disk_cache"));
        assertEquals("true",pursuit.get("gta4_native_pipeline_prewarm"));
        assertEquals("true",pursuit.get("gta4_native_pipeline_lookahead"));
        assertEquals("true",pursuit.get("android_thermal_guard"));
    }

    @Test public void ultraVisualEnablesReflectionCaptures() {
        Map<String,String> settings=new LinkedHashMap<>();
        S26GraphicsProfiles.apply(settings,S26GraphicsProfiles.ULTRA_VISUAL);
        assertEquals("ultra",settings.get("gta4_native_smaa_quality"));
        assertEquals("1",settings.get("gta4_native_water_reflection_interval"));
        assertEquals("1",settings.get("gta4_native_environment_reflection_interval"));
        assertEquals("4x",settings.get("gta4_reflection_aa"));
        assertEquals("full",settings.get("gta4_reflection_resolution"));
        assertEquals("true",settings.get("android_thermal_guard"));
    }

    @Test public void highAndMaximumAreOptIn() {
        Map<String,String> settings = new LinkedHashMap<>();
        S26GraphicsProfiles.apply(settings,S26GraphicsProfiles.BALANCED);
        assertEquals("720p",settings.get("resolution"));
        assertEquals("256",settings.get("gta4_shadow_map_base_size"));
        S26GraphicsProfiles.apply(settings,S26GraphicsProfiles.HIGH);
        assertEquals("900p",settings.get("resolution"));
        assertEquals("512",settings.get("gta4_shadow_map_base_size"));
        S26GraphicsProfiles.apply(settings,S26GraphicsProfiles.MAX_EXPERIMENTAL);
        assertEquals("1080p",settings.get("resolution"));
        assertEquals("1024",settings.get("gta4_shadow_map_base_size"));
        assertEquals("full",settings.get("gta4_reflection_resolution"));
    }

    @Test(expected=IllegalArgumentException.class)
    public void unknownProfileIsRejected() {
        S26GraphicsProfiles.apply(new LinkedHashMap<>(),"unverified");
    }
}
