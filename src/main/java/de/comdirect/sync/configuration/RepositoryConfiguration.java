package de.comdirect.sync.configuration;

import org.springframework.context.annotation.Configuration;

/**
 * Configuración para seleccionar la implementación del repositorio de autenticación
 * 
 * Las implementaciones están marcadas con @Profile:
 * - ComdirectAuthRepositoryInMemory: perfiles "dev", "test", "default"
 * - ComdirectAuthRepositoryHttp: perfil "prod"
 */
@Configuration
public class RepositoryConfiguration {
    // La configuración se maneja automáticamente por @Profile en las implementaciones
}