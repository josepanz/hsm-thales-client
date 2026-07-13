# Checklist: de mock a Thales Luna real

Este documento es el "paso a paso donde y como" para pasar de desarrollar sin hardware a
conectar contra un Luna Network HSM de verdad. Ver tambien
[`THALES_9000_10K_LUNA.md`](THALES_9000_10K_LUNA.md) para el contexto de por que Luna es un
puerto separado del HSM de pagos.

## Estado actual (por defecto)

```properties
hsm.luna.enabled=false     # ver src/main/resources/application.properties
```

Sin tocar nada, `GenericCryptoPort` no tiene ningun bean - el que quiera inyectarlo va a fallar
el arranque de Spring con un mensaje claro de "no qualifying bean", no en silencio.

## Paso 1 - Desarrollar/testear sin hardware (mock)

```properties
# application-dev.properties, o tus propias variables de entorno
hsm.luna.enabled=true
hsm.luna.mock-mode=true
```

Con esto, `LunaConfig` (ver `src/main/java/com/hsmclient/config/LunaConfig.java`) inyecta
`MockGenericCryptoAdapter` (`src/main/java/com/hsmclient/infrastructure/luna/MockGenericCryptoAdapter.java`):
JCE puro contra los providers por defecto de la JVM, sin PKCS#11 ni libreria nativa. Sirve para:

- Desarrollar features que dependen de `GenericCryptoPort` sin esperar acceso a un HSM.
- Correr `MockGenericCryptoAdapterTest` en CI (`mvn test -Dtest=MockGenericCryptoAdapterTest`).

**Limite a proposito:** el mock solo modela pares de llaves asimetricas por alias (RSA). Si tu
caso de uso necesita llaves simetricas persistidas por alias, es una extension chica: agregar un
`Map<String, SecretKey>` igual al de los `KeyPair` dentro de esa clase (esta comentado ahi mismo).

**Nunca en produccion:** las llaves del mock viven en la memoria de la JVM sin ninguna
proteccion de hardware. `LunaConfig` logea un `WARN` cada vez que arranca en este modo,
justamente para que sea imposible no notarlo.

## Paso 2 - Conectar contra un Luna real (o su simulador)

Cuando tengas acceso a un Luna Network HSM (o al "Luna HSM Client" con particion simulada que
provee Thales para desarrollo):

1. **Instalar el cliente Luna** en el host/contenedor donde corre esta app (el paquete
   "Chrystoki"/"cryptoki", que trae la libreria nativa `.so`/`.dll` que usa el provider
   `SunPKCS11` de la JVM).
2. **Crear el archivo de configuracion PKCS#11**, algo como:
   ```
   name = LunaHSM
   library = /usr/safenet/lunaclient/lib/libCryptoki2_64.so
   slot = 0
   ```
   y apuntar `hsm.luna.pkcs11-config-path` a ese archivo.
3. **Setear el PIN/password de la particion** en `hsm.luna.key-store-password`, desde una
   variable de entorno (ver `application-prod.properties`, `${HSM_LUNA_KEYSTORE_PASSWORD}`),
   nunca hardcodeado en un archivo commiteado.
4. **Cambiar una sola propiedad**: `hsm.luna.mock-mode=false`. No hace falta tocar codigo -
   `LunaConfig.genericCryptoPort(...)` ya tiene la rama que instancia
   `LunaCryptoAdapter` en vez del mock (ver el `if (properties.isMockMode())` en esa clase).
5. **Smoke test manual** antes de confiar en esto para produccion: generar un par de llaves,
   firmar algo y verificar la firma, envolver una llave simetrica y desenvolverla - los mismos
   tres casos que ya estan cubiertos por `MockGenericCryptoAdapterTest`, pero corridos a mano
   contra el HSM real para confirmar que el provider PKCS#11 quedo bien configurado.

## Resumen de donde esta cada cosa

| Que | Donde |
|---|---|
| Flag de encendido/apagado | `LunaProperties.enabled` |
| Flag mock vs real | `LunaProperties.mockMode` |
| Wiring condicional (Spring) | `LunaConfig` |
| Implementacion software (dev/test) | `MockGenericCryptoAdapter` |
| Implementacion real (PKCS#11) | `LunaCryptoAdapter` |
| Tests que corren sin hardware | `MockGenericCryptoAdapterTest`, `LunaPropertiesTest`, `LunaConfigTest` |
