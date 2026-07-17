# hsm-thales-client

Cliente Spring Boot para HSM de pagos Thales (payShield 9000/10000, protocolo Variant LMK),
implementado con arquitectura hexagonal, con las decisiones de diseno descritas en `docs/`.

Incluye tambien un adapter de ejemplo para **Thales Luna** (JCE/PKCS#11) para dejar explicito
que es un producto distinto, no un reemplazo del HSM de pagos - ver
[`docs/THALES_9000_10K_LUNA.md`](docs/THALES_9000_10K_LUNA.md).

## Arquitectura

Ver [`docs/ARCHITECTURE.md`](docs/ARCHITECTURE.md). Resumen: dominio puro (sin Spring, sin
sockets) en el centro; casos de uso en `application`; adapters de infraestructura (Thales TCP,
Luna PKCS#11, API REST) alrededor, cada uno implementando o consumiendo un puerto del dominio.

```
domain/         modelo + puertos (in/out) + excepciones -- sin dependencias externas
application/    casos de uso, orquestan los puertos "out"
infrastructure/
  thales/       protocolo, transporte TCP y adapter -> implementa PaymentHsmPort
  luna/         adapter JCE/PKCS11 -> implementa GenericCryptoPort (fuera del dominio de pagos)
api/            controllers REST, consumen los puertos "in"
config/         wiring de Spring Boot
```

## Requisitos

- Java 17
- Maven 3.9+

## Build y tests

```bash
mvn test          # unit tests
mvn package        # jar ejecutable en target/
mvn spring-boot:run
```

## Configuracion

Ver `src/main/resources/application.properties` y los perfiles `-dev`/`-prod`. Las propiedades
relevantes:

```properties
hsm.thales.nodes=host1:1500,host2:1500   # uno o mas nodos, failover round-robin
hsm.thales.connect-timeout=5s
hsm.thales.request-timeout=5s
hsm.luna.enabled=false                    # ver docs/THALES_9000_10K_LUNA.md
```

En produccion, todo lo sensible viene de variables de entorno (ver `application-prod.properties`),
nunca hardcodeado en el repo.

## API de ejemplo

```
POST /api/v1/pin/translate/tpk     { sourceTpk, destinationZpk, sourcePinBlock, pan }
POST /api/v1/pin/translate/dukpt   { sourceBdk, destinationZpk, ksnDescriptor, ksn, sourcePinBlock, pan }
GET  /actuator/health/thales
```

## Documentacion

- [`docs/ARCHITECTURE.md`](docs/ARCHITECTURE.md) - arquitectura hexagonal, por que cada capa existe.
- [`docs/SECURITY_FINDINGS.md`](docs/SECURITY_FINDINGS.md) - decisiones de diseno y riesgos evitados.
- [`docs/THALES_9000_10K_LUNA.md`](docs/THALES_9000_10K_LUNA.md) - compatibilidad payShield 9000/10000 y por que Luna no aplica al dominio de pagos.
- [`docs/LUNA_SETUP_CHECKLIST.md`](docs/LUNA_SETUP_CHECKLIST.md) - paso a paso para pasar del mock software (`hsm.luna.mock-mode=true`, sin hardware) a un Luna real.

## CI/CD

Pipelines de ejemplo listos para usar en `.github/workflows/ci.yml` (GitHub Actions) y
`.gitlab-ci.yml` (GitLab CI): lint -> test -> package -> imagen Docker (multi-stage, sin tag
`latest`, ver `Dockerfile`).
