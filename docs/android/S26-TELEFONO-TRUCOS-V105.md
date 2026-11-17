# XHYN IV S26 — teléfono de Niko, candidato v105

## Identidad y autorización

La referencia instalada del propietario es **v102, 0.5.9.8-s26-stable3**.
La rama contiene v104, pero no debe exigirse instalarla como paso intermedio.

- Mantener `applicationId = com.kasoncubillos.xhynivs26stable`.
- Mantener la clave privada permanente y su configuración; no generar ni sustituir claves.
- `versionCode` permanece en **104** hasta completar integración, build y pruebas reales;
  después se podrá cambiar a **105**.
- No firmar, publicar ni distribuir APK sin autorización posterior del propietario.
- No modificar partidas ni el bitset persistente de trucos desbloqueados.

## Implementación candidata

`gta4_phone_cheat_patch.h` amplía el parche existente de Quicksave **en memoria**,
antes de que el constructor copie el programa `spcellphonemain`. No abre Android UI.

1. Se exige el mismo SHA-256 del SCO decodificado y el perfil de tamaño/locales
   que usa Quicksave. Solo se amplía el juego base, episodio 0, compatible con
   las 25 acciones del puente existente; TLAD/TBoGT conservan Quicksave.
2. Se localiza la llamada stock que añade `CP_CHEATS`, la construcción del
   estado **1032** y la aceptación que llama al mapeo stock de `ACTIVATE_CHEAT`.
   Los contratos comprueban helpers, argumentos y unicidad. No se usan offsets PC.
3. Se suprime la entrada stock condicional y se añade siempre **Trucos / Cheats**
   apuntando al estado original 1032. Se conserva el renderer y la navegación stock.
4. Entre el reset y el renderer se sustituye el bucle de trucos desbloqueados por
   25 llamadas al helper de opciones original. Se usan acciones reservadas
   `0x7E00..0x7E18` y claves de texto `LR_C00..LR_C24`.
5. La aceptación valida programa, episodio, estado y acción seleccionada. Solo
   entonces solicita una acción mediante la cola existente. La solicitud espera
   al cierre stock del teléfono y a gameplay válido; expira o se cancela al
   cambiar mundo, jugador o contexto. El mapeo retail original no se ejecuta además.
6. Listar, navegar y volver no llaman al thunk de solicitudes. El constructor
   de la lista no modifica el estado de las partidas ni activa trucos.
7. Si un contrato falla, se conserva el teléfono stock y el parche de Quicksave;
   se registra `gta4-phone-cheats: contract rejected`. El fallo no se presenta
   como integración funcional.

Desactivar Quicksave ya no impide analizar el script del teléfono. El parámetro
`quicksave_enabled` conserva la preferencia de ocultar su entrada y permite
integrar Cheats independientemente.

