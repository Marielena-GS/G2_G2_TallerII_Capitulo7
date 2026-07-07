# Guía de contribución - Taller II (Capítulo 7)

Este documento define las reglas y convenciones para contribuir al repositorio de **Taller II - Capítulo 7**, el cual es un trabajo colaborativo entre el **Grupo 2 y Grupo 3**. El objetivo es mantener un historial limpio, un código legible y consistente en Java, y un flujo de trabajo estructurado.

---

## Flujo general de trabajo

1. **Sincronizar:** Actualizar tu rama local con la última versión de `main` antes de comenzar a trabajar:
   ```bash
   git checkout main
   git pull origin main
   ```
2. **Crear rama:** Crear una rama corta y descriptiva para cada cambio o funcionalidad.
3. **Desarrollar y Probar:** Escribir el código en Java asegurándose de seguir los lineamientos de estilo y verificar que compila correctamente y pasa los tests locales.
4. **Hacer Commits:** Realizar commits pequeños, atómicos y descriptivos siguiendo la convención detallada más abajo.
5. **Subir y crear PR:** Subir la rama y abrir un Pull Request (PR) hacia la rama `main` explicando detalladamente los cambios realizados.
6. **Revisión:** Esperar la aprobación de los compañeros antes de fusionar.

---

## Convenciones de Ramas

Las ramas deben seguir una nomenclatura clara según el tipo de aporte:

- **Funcionalidades:** `feat/descripcion-breve` (ej. `feat/algoritmo-procesamiento`)
- **Correcciones:** `fix/descripcion-breve` (ej. `fix/excepcion-puntero-nulo`)
- **Documentación:** `docs/descripcion-breve` (ej. `docs/actualizar-javadoc`)
- **Refactorización:** `refactor/descripcion-breve` (ej. `refactor/modularizacion-clases`)
- **Pruebas:** `test/descripcion-breve` (ej. `test/pruebas-unitarias`)
- **Mantenimiento/Configuración:** `chore/descripcion-breve` (ej. `chore/actualizar-gradle`)

---

## Formato de Commits

Seguimos la convención de **Conventional Commits** adaptada al entorno Java del proyecto:

```text
tipo(alcance): descripcion breve
```

*Nota: La descripción debe ser escrita en minúsculas, preferiblemente en infinitivo o presente, y sin punto al final.*

### Tipos permitidos
- `feat`: Nueva funcionalidad en Java (nuevas clases, lógica de negocio, etc.).
- `fix`: Corrección de errores o bugs de ejecución.
- `docs`: Cambios en la documentación (informes en LaTeX, archivos Markdown, README o Javadoc).
- `refactor`: Cambios en el código Java que no añaden funcionalidad ni corrigen bugs (mejoras de diseño o arquitectura).
- `style`: Cambios visuales o de formato que no alteran la lógica (organización de imports, indentación, espaciado).
- `test`: Añadir o modificar pruebas unitarias (JUnit, etc.).
- `chore`: Tareas de mantenimiento o configuración (modificar archivos de compilación como Gradle/Maven, .gitignore).
- `assets`: Recursos que no son código (imágenes, archivos de texto de prueba, etc.).

### Alcances recomendados (Scopes)
Indica la parte del proyecto Java afectada:
- `core`: Lógica principal del negocio o algoritmos.
- `ui`: Interfaz de usuario (consola, Swing, JavaFX, etc.).
- `io`: Lectura y escritura de archivos o flujos de datos.
- `test`: Suite de pruebas y validaciones.
- `config`: Archivos de configuración de construcción (Gradle/Maven) o del entorno del repositorio.
- `docs`: Documentos de informe o documentación técnica.

### Ejemplos válidos
```text
feat(core): implementar clases base para el capitulo 7
fix(io): corregir excepcion de archivo no encontrado al leer datos
test(test): agregar casos de prueba unitarios para los hilos
docs(readme): agregar nombres de los integrantes de los grupos
chore(config): actualizar dependencias en el archivo build.gradle
```

---

## Reglas de Desarrollo en Java

Para mantener la uniformidad en el código del proyecto, sigue estas reglas:

### 1. Estilo de Código
- **Nomenclatura:**
  - **Clases e Interfaces:** `PascalCase` (ej. `ProcesadorDatos`).
  - **Métodos y Variables:** `camelCase` (ej. `calcularPromedio`).
  - **Constantes (`static final`):** `UPPER_SNAKE_CASE` (ej. `MAX_HILOS`).
- **Comentarios:** Documentar los métodos principales y clases complejas mediante **Javadoc**.
- **Imports:** Eliminar imports no utilizados antes de confirmar los cambios.

### 2. Control de Versiones y Limpieza
- **Ignorar archivos innecesarios:** Asegúrate de que los archivos de configuración local del IDE (`.idea/`, `.vscode/`, `.settings/`), archivos de compilación (`build/`, `bin/`, `out/`, `.gradle/`) no sean subidos al repositorio. Usa siempre un archivo `.gitignore` adecuado para proyectos Java.
- **Independencia de commits:** No mezcles código Java y cambios en la documentación en un solo commit si no están relacionados.

### 3. Verificación
- **Compilación:** Antes de realizar commits o abrir un Pull Request, asegúrate de que el proyecto compile de forma limpia y pase todas las pruebas existentes:
  - Si usas Gradle: `./gradlew build` o `./gradlew test`
  - Si usas Maven: `mvn clean test`
  - Si usas la terminal estándar: Asegúrate de que no haya errores de sintaxis en `javac`.

---

## Estructura de Pull Requests

Al abrir un PR, utiliza la siguiente plantilla para describir tus cambios:

```markdown
## Resumen
- [ ] Describe de forma general los cambios realizados.
- [ ] ¿Qué problema o funcionalidad del Capítulo 7 resuelve?

## Archivos modificados/agregados
- [ ] Lista las principales clases Java o archivos de configuración afectados.

## Pruebas realizadas
- [ ] Explica cómo verificaste que el código funciona (e.g., ejecución de pruebas unitarias, salidas por consola).
```

## Antes de Fusionar (Checklist final)
- [ ] ¿El código compila correctamente sin warnings críticos?
- [ ] ¿Se han seguido las normas de estilo de nombres y Javadoc?
- [ ] ¿El historial de commits sigue el formato convencional?
- [ ] ¿Está la rama actualizada con `main`?
