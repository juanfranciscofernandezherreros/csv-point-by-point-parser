# AGENTS.md

Estas reglas son obligatorias para cualquier agente, asistente o automatización que modifique este repositorio.

## Flujo de trabajo

1. Trabajar siempre en una rama dedicada; no hacer commits ni push directamente a `main`.
2. Mantener un único cambio lógico por commit.
3. Abrir una Pull Request hacia `main`.
4. Ejecutar los checks y tests aplicables antes del merge.
5. No fusionar una Pull Request salvo autorización explícita del usuario.
6. Si el usuario autoriza merge automático, fusionar únicamente cuando los checks requeridos estén en verde.
7. Eliminar la rama origen después del merge cuando el repositorio tenga automatización para ello.

## Versionado Maven CI-friendly

El proyecto usa Maven CI-friendly versions:

```xml
<version>${revision}${sha1}${changelist}</version>

<properties>
    <revision>X.Y.Z</revision>
    <sha1></sha1>
    <changelist></changelist>
</properties>
```

Reglas:

- `revision` contiene la versión SemVer funcional del proyecto.
- `sha1` identifica el build/commit y no debe modificarse manualmente; CI lo establece como `-<short-sha>`.
- `changelist` controla explícitamente el estado SNAPSHOT.
- Usar `-SNAPSHOT` en `changelist` solo cuando el proyecto deba mantenerse como SNAPSHOT.
- Para cambios `major`, `minor` o `patch`, modificar únicamente `revision`.
- No escribir manualmente una versión de build como `X.Y.Z-abcdef12` en el POM.
- Los builds de DEV, INT y QA deben promover el mismo artefacto y conservar exactamente la misma versión.

Ejemplos:

- `revision=2.0.0`, `sha1=`, `changelist=` -> `2.0.0`
- `revision=2.0.0`, `sha1=-a84fc921`, `changelist=` -> `2.0.0-a84fc921`
- `revision=0.1.5`, `sha1=-a84fc921`, `changelist=-SNAPSHOT` -> `0.1.5-a84fc921-SNAPSHOT`

## SemVer y CHANGELOG

Cuando un cambio requiera nueva versión funcional:

- `patch`: `X.Y.Z` -> `X.Y.(Z+1)`
- `minor`: `X.Y.Z` -> `X.(Y+1).0`
- `major`: `X.Y.Z` -> `(X+1).0.0`

El `CHANGELOG.md` debe usar la versión funcional de `revision`, nunca la versión concreta de build con SHA.

## Documentación

Si el repositorio exige sincronización entre `pom.xml`, `README.md` y `CHANGELOG.md`, respetar esa política en el mismo cambio.

## Tests

- Baseline Java: JDK 21.
- Ejecutar como mínimo `mvn -B test` cuando sea aplicable.
- Para cambios de persistencia, migraciones o integración, ejecutar también los perfiles de integración definidos por el repositorio.
- Añadir o actualizar tests para cambios funcionales o de configuración cuando corresponda.

## Pull Requests

Antes de abrir o actualizar una PR comprobar:

- La rama no es `main`.
- `revision` refleja el SemVer funcional esperado.
- `sha1` no contiene un valor persistido manualmente.
- `changelist` refleja correctamente si el proyecto es SNAPSHOT.
- `CHANGELOG.md` usa la versión funcional, no el SHA del build.
- Los tests relevantes se han ejecutado cuando sea posible.
- La PR describe el cambio y cualquier cambio de versión funcional.
