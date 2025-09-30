#!/usr/bin/env pwsh
# PowerShell script para ejecutar comdirect-sync en modo producción
# Equivalente de run-prod.sh para Windows

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

# Verificar si existe el archivo .env y cargarlo
if (Test-Path ".env") {
    Write-ColorOutput Yellow "🔧 Cargando configuración desde .env..."
    
    # Cargar variables del archivo .env
    Get-Content ".env" | ForEach-Object {
        if ($_ -match "^([^#].*)=(.*)$") {
            $name = $matches[1]
            $value = $matches[2]
            Set-Item -Path "Env:$name" -Value $value
        }
    }
    
    Write-ColorOutput Green "✅ Variables de entorno cargadas desde .env"
} else {
    Write-ColorOutput Red "❌ Error: Archivo .env requerido para modo producción"
    Write-ColorOutput Yellow "💡 Crea el archivo .env con tus credenciales de Comdirect:"
    Write-ColorOutput White @"
COMDIRECT_LANGUAGE=es
COMDIRECT_CLIENT_ID=tu_client_id
COMDIRECT_CLIENT_SECRET=tu_client_secret
COMDIRECT_USERNAME=tu_usuario
COMDIRECT_PASSWORD=tu_contraseña
"@
    exit 1
}

# Verificar variables críticas para producción
$requiredVars = @("COMDIRECT_CLIENT_ID", "COMDIRECT_CLIENT_SECRET", "COMDIRECT_USERNAME", "COMDIRECT_PASSWORD")
$missingVars = @()

foreach ($var in $requiredVars) {
    if (-not (Get-Item "Env:$var" -ErrorAction SilentlyContinue)) {
        $missingVars += $var
    }
}

if ($missingVars.Count -gt 0) {
    Write-ColorOutput Red "❌ Variables de entorno faltantes para producción:"
    foreach ($var in $missingVars) {
        Write-ColorOutput Red "   - $var"
    }
    Write-ColorOutput Yellow "💡 Añade estas variables a tu archivo .env"
    exit 1
}

Write-ColorOutput Cyan "🏭 Ejecutando con perfil PRODUCCIÓN (API Real de Comdirect)..."

# Mostrar configuración de idioma
if ($env:COMDIRECT_LANGUAGE) {
    Write-ColorOutput Green "🌍 Idioma configurado: $env:COMDIRECT_LANGUAGE"
} else {
    Write-ColorOutput Yellow "🌍 Idioma: auto-detectar del sistema"
}

# Mostrar argumentos
Write-ColorOutput Cyan "📅 Argumentos: $args"

# Verificar si Maven está disponible
try {
    $mvnVersion = mvn --version 2>$null
    if ($LASTEXITCODE -ne 0) {
        throw "Maven no encontrado"
    }
} catch {
    Write-ColorOutput Red "❌ Error: Maven no está instalado o no está en el PATH"
    Write-ColorOutput Yellow "💡 Instala Maven desde: https://maven.apache.org/download.cgi"
    Write-ColorOutput Yellow "   O usa chocolatey: choco install maven"
    exit 1
}

# Verificar Java
try {
    $javaVersion = java -version 2>&1
    if ($LASTEXITCODE -ne 0) {
        throw "Java no encontrado"
    }
    Write-ColorOutput Green "☕ Java encontrado"
} catch {
    Write-ColorOutput Red "❌ Error: Java no está instalado o no está en el PATH"
    Write-ColorOutput Yellow "💡 Instala Java 21 LTS desde: https://adoptium.net/"
    exit 1
}

Write-ColorOutput Red "⚠️  MODO PRODUCCIÓN: Conectando con API real de Comdirect"
Write-ColorOutput Yellow "📱 Asegúrate de tener tu móvil disponible para autenticación 2FA"

# Confirmación de usuario para producción
Write-ColorOutput White "¿Continuar con modo producción? (s/N): " -NoNewline
$confirmation = Read-Host

if ($confirmation -ne "s" -and $confirmation -ne "S" -and $confirmation -ne "y" -and $confirmation -ne "Y") {
    Write-ColorOutput Yellow "🛑 Ejecución cancelada por el usuario"
    exit 0
}

Write-ColorOutput Green "🚀 Iniciando aplicación en modo producción..."
Write-ColorOutput Gray "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"

# Ejecutar Maven con perfil prod y argumentos adicionales
try {
    & mvn spring-boot:run -Dspring-boot.run.profiles=prod -Dspring-boot.run.arguments="$($args -join ' ')"
    
    if ($LASTEXITCODE -eq 0) {
        Write-ColorOutput Green "✅ Aplicación ejecutada exitosamente"
    } else {
        Write-ColorOutput Red "❌ Error en la ejecución (código: $LASTEXITCODE)"
    }
} catch {
    Write-ColorOutput Red "❌ Error crítico ejecutando la aplicación: $_"
    exit 1
}

Write-ColorOutput Gray "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
Write-ColorOutput Cyan "🎯 Ejecución completada"