# Riesgos evitados respecto a un enfoque clasico basado en jPOS (`org.jpos.security.thales`)

Este documento existe para que las decisiones de diseno sean auditables: que riesgo se evita,
por que, y que sigue siendo una limitacion conocida.

## Decisiones de diseno en esta implementacion

1. **Estado mutable compartido en el adapter (bug de concurrencia real).**
   Un enfoque clasico basado en un adapter singleton (ej. un `NameRegistrar` de jPOS) suele
   guardar `requestVersion`/`responseVersion` como campos de instancia, seteados antes de armar
   el request y leidos despues de recibir la respuesta. Bajo trafico concurrente, dos hilos
   pueden pisarse esos campos y un request termina parseando la respuesta con el schema
   equivocado. Aca cada `ThalesCommand` es inmutable y stateless (un `record`); no hay estado
   compartido entre requests.

2. **Riesgo de campo mal declarado en un schema XML (ej. `hsm-EA32.xml`).** Es facil que un
   schema FSDMsg declare el flag de esquema del PVK copiando el literal `zpk-type` en vez de
   `pvk-type` (ver
   `ValidatePinOffsetCommandTest#doubleLengthPvkGetsItsOwnSchemeFlagNotACopyOfTheZpkFlag`). Ese
   error no rompe el pack si el valor literal ("U") es igual en ambos casos, pero es una trampa
   para cualquier cambio futuro. Aca el flag de cada llave sale de su propio `KeyScheme`, no hay
   nombres de campo hardcodeados que puedan copiarse mal.

3. **Sin validacion de longitud/formato al armar mensajes.** Un enfoque sin validacion explicita
   puede mandar un campo con una longitud distinta a la declarada sin que nada lo detecte antes
   de llegar al HSM. Aca `FieldCodec.fixed(...)` valida cada campo en tiempo de codificacion y
   falla rapido con un mensaje claro.

4. **Correlacion request/response sin depender de polling sobre una cola compartida.** Un enfoque
   basado en una cola compartida (ej. jPOS Space) con polling cada 500ms agrega latencia
   innecesaria. Aca un hilo lector dedicado por conexion reacciona apenas llega el frame (menor
   latencia), y la correlacion es un `ConcurrentHashMap<String, CompletableFuture<String>>` en
   memoria, sin depender de un framework de colas externo.

5. **Failover explicito entre nodos.** Repartir requests de forma implicita entre hilos que
   compiten por una cola compartida no da control sobre la salud de cada nodo. Aca
   `ThalesConnectionPool` hace round-robin explicito sobre nodos saludables y reconecta en
   background los que caen.

6. **Catalogo de errores como `HashMap<String,String>` sin validar en compilacion.** Aca es un
   enum (`ThalesErrorCode`) exhaustivo: un codigo mal escrito en el codigo que lo consume no
   compila (o cae visiblemente en `UNKNOWN`), en vez de fallar en silencio con un `HashMap.get`
   que devuelve `null`.

## Limitaciones conocidas, dejadas explicitas en vez de ocultas

7. **Comando EI/EJ (generacion de par RSA): riesgo de respuesta de schema incompleta.** Un schema
   XML que declare la llave publica en 32 hex chars (16 bytes) es insuficiente para el modulo de
   una llave RSA-2048 real, y sin un test que verifique la estructura contra un HSM real ese
   error pasa desapercibido. Aca se implemento con lectura de largo explicito (length-prefixed)
   para la llave privada, pero se documenta como "mejor esfuerzo" en el Javadoc de
   `GenerateRsaKeyPairCommand` - **validar contra el manual de tu firmware exacto antes de
   usarlo en produccion**.

8. **Comando A0 modo 1 (generar llave envuelta bajo un ZMK existente): sin test contra HSM real.**
   El test de este proyecto (`testGenerateKey`) ejercita el modo 0 (bajo LMK solamente). El modo 1
   se implemento siguiendo la especificacion del protocolo Variant LMK, pero sin evidencia de que
   haya corrido contra un HSM real - mismo disclaimer que el punto anterior.

## Fuera de alcance de este cliente (por diseño, no por descuido)

9. **Material de llaves en `keys.properties` plano.** Un deploy tipico basado en jPOS (ej.
   `30_thales_manager.xml`) referencia `cfg/keys.properties` como archivo de configuracion de
   llaves. Este proyecto no incluye gestion de secretos: las propiedades de conexion vienen de
   variables de entorno (ver `application-prod.properties`), pero la custodia de llaves de
   aplicacion es responsabilidad de quien despliegue esto - considerar Vault/KMS en vez de un
   archivo plano en el filesystem del deploy.

10. **Sin TLS entre esta app y el HSM.** El transporte (`ThalesConnection`) usa un `Socket` plano.
    En la mayoria de los despliegues el segmento host-HSM esta aislado a nivel de red (requisito
    tipico de validacion PCI HSM), pero si tu topologia no lo garantiza, envolver el socket con
    `SSLContext`/`SSLSocketFactory` es un cambio acotado en `ThalesConnection.connect(...)` antes
    de ir a produccion.
