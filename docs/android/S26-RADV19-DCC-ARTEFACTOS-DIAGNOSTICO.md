# S26 — RADV Xclipse 1.9: diagnóstico de artefactos y límites de rendimiento

## Informe del usuario (Galaxy S26)

Con la APK v106 y RADV Xclipse 1.9 importado el usuario sigue viendo una
franja cuadriculada/negra ocasional, y en una captura anterior el minimapa
aparecía fuera de su máscara. La imagen lejana se percibe borrosa,
los edificios/vehículos cargan tarde y al calentarse caen FPS,
se distorsiona el audio y aumenta el drenaje de batería.
No se dispone de FPS/temperatura/driver efectivo medidos durante el fallo.

**No declarar que RADV es la causa hasta comparar con System driver**, ni
declarar resuelta la corrupción solo porque una prueba puntual pase.
El usuario quiere calidad alta y objetivo de 60 FPS sostenidos; no se puede
prometer simultáneamente todos los ajustes al máximo y 60 FPS cuando hay
limitación térmica de CPU/GPU.

## Hallazgo oficial y cambio propuesto

La documentación de https://github.com/JimVulkan/radv-xclipse indica:
- `RADV_XCLIPSE_DCC_SMALL=0` desactiva la compresión DCC para render targets
  de 512 × 512 píxeles o menores.
- `RADV_XCLIPSE_DCC=0` desactiva completamente la compresión DCC.
- La descompresión adicional puede perjudicar ancho de banda, consumo y FPS.
- RT es inseguro en Xclipse 960 y puede bloquear/reiniciar el equipo.

Esta PR añade dentro del selector de **Drivers** una opción reversible
«RADV Xclipse · corregir cuadros negros (prueba)» con tres ajustes:
1. **Normal (predeterminado):** sin modificación del driver.
2. **Prueba A:** desactivar DCC solo para destinos gráficos pequeños.
3. **Prueba B:** desactivar DCC por completo.

Los valores se guardan exclusivamente en el perfil del driver importado
(`DriverState.options`), no modifican el driver del sistema, no cambian
archivos de juego, guardados ni cachés, y solo se aplican cuando el driver
seleccionado es `custom:...` y su biblioteca es `vulkan.radeon.so`.
Se envían como variables de entorno al proceso del juego antes de iniciar
Vulkan. No se usan propiedades globales ni ajustes de GPU peligrosos.

## Plan de comparación

1. Guardar la partida. No jugar durante una advertencia térmica grave.
2. En **System driver**, ruta corta a pie y vuelo en la misma zona para
   confirmar si la franja ajedrezada/minimapa corrupto desaparece.
3. Con RADV Xclipse importado, poner **Normal** y registrar el mismo caso,
   incluido si `Loaded mode` coincide con el driver deseado.
4. Si persiste la corrupción solo en RADV, poner **Prueba A** y reiniciar el
   proceso del juego. Registrar imagen y FPS en frío.
5. Si A no resuelve, valorar **Prueba B** en una prueba breve (máx. 3-5 min),
   con temperatura estable y sin cargador. Volver a Normal si aumenta
   consumo/calor, aparecen cuelgues o baja FPS. No repetir pruebas después
   de GPU hang; reiniciar el teléfono antes de volver a usar gráficos 3D.
6. Reportar en una tabla simple: driver y opción, FPS 1/5/15 minutos,
   estado térmico (Normal/Moderate/Severe), caída de batería, detalle visual,
   tiempo de aparición de edificios y si hubo audio distorsionado.
7. Si solo afecta a RADV, reportar al desarrollador con una captura y
   log depurado de datos personales. No confundir artefactos del driver
   con el `gta4_dynamic_draw_distance` o el streaming de GTA IV.

## Límites

- Esta PR **no es una optimización demostrada** ni una nueva APK.
- Desactivar DCC puede reducir el rendimiento de memoria.
- FSR1, distancia de dibujado y ajustes de sombras no se alteran.
- El trabajo para optimizar la carga de la ciudad y lograr un objetivo de
  60 FPS necesita telemetría de CPU/GPU, streaming, temperatura y audio,
  además de cambios **nativos** comprobables.
- Mantener siempre identificador `com.kasoncubillos.xhynivs26stable`,
  certificado y menú MOD, y actualizar solo con versionCode ascendente.
