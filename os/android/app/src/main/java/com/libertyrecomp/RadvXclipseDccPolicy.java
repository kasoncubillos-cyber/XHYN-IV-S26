package com.libertyrecomp;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Driver-specific, opt-in diagnostics for visual corruption on an imported
 * RADV Xclipse driver. Never change the system Vulkan driver or global Android
 * properties, and never touch renderer preferences or game saves.
 */
final class RadvXclipseDccPolicy {
    static final String OPTION = "radv_dcc_mode";
    static final String DEFAULT = "default";
    static final String SMALL_OFF = "small_off";
    static final String ALL_OFF = "all_off";

    static boolean isImportedRadv(String selectedMode, String libraryName) {
        return selectedMode != null && selectedMode.startsWith("custom:")
                && "vulkan.radeon.so".equals(libraryName);
    }

    static Map<String,String> environment(String selectedMode, String libraryName, String requested) {
        if (!isImportedRadv(selectedMode, libraryName)) return Collections.emptyMap();
        Map<String,String> vars = new LinkedHashMap<>();
        if (SMALL_OFF.equals(requested)) {
            // Work around corrupt small render targets such as minimap masks.
            vars.put("RADV_XCLIPSE_DCC_SMALL","0");
        } else if (ALL_OFF.equals(requested)) {
            // Second diagnostic only. This may increase GPU bandwidth and heat.
            vars.put("RADV_XCLIPSE_DCC","0");
        }
        return vars;
    }

    private RadvXclipseDccPolicy() {}
}
