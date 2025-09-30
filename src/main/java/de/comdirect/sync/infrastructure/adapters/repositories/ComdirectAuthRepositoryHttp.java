package de.comdirect.sync.infrastructure.adapters.repositories;

import de.comdirect.sync.configuration.ComdirectProperties;
import de.comdirect.sync.domain.entities.AccessToken;
import de.comdirect.sync.domain.entities.SessionInfo;
import de.comdirect.sync.domain.repositories.ComdirectAuthRepository;
import de.comdirect.sync.domain.valueobjects.TokenId;
import de.comdirect.sync.domain.valueobjects.SessionId;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.http.*;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.*;

/**
 * Implementación HTTP del repositorio de autenticación para Comdirect API
 * Hace llamadas reales a los endpoints de Comdirect según la documentación
 */
@Repository
@Profile("prod") // Solo en perfil de producción
public class ComdirectAuthRepositoryHttp implements ComdirectAuthRepository {
    
    private static final Logger logger = LoggerFactory.getLogger(ComdirectAuthRepositoryHttp.class);
    
    private final ComdirectProperties properties;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final Map<String, AccessToken> tokenCache = new HashMap<>();
    private final Map<String, SessionInfo> sessionCache = new HashMap<>();
    private final Map<String, String> challengeInfoCache = new HashMap<>(); // Para almacenar challenge info del paso 2.3
    
    @Autowired
    public ComdirectAuthRepositoryHttp(ComdirectProperties properties, RestTemplate restTemplate) {
        this.properties = properties;
        this.restTemplate = restTemplate;
        this.objectMapper = new ObjectMapper();
        logger.info("RestTemplate configurado con Apache HttpClient5 para soporte PATCH");
    }
    
