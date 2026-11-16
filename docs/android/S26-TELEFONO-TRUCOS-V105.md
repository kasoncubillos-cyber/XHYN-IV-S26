# XHYN IV S26 — Teléfono de Niko / catálogo de trucos (preparación v105)

## Requisito
La actualización siguiente debe instalarse sobre la aplicación estable v104 instalada,
manteniendo paquete, firma digital y datos del usuario. No generar una aplicación paralela.

- `applicationId = "com.kasoncubillos.xhynivs26stable"`.
- Incrementar `versionCode` de 104 a 105 **solo cuando** la funcionalidad esté implementada,
  compilada en el motor ARM64, verificada y lista para el canal de pruebas.
- Firmar exclusivamente con los mismos secretos de la v104:
  `XHYN_S26_KEYSTORE_BASE64` y `XHYN_S26_KEYSTORE_PASSWORD`.
- No modificar el almacén de partidas para precargar trucos; no ejecutar trucos automáticamente.

## Función objetivo
Con el teléfono original de Niko ya disponible, abrir **Cheats/Trucos** y ver las 25
acciones compatibles sin marcar ningún número antes. Al seleccionar, cerrar el teléfono y
ejecutar exactamente un truco cuando el contexto de juego libre lo permita. Al cancelar,
no activar nada. Mantener intacto Quicksave, la navegación y la presentación del teléfono.

## Código existente
- `gta4_phone_cheat_catalog.h`: 25 entradas, etiquetas/categorías, índices sin duplicar.
- `gta4_phone_cheat_catalog_test.cpp`: cobertura de entradas/índices (incluido 5 no admitido).
- `gta4_cheat_policy.h`, `gta4_quicksave_hooks.cpp`: cola y ejecución actual de trucos.
- `gta4_phone_quicksave_profiles.h`: extensión verificada de `spcellphonemain` por hash
  que añade el botón Quicksave. Es la referencia para ampliar el teléfono nativo.

## Bloqueador técnico: no empaquetar motor antiguo
El workflow `s26-updates-signed.yml` reutiliza **los 11 .so de Alpha1** con
`package_release.py --native-apk`. Ese mecanismo solo sirve para arreglos Java/recursos,
**no** distribuye cambios de `gta4-recomp/src/*.cpp` ni nuevos ganchos de teléfono.

Por eso la nueva versión debe:
1. Identificar y validar con evidencia el constructor del submenú stock Cheats o crear
   un submenú nativo compatible en `spcellphonemain` (no una pantalla Android externa).
2. Ampliar el parche con contratos de hash/offset y fallar de forma segura si cambian.
3. Compilar el motor nativo desde esta rama con
   `tools/setup_repo.py`, `os/android/scripts/build_native.sh` y
   `os/android/scripts/build_driver_proxy.sh`.
4. Usar `package_release.py --unsigned` **sin** `--native-apk`; verificar que el
   paquete contenga las bibliotecas del build actual.
5. Ejecutar regresiones, comprobar efecto real en S26 y solo después generar un APK
   firmado con la identidad estable y versión incremental.

## Estado verificable
**NO IMPLEMENTADO TODAVÍA:** las entradas no están vinculadas al teléfono.
El catálogo aislado y las pruebas no constituyen una APK jugable con menú.
La rama se conserva como PR en borrador hasta pasar las comprobaciones.

## Prueba final
- Nuevo juego y partida guardada existente: 25 acciones visibles sin marcación.
- Ninguna activación al listar ni al cancelar; una acción exacta tras seleccionarla.
- Continuidad del menú y Quicksave, sin bloqueo de logros inesperado.
- No empeorar FPS ni romper estabilidad térmica.
- `apksigner verify` y comparación de certificado con v104; mismo `applicationId`,
  `versionCode > 104`; instalación directa encima y datos intactos.
