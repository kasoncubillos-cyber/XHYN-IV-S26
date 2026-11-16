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
                S26GraphicsProfiles.ULTRA_VISUAL}) {
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
        for(String profile:new String[]{S26GraphicsProfiles.BALANCED,S26GraphicsProfiles.HIGH,S26GraphicsProfiles.MAX_EXPERIMENTAL,S26GraphicsProfiles.ULTRA_VISUAL}){
            S26GraphicsProfiles.apply(settings,profile);
            assertEquals("60",settings.get("gta4_frame_limit"));
        }
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
