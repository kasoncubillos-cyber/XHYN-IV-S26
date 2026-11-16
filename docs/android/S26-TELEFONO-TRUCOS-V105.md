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
   estado **1035** y la aceptación que llama al mapeo stock de `ACTIVATE_CHEAT`.
   Los contratos comprueban helpers, argumentos y unicidad. No se usan offsets PC.
3. Se suprime la entrada stock condicional y se añade siempre **Trucos / Cheats**
   apuntando al estado original 1035. Se conserva el renderer y la navegación stock.
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
- Catch2 con ASan/UBSan: 156117 aserciones del teléfono en tres casos; sin errores.
- Núcleo PPC/CRT: 192 aserciones en 45 casos, con ASan/UBSan; correctas.
- Sintaxis host del archivo completo de hooks: correcta.
- Registro de hacks: 110 registros válidos.
- Inspección adicional del SCO PC de FusionFix incluido en el repositorio:
  decodificado localmente con RageLib/DataUtil como referencia, 52224 bytes de
  código. El adaptador resuelve sus contratos estructurales con parámetros PC
  temporales (helpers de 10 argumentos, menú local 1, opciones local 226).
  La prueba detectó y corrigió el caso de renderers de estados vecinos y los
  offsets locales cortos PUSH_S. No se añade ese perfil PC a producción ni se
  relajan los hashes Xbox admitidos.

Estos tests no demuestran que los contratos coincidan con el SCO real instalado,
que la pantalla se renderice correctamente ni que se produzcan los 25 efectos.
No hay datos del juego ni dispositivo Galaxy S26 conectado en el entorno de trabajo.
La PR sigue en borrador y **no es todavía una actualización lista para instalar**.

## Compilación ARM64

El workflow `s26-phone-native-ci.yml` valida catálogo, adaptador y regresión de
Quicksave en cada cambio relevante. Su ejecución manual con `build_native=true`
prepara dependencias, compila el motor y el proxy desde el código fuente y muestra
la arquitectura y hashes de las bibliotecas. No crea, firma ni sube APK.

El primer intento local de `build_native.sh 2` se detuvo antes de configurar:
`no NDK at /home/agent/Android/Sdk/ndk/29.0.14206865`.
El build remoto y sus resultados deben registrarse antes de considerar listo v105.
No usar el workflow firmado `s26-updates-signed.yml` para transportar este cambio:
reutiliza bibliotecas Alpha1, que no contienen la integración C++.

## Validación pendiente antes de 105

- Verificar contratos contra el SCO real admitido y el log de publicación del programa.
- Nuevo juego y partida existente: menú visible sin marcar, 25 acciones, scroll completo,
  Back sin activación y una sola acción exacta por aceptación.
- Probar Quicksave habilitado/deshabilitado, regreso al menú, llamadas e interrupciones.
- Probar los efectos de las 25 acciones en juego libre y cancelaciones por carga/misión.
- Comprobar estabilidad, FPS y protección térmica en Galaxy S26.
- Con autorización posterior de firma: comparar certificado contra **v102**, verificar
  paquete y versión con herramientas Android, instalar encima sin desinstalar ni
  borrar datos y comprobar las mismas partidas. Nunca usar una firma de prueba.
