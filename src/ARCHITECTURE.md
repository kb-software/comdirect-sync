# Arquitectura DDD - Comdirect Sync

Este proyecto implementa **Domain-Driven Design (DDD)** junto con **Arquitectura Hexagonal** para crear una aplicación robusta y mantenible que se comunica con la API REST de Comdirect.

## Estructura de Directorios

```
src/
├── application/           # Capa de Aplicación (Casos de Uso)
│   ├── commands/         # Comandos CQRS
│   ├── dto/             # Data Transfer Objects
│   ├── handlers/        # Manejadores de comandos/consultas
│   ├── queries/         # Consultas CQRS
│   ├── services/        # Servicios de aplicación
│   └── usecases/        # Casos de uso específicos
├── domain/              # Capa de Dominio (Núcleo del negocio)
│   ├── aggregates/      # Agregados del dominio
│   ├── entities/        # Entidades del dominio
│   ├── events/          # Eventos de dominio
│   ├── exceptions/      # Excepciones específicas del dominio
│   ├── repositories/    # Interfaces de repositorios
│   ├── services/        # Servicios de dominio
│   └── valueobjects/    # Objetos de valor
├── infrastructure/      # Capa de Infraestructura (Detalles técnicos)
│   ├── adapters/        # Adaptadores para comunicación externa
│   │   ├── external/    # Adaptadores para servicios externos (Comdirect API)
│   │   ├── messaging/   # Adaptadores de mensajería
│   │   └── persistence/ # Implementaciones de repositorios
│   ├── config/          # Configuraciones de Spring Boot
│   ├── security/        # Configuraciones de seguridad
│   └── web/            # Controladores REST y UI (JavaFX)
├── shared/              # Componentes compartidos
│   ├── constants/       # Constantes globales
│   ├── exceptions/      # Excepciones comunes
│   └── utils/          # Utilidades comunes
└── test/               # Pruebas unitarias e integración
```

## Capas y Responsabilidades

### 🎯 Domain (Dominio)
**Propósito**: Contiene la lógica de negocio pura, libre de dependencias externas.

- **`entities/`**: Objetos con identidad que representan conceptos fundamentales del negocio
  - `User.java` - Usuario de Comdirect
  - `Document.java` - Documento financiero
  - `Account.java` - Cuenta bancaria
  
- **`valueobjects/`**: Objetos inmutables que representan valores del dominio
  - `ClientId.java` - Identificador del cliente
  - `DocumentType.java` - Tipo de documento
  - `Amount.java` - Cantidades monetarias
  
- **`aggregates/`**: Raíces de agregado que mantienen consistencia
  - `UserSession.java` - Sesión de usuario con autenticación
  - `DocumentCollection.java` - Colección de documentos
  
- **`repositories/`**: Interfaces para persistencia (implementadas en infrastructure)
  - `UserRepository.java`
  - `DocumentRepository.java`
  
- **`services/`**: Lógica de dominio que no pertenece a una entidad específica
  - `AuthenticationService.java`
  - `DocumentValidationService.java`

### 🚀 Application (Aplicación)
**Propósito**: Orquesta casos de uso y coordina el flujo de la aplicación.

- **`usecases/`**: Casos de uso específicos del negocio
  - `AuthenticateUserUseCase.java`
  - `DownloadDocumentsUseCase.java`
  - `RefreshSessionUseCase.java`
  
- **`commands/`**: Comandos para operaciones de escritura (CQRS)
  - `AuthenticateUserCommand.java`
  - `DownloadDocumentCommand.java`
  
- **`queries/`**: Consultas para operaciones de lectura (CQRS)
  - `GetUserDocumentsQuery.java`
  - `GetAccountInfoQuery.java`
  
- **`dto/`**: Objetos para transferencia de datos entre capas
  - `UserAuthenticationDTO.java`
  - `DocumentDTO.java`

### 🔧 Infrastructure (Infraestructura)
**Propósito**: Implementa detalles técnicos y comunicación externa.

- **`adapters/external/`**: Comunicación con servicios externos
  - `ComdirectApiAdapter.java` - Cliente para API de Comdirect
  - `XmlProcessorAdapter.java` - Procesamiento de archivos XML
  
- **`adapters/persistence/`**: Implementaciones de repositorios
  - `JpaUserRepository.java`
  - `FileDocumentRepository.java`
  
- **`web/`**: Interfaces de usuario y API REST
  - `JavaFxController.java` - Controlador de JavaFX
  - `RestController.java` - API REST si es necesaria
  
- **`config/`**: Configuraciones de Spring Boot
  - `ApplicationConfig.java`
  - `ComdirectApiConfig.java`

### 🔄 Shared (Compartido)
**Propósito**: Componentes utilizados por múltiples capas.

- **`exceptions/`**: Excepciones base
- **`utils/`**: Utilidades comunes
- **`constants/`**: Constantes de la aplicación

## Principios Aplicados

### 1. **Dependency Inversion**
- El dominio no depende de infraestructura
- Las dependencias apuntan hacia el centro (dominio)

### 2. **Single Responsibility**
- Cada clase tiene una única razón para cambiar
- Separación clara de responsabilidades por capa

### 3. **Open/Closed Principle**
- Abierto para extensión, cerrado para modificación
- Uso de interfaces para abstraer implementaciones

### 4. **Interface Segregation**
- Interfaces específicas en lugar de una grande
- Los clientes no dependen de métodos que no usan

## Flujo de Datos

```
JavaFX UI → Application Layer → Domain Layer ← Infrastructure Layer → Comdirect API
```

1. **Usuario interactúa** con JavaFX UI
2. **UI llama** a casos de uso en Application Layer
3. **Casos de uso** utilizan servicios y entidades del Domain
4. **Domain** define interfaces para repositorios
5. **Infrastructure** implementa estas interfaces
6. **Adapters** se comunican con API externa de Comdirect

## Casos de Uso Principales

1. **Autenticación de Usuario**
   - Login con credenciales de Comdirect
   - Manejo de OAuth2
   - Gestión de sesiones

2. **Descarga de Documentos**
   - Listado de documentos disponibles
   - Selección y descarga de PDFs
   - Procesamiento a XML

3. **Gestión de Configuración**
   - Carga de configuración desde JSON
   - Validación de parámetros
   - Actualización de configuración

4. **Manejo de Errores y Timeouts**
   - Detección de sesiones expiradas
   - Renovación automática de tokens
   - Notificaciones al usuario

## Tecnologías Utilizadas

- **Java** - Lenguaje principal
- **Spring Boot** - Framework de aplicación
- **JavaFX** - Interfaz gráfica de usuario
- **Maven/Gradle** - Gestión de dependencias
- **Jackson** - Procesamiento JSON
- **JUnit** - Pruebas unitarias

Esta estructura garantiza:
- ✅ **Testabilidad**: Cada capa puede probarse independientemente
- ✅ **Mantenibilidad**: Código organizado y fácil de modificar
- ✅ **Escalabilidad**: Fácil agregar nuevas funcionalidades
- ✅ **Flexibilidad**: Cambios en infraestructura no afectan el dominio