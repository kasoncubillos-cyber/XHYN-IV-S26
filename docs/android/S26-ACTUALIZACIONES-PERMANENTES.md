# XHYN IV S26 — actualizaciones sin instalar otra aplicación

## Por qué Beta3 no se puede actualizar directamente
Las Betas 1–3 utilizaron claves PKCS12 **temporales distintas**. La compilación de Beta3 borró su clave privada del runner después de firmarla. Android exige el mismo certificado/clave de firma para una actualización del mismo paquete. No se puede recuperar la clave privada a partir de una APK firmada y no es seguro prometer una actualización directa de Beta3.

## Una única migración al canal definitivo
La primera entrega del canal definitivo será `com.kasoncubillos.xhynivs26stable` con versionCode 100. Puede instalarse **al lado** de Beta3; solo cuando la instalación y las partidas funcionen se puede retirar Beta3. El usuario debe conservar su propia copia legal de los datos del juego. Para mover avances, exportar una copia de las partidas/logros desde la app anterior e importarlos en la nueva. La ubicación de los archivos de juego no se comparte entre paquetes sin una importación explícita.

## Clave privada permanente
Se generó una clave PKCS12 nueva, con alias `xhyn-s26-stable` y vigencia de 25 años. El propietario recibe un respaldo privado aparte de este repositorio.

NUNCA subir al repositorio público la clave `.p12`, ni contraseña, ni base64 de esa clave. La base64 NO es cifrado. Guardar varias copias de seguridad seguras.

1. Abrir [Secretos de GitHub Actions](https://github.com/kasoncubillos-cyber/XHYN-IV-S26/settings/secrets/actions) con la cuenta propietaria.
2. Crear secreto de repositorio `XHYN_S26_KEYSTORE_BASE64` con el contenido completo del archivo `KEYSTORE-BASE64.txt` del respaldo privado.
3. Crear secreto de repositorio `XHYN_S26_KEYSTORE_PASSWORD` con la contraseña exacta de `CONTRASENA-PRIVADA.txt`.
4. Abrir [S26 Actualizaciones Firmadas](https://github.com/kasoncubillos-cyber/XHYN-IV-S26/actions/workflows/s26-updates-signed.yml), seleccionar rama `feature/s26-conduccion-fluida-updates` y ejecutar el flujo. Si el flujo manual no aparece (workflow no está aún en la rama principal), se activa al hacer un nuevo commit en la rama; no introducir contraseñas como inputs públicos.

El flujo `.github/workflows/s26-updates-signed.yml` verificará tests y hashes nativos antes de usar los secretos para firmar. Sin secretos no se publica un APK instalable. Los secretos solo se inyectan en el paso de firma y se borran los archivos temporales.

## Actualizaciones siguientes
- Mantener **exactamente** `applicationId = "com.kasoncubillos.xhynivs26stable"`.
- Incrementar `versionCode` a 101, 102, etc.; actualizar `versionName`.
- Firmar siempre con el mismo `XHYN_S26_KEYSTORE_BASE64`, `XHYN_S26_KEYSTORE_PASSWORD` y alias `xhyn-s26-stable`.
- Se publica una APK directamente en GitHub Releases con tag `s26-update-vNNN`.
- Instalarla sobre la anterior, sin desinstalarla. Conserva datos y partidas **si Android acepta la firma y el paquete coincidente**.

El objetivo es estabilidad a largo plazo: nunca incrustar ni publicar la clave de firma en código o releases.
