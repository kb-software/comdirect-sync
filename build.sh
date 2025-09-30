#!/bin/bash
# Script de construcción para comdirect-sync
# Genera JAR ejecutable con todas las dependencias

set -e

# Colores para output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
CYAN='\033[0;36m'
NC='\033[0m' # No Color

# Función para mostrar mensajes con color
print_color() {
    local color=$1
    local message=$2
    echo -e "${color}${message}${NC}"
}

print_color $CYAN "🔨 === COMDIRECT-SYNC BUILD SCRIPT ==="

# Verificar que estamos en el directorio correcto
if [ ! -f "pom.xml" ]; then
    print_color $RED "❌ Error: pom.xml no encontrado. Ejecuta este script desde la raíz del proyecto."
    exit 1
fi

# Verificar Java
if ! command -v java &> /dev/null; then
    print_color $RED "❌ Error: Java no está instalado o no está en el PATH"
    print_color $YELLOW "💡 Instala Java 21 LTS desde: https://adoptium.net/"
    exit 1
fi

# Verificar Maven
if ! command -v mvn &> /dev/null; then
    print_color $RED "❌ Error: Maven no está instalado o no está en el PATH"
    print_color $YELLOW "💡 Instala Maven desde: https://maven.apache.org/download.cgi"
    exit 1
fi

print_color $GREEN "☕ Java y Maven encontrados"

# Limpiar builds anteriores
print_color $YELLOW "🧹 Limpiando builds anteriores..."
mvn clean > /dev/null

# Ejecutar tests (opcional, se puede saltar con --skip-tests)
if [[ "$1" != "--skip-tests" ]]; then
    print_color $BLUE "🧪 Ejecutando tests..."
    mvn test
    
    if [ $? -ne 0 ]; then
        print_color $RED "❌ Tests fallaron. Usa --skip-tests para omitir tests."
        exit 1
    fi
    
    print_color $GREEN "✅ Tests pasaron exitosamente"
else
    print_color $YELLOW "⏭️  Saltando tests..."
fi

# Construir JAR ejecutable
print_color $BLUE "📦 Construyendo JAR ejecutable..."
mvn package -Dmaven.test.skip=true

if [ $? -ne 0 ]; then
    print_color $RED "❌ Error en la construcción del JAR"
    exit 1
fi

# Encontrar el JAR generado
JAR_FILE=$(find target -name "*-executable.jar" | head -1)

if [ -z "$JAR_FILE" ]; then
    print_color $RED "❌ Error: JAR ejecutable no encontrado en target/"
    exit 1
fi

# Información del JAR generado
print_color $GREEN "✅ ¡Construcción exitosa!"
print_color $CYAN "📁 JAR ejecutable generado:"
print_color $CYAN "   📄 Archivo: $JAR_FILE"
print_color $CYAN "   📏 Tamaño: $(du -h "$JAR_FILE" | cut -f1)"

# Crear enlace simbólico para facilitar el uso
ln -sf "$JAR_FILE" "comdirect-sync.jar"
print_color $CYAN "   🔗 Enlace: comdirect-sync.jar -> $JAR_FILE"

print_color $YELLOW "💡 Cómo ejecutar el JAR:"
print_color $WHITE "   # Modo desarrollo:"
print_color $WHITE "   java -jar comdirect-sync.jar --spring.profiles.active=dev"
print_color $WHITE ""
print_color $WHITE "   # Modo producción:"
print_color $WHITE "   java -jar comdirect-sync.jar --spring.profiles.active=prod"
print_color $WHITE ""
print_color $WHITE "   # Con argumentos de fecha:"
print_color $WHITE "   java -jar comdirect-sync.jar --spring.profiles.active=prod --from=2024-01-01 --to=2024-12-31"

print_color $CYAN "🎯 Build completado exitosamente"