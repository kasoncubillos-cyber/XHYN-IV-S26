# XHYN IV S26 — Alpha 1 (experimento privado)

Proyecto derivado de [XHYN-PH/XHYN-IV-recomp](https://github.com/XHYN-PH/XHYN-IV-recomp). Mantiene la licencia GPLv3 y los avisos de derechos del repositorio original.

## Objetivo
Reproducir y comprobar si desaparece el cierre SIGSEGV durante la conducción de GTA IV en Samsung Galaxy S26 (Exynos/Xclipse). No se ha demostrado todavía una corrección definitiva en el dispositivo.

## Base de la rama
- Base: commit upstream `f12592072fcc42e730d89645c671773a5e1c46d8` (Test13 diagnóstico + Test14 corrección de anchos de argumentos del CRT).
- Los 14 hooks nativos de memoria y cadenas usan tamaños de Xbox de 32 bits, en vez de `size_t` de Android ARM64.
- Se mantienen los diagnósticos de fallos introducidos con Test13.

## Identidad Android de prueba
- `applicationId = com.kasoncubillos.xhynivs26`
- `versionName = 0.5.9.3-s26-alpha1`
- `versionCode = 1`
- Etiqueta: `XHYN IV S26 Alpha`

La instalación de prueba es **separada** de `com.xhyn.ivrecomp`. Cada app tiene sus propios datos y archivos de instalación del juego. **No desinstalar ni borrar** la app original. La prueba separada puede requerir repetir la instalación de los archivos del juego desde una copia propia; no se copiarán automáticamente partidas o datos de la otra app.

## Compilación y seguridad
El workflow `.github/workflows/s26-alpha1-apk.yml` prepara las herramientas Android/NDK, recompila las bibliotecas nativas y empaqueta la APK. Crea una clave temporal de firma **solo dentro del runner** y adjunta la APK firmada como artefacto de GitHub Actions, **no como Release pública**. Por tanto, nuevas ejecuciones pueden generar una firma distinta: para instalar una Alpha posterior sobre esta Alpha puede ser necesario exportar sus datos y desinstalar únicamente la Alpha anterior.

No subir claves, contraseñas, ISO, Title Update, archivos privados de juego ni partidas a GitHub. La APK se construye a partir del código fuente; los datos del juego debe aportarlos el usuario.

## Comprobaciones antes de llamar a la prueba «resuelta»
1. Confirmar que el CI de Android pasa las pruebas y que el APK construido está firmado, íntegro y alineado.
2. Instalar la APK S26 por separado, seleccionar **System driver** como punto de partida y escoger los archivos propios de GTA IV y TU8.
3. Ejecutar la misma ruta en auto que antes provocaba el cierre, con **Play with logs**.
4. Si falla, compartir el ZIP del registro sin exponer datos personales y localizar el nuevo `pc`, módulo, señal y offsets.
5. Registrar qué cambió; no atribuir estabilidad a la corrección sin confirmación en el teléfono.

**Estado actual**: código de Test14 integrado en la rama de experimentación; ejecución del juego en Galaxy S26 todavía no validada.
