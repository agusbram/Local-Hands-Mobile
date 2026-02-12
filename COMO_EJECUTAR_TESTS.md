# Guía Rápida: Ejecutar Tests

## Requisitos Previos
- Android Studio instalado
- JDK 17 configurado
- Gradle configurado

## Comandos para Ejecutar Tests

### 1. Ejecutar TODOS los tests unitarios
```bash
./gradlew test
```

### 2. Ejecutar tests con reporte detallado
```bash
./gradlew test --info
```

### 3. Ejecutar tests específicos

#### Tests de PasswordManager (Seguridad)
```bash
./gradlew test --tests "*PasswordManagerTest*"
```

#### Tests de AuthRepository (Autenticación)
```bash
./gradlew test --tests "*AuthRepositoryTest*"
```

#### Tests de FavoriteRepository (Favoritos)
```bash
./gradlew test --tests "*FavoriteRepositoryTest*"
```

#### Tests de ProductRepository (Productos)
```bash
./gradlew test --tests "*ProductRepositoryTest*"
```

### 4. Ejecutar un test individual
```bash
./gradlew test --tests "PasswordManagerTest.hashPassword_generatesValidBCryptHash"
```

### 5. Ver reportes de tests
Los reportes HTML se generan en:
```
app/build/reports/tests/testDebugUnitTest/index.html
```

Ábrelo en tu navegador para ver resultados detallados.

### 6. Limpiar y ejecutar tests
```bash
./gradlew clean test
```

## Desde Android Studio

1. **Ver todos los tests**:
   - Panel izquierdo → `app/src/test/java`
   - Click derecho en carpeta → "Run Tests"

2. **Ejecutar una clase de test**:
   - Abrir archivo de test
   - Click en el icono verde ▶️ junto al nombre de la clase
   - O: Click derecho → "Run 'NombreTest'"

3. **Ejecutar un test individual**:
   - Click en el icono verde ▶️ junto al método `@Test`
   - O: Click derecho en el método → "Run 'nombreDelTest'"

4. **Ver cobertura de código**:
   - Click derecho en test → "Run with Coverage"

## Verificar que los tests están funcionando

### Ejecuta el test de ejemplo primero:
```bash
./gradlew test --tests "*ExampleUnitTest*"
```

Si este pasa, puedes ejecutar el resto de los tests.

## Solución de Problemas

### Error: "Task 'test' not found"
```bash
./gradlew :app:test
```

### Error de compilación
```bash
./gradlew clean
./gradlew build
```

### Limpiar cache de Gradle
```bash
./gradlew clean --no-daemon
rm -rf .gradle
./gradlew test
```

### Ver logs detallados
```bash
./gradlew test --stacktrace --info
```

## Tests Implementados

### Resumen de Cobertura:

| Componente | Tests | Descripción |
|------------|-------|-------------|
| **PasswordManager** | 12 | Hashing y verificación de contraseñas |
| **AuthRepository** | 17 | Login, registro, sesión |
| **FavoriteRepository** | 8 | Gestión de favoritos |
| **ProductRepository** | 15+ | CRUD y sincronización de productos |
| **TOTAL** | **52+** | Tests de funcionalidades críticas |

## Integración Continua (CI/CD)

Para GitHub Actions, agrega este workflow en `.github/workflows/test.yml`:

```yaml
name: Run Tests

on: [push, pull_request]

jobs:
  test:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      - name: Set up JDK 17
        uses: actions/setup-java@v3
        with:
          java-version: '17'
          distribution: 'temurin'
      - name: Grant execute permission for gradlew
        run: chmod +x gradlew
      - name: Run unit tests
        run: ./gradlew test
      - name: Upload test reports
        if: always()
        uses: actions/upload-artifact@v3
        with:
          name: test-reports
          path: app/build/reports/tests/
```

## Próximos Pasos

1. Ejecuta los tests regularmente durante el desarrollo
2. Agrega nuevos tests cuando implementes nuevas funcionalidades
3. Mantén la cobertura de código alta (>80%)
4. Revisa los reportes de tests para identificar problemas

---

Para más detalles sobre la arquitectura y diseño de los tests, consulta [TESTING.md](TESTING.md)
