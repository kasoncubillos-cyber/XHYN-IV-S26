# XHYN IV S26 v106 — Piloto del modo sostenido con RADV Xclipse 1.9

## Solicitud
El usuario confirmó que el controlador **RADV Xclipse 1.9 importado** parecía
más estable en su Galaxy S26, pero la ciudad quedaba borrosa y edificios,
autos y personajes distantes aparecían demasiado tarde. Anteriormente informó
que v105 provocó mayor calor, drenaje de batería, caídas de FPS y sonido
distorsionado. Capturas con RADV mostraron además una franja ajedrezada y un
minimapa que se extendía fuera del contorno.

El objetivo es **mejorar el botón existente «S26 60 FPS sostenidos»**, sin
agregar más perfiles ni reemplazar el controlador ni los trucos. No afirmar
mejoras hasta compararlas en el móvil.

## Intervención Java v106
- No instala ni fuerza ningún driver. El piloto se aplica **solamente al
  seleccionar manualmente** el botón del perfil sostenido en Gráficos cuando
  `DriverState.readChoice` devuelve un `custom:...` cuyo identificador contiene
  `radv` y `xclipse`. El selector ya fue implementado en la aplicación
  anteriormente y el usuario importó su driver.
- Para RADV importado: mantiene 1080p de salida y FSR1 Ultra Quality;
  reduce la suavidad FSR 0,20→0,05; distancia base 0,90→1,05;
  desactiva `gta4_dynamic_draw_distance` para observar una distancia estable;
  disminuye sombras 512→256 y SMAA High→Medium para liberar GPU.
- Los demás drivers mantienen gráficos idénticos a v105.
- Retira el aumento fallido del audio de v105: `audio_maxqframes` 40→32;
  mantiene `audio_adaptive_buffer=true` y `audio_low_latency=false`.
- Al pulsar el botón del modo sostenido deja `ui_sustained` y
  `ui_battery_refresh` en falso; el v105 había activado automáticamente
  el primero cuando Android reportaba compatibilidad. El usuario puede
  volver a activarlos manualmente en sus opciones si quiere.
- Conserva `android_thermal_guard=true`, `android_gpu_turbo=false`.
- No edita ni borra partidas, configuraciones ajenas al botón de perfil,
  bibliotecas nativas del juego o instaladores existentes.

## Límites y precauciones
1. **Mayor distancia de dibujado no equivale a acelerar streaming.**
   Si el problema es carga de modelos/texturas o GPU driver, los objetos
   todavía pueden tardar. El artefacto de minimapa/interior no se corrige
   aquí y podría requerir un cambio en el driver o en el renderizador.
2. Desactivar la adaptación de distancia puede AUMENTAR el uso de CPU y
   batería, y reducir FPS en zonas exigentes. El sombreado y AA reducidos
   compensan solo parte del costo. Si la batería se sigue drenando, detener.
3. No activar ray tracing en Xclipse 960; el mantenedor de RADV advierte
   que puede provocar fallos graves y bloqueos. No distribuir el driver
   como obligatorio ni sustituir el controlador Samsung.
4. La v106 reutiliza **bibliotecas ARM64 idénticas a la v105 firmada**
   verificada SHA-256, y su APK tiene que usar mismo paquete/firmante que
   v102/v105, con versionCode 106. Trucos MOD y partidas permanecen.
5. **No se garantizan 60 FPS ni ausencia de bugs**: es un piloto.

## Prueba de usuario sin mezclar cambios
1. Instalar la v106 encima de la v105, sin desinstalar ni borrar datos.
2. Elegir RADV Xclipse importado, salir del juego y cerrar el proceso.
3. Ajustes → Gráficos → pulsar de nuevo **S26 60 FPS sostenidos** y guardar.
4. Reiniciar y anotar si mejora la nitidez y la aparición de objetos en el
   mismo trayecto de 2–5 minutos; después comparar con System driver.
5. Capturar overlay de FPS, temperatura térmica Android, resolución
   interna/de salida y observar el consumo aproximado por minuto.
6. Si reaparece cuadriculado, falla el mapa o baja el rendimiento, volver
   a System driver y usar el modo sostenido con sus gráficos habituales.
7. No borrar `Android/data`, caches de juego, ni partidas para solucionar
   el problema; en todo caso diagnosticar el pipeline/driver con logs.

## Puerta de salida
Solo distribuir como candidata de prueba cuando GitHub Actions apruebe los
tests del modo RADV, el control de firma y el chequeo de bibliotecas nativas.
No promocionar como optimización comprobada hasta tener mediciones.
