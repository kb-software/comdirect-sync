# Guía de Compilación y Ejecución - Comdirect Sync

## 📋 Prerrequisitos

- **Java 17** o superior
- **Maven 3.8+**
- **Git**

## 🚀 Configuración e Instalación

### 1. Clonar el repositorio
```bash
git clone https://github.com/kb-software/comdirect-sync.git
cd comdirect-sync
```

### 2. Compilar el proyecto
```bash
mvn clean compile
```

### 3. Ejecutar tests
```bash
# Tests unitarios
mvn test

# Tests de integración
mvn verify

# Todos los tests con coverage
mvn clean verify jacoco:report
```

### 4. Compilar y empaquetar
```bash
mvn clean package
```

## 🔧 Comandos Maven Útiles

### Desarrollo
```bash
# Compilar y ejecutar la aplicación
mvn spring-boot:run

# Ejecutar con perfil específico
mvn spring-boot:run -Dspring-boot.run.profiles=dev

# Ejecutar solo tests del dominio
mvn test -Dtest="domain/**/*Test"

# Ejecutar con JavaFX
mvn javafx:run
```

### Testing
```bash
# Ejecutar tests específicos
mvn test -Dtest="UserTest"

# Ejecutar tests con tags específicos
mvn test -Dgroups="unit"

# Skip tests
mvn package -DskipTests
```

### Limpieza
```bash
# Limpiar artifacts
mvn clean

# Limpiar y reinstalar dependencias
mvn clean dependency:resolve
```

## 🏗️ Estructura del Proyecto

```
src/
├── main/java/
│   ├── domain/              # Lógica de negocio pura
│   ├── application/         # Casos de uso
│   ├── infrastructure/      # Detalles técnicos
│   ├── shared/             # Componentes compartidos
│   └── ComdirectSyncApplication.java
├── main/resources/
│   ├── application.properties
│   └── application-dev.properties
└── test/java/
    ├── domain/             # Tests unitarios del dominio
    ├── application/        # Tests de casos de uso
    └── infrastructure/     # Tests de infraestructura
```

## 🧪 Testing

### Convenciones de Tests
- **Unit Tests**: `*Test.java` - Tests unitarios rápidos
- **Integration Tests**: `*IntegrationTest.java` o `*IT.java` - Tests de integración

### Ejecutar diferentes tipos de tests
```bash
# Solo tests unitarios (rápidos)
mvn surefire:test

# Solo tests de integración
mvn failsafe:integration-test

# Coverage report
mvn jacoco:report
# Reporte disponible en: target/site/jacoco/index.html
```

## 📊 Herramientas de Calidad

### Coverage de Código
```bash
mvn clean verify jacoco:report
open target/site/jacoco/index.html
```

### Análisis estático (si se configura SonarQube)
```bash
mvn sonar:sonar
```

## 🔍 Troubleshooting

### Problemas comunes

1. **JavaFX no encontrado**
   ```bash
   # Asegúrate de tener JavaFX en el classpath
   mvn javafx:run
   ```

2. **Tests fallan por dependencias**
   ```bash
   mvn dependency:tree
   mvn clean install
   ```

3. **Problemas de encoding**
   ```bash
   export MAVEN_OPTS="-Dfile.encoding=UTF-8"
   mvn clean compile
   ```

4. **Limpiar cache de Maven**
   ```bash
   rm -rf ~/.m2/repository/de/comdirect
   mvn clean install
   ```

## 🏷️ Perfiles Maven

- **dev** (default): Desarrollo local
- **test**: Para ejecutar tests
- **prod**: Producción

```bash
mvn spring-boot:run -Pdev
mvn test -Ptest
mvn package -Pprod
```

## 📝 Dependencias Principales

- **Spring Boot 3.2.0**: Framework principal
- **JUnit 5**: Testing framework
- **JavaFX 21**: Interfaz gráfica
- **Jackson**: Procesamiento JSON
- **Mockito**: Mocking para tests
- **AssertJ**: Assertions fluidas
- **WireMock**: Mock de APIs REST para tests

## 🎯 Próximos Pasos

1. Configurar tu IDE con el proyecto Maven
2. Ejecutar `mvn test` para verificar que todo funciona
3. Crear tu archivo de configuración `config.json`
4. Empezar a desarrollar casos de uso específicos