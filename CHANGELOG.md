# Changelog

## 1.0.1 - 2026-09-24
- Corrige la captura de `expectedRows` dentro de la lambda que publica filas.
- Mantiene el valor validado en una variable final para que compile con Java 21.

## 1.0.0 - 2026-09-24
- Separa el parseo de POINT_BY_POINT del micro monolítico.
- Publica protocolo Avro START/ROW/COMPLETED/FAILED en `point-by-point.parsed`.
- Elimina PostgreSQL, JPA y Flyway del parser.
