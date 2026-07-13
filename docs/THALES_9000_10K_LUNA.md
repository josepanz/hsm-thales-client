# Compatibilidad: payShield 9000, payShield 10000 y Thales Luna

## payShield 9000 y payShield 10000

El protocolo implementado en `infrastructure.thales.protocol` es el set de comandos clasico
"Variant LMK" (a veces llamado protocolo Racal/Thales heredado), el mismo que ya usaba el
cliente jPOS original: `NC`, `NO`, `A0`, `EI`, `EA`, `BK`, `G0`, `CA`.

- **payShield 9000**: lo soporta nativamente, sin configuracion adicional.
- **payShield 10000**: lo soporta en modo de compatibilidad/legacy LMK. El 10000 tambien soporta
  (y para instalaciones nuevas, recomienda) el modo **Key Block** (ANSI TR-31 / ISO 20038), que
  es un protocolo de mensajes distinto con un formato de llave diferente (header explicito con
  metadata en vez del flag de un caracter "U"/"T" que usa Variant LMK).

**Consecuencia practica:** este cliente funciona contra un payShield 10000 *siempre y cuando*
ese HSM este configurado para aceptar comandos Variant LMK (que es la config por defecto en la
mayoria de migraciones desde 9000). Si en algun momento el 10000 se reconfigura en modo
"Key Block only", los comandos de este cliente (`CA`, `EA`, `BK`, `A0`...) van a fallar, y haria
falta implementar el set de comandos Key Block equivalente (fuera del alcance de este proyecto).

## Thales Luna Network HSM

Luna es un producto de una familia completamente distinta dentro del portfolio de Thales:

| | payShield 9000/10000 | Thales Luna |
|---|---|---|
| Proposito | HSM de **pagos**: PIN block, PVK/CVV, llaves de esquema DES/3DES bajo LMK | HSM de **proposito general**: custodia de llaves, firma, cifrado de aplicacion |
| Interfaz | Protocolo de mensajes propietario sobre TCP (el que implementa este cliente) | PKCS#11 / JCE / KMIP - APIs estandar de la industria |
| Comandos de pago (CA/EA/BK/G0...) | Si | **No existen** - Luna no tiene concepto de TPK/ZPK/PVK bajo un LMK Variant ni de traduccion de PIN block |
| Como se integra en este proyecto | `infrastructure.thales` -> implementa `PaymentHsmPort` | `infrastructure.luna` -> implementa `GenericCryptoPort` (interfaz distinta, a proposito) |

**Por que no hay una sola interfaz "HSM" que cubra ambos:** forzar a Luna detras de
`PaymentHsmPort` hubiera significado, o (a) dejar la mayoria de los metodos sin implementacion
real (lanzando `UnsupportedOperationException`), o (b) intentar "simular" PIN translate con
crypto generica en la aplicacion - lo cual rompe la garantia de seguridad completa del asunto
(que el PIN nunca pase por la memoria de la aplicacion en claro). Por eso `GenericCryptoPort` es
un puerto separado con metodos que Luna si puede cumplir de verdad: `sign`, `wrapKey`,
`generateKeyPair`.

**Cuando usar Luna en un proyecto como este:** para todo lo que no sea el flujo de PIN/tarjeta -
por ejemplo, custodiar la llave privada de un certificado TLS mutual-auth hacia un adquirente,
firmar tokens/JWT de un servicio interno, o proteger una llave de cifrado a nivel de aplicacion
(no de tarjeta). Ver `LunaCryptoAdapter` y `application.properties` (`hsm.luna.*`, deshabilitado
por defecto).
