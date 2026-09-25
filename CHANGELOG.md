# Changelog

## 2.0.4 - 2026-09-25

- [patch] Refuerza AGENTS.md: lectura obligatoria por tarea, flujo autónomo y prohibición absoluta de escrituras directas en main.

## 2.0.3

- [patch] Estandariza la automatización del repositorio con el flujo autónomo de csv-results-parser.

## 2.0.2 - 2026-09-25

- [patch] Exige confirmar rama y nivel SemVer antes de cualquier cambio.
- [patch] Alinea Maven CI-friendly con revision, sha1 y changelist.

## 2.0.0 - 2026-09-24
- Consolida como contrato MAJOR la arquitectura parser Kafka sin PostgreSQL.
- Mantiene la publicación Avro en `point-by-point.parsed` y documenta el gobierno común del repositorio.
- Alinea versión, README, CHANGELOG, AGENTS.md y CI con el resto de microservicios CSV.

## 1.0.2 - 2026-09-24
- Añade auto-merge tras pasar los checks del PR y elimina la rama origen tras fusionar.

## 1.0.1 - 2026-09-24
- Corrige la captura de `expectedRows` dentro de la lambda que publica filas.
- Mantiene el valor validado en una variable final para que compile con Java 21.

## 1.0.0 - 2026-09-24
- Separa el parseo de POINT_BY_POINT del micro monolítico.
- Publica protocolo Avro START/ROW/COMPLETED/FAILED en `point-by-point.parsed`.
- Elimina PostgreSQL, JPA y Flyway del parser.
