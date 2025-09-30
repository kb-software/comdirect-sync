package de.comdirect.sync.infrastructure.configuration;

import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

/**
 * Configuración de RestTemplate para usar Apache HttpClient5
 * que soporta el método PATCH requerido por la API de Comdirect
 */
@Configuration
@Profile("prod")
public class HttpClientConfiguration {
    
    @Bean
    public RestTemplate restTemplate() {
        // Crear HttpClient de Apache que soporta PATCH
        CloseableHttpClient httpClient = HttpClients.createDefault();
        
        // Crear factory que usa Apache HttpClient
        HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory();
        factory.setHttpClient(httpClient);
        
        // Configurar timeouts
        factory.setConnectTimeout(30000); // 30 segundos
        factory.setConnectionRequestTimeout(30000); // 30 segundos
        
        // Crear RestTemplate con el factory personalizado
        return new RestTemplate(factory);
    }
}