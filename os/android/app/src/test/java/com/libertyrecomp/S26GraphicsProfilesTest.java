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
        assertEquals("40",settings.get("audio_maxqframes"));
        assertEquals("true",settings.get("audio_adaptive_buffer"));
        assertEquals("false",settings.get("audio_low_latency"));
        assertEquals("true",settings.get("ui_battery_refresh"));
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
        assertEquals("40",city.get("audio_maxqframes"));
        assertEquals("true",city.get("audio_adaptive_buffer"));
        assertEquals("false",city.get("audio_low_latency"));
        assertEquals("true",city.get("ui_battery_refresh"));
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