La estructura de referencia de los estados, helpers y aceptación puede revisarse en
[spcellphonemain decompilado, revisión fija](https://github.com/0x52617665/GTAIVMissionScripts/blob/9d836ff2d955ba2b19048c496f81a0f9eb6c84cb/IV/spcellphonemain.txt).
Es evidencia de diseño, **no** una prueba binaria del SCO Xbox con el hash admitido.

## Pruebas y límites

- Catálogo: 80 comprobaciones, 25 IDs coincidentes con `CheatsUi.java`.
- Quicksave: 155971 aserciones de políticas y contratos de parche.
- Adaptador: fixture de instrucciones enlazadas **sintético**, 25 entradas,
  conservación del renderer, IDs reservados, rechazo de contratos ausentes,
  duplicados y episodio incorrecto; espera al cierre, expiración y cambio de mundo.
- Catch2 con ASan/UBSan: 156118 aserciones del teléfono en tres casos; sin errores.
- Núcleo PPC/CRT: 192 aserciones en 45 casos, con ASan/UBSan; correctas.
- Sintaxis host del archivo completo de hooks: correcta.
- Registro de hacks: 110 registros válidos.
- La inspección PC previa ayudó a resolver los helpers y operandos PUSH_S,
  pero trasladó incorrectamente el destino Cheats 1035. El archivo Xbox real
  demuestra que el destino correcto es **1032**; esta revisión corrige la
  constante usada por el constructor y la validación de aceptación.
- La fixture de CI fija 1032 como dato independiente del código del catálogo,
  para detectar una futura regresión al identificador de PC.

## Validación del archivo Xbox real recibido del propietario

El 17-11-2026 se recibió por Drive el `script.img` exportado desde la aplicación.
Coincide byte por byte con el informe enviado por el propietario:

| Dato | Valor |
| --- | --- |
| IMG | 15212544 bytes; SHA-256 `bbd59f160fa5ed2e29c5908e8fd9bd4e259804aaff61d2c43cd81e36d06b974a` |
| Contenedor | IMG v3, 364 entradas, TOC de 11353 bytes |
| Entrada | `spcellphonemain.sco`, índice 307, offset 14147584, 49384 bytes |
| SCO original | SHA-256 `6b595de1d55375b2c71b026722d582fadd5d1732da268ec4a05d635b16b68dcf` |
| Código decodificado | 45292 bytes, 1017 locales, SHA-256 `9ab320e5b78782cefc259fb885b391a6d6db5113e06a28cf307ca1feda09b12b` |
| Estado Cheats | 1032, confirmado en el switch stock de Xbox |

Los archivos comerciales se conservan solo como entrada privada de diagnóstico;
no se incluyen en el repositorio, la APK ni los fixtures publicados.

`tools/test_s26_phone_program.cpp` admite como entrada opcional ese código
privado decodificado. Valida su SHA-256 con el perfil existente, simula la
sustitución de operandos nativos realizada por el linker y comprueba los caminos
generados con un intérprete pequeño de las instrucciones emitidas:

- **91859 aserciones**, también con ASan/UBSan; sin errores.
- Quicksave habilitado y deshabilitado: 25 opciones, labels y acciones exactas.
- Ninguna solicitud al construir/listar; una solicitud al aceptar; cierre solo
  si la cola admite la solicitud; pila equilibrada al regresar al código stock.
- Código original intacto fuera de las cinco ventanas de salto previstas.
- Código parcheado: 46183 bytes con Quicksave y 46152 sin Quicksave.

Reproducción, con los datos privados disponibles localmente:

```sh
g++ -std=c++20 -O2 -Wall -Wextra -Werror \
  -Iglue/rexglue-sdk-main/gta4-recomp/src \
  -Iglue/rexglue-sdk-main/thirdparty \
  tools/test_s26_phone_program.cpp -o /tmp/s26-real-phone
/tmp/s26-real-phone /ruta/privada/spcellphonemain.code.bin
```

Esta prueba verifica los contratos binarios y la emisión del parche sobre el
SCO Xbox real; no ejecuta el juego ni demuestra los 25 efectos en un S26.
La PR sigue en borrador hasta completar esa validación física.

## Compilación ARM64

El workflow `s26-phone-native-ci.yml` valida catálogo, adaptador y regresión de
Quicksave en cada cambio relevante. Su ejecución manual con `build_native=true`
prepara dependencias, compila el motor y el proxy desde el código fuente y muestra
la arquitectura y hashes de las bibliotecas. La firma y publicación se habilitan
solo con la opción manual adicional `diagnostic_apk=true`, autorizada por el propietario.

El primer intento local de `build_native.sh 2` se detuvo antes de configurar:
`no NDK at /home/agent/Android/Sdk/ndk/29.0.14206865`.
El [build remoto completo](https://github.com/kasoncubillos-cyber/XHYN-IV-S26/actions/runs/43982560594)
terminó correctamente el 17-11-2026 a las 00:11 UTC para el commit `ea1cbd2f`:
compilación/enlace del motor, proxy Vulkan y registro de bibliotecas aprobados.
`libmain.so` fue identificado como ELF 64-bit ARM aarch64 y su SHA-256 fue
`65da870840cd8c77c90cd5a2a772c31f592ebe3331edcc1d55f0d1a2f28d1a3c`.
Los logs confirman la compilación de `gta4_quicksave_hooks.cpp` desde esta rama.
No se creó, firmó ni distribuyó ninguna APK. Esto completa el build, pero no la
validación funcional del teléfono dentro del juego.
No usar el workflow firmado `s26-updates-signed.yml` para transportar este cambio:
reutiliza bibliotecas Alpha1, que no contienen la integración C++.

## Validación pendiente antes de 105

- Completado: contratos del SCO Xbox real y caminos emitidos. Pendiente: log de
  publicación del programa dentro del juego y resultados físicos en S26.
- Nuevo juego y partida existente: menú visible sin marcar, 25 acciones, scroll completo,
  Back sin activación y una sola acción exacta por aceptación.
- Probar Quicksave habilitado/deshabilitado, regreso al menú, llamadas e interrupciones.
- Probar los efectos de las 25 acciones en juego libre y cancelaciones por carga/misión.
- Comprobar estabilidad, FPS y protección térmica en Galaxy S26.
- Con la autorización de firma ya concedida: comparar certificado contra **v102**, verificar
  paquete y versión con herramientas Android, instalar encima sin desinstalar ni
  borrar datos y comprobar las mismas partidas. Nunca usar una firma de prueba.


## Diagnóstico desde Android sin acceso a carpetas protegidas

El propietario autorizó posteriormente la firma y distribución. Como Android
no permite abrir la carpeta del juego desde su administrador de archivos y no
hay computadora disponible, se añade **Exportar diagnóstico del teléfono** a la
pantalla de ubicación del juego. Usa `ACTION_CREATE_DOCUMENT` para guardar
`XHYN-S26-telefono.zip` en Descargas, sin cambiar la ubicación del juego.

El exportador solo lee `script.img`, `script.rpf` y `spcellphonemain.sco` dentro
de la instalación seleccionada. Incluye rutas relativas y SHA-256; no busca
partidas ni preferencias. No sigue enlaces simbólicos y limita archivos/tamaño.
Si no encuentra scripts sueltos, produce únicamente un informe con nombres
relacionados para investigar el formato de instalación sin pedir otra copia del
juego. Las pruebas comparan bytes originales/exportados, conservación de partidas,
exclusión de enlaces, reporte de ausencia y rechazo de archivos excesivos.

La APK de diagnóstico mantiene versionCode **104** (superior a la v102 instalada),
paquete estable y clave permanente. Se distingue por versionName
`0.5.9.10-s26-phone-diagnostics`. La opción manual `diagnostic_apk=true` del
workflow reconstruye todo ARM64, empaqueta esas bibliotecas, firma con los
secretos permanentes y exige certificado idéntico al APK v102 antes de publicar
una prerelease separada `s26-phone-diagnostics-v104`. No modifica la release v104
existente ni declara completada la validación funcional de v105.


## Revisión R2

Corrige el estado Cheats a 1032 a partir del archivo Xbox real. Mantiene
versionCode 104 y cambia versionName a `0.5.9.10-s26-phone-diagnostics-r2`.
Se publica como `s26-phone-diagnostics-v104-r2` con el instalador
`XHYN-IV-S26-telefono-r2-v104.apk`, sin reemplazar las releases anteriores.
El workflow recompila todo ARM64 y compara el certificado con v102 antes de
publicar. La R2 se puede instalar como actualización de v102 o del diagnóstico
v104 anterior. Sigue pendiente probar la navegación, los 25 efectos y el
rendimiento en el dispositivo antes de habilitar v105.
