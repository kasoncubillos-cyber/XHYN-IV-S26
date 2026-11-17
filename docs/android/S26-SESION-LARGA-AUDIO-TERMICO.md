# XHYN IV S26 — Mejorar el perfil existente «60 FPS sostenidos»

## Instrucción del usuario
El propietario **no quiere otro modo**: quiere mejorar directamente el preset
`S26 60 FPS sostenidos` que utiliza habitualmente, conservando el estilo visual
y los trucos ya funcionales en el menú MOD de la aplicación. No reabrir la
integración opcional de trucos en el teléfono de Niko como requisito de esta tarea.

## Problema que queremos resolver
En Galaxy S26, GTA IV se ve y funciona bien al comenzar. Después de jugar un rato,
el teléfono se calienta y aparecen caídas fuertes de FPS y distorsión del audio.

La sincronía entre calor y caída de FPS es compatible con *thermal throttling*,
pero la causa aún no se ha confirmado mediante mediciones. La distorsión sonora
también puede relacionarse con interrupciones del productor de audio por
sobrecarga general. No se han medido el consumo, los FPS en frío/caliente ni
las pérdidas de bloques de audio en el dispositivo.

## Cambios realizados en ESTA rama
Se modifica el preset **existente** `SUSTAINED_60` de
`S26GraphicsProfiles.apply`; no se añade otro preset ni otro botón.

Se mantienen sus ajustes gráficos originales:
- Límite objetivo 60 FPS (no garantía frente a calor).
- Salida 1080p; FSR1 Ultra Quality con menos píxeles internos.
- SMAA High, sombras 512, refresco de reflejos reducido, distancia
  de dibujado `0.9`, distancia dinámica y cachés actuales.
- `android_thermal_guard=true`; `android_gpu_turbo=false`.

Se añaden **en el mismo preset**:
- `audio_maxqframes=40` (Android utilizaba 32 por defecto en el motor).
- `audio_adaptive_buffer=true`.
- `audio_low_latency=false`.
- `ui_battery_refresh=true`.
- El botón «S26 60 FPS sostenidos» solicita `ui_sustained=true` solo si
  Android declara el modo compatible, sin forzarlo cuando no es compatible.

El perfil `SUSTAINED_60_CITY` hereda automáticamente esos cambios y conserva
su única diferencia visual, distancia de dibujado `1.05` en vez de `0.9`.

La opción nueva de «Sesión larga» creada inicialmente en esta PR fue **retirada**
porque el usuario quiere actualizar el modo que ya usa. La profundidad de
búfer de audio (32, 40 o 48) sigue disponible en ajustes avanzados
para poder revertir/diagnosticar cortes.

## Compromisos y límites
La cola de 40 bloques de 256 muestras a 48 kHz equivale a unos 213 ms de
profundidad máxima, frente a unos 171 ms con 32. Puede absorber tirones breves,
pero potencialmente añade hasta unos 43 ms de latencia sonora.
No puede resolver saturación continua del motor ni evitar que Android reduzca
frecuencias por temperaturas altas.

No reducir agresivamente distancia de visión, detalle de vehículos ni resolución
de salida: el usuario ya se quejó de los autos y caminos cuadrados a lo lejos.
En pruebas futuras, usar valores medidos de FPS, severidad térmica y audio
para decidir si deben cambiarse sombras/reflejos, procesamiento de gráficos o
cuellos de botella del sonido.

## Comprobación antes de APK
1. Ejecutar `:app:testDebugUnitTest` con los tests de `S26GraphicsProfilesTest`,
   incluyendo igualdad entre la versión ciudad y el modo original excepto
   la distancia de visión.
2. Confirmar que el botón actual de «60 FPS sostenidos» se conserva y que
   los ajustes de audio se aplican al guardar/reiniciar. No debe aparecer un
   nuevo mosaico de modo «Sesión larga».
3. Asegurar que se conserva el menú MOD de trucos funcionando en la versión
   instalada; integrar sobre una base de código compatible con esa entrega.
4. Compilar sin borrar juegos/partidas, verificar mismo paquete
   `com.kasoncubillos.xhynivs26stable`, certificado idéntico al de la
   versión del usuario y `versionCode` incrementado en la futura entrega.
5. Test en Galaxy S26 con overlay FPS + estado térmico durante 5 y 15-20
   minutos, sin jugar conectado al cargador, y revisar la sincronía de audio.
6. Si los FPS bajan solo después de calentarse, registrar los indicadores
   sin anular Thermal Guard, protecciones del dispositivo ni forzar GPU Turbo.

## Estado
Solo es una **propuesta de código en PR en borrador**. No se ha publicado
APK actualizada, ni demostrado una mejora real en sesión larga.
