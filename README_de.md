# Comdirect Sync

[![Java](https://img.shields.io/badge/Java-21%20LTS-orange.svg)](https://openjdk.java.net/projects/jdk/21/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.4.0-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Maven](https://img.shields.io/badge/Maven-3.9.11-blue.svg)](https://maven.apache.org/)
[![License](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

Eine robuste Java-Anwendung zur Synchronisierung und zum Download von Dokumenten von der Comdirect Bank unter Verwendung von OAuth2-Authentifizierung mit 2FA-Unterstützung.

## 🌍 Mehrsprachige Unterstützung

Diese README ist in mehreren Sprachen verfügbar:

- 🇬🇧 [English](README.md)
- 🇪🇸 [Español](README_es.md)
- 🇩🇪 **Deutsch** (diese Datei)

## ✨ Funktionen

- **🔐 Vollständiger OAuth2-Ablauf**: Vollständige Authentifizierung mit der Comdirect-API
- **📱 2FA-Unterstützung**: Interaktive Zwei-Faktor-Authentifizierung
- **📄 Dokumentensynchronisation**: Automatischer Download und Organisation von Dokumenten
- **🌍 Internationalisierung**: Unterstützung für Englisch, Spanisch und Deutsch
- **📁 Plattformübergreifende Speicherung**: Intelligente Dateispeicherung für Mac/Linux/Windows
- **📊 Download-Verlauf**: Verhindert doppelte Downloads mit Session-Tracking
- **⚙️ Umgebungskonfiguration**: Einfache Einrichtung über `.env`-Datei
- **🔄 Intelligente Datumsbereiche**: Konfigurierbare Datumsfilterung für Dokumentenabruf

## 🛠️ Technologie-Stack

- **Java 21 LTS** - Moderne Java-Runtime mit neuesten Features
- **Spring Boot 3.4.0** - Enterprise-Level Application Framework
- **Maven 3.9.11** - Build-Automatisierung und Dependency-Management
- **OAuth2** - Sicheres Authentifizierungsprotokoll
- **JSON-Verarbeitung** - Dokument- und Session-Management
- **Plattformübergreifend** - Unterstützung für Mac, Linux und Windows

## 📋 Voraussetzungen

- **Java 21 LTS** oder höher
- **Maven 3.8+**
- **Comdirect Bankkonto** mit API-Zugang
- **Mobile App** für 2FA-Authentifizierung

## 🚀 Schnellstart

### 1. Repository Klonen

```bash
git clone https://github.com/kb-software/comdirect-sync.git
cd comdirect-sync
```

### 2. Umgebung Konfigurieren

Eine `.env`-Datei im Projektverzeichnis erstellen:

```bash
# Sprachkonfiguration (en, es, de)
COMDIRECT_LANGUAGE=de

# Authentifizierung (für Produktion)
COMDIRECT_CLIENT_ID=ihre_client_id
COMDIRECT_CLIENT_SECRET=ihr_client_secret
COMDIRECT_USERNAME=ihr_benutzername
COMDIRECT_PASSWORD=ihr_passwort

# Speicherkonfiguration (optional)
DOWNLOAD_DIRECTORY=/pfad/zu/downloads
HISTORY_DIRECTORY=/pfad/zu/verlauf
```

### 3. Anwendung Starten

**Entwicklungsmodus (mit Mock-Daten):**

```bash
./run-dev.sh
```

**Produktionsmodus:**

```bash
./run-prod.sh
```

**Mit angepasstem Datumsbereich:**

```bash
./run-prod.sh --from=2024-01-01 --to=2024-12-31
```

### 🪟 Windows-Unterstützung

**Entwicklungsmodus:**

```powershell
.\run-dev.ps1
```

**Produktionsmodus:**

```powershell
.\run-prod.ps1
```

### 📦 Standalone JAR-Ausführung

**JAR erstellen:**

```bash
# Unix/Linux/Mac
./build.sh

# Windows
.\build.ps1
```

**JAR ausführen:**

```bash
# Entwicklungsmodus
java -jar comdirect-sync.jar --spring.profiles.active=dev

# Produktionsmodus
java -jar comdirect-sync.jar --spring.profiles.active=prod
```

## 📖 Verwendung

### Authentifizierungs-Ablauf

1. **Erste Authentifizierung**: Die Anwendung startet den OAuth2-Ablauf
2. **Session-Validierung**: Validiert die Authentifizierungs-Session
3. **2FA-Aktivierung**: Fordert zur Autorisierung in der Mobile App auf
4. **Token-Abruf**: Erhält sekundären Zugangs-Token
5. **Dokumentenverarbeitung**: Lädt Dokumente herunter und organisiert sie

### Sprachkonfiguration

Die Anwendungssprache durch Ändern der `.env`-Datei anpassen:

```bash
# Englisch
COMDIRECT_LANGUAGE=en

# Spanisch
COMDIRECT_LANGUAGE=es

# Deutsch
COMDIRECT_LANGUAGE=de

# Auto-Erkennung vom System (Standard)
COMDIRECT_LANGUAGE=auto
```

### Unterstützte Dokumenttypen

- **Dividendengutschriften** (Dividendengutschrift)
- **Steuermitteilungen** (Steuermitteilung)
- **Buchungsanzeigen** (Buchungsanzeige)
- **Finanzreports** (Finanzreport)
- **Ertragsgutschriften** (Ertragsgutschrift)

## 📁 Verzeichnisstruktur

```text
comdirect-sync/
├── src/
│   ├── main/java/de/comdirect/sync/
│   │   ├── application/          # Anwendungsschicht
│   │   ├── domain/              # Domain-Modelle
│   │   ├── infrastructure/      # Externe Services
│   │   └── ComdirectSyncApplication.java
│   └── resources/
│       ├── messages.properties     # Englische Nachrichten
│       ├── messages_es.properties  # Spanische Nachrichten
│       ├── messages_de.properties  # Deutsche Nachrichten
│       └── application*.properties
├── downloads/                   # Heruntergeladene Dokumente
├── history/                    # Download-Verlauf
├── .env                       # Umgebungskonfiguration
├── run-dev.sh                 # Entwicklungsskript
└── run-prod.sh               # Produktionsskript
```

## ⚙️ Konfiguration

### Umgebungsvariablen

| Variable | Standard | Beschreibung |
|----------|----------|--------------|
| `COMDIRECT_LANGUAGE` | `auto` | UI-Sprache (en/es/de/auto) |
| `COMDIRECT_CLIENT_ID` | - | OAuth2-Client-ID |
| `COMDIRECT_CLIENT_SECRET` | - | OAuth2-Client-Secret |
| `COMDIRECT_USERNAME` | - | Bank-Benutzername |
| `COMDIRECT_PASSWORD` | - | Bank-Passwort |
| `DOWNLOAD_DIRECTORY` | `~/comdirect-sync/downloads` | Download-Ordner |
| `HISTORY_DIRECTORY` | `~/comdirect-sync/history` | Verlaufs-Ordner |

### Anwendungsprofile

- **dev**: Entwicklungsmodus mit In-Memory-Mock-Repositories
- **prod**: Produktionsmodus mit echten Comdirect-API-Aufrufen

## 🔧 Entwicklung

### Projekt Bauen

```bash
mvn clean compile
```

### Tests Ausführen

```bash
mvn test
```

### Test-Coverage Generieren

```bash
mvn jacoco:report
```

### Angepasster Build

```bash
mvn clean package -DskipTests
```

## 🐛 Fehlerbehebung

### Häufige Probleme

1. **Authentifizierungsfehler**
   - Anmeldedaten in `.env`-Datei überprüfen
   - Internetverbindung prüfen
   - Sicherstellen, dass Mobile App für 2FA verfügbar ist

2. **Sprache Lädt Nicht**
   - Syntax der `.env`-Datei überprüfen
   - Anwendung neu starten
   - `COMDIRECT_LANGUAGE`-Wert überprüfen

3. **Dateiberechtigungsfehler**
   - Download-Verzeichnis-Berechtigungen überprüfen
   - Ausreichend Speicherplatz sicherstellen
   - Pfad-Zugänglichkeit überprüfen

### Debug-Modus

Debug-Logging durch Hinzufügen zum Ausführungsbefehl aktivieren:

```bash
./run-dev.sh --debug
```

## 📊 Architektur

Die Anwendung folgt **Domain-Driven Design (DDD)**-Prinzipien:

- **Anwendungsschicht**: Orchestriert Geschäftsoperationen
- **Domain-Schicht**: Kern-Geschäftslogik und Entitäten
- **Infrastruktur-Schicht**: Externe Systeme und Persistierung
- **Geteilte Schicht**: Gemeinsame Utilities und Services

## 🤝 Beitragen

1. Repository forken
2. Feature-Branch erstellen (`git checkout -b feature/amazing-feature`)
3. Änderungen committen (`git commit -m 'Amazing Feature hinzufügen'`)
4. Branch pushen (`git push origin feature/amazing-feature`)
5. Pull Request öffnen

## 📄 Lizenz

Dieses Projekt ist unter der MIT-Lizenz lizenziert - siehe die [LICENSE](LICENSE)-Datei für Details.

## 🆘 Support

Für Support und Fragen:

- **Issues**: [GitHub Issues](https://github.com/kb-software/comdirect-sync/issues)
- **Dokumentation**: [Wiki](https://github.com/kb-software/comdirect-sync/wiki)
- **Diskussionen**: [GitHub Discussions](https://github.com/kb-software/comdirect-sync/discussions)

## 🎯 Roadmap

- [ ] Web-Interface für Konfiguration
- [ ] Automatische Zeitplanung
- [ ] Zusätzliche Dokumentenformate
- [ ] Erweiterte Filteroptionen
- [ ] API für Drittanbieter-Integrationen

---

**⭐ Wenn Sie dieses Projekt nützlich finden, geben Sie ihm bitte einen Stern!**