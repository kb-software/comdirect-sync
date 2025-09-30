package de.comdirect.sync.configuration;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * Configuración de propiedades de Comdirect desde archivo .env
 */
@Component
public class ComdirectProperties {
    
    private final Dotenv dotenv;
    
    @Autowired
    public ComdirectProperties(Dotenv dotenv) {
        this.dotenv = dotenv;
    }
    
    public String getClientId() {
        return dotenv.get("COMDIRECT_CLIENT_ID", "");
    }
    
    public String getClientSecret() {
        return dotenv.get("COMDIRECT_CLIENT_SECRET", "");
    }
    
    public String getUsername() {
        return dotenv.get("COMDIRECT_USERNAME", "");
    }
    
    public String getPassword() {
        return dotenv.get("COMDIRECT_PASSWORD", "");
    }
    
    public String getBaseUrl() {
        return dotenv.get("COMDIRECT_BASE_URL", "https://api.comdirect.de");
    }
}