# Hallazgos y correcciones respecto al cliente original (jPOS `org.jpos.security.thales`)

Este documento existe para que la migracion sea auditable: que se cambio, por que, y que sigue
siendo una limitacion conocida.

## Corregido en esta reimplementacion

1. **Estado mutable compartido en el adapter (bug de concurrencia real).**
   El original guardaba `requestVersion`/`responseVersion` como campos de instancia de
   `ThalesAdapter` (un singleton registrado en `NameRegistrar`), seteados antes de armar el
   request y leidos despues de recibir la respuesta. Bajo trafico concurrente, dos hilos podian
   pisarse esos campos y un request terminar parseando la respuesta con el schema equivocado.
   Aca cada `ThalesCommand` es inmutable y stateless (un `record`); no hay estado compartido
   entre requests.

2. **Campo mal declarado en `hsm-EA32.xml`.** El schema original declaraba el flag de esquema
   del PVK copiando el literal `zpk-type` en vez de `pvk-type` (ver
   `ValidatePinOffsetCommandTest#doubleLengthPvkGetsItsOwnSchemeFlagNotACopyOfTheZpkFlag`). No
   rompia el pack porque el valor literal ("U") era igual en ambos casos, pero era una trampa
   para cualquier cambio futuro. Aca el flag de cada llave sale de su propio `KeyScheme`, no hay
   nombres de campo hardcodeados que puedan copiarse mal.

3. **Sin validacion de longitud/formato al armar mensajes.** El original no validaba que un
   campo tuviera la longitud declarada antes de mandarlo al HSM. Aca `FieldCodec.fixed(...)`
   valida cada campo en tiempo de codificacion y falla rapido con un mensaje claro.

4. **Correlacion request/response acoplada a jPOS Space + polling de 500ms.** El original hacia
   polling de la cola cada 500ms para leer requests salientes. Aca un hilo lector dedicado por
   conexion reacciona apenas llega el frame (menor latencia), y la correlacion es un
   `ConcurrentHashMap<String, CompletableFuture<String>>` en memoria, sin depender de un
   framework de colas externo.

5. **Sin failover explicito entre nodos.** El original abria un hilo por `host:port` configurado
   y dependia de que el hilo libre tomara el siguiente request de una cola compartida - reparto
   implicito, sin logica de salud por nodo. Aca `ThalesConnectionPool` hace round-robin
   explicito sobre nodos saludables y reconecta en background los que caen.

6. **Catalogo de errores como `HashMap<String,String>` sin validar en compilacion.** Aca es un
   enum (`ThalesErrorCode`) exhaustivo: un codigo mal escrito en el codigo que lo consume no
   compila (o cae visiblemente en `UNKNOWN`), en vez de fallar en silencio con un `HashMap.get`
   que devuelve `null`.

## Limitaciones conocidas, dejadas explicitas en vez de ocultas

7. **Comando EI/EJ (generacion de par RSA): la respuesta del schema original no parece completa.**
   `hsm-resp-EJ.xml` declaraba la llave publica en 32 hex chars (16 bytes) - insuficiente para el
   modulo de una llave RSA-2048 real. El unico test original para este comando estaba
   `@Ignore` y sin asserts de estructura, es decir: nunca se verifico contra un HSM real. Aca se
   implemento con lectura de largo explicito (length-prefixed) para la llave privada, pero se
   documenta como "mejor esfuerzo" en el Javadoc de `GenerateRsaKeyPairCommand` - **validar
   contra el manual de tu firmware exacto antes de usarlo en produccion**.

8. **Comando A0 modo 1 (generar llave envuelta bajo un ZMK existente): tampoco tiene test.**
   El unico test original (`testGenerateKey`) ejercita el modo 0 (bajo LMK solamente). El modo 1
   se implemento siguiendo la intencion del schema original, pero sin evidencia de que haya
   corrido contra un HSM real - mismo disclaimer que el punto anterior.

## Fuera de alcance de este cliente (por diseño, no por descuido)

9. **Material de llaves en `keys.properties` plano.** El deploy original
   (`30_thales_manager.xml`) referenciaba `cfg/keys.properties` como archivo de configuracion
   de llaves. Este proyecto no incluye gestion de secretos: las propiedades de conexion vienen de
   variables de entorno (ver `application-prod.properties`), pero la custodia de llaves de
   aplicacion es responsabilidad de quien despliegue esto - considerar Vault/KMS en vez de un
   archivo plano en el filesystem del deploy.

10. **Sin TLS entre esta app y el HSM.** El transporte (`ThalesConnection`) usa un `Socket` plano,
    igual que el original. En la mayoria de los despliegues el segmento host-HSM esta aislado a
    nivel de red (requisito tipico de validacion PCI HSM), pero si tu topologia no lo garantiza,
    envolver el socket con `SSLContext`/`SSLSocketFactory` es un cambio acotado en
    `ThalesConnection.connect(...)` antes de ir a produccion.
