# Repository Rules for OpenAI and AI Agents

## Every non-merge commit

1. Trabajar en una rama dedicada y no enviar cambios directamente a `main`.
2. Hacer un único cambio lógico por commit.
3. Incrementar la versión de `pom.xml` y sincronizar `README.md` y `CHANGELOG.md`.
4. Añadir o actualizar pruebas JUnit.
5. Ejecutar `mvn -B test` con JDK 21.
6. Incluir la versión en el mensaje de commit.
7. Abrir Pull Request hacia `main`; fusionar solo con `documentation-policy` y `test` en verde.
8. Eliminar la rama origen después del merge.

## Baseline

JDK 21. El parser publica contratos Avro en Kafka y no debe incorporar PostgreSQL, JPA ni Flyway.
