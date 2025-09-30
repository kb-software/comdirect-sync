# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

## [1.0.0] - 2025-09-30

### Added

#### Core Features

- Complete OAuth2 authentication flow with Comdirect API
- Interactive Two-Factor Authentication (2FA) support with mobile app integration
- Automated document download and synchronization
- Cross-platform file storage system (Mac, Linux, Windows)
- Document history tracking to prevent duplicate downloads
- Session management with JSON-based persistence

#### Internationalization

- Multi-language support for English, Spanish, and German
- Dynamic language detection from system locale
- Environment-based language configuration via `.env` file
- Comprehensive message externalization with 140+ translated keys
- MessageService for centralized localization management

#### Configuration & Environment

- Environment variable configuration via `.env` file support
- Development and production profiles with different data sources
- Configurable download and history directories
- Smart date range handling with default current month
- Command-line argument parsing for date ranges

#### Authentication & Security

- Complete OAuth2 implementation with primary and secondary tokens
- Session validation and timeout handling
- 2FA workflow with user interaction prompts
- Secure credential management through environment variables

#### Document Management

- Support for multiple document types:
  - Dividend Reports (Dividendengutschrift)
  - Tax Statements (Steuermitteilung)  
  - Transaction Notifications (Buchungsanzeige)
  - Financial Reports (Finanzreport)
  - Income Statements (Ertragsgutschrift)
- Smart filename sanitization for cross-platform compatibility
- Duplicate detection and skipping mechanism
- Download progress tracking and reporting

#### Technical Infrastructure

- Domain-Driven Design (DDD) architecture
- Spring Boot 3.4.0 framework with dependency injection
- In-memory repositories for development/testing
- RESTful service interfaces ready for production implementation
- Comprehensive logging with localized messages
- JaCoCo test coverage integration

### Technical Details

#### Java & Dependencies

- **Java Runtime**: Upgraded to Java 21 LTS from Java 17
- **Spring Boot**: Updated to 3.4.0 from 3.2.0
- **Maven**: Updated to 3.9.11 with compiler plugin 3.12.1
- **JavaFX**: Updated to 22.0.2 for potential future UI components

#### Architecture

- **Application Layer**: ComdirectSyncApplication, CommandLineArgsParser, UserInteractionService
- **Domain Layer**: User, Document, DateRange, Token entities and value objects
- **Infrastructure Layer**: MessageService, DocumentStorageService, DocumentHistoryService, Repository implementations
- **Configuration**: Multi-profile application properties and environment management

#### Development Tools

- Maven build automation with release 21 target
- JaCoCo code coverage reporting
- Development and production run scripts
- Comprehensive unit test structure for domain objects

### Files Added

- `src/main/resources/messages.properties` - English message catalog
- `src/main/resources/messages_es.properties` - Spanish message catalog  
- `src/main/resources/messages_de.properties` - German message catalog
- `src/main/java/de/comdirect/sync/infrastructure/services/MessageService.java` - Localization service
- `src/main/java/de/comdirect/sync/application/services/UserInteractionService.java` - 2FA interaction handler
- `src/main/java/de/comdirect/sync/infrastructure/services/DocumentStorageService.java` - File management
- `src/main/java/de/comdirect/sync/infrastructure/services/DocumentHistoryService.java` - Download tracking
- `.env` - Environment configuration template
- `run-dev.sh` - Development execution script with environment loading
- `run-prod.sh` - Production execution script
- `README.md` - English documentation
- `README_es.md` - Spanish documentation  
- `README_de.md` - German documentation

### Changed

- **pom.xml**: Updated all dependencies to latest compatible versions
- **ComdirectSyncApplication.java**: Refactored to use MessageService and support localization
- **Application properties**: Enhanced with multi-profile configuration
- All hardcoded strings replaced with internationalized message keys

### Technical Notes

- This is the initial production release of the comdirect-sync application
- All core functionality is complete and tested in development mode
- Production deployment requires valid Comdirect API credentials
- The application follows enterprise-grade security and architecture patterns

---

## Release Notes

### Version 1.0.0 - "Foundation Release"

This release establishes the complete foundation for the Comdirect document synchronization system. The application provides a robust, enterprise-ready solution for automated document management with full internationalization support.

**Key Highlights:**

- 🔐 Production-ready OAuth2 + 2FA authentication
- 🌍 Complete multi-language support (EN/ES/DE)
- 📁 Cross-platform file management
- 📊 Intelligent duplicate prevention
- ⚙️ Environment-based configuration
- 🏗️ Clean DDD architecture ready for scaling

The application is now ready for production deployment and can serve as a foundation for future enhancements and integrations.

[Unreleased]: https://github.com/kb-software/comdirect-sync/compare/v1.0.0...HEAD
[1.0.0]: https://github.com/kb-software/comdirect-sync/releases/tag/v1.0.0
