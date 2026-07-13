# Arquitectura

Hexagonal / clean architecture: el dominio no sabe que existe Spring, TCP, ni HTTP.

```
                     ┌─────────────────────┐
   HTTP request ---> │  api (controllers)  │
                     └─────────┬───────────┘
                               │ usa
                     ┌─────────▼───────────┐
                     │  domain.port.in     │   <- casos de uso (interfaces)
                     └─────────┬───────────┘
                               │ implementado por
                     ┌─────────▼───────────┐
                     │  application.service│   <- orquesta, sin logica de protocolo
                     └─────────┬───────────┘
                               │ usa
                     ┌─────────▼───────────┐
                     │  domain.port.out    │   <- PaymentHsmPort / GenericCryptoPort
                     └─────────┬───────────┘
                               │ implementado por
              ┌────────────────┴─────────────────┐
   ┌──────────▼───────────┐          ┌────────────▼───────────┐
   │ infrastructure.thales│          │ infrastructure.luna     │
   │ (TCP + protocolo)    │          │ (JCE/PKCS#11)           │
   └───────────────────────┘          └─────────────────────────┘
```

## Por que esta separacion

- **domain/model**: value objects inmutables (`EncryptedKey`, `PinBlock`, `Pan`...) que validan
  sus invariantes en el constructor. Un `PinBlock` invalido no puede existir en el sistema, no
  hace falta re-validar en cada capa.
- **domain/port.out**: lo que el dominio *necesita* del mundo exterior (`PaymentHsmPort`,
  `GenericCryptoPort`), sin decir como se implementa. Permite testear la capa de aplicacion sin
  sockets ni HSM real (ver `application/service/*Test.java`, todos con Mockito).
- **domain/port.in**: lo que el mundo exterior puede *pedirle* al dominio (casos de uso). El
  controller REST depende de esto, no de la implementacion concreta.
- **infrastructure.thales**: todo el protocolo Thales vive aca, dividido en:
  - `protocol/` - framing, header, y un `ThalesCommand<R>` por comando (CA, G0, EA, BK, A0, EI,
    NC, NO). Cada comando sabe codificar sus propios campos y parsear su propia respuesta;
    nadie mas necesita saber el formato de bytes de "CA".
  - `transport/` - la conexion TCP y el pool. No sabe nada de PIN blocks ni de llaves, solo
    envia bytes y correla respuestas por el header de 12 digitos.
  - `ThalesPaymentHsmAdapter` - el pegamento fino entre el dominio y los comandos concretos.
- **infrastructure.luna**: deliberadamente implementa un puerto *distinto*
  (`GenericCryptoPort`), no `PaymentHsmPort` - ver
  [`THALES_9000_10K_LUNA.md`](THALES_9000_10K_LUNA.md) sobre por que.

## Por que no se uso el enfoque original (FSDMsg + XML)

El cliente jPOS original describia cada comando en un archivo XML suelto (`hsm-CA16.xml`,
`hsm-EA32.xml`...) sin ningun chequeo de que la longitud declarada coincidiera con la real. Esto
permitio, por ejemplo, que `hsm-EA32.xml` declarara el campo `zpk-type` dos veces por error en
vez de `pvk-type` (ver `docs/SECURITY_FINDINGS.md`) sin que nada lo detectara.

Aca cada comando es una clase Java (`record` que implementa `ThalesCommand<R>`), y cada campo se
valida con `FieldCodec.fixed(...)` en tiempo de codificacion. Un error de longitud o de campo
falla en tiempo de ejecucion con un mensaje claro (idealmente detectado por un test), no en
silencio contra un HSM productivo.

## Sobre JCE

`infrastructure.luna.LunaCryptoAdapter` es el unico punto del proyecto que usa JCE "estandar"
(`java.security.Provider`, `KeyStore` tipo `PKCS11`, `Signature`, `Cipher`, `KeyPairGenerator`).
El HSM de pagos (Thales payShield) no se integra por JCE: habla su propio protocolo de mensajes
sobre TCP, por eso `infrastructure.thales` no usa `java.security` en absoluto - es simplemente
un cliente de un protocolo de aplicacion propio, no un `Provider` criptografico de la JVM.
