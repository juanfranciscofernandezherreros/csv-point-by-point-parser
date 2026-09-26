![version](https://img.shields.io/badge/version-2.2.0-blue)
# csv-point-by-point-parser

Parser separado de `csv-point-by-point`.

```text
file.ready.point-by-point -> validación completa CSV -> START/ROW/COMPLETED|FAILED -> point-by-point.parsed
```

El parser conserva las reglas del micro original: UTF-8/BOM, 13 columnas de datos, `link_url` y `source_url` opcionales, un único `match_id` por fichero y validación de `expectedRows`.

Antes de publicar ninguna fila hace una pasada completa de validación. Después publica todos los mensajes con la misma key Avro `sourceEventId`, preservando el orden por partición. No usa JPA, Flyway ni PostgreSQL.

## Publicación Kafka por chunks

KAN-57 elimina la espera síncrona por cada ROW.

El protocolo mantiene estas barreras:

1. `START` se publica y confirma antes de enviar filas.
2. Cada chunk de hasta 500 ROW se envía de forma asíncrona manteniendo la misma key/partición.
3. Se espera una sola vez por todas las confirmaciones del chunk con `CompletableFuture.allOf(...)`.
4. El siguiente chunk no comienza hasta confirmar el anterior.
5. `COMPLETED` solo se publica después de confirmar todas las ROW.
6. Si cualquier publicación ROW falla, no se emite `COMPLETED`; se intenta publicar `FAILED` y el error Kafka se propaga para que aplique la política de retry/DLT.

Esto elimina el patrón de latencia acumulada `N × ack` dentro del chunk y conserva el orden requerido por Kafka para una misma key/partición.

## Contratos Avro compartidos

`FileEventKey`, `FileEventValue`, `PointByPointKey` y `PointByPointValue` se consumen desde:

```text
com.fernandez.basketball:basketball-event-contracts:1.1.0
```

Este repositorio ya no mantiene copias locales de esos schemas ni genera las clases Avro durante su propia build. Fuera de GitHub Actions, Maven necesita credenciales con `read:packages` para resolver el artefacto desde GitHub Packages.

Variables: `KAFKA_BOOTSTRAP_SERVERS`, `KAFKA_SCHEMA_REGISTRY_URL`, `KAFKA_FILE_READY_TOPIC`, `KAFKA_PARSED_POINT_BY_POINT_TOPIC`, `CSV_ALLOWED_ROOT`.

Antes de abrir el fichero, la ruta absoluta se resuelve con `toRealPath()` y debe permanecer dentro de `CSV_ALLOWED_ROOT` (por defecto `/data/csv`). Esto bloquea traversal y symlinks que escapen de la raíz permitida.

## Estrategia de errores Kafka

KAN-110 aplica la política de KAN-18 al consumo de `file.ready.point-by-point`.

- errores de validación, ruta o contenido CSV: non-retryable;
- fallos transitorios de Kafka al publicar START/ROW/COMPLETED/FAILED: retryable;
- el protocolo `FAILED` del dominio se conserva;
- si el procesamiento del evento no puede recuperarse, el registro original se publica en `file.ready.point-by-point.DLT`;
- `KAFKA_RETRY_MAX_ATTEMPTS`: intentos totales, default `3`;
- `KAFKA_RETRY_BACKOFF_MS`: backoff fijo, default `1000`;
- `KAFKA_POINT_BY_POINT_PARSER_DLT_TOPIC`: topic DLT configurable.

El consumer usa `ErrorHandlingDeserializer` para que un Avro corrupto entre en el flujo normal de recuperación. La DLT acepta objetos Avro y los `byte[]` originales, deja que Kafka seleccione una partición válida, conserva los headers de excepción de Spring Kafka y hace visible cualquier fallo al publicar en DLT.