    @Override
    public AccessToken authenticateUser(String clientId, String clientSecret, String username, String password) {
        logger.info("=== PASO 2.1: Autenticación inicial HTTP ===");
        logger.info("Enviando petición a: {}", getOAuthTokenEndpoint());
        
        try {
            // Preparar headers con headers requeridos
            HttpHeaders headers = createStandardHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            
            // Preparar body para OAuth2 Resource Owner Password Credentials Grant
            String body = String.format(
                "client_id=%s&client_secret=%s&grant_type=password&username=%s&password=%s",
                clientId, clientSecret, username, password
            );
            
            HttpEntity<String> request = new HttpEntity<>(body, headers);
            
            // Hacer llamada HTTP
            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                getOAuthTokenEndpoint(),
                HttpMethod.POST,
                request,
                (Class<Map<String, Object>>) (Class<?>) Map.class
            );
            
            // Procesar respuesta
            Map<String, Object> responseBody = response.getBody();
            if (responseBody == null) {
                throw new RuntimeException("Empty response from authentication endpoint");
            }
            
            logger.info("Respuesta recibida: {}", responseBody);
            
            // Crear AccessToken desde la respuesta
            AccessToken token = mapResponseToAccessToken(responseBody);
            tokenCache.put(token.getAccessToken(), token);
            
            logger.info("✅ Token obtenido exitosamente: {}", token.getId().getValue());
            return token;
            
        } catch (HttpClientErrorException e) {
            logger.error("Error HTTP autenticando usuario: {} - {}", 
                e.getStatusCode(), e.getResponseBodyAsString());
            throw new RuntimeException("HTTP authentication failed: " + e.getMessage());
        } catch (Exception e) {
            logger.error("Error inesperado autenticando usuario", e);
            throw new RuntimeException("Unexpected authentication error", e);
        }
    }
    
    @Override
    public Optional<SessionInfo> getSessionStatus(String accessToken) {
        logger.info("=== PASO 2.2: Obtener estado de sesión HTTP ===");
        String url = getSessionStatusEndpoint();
        logger.info("Enviando petición a: {}", url);
        
        try {
            // Preparar headers con el token de autorización y headers requeridos
            HttpHeaders headers = createStandardHeaders();
            headers.setBearerAuth(accessToken);
            
            HttpEntity<?> request = new HttpEntity<>(headers);
            
            // Hacer llamada HTTP - La API devuelve un array de sesiones
            ResponseEntity<List> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                request,
                List.class
            );
            
            // Procesar respuesta (la API devuelve un array de sesiones)
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> responseBody = (List<Map<String, Object>>) response.getBody();
            if (responseBody == null || responseBody.isEmpty()) {
                return Optional.empty();
            }
            
            // Tomar la primera sesión (la activa)
            Map<String, Object> session = responseBody.get(0);
            logger.info("Estado de sesión recibido: {}", session);
            
            // Mapear respuesta a SessionInfo
            SessionInfo sessionInfo = mapResponseToSessionInfo(session);
            sessionCache.put(sessionInfo.getIdentifier(), sessionInfo);
            
            logger.info("✅ Estado de sesión obtenido: {}", sessionInfo);
            return Optional.of(sessionInfo);
            
        } catch (HttpClientErrorException e) {
            logger.error("Error HTTP obteniendo estado de sesión: {} - {}", 
                e.getStatusCode(), e.getResponseBodyAsString());
            return Optional.empty();
        } catch (Exception e) {
            logger.error("Error inesperado obteniendo estado de sesión", e);
            throw new RuntimeException("Unexpected session status error", e);
        }
    }
    
    @Override
    public SessionInfo validateSessionTan(String accessToken, String sessionIdentifier) {
        logger.info("=== PASO 2.3: Validar sesión TAN HTTP ===");
        String url = getSessionValidateEndpoint().replace("{sessionId}", sessionIdentifier);
        logger.info("Enviando petición a: {}", url);
        
        try {
            // Preparar headers con el token de autorización y headers requeridos
            HttpHeaders headers = createStandardHeaders();
            headers.setBearerAuth(accessToken);
            
            // Preparar body según documentación oficial de Comdirect
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("identifier", sessionIdentifier);
            requestBody.put("sessionTanActive", true);
            requestBody.put("activated2FA", true);
            
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);
            
            // Hacer llamada HTTP - usar POST sin body
            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                request,
                (Class<Map<String, Object>>) (Class<?>) Map.class
            );
            
            // Procesar respuesta
            Map<String, Object> responseBody = response.getBody();
            if (responseBody == null) {
                throw new RuntimeException("Empty response from session validation endpoint");
            }
            
            logger.info("Sesión validada: {}", responseBody);
            
            // Capturar challenge ID de los headers de respuesta para uso en paso 2.4
            HttpHeaders responseHeaders = response.getHeaders();
            String challengeInfo = responseHeaders.getFirst("x-once-authentication-info");
            logger.info("Challenge info recibido: {}", challengeInfo);
            
            // Actualizar SessionInfo en caché
            SessionInfo sessionInfo = mapResponseToSessionInfo(responseBody);
            if (challengeInfo != null) {
                // Guardamos el challenge info en el cache separado para uso en paso 2.4
                challengeInfoCache.put(sessionInfo.getIdentifier(), challengeInfo);
            }
            sessionCache.put(sessionInfo.getIdentifier(), sessionInfo);
            
            logger.info("✅ Sesión TAN validada: {}", sessionInfo);
            return sessionInfo;
            
        } catch (HttpClientErrorException e) {
            logger.error("Error HTTP validando sesión TAN: {} - {}", 
                e.getStatusCode(), e.getResponseBodyAsString());
            throw new RuntimeException("HTTP session validation failed: " + e.getMessage());
        } catch (Exception e) {
            logger.error("Error inesperado validando sesión TAN", e);
            throw new RuntimeException("Unexpected session validation error", e);
        }
    }
    
    @Override
    public SessionInfo activateSessionTan(String accessToken, String sessionIdentifier, 
                                        String tanCode, String authenticationId) {
        logger.info("=== PASO 2.4: Activar sesión TAN HTTP ===");
        String url = getSessionActivateEndpoint()
            .replace("{sessionId}", sessionIdentifier);
        logger.info("Enviando petición a: {}", url);
        
        try {
            // Preparar headers con el token de autorización y headers requeridos según documentación
            HttpHeaders headers = createStandardHeaders();
            headers.setBearerAuth(accessToken);
            
            // Obtener challenge info del paso 2.3
            String challengeInfo = challengeInfoCache.get(sessionIdentifier);
            if (challengeInfo != null) {
                try {
                    // Parsear el JSON para extraer solo el ID
                    Map<String, Object> challengeJson = objectMapper.readValue(challengeInfo, Map.class);
                    String challengeId = (String) challengeJson.get("id");
                    
                    if (challengeId != null) {
                        // Crear el header con formato correcto: {"id":"582734842"}
                        String challengeHeaderValue = "{\"id\":\"" + challengeId + "\"}";
                        headers.set("x-once-authentication-info", challengeHeaderValue);
                        logger.info("Challenge ID extraído y agregado a headers: {}", challengeHeaderValue);
                    } else {
                        logger.warn("No se pudo extraer challenge ID del JSON: {}", challengeInfo);
                    }
                } catch (Exception e) {
                    logger.error("Error parseando challenge info JSON: {}", challengeInfo, e);
                }
            } else {
                logger.warn("No se encontró challenge info para session: {}", sessionIdentifier);
            }
            
            // Agregar código TAN al header según documentación
            if (tanCode != null && !tanCode.trim().isEmpty()) {
                headers.set("x-once-authentication", tanCode);
                logger.info("Código TAN agregado a headers");
            } else {
                logger.info("No se proporcionó código TAN (posiblemente photoTAN-Push)");
            }
            
            // Preparar body según documentación oficial
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("identifier", sessionIdentifier);
            requestBody.put("sessionTanActive", true);
            requestBody.put("activated2FA", true);
            
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);
            
            // Hacer llamada HTTP - Usar PATCH según documentación oficial
            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                url,
                HttpMethod.PATCH, // PATCH según documentación oficial de Comdirect
                request,
                (Class<Map<String, Object>>) (Class<?>) Map.class
            );
            
            // Procesar respuesta
            Map<String, Object> responseBody = response.getBody();
            if (responseBody == null) {
                throw new RuntimeException("Empty response from session activation endpoint");
            }
            
            logger.info("Sesión TAN activada: {}", responseBody);
            
            // Actualizar SessionInfo en caché
            SessionInfo sessionInfo = mapResponseToSessionInfo(responseBody);
            sessionCache.put(sessionInfo.getIdentifier(), sessionInfo);
            
            logger.info("✅ Sesión TAN activada: {}", sessionInfo);
            return sessionInfo;
            
        } catch (HttpClientErrorException e) {
            logger.error("Error HTTP activando sesión TAN: {} - {}", 
                e.getStatusCode(), e.getResponseBodyAsString());
            throw new RuntimeException("HTTP session activation failed: " + e.getMessage());
        } catch (Exception e) {
            logger.error("Error inesperado activando sesión TAN", e);
            throw new RuntimeException("Unexpected session activation error", e);
        }
    }
    
    @Override
    public AccessToken getSecondaryToken(String clientId, String clientSecret, String primaryToken) {
        logger.info("=== PASO 2.5: Obtener token secundario HTTP ===");
        logger.info("Enviando petición a: {}", getOAuthTokenEndpoint());
        
        try {
            // Preparar headers con headers requeridos
            HttpHeaders headers = createStandardHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            headers.setBearerAuth(primaryToken);
            
            // Preparar body para CD Secondary Flow
            String body = String.format(
                "client_id=%s&client_secret=%s&grant_type=cd_secondary&token=%s",
                clientId, clientSecret, primaryToken
            );
            
            HttpEntity<String> request = new HttpEntity<>(body, headers);
            
            // Hacer llamada HTTP
            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                getOAuthTokenEndpoint(),
                HttpMethod.POST,
                request,
                (Class<Map<String, Object>>) (Class<?>) Map.class
            );
            
            // Procesar respuesta
            Map<String, Object> responseBody = response.getBody();
            if (responseBody == null) {
                throw new RuntimeException("Empty response from secondary token endpoint");
            }
            
            logger.info("Token secundario recibido: {}", responseBody);
            
            // Crear AccessToken desde la respuesta
            AccessToken secondaryToken = mapResponseToAccessToken(responseBody);
            tokenCache.put(secondaryToken.getAccessToken(), secondaryToken);
            
            logger.info("✅ Token secundario obtenido exitosamente: {}", secondaryToken.getId().getValue());
            return secondaryToken;
            
        } catch (HttpClientErrorException e) {
            logger.error("Error HTTP obteniendo token secundario: {} - {}", 
                e.getStatusCode(), e.getResponseBodyAsString());
            throw new RuntimeException("HTTP secondary token failed: " + e.getMessage());
        } catch (Exception e) {
            logger.error("Error inesperado obteniendo token secundario", e);
            throw new RuntimeException("Unexpected secondary token error", e);
        }
    }
    
    // Métodos de URL
    private String getOAuthTokenEndpoint() {
        return properties.getBaseUrl() + "/oauth/token";
    }
    
    private String getSessionStatusEndpoint() {
        return properties.getBaseUrl() + "/api/session/clients/user/v1/sessions";
    }
    
    private String getSessionValidateEndpoint() {
        return properties.getBaseUrl() + "/api/session/clients/user/v1/sessions/{sessionId}/validate";
    }
    
    private String getSessionActivateEndpoint() {
        return properties.getBaseUrl() + "/api/session/clients/user/v1/sessions/{sessionId}";
    }
    
    // Métodos de mapeo
    private AccessToken mapResponseToAccessToken(Map<String, Object> response) {
        String accessToken = (String) response.get("access_token");
        String tokenType = (String) response.get("token_type");
        Integer expiresIn = (Integer) response.get("expires_in");
        String scope = (String) response.get("scope");
        Object kdnrObj = response.get("kdnr");
        Object bpidObj = response.get("bpid");
        Object kontaktIdObj = response.get("kontaktId");
        
        // Manejar kdnr que puede venir como Integer, Long o String
        String kdnr = null;
        if (kdnrObj instanceof Integer) {
            kdnr = kdnrObj.toString();
        } else if (kdnrObj instanceof Long) {
            kdnr = kdnrObj.toString();
        } else if (kdnrObj instanceof String) {
            kdnr = (String) kdnrObj;
        }
        
        // Manejar bpid que puede venir como Integer, Long o String
        Long bpid = null;
        if (bpidObj instanceof Integer) {
            bpid = ((Integer) bpidObj).longValue();
        } else if (bpidObj instanceof Long) {
            bpid = (Long) bpidObj;
        } else if (bpidObj instanceof String) {
            bpid = Long.parseLong((String) bpidObj);
        }
        
        // Manejar kontaktId que puede venir como Integer, Long o String
        Long kontaktId = null;
        if (kontaktIdObj instanceof Integer) {
            kontaktId = ((Integer) kontaktIdObj).longValue();
        } else if (kontaktIdObj instanceof Long) {
            kontaktId = (Long) kontaktIdObj;
        } else if (kontaktIdObj instanceof String) {
            kontaktId = Long.parseLong((String) kontaktIdObj);
        }
        
        return AccessToken.builder()
                .accessToken(accessToken)
                .tokenType(tokenType != null ? tokenType : "bearer")
                .expiresIn(expiresIn != null ? expiresIn : 600)
                .scope(scope != null ? Set.of(scope.split("\\s+")) : Set.of("TWO_FACTOR"))
                .kdnr(kdnr)
                .bpid(bpid)
                .kontaktId(kontaktId)
                .build();
    }
    
    private SessionInfo mapResponseToSessionInfo(Map<String, Object> response) {
        String sessionId = (String) response.get("sessionId");
        String identifier = (String) response.get("identifier");
        Boolean sessionTanActive = (Boolean) response.get("sessionTanActive");
        Boolean activated2FA = (Boolean) response.get("activated2FA");
        
        return SessionInfo.builder()
                .identifier(identifier != null ? identifier : sessionId)
                .sessionTanActive(sessionTanActive != null ? sessionTanActive : false)
                .activated2FA(activated2FA != null ? activated2FA : false)
                .build();
    }
    
    @Override
    public Optional<AccessToken> findTokenById(TokenId tokenId) {
        return tokenCache.values().stream()
                .filter(token -> token.getAccessToken().equals(tokenId.getValue()))
                .findFirst();
    }
    
    @Override
    public Optional<SessionInfo> findSessionById(SessionId sessionId) {
        return sessionCache.values().stream()
                .filter(session -> session.getIdentifier().equals(sessionId.getValue()))
                .findFirst();
    }
    
    private HttpHeaders createStandardHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
        headers.setContentType(MediaType.APPLICATION_JSON);
        
        // Header requerido por la API de Comdirect
        headers.set("x-http-request-info", "{\"clientRequestId\":{\"sessionId\":\"" + 
                    java.util.UUID.randomUUID().toString() + "\",\"requestId\":\"" + 
                    java.util.UUID.randomUUID().toString() + "\"}}");
        
        return headers;
    }
}
    
