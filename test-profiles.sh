#!/bin/bash

echo "🚀 Testing ComdirectSyncApplication with different profiles"
echo ""

echo "📋 1. Testing with DEV profile (InMemory repository):"
echo "   Command: mvn spring-boot:run -Dspring-boot.run.profiles=dev"
mvn spring-boot:run -Dmaven.test.skip=true -Dspring-boot.run.profiles=dev -q

echo ""
echo "📋 2. Testing with PROD profile (HTTP repository):"
echo "   Command: mvn spring-boot:run -Dspring-boot.run.profiles=prod"
echo "   Note: This will attempt real HTTP calls to Comdirect API"
mvn spring-boot:run -Dmaven.test.skip=true -Dspring-boot.run.profiles=prod -q

echo ""
echo "✅ Profile testing completed!"