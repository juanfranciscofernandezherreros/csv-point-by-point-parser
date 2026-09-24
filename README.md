Current version: **1.0.0**

# csv-point-by-point-parser

Parser separado de `csv-point-by-point`.

```text
file.ready.point-by-point -> validación completa CSV -> START/ROW/COMPLETED|FAILED -> point-by-point.parsed
```

El parser conserva las reglas del micro original: UTF-8/BOM, 13 columnas de datos, `link_url` y `source_url` opcionales, un único `match_id` por fichero y validación de `expectedRows`.

Antes de publicar ninguna fila hace una pasada completa de validación. Después publica todos los mensajes con la misma key Avro `sourceEventId`, preservando el orden por partición. No usa JPA, Flyway ni PostgreSQL.

Variables: `KAFKA_BOOTSTRAP_SERVERS`, `KAFKA_SCHEMA_REGISTRY_URL`, `KAFKA_FILE_READY_TOPIC`, `KAFKA_PARSED_POINT_BY_POINT_TOPIC`.
