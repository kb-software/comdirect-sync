#!/bin/bash

# Función para mostrar ayuda del script
show_help() {
    echo "🔧 Script de ejecución en modo DESARROLLO"
    echo ""
    echo "Uso: ./run-dev.sh [OPCIONES]"
    echo ""
    echo "OPCIONES:"
    echo "  --init-date=YYYY-MM-DD    Fecha inicial para descarga de documentos"
    echo "  --end-date=YYYY-MM-DD     Fecha final para descarga de documentos"
    echo "  --help                    Muestra esta ayuda"
    echo ""
    echo "Ejemplos:"
    echo "  ./run-dev.sh --init-date=2025-06-01 --end-date=2025-09-20"
    echo "  ./run-dev.sh --init-date=2025-06-01"
    echo "  ./run-dev.sh"
    echo ""
}

# Verificar si se solicita ayuda
if [[ "$1" == "--help" || "$1" == "-h" ]]; then
    show_help
    exit 0
fi

# Cargar variables de entorno desde el archivo .env
if [[ -f ".env" ]]; then
    echo "� Cargando configuración desde .env..."
    export $(grep -v '^#' .env | xargs)
else
    echo "⚠️  Archivo .env no encontrado, usando configuración por defecto"
fi

echo "�🔧 Ejecutando con perfil DEV (InMemory repository)..."
echo "🌍 Idioma configurado: ${COMDIRECT_LANGUAGE:-auto}"
echo "📅 Argumentos: $*"
echo ""

# Pasar todos los argumentos a la aplicación Spring Boot
mvn spring-boot:run -Dmaven.test.skip=true -Dspring-boot.run.profiles=dev -Dspring-boot.run.arguments="$*"