# Comdirect Sync

[![Java](https://img.shields.io/badge/Java-21%20LTS-orange.svg)](https://openjdk.java.net/projects/jdk/21/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.4.0-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Maven](https://img.shields.io/badge/Maven-3.9.11-blue.svg)](https://maven.apache.org/)
[![License](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

A robust Java application for synchronizing and downloading documents from Comdirect bank using OAuth2 authentication with 2FA support.

## 🌍 Multi-Language Support

This README is available in multiple languages:

- �🇧 **English** (this file)
- 🇪🇸 [Español](README_es.md)
- 🇩🇪 [Deutsch](README_de.md)

## ✨ Features

- **🔐 Complete OAuth2 Flow**: Full authentication with Comdirect API
- **📱 2FA Support**: Interactive Two-Factor Authentication handling
- **📄 Document Sync**: Automated document download and organization
- **🌍 Internationalization**: Support for English, Spanish, and German
- **📁 Cross-Platform Storage**: Intelligent file storage for Mac/Linux/Windows
- **📊 Download History**: Prevents duplicate downloads with session tracking
- **⚙️ Environment Configuration**: Easy setup via `.env` file
- **🔄 Smart Date Ranges**: Configurable date filtering for document retrieval

## 🛠️ Technology Stack

- **Java 21 LTS** - Modern Java runtime with latest features
- **Spring Boot 3.4.0** - Enterprise-grade application framework
- **Maven 3.9.11** - Build automation and dependency management
- **OAuth2** - Secure authentication protocol
- **JSON Processing** - Document and session management
- **Cross-Platform** - Mac, Linux, and Windows support

## 📋 Prerequisites

- **Java 21 LTS** or higher
- **Maven 3.8+**
- **Comdirect Bank Account** with API access
- **Mobile App** for 2FA authentication

## � Quick Start

### 1. Clone the Repository

```bash
git clone https://github.com/kb-software/comdirect-sync.git
cd comdirect-sync
```

### 2. Configure Environment

Create a `.env` file in the project root:

```bash
# Language Configuration (en, es, de)
COMDIRECT_LANGUAGE=en

# Authentication (for production)
COMDIRECT_CLIENT_ID=your_client_id
COMDIRECT_CLIENT_SECRET=your_client_secret
COMDIRECT_USERNAME=your_username
COMDIRECT_PASSWORD=your_password

# Storage Configuration (optional)
DOWNLOAD_DIRECTORY=/path/to/downloads
HISTORY_DIRECTORY=/path/to/history
```

### 3. Run the Application

**Development Mode (with mock data):**

```bash
./run-dev.sh
```

**Production Mode:**

```bash
./run-prod.sh
```

**With Custom Date Range:**

```bash
./run-prod.sh --from=2024-01-01 --to=2024-12-31
```

### 🪟 Windows Support

**Development Mode:**

```powershell
.\run-dev.ps1
```

**Production Mode:**

```powershell
.\run-prod.ps1
```

**With Custom Date Range:**

```powershell
.\run-prod.ps1 --from=2024-01-01 --to=2024-12-31
```

### 📦 Standalone JAR Execution

**Build the JAR:**

```bash
# Unix/Linux/Mac
./build.sh

# Windows
.\build.ps1
```

**Run the JAR:**

```bash
# Development mode
java -jar comdirect-sync.jar --spring.profiles.active=dev

# Production mode  
java -jar comdirect-sync.jar --spring.profiles.active=prod

# With custom date range
java -jar comdirect-sync.jar --spring.profiles.active=prod --from=2024-01-01 --to=2024-12-31
```

## 📖 Usage

### Authentication Flow

1. **Initial Authentication**: The application starts OAuth2 flow
2. **Session Validation**: Validates the authentication session
3. **2FA Activation**: Prompts for mobile app authorization
4. **Token Retrieval**: Obtains secondary access token
5. **Document Processing**: Downloads and organizes documents

### Language Configuration

Change the application language by modifying the `.env` file:

```bash
# English
COMDIRECT_LANGUAGE=en

# Spanish
COMDIRECT_LANGUAGE=es

# German
COMDIRECT_LANGUAGE=de

# Auto-detect from system (default)
COMDIRECT_LANGUAGE=auto
```

### Document Types Supported

- **Dividend Reports** (Dividendengutschrift)
- **Tax Statements** (Steuermitteilung)
- **Transaction Notifications** (Buchungsanzeige)
- **Financial Reports** (Finanzreport)
- **Income Statements** (Ertragsgutschrift)

## 📁 Directory Structure

```
comdirect-sync/
├── src/
│   ├── main/java/de/comdirect/sync/
│   │   ├── application/          # Application layer
│   │   ├── domain/              # Domain models
│   │   ├── infrastructure/      # External services
│   │   └── ComdirectSyncApplication.java
│   └── resources/
│       ├── messages.properties     # English messages
│       ├── messages_es.properties  # Spanish messages
│       ├── messages_de.properties  # German messages
│       └── application*.properties
├── downloads/                   # Downloaded documents
├── history/                    # Download history
├── .env                       # Environment configuration
├── run-dev.sh                 # Development script
└── run-prod.sh               # Production script
```

## ⚙️ Configuration

### Environment Variables

| Variable | Default | Description |
|----------|---------|-------------|
| `COMDIRECT_LANGUAGE` | `auto` | UI language (en/es/de/auto) |
| `COMDIRECT_CLIENT_ID` | - | OAuth2 client ID |
| `COMDIRECT_CLIENT_SECRET` | - | OAuth2 client secret |
| `COMDIRECT_USERNAME` | - | Bank username |
| `COMDIRECT_PASSWORD` | - | Bank password |
| `DOWNLOAD_DIRECTORY` | `~/comdirect-sync/downloads` | Download folder |
| `HISTORY_DIRECTORY` | `~/comdirect-sync/history` | History folder |

### Application Profiles

- **dev**: Development mode with in-memory mock repositories
- **prod**: Production mode with real Comdirect API calls

## 🔧 Development

### Building the Project

**Standard Maven build:**

```bash
mvn clean compile
```

**Building Executable JAR:**

```bash
# Unix/Linux/Mac
./build.sh

# Windows  
.\build.ps1

# Skip tests (if needed)
./build.sh --skip-tests
.\build.ps1 --skip-tests
```

**Manual JAR build:**

```bash
mvn clean package -Dmaven.test.skip=true
```

### Running Tests

```bash
mvn test
```

### Generating Test Coverage

```bash
mvn jacoco:report
```

### Custom Build

```bash
mvn clean package -DskipTests
```

## 🐛 Troubleshooting

### Common Issues

1. **Authentication Errors**
   - Verify credentials in `.env` file
   - Check internet connection
   - Ensure mobile app is available for 2FA

2. **Language Not Loading**
   - Check `.env` file syntax
   - Restart the application
   - Verify `COMDIRECT_LANGUAGE` value

3. **File Permission Errors**
   - Check download directory permissions
   - Ensure sufficient disk space
   - Verify path accessibility

### Debug Mode

Enable debug logging by adding to your run command:

```bash
./run-dev.sh --debug
```

## 📊 Architecture

The application follows **Domain-Driven Design (DDD)** principles:

- **Application Layer**: Orchestrates business operations
- **Domain Layer**: Core business logic and entities
- **Infrastructure Layer**: External systems and persistence
- **Shared Layer**: Common utilities and services

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 🆘 Support

For support and questions:

- **Issues**: [GitHub Issues](https://github.com/kb-software/comdirect-sync/issues)
- **Documentation**: [Wiki](https://github.com/kb-software/comdirect-sync/wiki)
- **Discussions**: [GitHub Discussions](https://github.com/kb-software/comdirect-sync/discussions)

## 🎯 Roadmap

- [ ] Web interface for configuration
- [ ] Automated scheduling
- [ ] Additional document formats
- [ ] Enhanced filtering options
- [ ] API for third-party integrations

---

**⭐ If you find this project useful, please consider giving it a star!**