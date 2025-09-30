package de.comdirect.sync.configuration;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/**
 * Configuración para cargar variables del archivo .env
 */
@Configuration
public class EnvironmentConfiguration {

    @Bean
    public Dotenv dotenv() {
        // Cargar el archivo .env desde la raíz del proyecto
        // En dev usará valores por defecto si no existe el archivo
        return Dotenv.configure()
                .directory(".")
                .ignoreIfMalformed()
                .ignoreIfMissing()
                .load();
    }
}