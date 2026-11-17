package com.libertyrecomp;

import static org.junit.Assert.*;
import java.util.Map;
import org.junit.Test;

/** Optional driver workaround must never affect the Vulkan system driver. */
public class RadvXclipseDccPolicyTest {
    private final String radv="custom:radv-xclipse-27.0.0-devel-cc87fdf";
    private final String library="vulkan.radeon.so";

    @Test public void normalModeNeverSetsEnvironment() {
        assertTrue(RadvXclipseDccPolicy.environment(radv,library,"default").isEmpty());
        assertTrue(RadvXclipseDccPolicy.environment(radv,library,null).isEmpty());
        assertTrue(RadvXclipseDccPolicy.environment(radv,library,"unexpected").isEmpty());
    }

    @Test public void onlyImportedRadvIsAffected() {
        for (String mode:new String[]{"auto","system","radv18","turnip","custom:turnip"}) {
            assertTrue(mode,RadvXclipseDccPolicy.environment(mode,library,"all_off").isEmpty());
        }
        assertTrue(RadvXclipseDccPolicy.environment(radv,"libvulkan_freedreno.so","all_off").isEmpty());
        assertTrue(RadvXclipseDccPolicy.environment(radv,"","all_off").isEmpty());
    }

    @Test public void smallOnlyWorkaroundDoesNotDisableGlobalDcc() {
        Map<String,String> vars=RadvXclipseDccPolicy.environment(
            radv,library,RadvXclipseDccPolicy.SMALL_OFF);
        assertEquals(1,vars.size());
        assertEquals("0",vars.get("RADV_XCLIPSE_DCC_SMALL"));
        assertFalse(vars.containsKey("RADV_XCLIPSE_DCC"));
    }

    @Test public void allDccOffOnlyInExplicitSecondTrial() {
        Map<String,String> vars=RadvXclipseDccPolicy.environment(
            radv,library,RadvXclipseDccPolicy.ALL_OFF);
        assertEquals(1,vars.size());
        assertEquals("0",vars.get("RADV_XCLIPSE_DCC"));
        assertFalse(vars.containsKey("RADV_XCLIPSE_DCC_SMALL"));
    }
}
