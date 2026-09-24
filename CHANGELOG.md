# Changelog

## 1.0.0 - 2026-09-24
- Separa el parseo de POINT_BY_POINT del micro monolítico.
- Publica protocolo Avro START/ROW/COMPLETED/FAILED en `point-by-point.parsed`.
- Elimina PostgreSQL, JPA y Flyway del parser.
