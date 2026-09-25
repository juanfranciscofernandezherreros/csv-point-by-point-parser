# AGENTS.md

Estas reglas son obligatorias para cualquier agente, asistente o automatización que modifique este repositorio.

## Confirmación obligatoria antes de empezar

Antes de realizar cualquier cambio, el agente debe preguntar al usuario y esperar respuesta explícita sobre:

1. **Nombre de la rama**, proponiendo uno por defecto.
2. **Tipo SemVer** del cambio: `major`, `minor` o `patch`.

No se debe modificar ningún archivo, crear commits ni abrir una Pull Request hasta disponer de ambas respuestas. Estas son las únicas confirmaciones que el agente debe solicitar al usuario durante el trabajo.

## Flujo de trabajo automático

Una vez confirmados la rama y el nivel SemVer, esa confirmación autoriza el ciclo completo del trabajo. A partir de ese momento, el agente debe continuar de forma autónoma y no debe solicitar ninguna autorización o confirmación adicional para cambios, commits, push, tests, correcciones, actualización de la PR, reejecución de checks, merge, eliminación de la rama o verificación final.

El ciclo obligatorio es:

1. Partir del `main` actualizado.
2. Confirmar nombre de rama.
3. Confirmar `major`, `minor` o `patch`.
4. Crear la rama dedicada; no hacer commits ni push directamente a `main`.
5. Aplicar el incremento SemVer sobre `revision`.
6. Realizar el cambio.
7. Actualizar `CHANGELOG.md` con la nueva versión funcional.
8. Actualizar `README.md` cuando el cambio o la política del repositorio lo requiera.
9. Ejecutar los tests y checks aplicables.
10. Abrir o actualizar una Pull Request hacia `main`.
11. Comprobar automáticamente los checks requeridos/aplicables al SHA actual de la Pull Request.
12. Si un check requerido/aplicable falla o se cancela, no fusionar: investigar, corregir el fallo en la misma rama y Pull Request y volver automáticamente al paso 9. No esperar una orden del usuario para continuar.
13. Cuando todos los checks requeridos/aplicables al SHA actual estén en verde, fusionar automáticamente la Pull Request sin solicitar autorización adicional. Los comentarios o revisiones meramente informativos de bots no bloquean el merge. Una protección de rama que GitHub marque explícitamente como bloqueante sí debe impedirlo.
14. Después del merge, eliminar automáticamente únicamente la rama origen de esa Pull Request; nunca eliminar `main`.
15. Verificar automáticamente que la rama origen ya no existe. Si la limpieza falla, investigar y corregir el mecanismo de cleanup dentro del mismo trabajo.
16. El trabajo no se considera terminado hasta que la PR esté fusionada y se haya verificado la eliminación de su rama origen.
17. Toda decisión de merge debe operar sobre el SHA actual de la PR para impedir fusionar una revisión obsoleta.

## Versionado Maven CI-friendly

```xml
<version>${revision}${sha1}${changelist}</version>
```

- `revision`: versión SemVer funcional.
- `sha1`: `-<short-sha>`, generado por CI; en la build de `main` corresponde al commit resultante del merge; no modificar manualmente.
- Los builds no productivos de `main` usan el formato `revision-shortSHA` (por ejemplo, `2.0.6-d47f1dfb`), donde `shortSHA` identifica el commit resultante del merge, sin añadir `-SNAPSHOT` por defecto.
- `changelist`: vacío o `-SNAPSHOT`.
- DEV, INT y QA deben promover el mismo artefacto y conservar la misma versión.

## SemVer

- `patch`: `X.Y.Z` -> `X.Y.(Z+1)`
- `minor`: `X.Y.Z` -> `X.(Y+1).0`
- `major`: `X.Y.Z` -> `(X+1).0.0`

El `CHANGELOG.md` usa `revision`, nunca la versión de build con SHA.

## Tests

- Baseline Java: JDK 21.
- Ejecutar como mínimo `mvn -B test`.
- Añadir o actualizar tests para cambios funcionales o de configuración.
- Revisar los tests en el mismo orden del flujo afectado: validación/entrada -> parsing/mapeo -> servicio/publicación -> consumer/integración. Cuando se introduzca o cambie una excepción propia, actualizar primero los tests unitarios que verifican su tipo, mensaje y causa; después ejecutar la suite completa.

## Pull Requests

Antes de abrir o actualizar una PR comprobar que rama y SemVer fueron confirmados, `revision` es correcta, `sha1` no está persistido manualmente, `changelist` es coherente con la política CI-friendly (vacío o `-SNAPSHOT`) y la PR indica nivel SemVer y versión anterior/nueva. Tras abrir o actualizar la PR, continuar automáticamente el ciclo definido en «Flujo de trabajo automático» hasta merge y limpieza verificada de la rama.
