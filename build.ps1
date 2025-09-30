#!/usr/bin/env pwsh
# Script de construcción PowerShell para comdirect-sync
# Genera JAR ejecutable con todas las dependencias

# Configuración de colores para Windows
$Host.UI.RawUI.ForegroundColor = "White"

# Función para mostrar mensajes con colores
function Write-ColorOutput($ForegroundColor) {
    $fc = $host.UI.RawUI.ForegroundColor
    $host.UI.RawUI.ForegroundColor = $ForegroundColor
    if ($args) {
        Write-Output $args
    } else {
        $input | Write-Output
    }
    $host.UI.RawUI.ForegroundColor = $fc
}

Write-ColorOutput Cyan "🔨 === COMDIRECT-SYNC BUILD SCRIPT ==="

# Verificar que estamos en el directorio correcto
if (-not (Test-Path "pom.xml")) {
    Write-ColorOutput Red "❌ Error: pom.xml no encontrado. Ejecuta este script desde la raíz del proyecto."
    exit 1
}

# Verificar Java
try {
    $null = java -version 2>&1
    if ($LASTEXITCODE -ne 0) {
        throw "Java no encontrado"
    }
} catch {
    Write-ColorOutput Red "❌ Error: Java no está instalado o no está en el PATH"
    Write-ColorOutput Yellow "💡 Instala Java 21 LTS desde: https://adoptium.net/"
    exit 1
}

# Verificar Maven
try {
    $null = mvn --version 2>$null
    if ($LASTEXITCODE -ne 0) {
        throw "Maven no encontrado"
    }
} catch {
    Write-ColorOutput Red "❌ Error: Maven no está instalado o no está en el PATH"
    Write-ColorOutput Yellow "💡 Instala Maven desde: https://maven.apache.org/download.cgi"
    Write-ColorOutput Yellow "   O usa chocolatey: choco install maven"
    exit 1
}

Write-ColorOutput Green "☕ Java y Maven encontrados"

# Limpiar builds anteriores
Write-ColorOutput Yellow "🧹 Limpiando builds anteriores..."
mvn clean | Out-Null

# Ejecutar tests (opcional, se puede saltar con --skip-tests)
$skipTests = $args -contains "--skip-tests"

if (-not $skipTests) {
    Write-ColorOutput Blue "🧪 Ejecutando tests..."
    mvn test
    
    if ($LASTEXITCODE -ne 0) {
        Write-ColorOutput Red "❌ Tests fallaron. Usa --skip-tests para omitir tests."
        exit 1
    }
    
    Write-ColorOutput Green "✅ Tests pasaron exitosamente"
} else {
    Write-ColorOutput Yellow "⏭️  Saltando tests..."
}

# Construir JAR ejecutable
Write-ColorOutput Blue "📦 Construyendo JAR ejecutable..."
mvn package -Dmaven.test.skip=true

if ($LASTEXITCODE -ne 0) {
    Write-ColorOutput Red "❌ Error en la construcción del JAR"
    exit 1
}

# Encontrar el JAR generado
$jarFile = Get-ChildItem -Path "target" -Filter "*-executable.jar" | Select-Object -First 1

if (-not $jarFile) {
    Write-ColorOutput Red "❌ Error: JAR ejecutable no encontrado en target/"
    exit 1
}

# Información del JAR generado
Write-ColorOutput Green "✅ ¡Construcción exitosa!"
Write-ColorOutput Cyan "📁 JAR ejecutable generado:"
Write-ColorOutput Cyan "   📄 Archivo: $($jarFile.FullName)"
Write-ColorOutput Cyan "   📏 Tamaño: $([math]::Round($jarFile.Length / 1MB, 2)) MB"

# Crear copia con nombre más simple
$simpleName = "comdirect-sync.jar"
Copy-Item $jarFile.FullName $simpleName -Force
Write-ColorOutput Cyan "   🔗 Copia: $simpleName"

Write-ColorOutput Yellow "💡 Cómo ejecutar el JAR:"
Write-ColorOutput White "   # Modo desarrollo:"
Write-ColorOutput White "   java -jar comdirect-sync.jar --spring.profiles.active=dev"
Write-ColorOutput White ""
Write-ColorOutput White "   # Modo producción:"
Write-ColorOutput White "   java -jar comdirect-sync.jar --spring.profiles.active=prod"
Write-ColorOutput White ""
Write-ColorOutput White "   # Con argumentos de fecha:"
Write-ColorOutput White "   java -jar comdirect-sync.jar --spring.profiles.active=prod --from=2024-01-01 --to=2024-12-31"

Write-ColorOutput Cyan "🎯 Build completado exitosamente"