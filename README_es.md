# Comdirect Sync

[![Java](https://img.shields.io/badge/Java-21%20LTS-orange.svg)](https://openjdk.java.net/projects/jdk/21/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.4.0-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Maven](https://img.shields.io/badge/Maven-3.9.11-blue.svg)](https://maven.apache.org/)
[![License](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

Una aplicación Java robusta para sincronizar y descargar documentos del banco Comdirect usando autenticación OAuth2 con soporte para 2FA.

## 🌍 Soporte Multi-Idioma

Este README está disponible en múltiples idiomas:

- 🇬🇧 [English](README.md)
- 🇪🇸 **Español** (este archivo)
- 🇩🇪 [Deutsch](README_de.md)

## ✨ Características

- **🔐 Flujo OAuth2 Completo**: Autenticación completa con la API de Comdirect
- **📱 Soporte 2FA**: Manejo interactivo de Autenticación de Dos Factores
- **📄 Sincronización de Documentos**: Descarga y organización automática de documentos
- **🌍 Internacionalización**: Soporte para inglés, español y alemán
- **📁 Almacenamiento Multi-Plataforma**: Almacenamiento inteligente para Mac/Linux/Windows
- **📊 Historial de Descargas**: Previene descargas duplicadas con seguimiento de sesiones
- **⚙️ Configuración de Entorno**: Configuración fácil via archivo `.env`
- **🔄 Rangos de Fecha Inteligentes**: Filtrado de fechas configurable para recuperación de documentos

## 🛠️ Stack Tecnológico

- **Java 21 LTS** - Runtime Java moderno con las últimas características
- **Spring Boot 3.4.0** - Framework de aplicación de nivel empresarial
- **Maven 3.9.11** - Automatización de construcción y gestión de dependencias
- **OAuth2** - Protocolo de autenticación segura
- **Procesamiento JSON** - Gestión de documentos y sesiones
- **Multi-Plataforma** - Soporte para Mac, Linux y Windows

## 📋 Prerrequisitos

- **Java 21 LTS** o superior
- **Maven 3.8+**
- **Cuenta Bancaria Comdirect** con acceso a API
- **Aplicación Móvil** para autenticación 2FA

## 🚀 Inicio Rápido

### 1. Clonar el Repositorio

```bash
git clone https://github.com/kb-software/comdirect-sync.git
cd comdirect-sync
```

### 2. Configurar Entorno

Crear un archivo `.env` en la raíz del proyecto:

```bash
# Configuración de Idioma (en, es, de)
COMDIRECT_LANGUAGE=es

# Autenticación (para producción)
COMDIRECT_CLIENT_ID=tu_client_id
COMDIRECT_CLIENT_SECRET=tu_client_secret
COMDIRECT_USERNAME=tu_usuario
COMDIRECT_PASSWORD=tu_contraseña

# Configuración de Almacenamiento (opcional)
DOWNLOAD_DIRECTORY=/ruta/a/descargas
HISTORY_DIRECTORY=/ruta/a/historial
```

### 3. Ejecutar la Aplicación

**Modo Desarrollo (con datos simulados):**

```bash
./run-dev.sh
```

**Modo Producción:**

```bash
./run-prod.sh
```

**Con Rango de Fechas Personalizado:**

```bash
./run-prod.sh --from=2024-01-01 --to=2024-12-31
```

### 🪟 Soporte para Windows

**Modo Desarrollo:**

```powershell
.\run-dev.ps1
```

**Modo Producción:**

```powershell
.\run-prod.ps1
```

### 📦 Ejecución JAR Independiente

**Construir el JAR:**

```bash
# Unix/Linux/Mac
./build.sh

# Windows
.\build.ps1
```

**Ejecutar el JAR:**

```bash
# Modo desarrollo
java -jar comdirect-sync.jar --spring.profiles.active=dev

# Modo producción
java -jar comdirect-sync.jar --spring.profiles.active=prod
```

## 📖 Uso

### Flujo de Autenticación

1. **Autenticación Inicial**: La aplicación inicia el flujo OAuth2
2. **Validación de Sesión**: Valida la sesión de autenticación
3. **Activación 2FA**: Solicita autorización de la aplicación móvil
4. **Recuperación de Token**: Obtiene token de acceso secundario
5. **Procesamiento de Documentos**: Descarga y organiza documentos

### Configuración de Idioma

Cambiar el idioma de la aplicación modificando el archivo `.env`:

```bash
# Inglés
COMDIRECT_LANGUAGE=en

# Español
COMDIRECT_LANGUAGE=es

# Alemán
COMDIRECT_LANGUAGE=de

# Auto-detectar del sistema (por defecto)
COMDIRECT_LANGUAGE=auto
```

### Tipos de Documentos Soportados

- **Reportes de Dividendos** (Dividendengutschrift)
- **Declaraciones Fiscales** (Steuermitteilung)
- **Notificaciones de Transacciones** (Buchungsanzeige)
- **Reportes Financieros** (Finanzreport)
- **Declaraciones de Ingresos** (Ertragsgutschrift)

## 📁 Estructura de Directorios

```text
comdirect-sync/
├── src/
│   ├── main/java/de/comdirect/sync/
│   │   ├── application/          # Capa de aplicación
│   │   ├── domain/              # Modelos de dominio
│   │   ├── infrastructure/      # Servicios externos
│   │   └── ComdirectSyncApplication.java
│   └── resources/
│       ├── messages.properties     # Mensajes en inglés
│       ├── messages_es.properties  # Mensajes en español
│       ├── messages_de.properties  # Mensajes en alemán
│       └── application*.properties
├── downloads/                   # Documentos descargados
├── history/                    # Historial de descargas
├── .env                       # Configuración de entorno
├── run-dev.sh                 # Script de desarrollo
└── run-prod.sh               # Script de producción
```

## ⚙️ Configuración

### Variables de Entorno

| Variable | Por Defecto | Descripción |
|----------|-------------|-------------|
| `COMDIRECT_LANGUAGE` | `auto` | Idioma de la UI (en/es/de/auto) |
| `COMDIRECT_CLIENT_ID` | - | ID de cliente OAuth2 |
| `COMDIRECT_CLIENT_SECRET` | - | Secreto de cliente OAuth2 |
| `COMDIRECT_USERNAME` | - | Usuario del banco |
| `COMDIRECT_PASSWORD` | - | Contraseña del banco |
| `DOWNLOAD_DIRECTORY` | `~/comdirect-sync/downloads` | Carpeta de descargas |
| `HISTORY_DIRECTORY` | `~/comdirect-sync/history` | Carpeta de historial |

### Perfiles de Aplicación

- **dev**: Modo desarrollo con repositorios simulados en memoria
- **prod**: Modo producción con llamadas reales a la API de Comdirect

## 🔧 Desarrollo

### Construir el Proyecto

```bash
mvn clean compile
```

### Ejecutar Pruebas

```bash
mvn test
```

### Generar Cobertura de Pruebas

```bash
mvn jacoco:report
```

### Construcción Personalizada

```bash
mvn clean package -DskipTests
```

## 🐛 Solución de Problemas

### Problemas Comunes

1. **Errores de Autenticación**
   - Verificar credenciales en archivo `.env`
   - Comprobar conexión a internet
   - Asegurar que la aplicación móvil esté disponible para 2FA

2. **Idioma No Se Carga**
   - Verificar sintaxis del archivo `.env`
   - Reiniciar la aplicación
   - Verificar valor de `COMDIRECT_LANGUAGE`

3. **Errores de Permisos de Archivo**
   - Verificar permisos del directorio de descargas
   - Asegurar espacio suficiente en disco
   - Verificar accesibilidad de la ruta

### Modo Debug

Habilitar logging de debug agregando a tu comando de ejecución:

```bash
./run-dev.sh --debug
```

## 📊 Arquitectura

La aplicación sigue los principios de **Diseño Dirigido por Dominio (DDD)**:

- **Capa de Aplicación**: Orquesta operaciones de negocio
- **Capa de Dominio**: Lógica de negocio principal y entidades
- **Capa de Infraestructura**: Sistemas externos y persistencia
- **Capa Compartida**: Utilidades y servicios comunes

## 🤝 Contribuir

1. Hacer fork del repositorio
2. Crear una rama de características (`git checkout -b feature/caracteristica-increible`)
3. Hacer commit de los cambios (`git commit -m 'Agregar característica increíble'`)
4. Hacer push a la rama (`git push origin feature/caracteristica-increible`)
5. Abrir un Pull Request

## 📄 Licencia

Este proyecto está licenciado bajo la Licencia MIT - ver el archivo [LICENSE](LICENSE) para detalles.

## 🆘 Soporte

Para soporte y preguntas:

- **Issues**: [GitHub Issues](https://github.com/kb-software/comdirect-sync/issues)
- **Documentación**: [Wiki](https://github.com/kb-software/comdirect-sync/wiki)
- **Discusiones**: [GitHub Discussions](https://github.com/kb-software/comdirect-sync/discussions)

## 🎯 Hoja de Ruta

- [ ] Interfaz web para configuración
- [ ] Programación automatizada
- [ ] Formatos de documento adicionales
- [ ] Opciones de filtrado mejoradas
- [ ] API para integraciones de terceros

---

**⭐ ¡Si encuentras este proyecto útil, por favor considera darle una estrella!**