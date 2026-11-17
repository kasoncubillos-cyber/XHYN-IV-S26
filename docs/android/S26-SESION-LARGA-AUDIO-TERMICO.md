# S26 — Prueba de sesiones largas, FPS y audio

## Problema informado

En Samsung Galaxy S26, el usuario indica buena imagen y FPS al comienzo de GTA IV, pero
después de un rato el teléfono se calienta, bajan fuertemente los cuadros y el audio
se distorsiona. Los trucos en el menú de la aplicación ya funcionan y deben conservarse.

La coincidencia temporal es compatible con **limitación térmica** y con **subalimentación
de audio** al incumplir plazos de procesamiento. Aún no está demostrado cuál es
el cuello de botella: CPU, GPU, hilo de audio, memoria o streaming. No hay datos de
FPS, duración, estado térmico ni registros de underrun aportados para el momento de la caída.

## Cambios de código de esta rama

- Perfil experimental **S26 sesión larga · audio estable**, derivado de
  `SUSTAINED_60_CITY`.
- Salida 1080p, FSR1 Ultra Quality (menos píxeles internos), SMAA High,
  sombras 512, draw distance 1.05×, distancia adaptable y cap objetivo 60 FPS.
- GPU Turbo desactivado y Thermal Guard habilitado (sin anular protecciones Android).
- Audio adaptativo activado, low-latency desactivado y límite de cola
  `audio_maxqframes=40` (frente a 32 por defecto en Android).
- `ui_battery_refresh=true`; el menú activa `ui_sustained` únicamente si Android
  informa compatibilidad. No se fuerza el modo en dispositivos no compatibles.
- Se añade un control individual de profundidad de cola para pruebas 32/40/48.

Cada bloque de audio del motor es de 256 muestras a 48 kHz (~5,33 ms).
Por tanto, 32/40/48 bloques equivalen a unos 171/213/256 ms de profundidad máxima.
El cambio 32→40 añade hasta ~43 ms de latencia, lo que podría perjudicar la sincronía
audio-video. Es una prueba reversible, **no una corrección garantizada**. El
sistema de recarga adaptativa existente tampoco puede producir audio que el juego
no alcanza a generar.

## Comprobación en el teléfono (sin instalación de una versión nueva)

El juego actual tiene las opciones equivalentes configurables manualmente:

1. Gráficos → `S26 60 FPS ciudad visible` (o `S26 60 FPS sostenidos` si la
   distancia lejana sigue causando caídas).
2. Rendimiento → Thermal Guard ON, GPU Turbo OFF,
   Sustained performance ON solo si el teléfono lo admite,
   Lower display refresh ON.
3. Audio → Audio dropout protection ON y Low-latency audio OFF.
4. Overlay → Performance overlay Detailed y Overlay thermal metric ON.
5. Dejar enfriar, jugar 5 minutos sin carga conectada y anotar FPS/severidad;
   repetir tras 15-20 minutos en una zona comparable.
6. Si el audio sigue distorsionado, reproducir el problema con el
   registro de diagnóstico habilitado y exportar el informe sin incluir datos
   personales ni archivos comerciales del juego.

**NO** se debe forzar GPU Turbo, desactivar protección térmica ni mantener el equipo
jugando si aparece advertencia grave de temperatura. Tampoco se deben interpretar
etiquetas `Moderate`/`Severe` como grados Celsius. Si los FPS se desploman
solo con calor y se recuperan al enfriar, se refuerza la hipótesis térmica.

## Para distribuir una actualización real

- No publicar ni firmar esta rama mientras el usuario no haya probado la
  combinación actual y revisado FPS, temperatura y audio.
- Conservar `com.kasoncubillos.xhynivs26stable` y la clave de firma que instala
  encima de v102/v104, sin reinstalación ni borrado de datos.
- Incrementar `versionCode` solamente al preparar una entrega validada.
- Comparar certificados de APK y efectuar una prueba real de instalación encima.
- Ejecutar `:app:testDebugUnitTest` y compilación/paquete compatible; este PR
  contiene solo ajustes Java y pruebas, y no es una APK nueva.
