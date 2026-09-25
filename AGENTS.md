# AGENTS.md

Estas reglas son obligatorias para cualquier agente, asistente o automatización.

## Confirmación obligatoria antes de empezar
Antes de cualquier cambio, preguntar y esperar confirmación explícita de:
1. **Nombre de la rama**, proponiendo uno por defecto.
2. **Tipo SemVer**: `major`, `minor` o `patch`.

No modificar archivos, crear commits ni abrir PR hasta tener ambas respuestas.

## Flujo obligatorio
Partir de `main` actualizado; crear rama dedicada; nunca trabajar directamente sobre `main`; aplicar el incremento sobre `revision`; actualizar CHANGELOG y README cuando corresponda; ejecutar tests/checks; abrir PR; no fusionar sin autorización explícita; con merge automático autorizado, fusionar solo con checks verdes.

## Maven CI-friendly
`<version>${revision}${sha1}${changelist}</version>`
- `revision`: SemVer funcional.
- `sha1`: `-<short-sha>` generado por CI; no persistir manualmente.
- `changelist`: vacío o `-SNAPSHOT`.
- DEV, INT y QA promocionan exactamente el mismo artefacto.

## SemVer
- patch: `X.Y.Z -> X.Y.(Z+1)`
- minor: `X.Y.Z -> X.(Y+1).0`
- major: `X.Y.Z -> (X+1).0.0`

El CHANGELOG usa `revision`, nunca el SHA de build.

## Tests
Baseline JDK 21. Ejecutar como mínimo `mvn -B test`.
