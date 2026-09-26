# Changelog

## 2.1.0 - 2026-09-26

- [minor] KAN-110 aplica la estrategia común de errores Kafka de KAN-18.
- [minor] Separa errores permanentes de CSV/ruta de fallos transitorios de Kafka sin eliminar el evento FAILED existente.
- [minor] Configura retries/backoff y DLT `file.ready.point-by-point.DLT`.
- [minor] Añade tests de clasificación de error permanente y transitorio.


## 2.0.7 - 2026-09-25

- [patch] KAN-84 sustituye los schemas locales FileEvent/PointByPoint por `basketball-event-contracts:1.1.0`, que incorpora `FileEventValue.expectedRows` opcional.
- [patch] Elimina generación Avro local y configura Maven/CI con lectura autenticada de GitHub Packages.
- [patch] Mantiene los namespaces, campos y protocolo START/ROW/COMPLETED/FAILED existentes.

## 2.0.6 - 2026-09-25

- [patch] KAN-70 valida POINT_BY_POINT contra `CSV_ALLOWED_ROOT` antes de cualquier lectura.
- [patch] Resuelve rutas reales para bloquear traversal y escapes mediante symlink.
- [patch] Añade tests de ruta permitida, traversal, fichero inexistente y symlink fuera de raíz.

## 2.0.5 - 2026-09-25

- [patch] Elimina de JUnit las aserciones que validaban el versionado entre pom.xml y README.md.

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
