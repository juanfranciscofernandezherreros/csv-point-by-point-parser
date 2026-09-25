# AGENTS.md

Estas reglas son obligatorias para cualquier agente, asistente o automatización que modifique este repositorio.

## Pre-flight obligatorio

La **primera operación de lectura del repositorio en cada tarea o sesión** debe ser abrir y leer completamente este `AGENTS.md` desde la rama por defecto. Si este archivo referencia otras reglas, deben leerse antes de cualquier escritura.

No cuenta haber leído estas reglas en otra conversación, sesión o tarea. No se debe confiar en memoria previa.

**Está prohibida cualquier operación de escritura antes de completar este pre-flight.**

## Autonomía sin bloqueos innecesarios

Una vez leído `AGENTS.md`, el agente debe continuar de forma autónoma. No debe pedir confirmación de nombre de rama, nivel SemVer, commits, push, tests, correcciones, actualización de PR, merge, eliminación de rama ni verificación final, salvo que el usuario haya pedido explícitamente participar en alguna de esas decisiones.

El agente debe:
1. elegir un nombre de rama descriptivo;
2. determinar de forma razonada el nivel SemVer (`major`, `minor` o `patch`) según el impacto real;
3. documentar esas decisiones en la Pull Request.

## Prohibición absoluta de escritura directa en `main`

**Ningún cambio puede escribirse, commitearse ni pushearse directamente a `main`.**

Esto incluye cambios de código, documentación, configuración, workflows, dependencias, versionado, badges, hotfixes, reverts y cualquier otro archivo.

Toda modificación debe seguir obligatoriamente este flujo:

1. Leer `AGENTS.md` y reglas referenciadas.
2. Partir del `main` actualizado.
3. Crear una rama dedicada antes de modificar archivos.
4. Determinar y aplicar el incremento SemVer sobre `revision`.
5. Realizar el cambio exclusivamente en la rama.
6. Actualizar `CHANGELOG.md` con la nueva versión funcional.
7. Mantener `README.md`, `pom.xml` y documentación de versión sincronizados cuando corresponda.
8. Ejecutar los tests y checks aplicables; como mínimo `mvn -B test`.
9. Abrir o actualizar una Pull Request hacia `main`.
10. Comprobar los checks requeridos sobre el SHA actual de la PR.
11. Si falla o se cancela un check aplicable, investigar y corregir en la misma rama y PR, y repetir los tests/checks.
12. Fusionar únicamente cuando todos los checks requeridos/aplicables al SHA actual estén en verde y GitHub no marque ninguna protección bloqueante.
13. Eliminar únicamente la rama origen después del merge.
14. Verificar que la rama origen ya no existe.

El trabajo no se considera terminado hasta completar merge y limpieza.

## Versionado Maven CI-friendly

```xml
<version>${revision}${sha1}${changelist}</version>
```

- `revision`: versión SemVer funcional.
- `sha1`: `-<short-sha>`, generado por CI; no modificar manualmente.
- `changelist`: vacío o `-SNAPSHOT`.
- DEV, INT y QA deben promover el mismo artefacto y conservar la misma versión.
- El `CHANGELOG.md` usa `revision`, nunca la versión de build con SHA.

## SemVer

- `patch`: `X.Y.Z` -> `X.Y.(Z+1)`
- `minor`: `X.Y.Z` -> `X.(Y+1).0`
- `major`: `X.Y.Z` -> `(X+1).0.0`

## Tests

- Baseline Java: JDK 21.
- Ejecutar como mínimo `mvn -B test`.
- Añadir o actualizar tests para cambios funcionales o de configuración.
- Revisar tests siguiendo el flujo afectado: validación/entrada -> parsing/mapeo -> servicio/publicación -> consumer/integración.

## Pull Requests y seguridad operativa

Toda decisión de merge debe operar sobre el SHA actual de la PR. Los comentarios informativos de bots no bloquean el merge; una protección de rama marcada por GitHub como bloqueante sí.

Si una instrucción del usuario contradice explícitamente estas reglas, detener únicamente la operación incompatible y explicar el conflicto; no improvisar una escritura directa a `main`.
