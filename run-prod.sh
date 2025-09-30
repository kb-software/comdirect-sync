#!/bin/bash

# Función para mostrar ayuda del script
show_help() {
    echo "🚀 Script de ejecución en modo PRODUCCIÓN"
    echo ""
    echo "Usage: ./run-prod.sh [OPTIONS]"
    echo ""
    echo "OPTIONS:"
    echo "  --init-date=YYYY-MM-DD    Initial date for document download"
    echo "  --end-date=YYYY-MM-DD     End date for document download"
    echo "  --help                    Show this help"
    echo ""
    echo "Examples:"
    echo "  ./run-prod.sh --init-date=2025-06-01 --end-date=2025-09-20"
    echo "  ./run-prod.sh --init-date=2025-06-01"
    echo "  ./run-prod.sh"
    echo ""
    echo "⚠️  Warning: This mode will make real calls to the Comdirect API"
    echo ""
}

# Verificar si se solicita ayuda
if [[ "$1" == "--help" || "$1" == "-h" ]]; then
    show_help
    exit 0
fi

echo "🚀 Running with PROD profile (HTTP repository)..."
echo "⚠️  Warning: This mode will make real calls to the Comdirect API"
echo "📅 Arguments: $*"
echo ""

# Pasar todos los argumentos a la aplicación Spring Boot
mvn spring-boot:run -Dmaven.test.skip=true -Dspring-boot.run.profiles=prod -Dspring-boot.run.arguments="$*"