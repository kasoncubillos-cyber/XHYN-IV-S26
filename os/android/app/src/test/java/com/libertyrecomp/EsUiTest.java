package com.libertyrecomp;

import static org.junit.Assert.*;
import org.junit.Test;

public class EsUiTest {
    @Test public void launcherActionsAppearInSpanish() {
        assertEquals("Jugar", EsUi.t("Play"));
        assertEquals("Configuración", EsUi.t("Settings"));
        assertEquals("Controles táctiles", EsUi.t("Touch controls"));
        assertEquals("Gráficos", EsUi.t("Graphics"));
    }

    @Test public void graphicLabelsAreTranslatedWithoutChangingConfigKeys() {
        assertEquals("Resolución de reflejos", EsUi.t("Reflection resolution"));
        assertEquals("Suavizado de bordes", EsUi.t("Anti-aliasing"));
        assertEquals("gta4_frame_limit", EsUi.t("gta4_frame_limit"));
    }

    @Test public void spanishHelpExplainsRealControls() {
        assertTrue(EsUi.guide("gta4_native_water_reflection_interval").contains("agua"));
        assertTrue(EsUi.guide("gta4_frame_limit").contains("60 FPS"));
    }
}
