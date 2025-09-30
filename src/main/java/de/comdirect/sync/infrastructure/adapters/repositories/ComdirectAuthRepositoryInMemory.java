package de.comdirect.sync.infrastructure.adapters.repositories;

import de.comdirect.sync.domain.entities.AccessToken;
import de.comdirect.sync.domain.entities.SessionInfo;
import de.comdirect.sync.domain.repositories.ComdirectAuthRepository;
import de.comdirect.sync.domain.valueobjects.TokenId;
import de.comdirect.sync.domain.valueobjects.SessionId;
import de.comdirect.sync.domain.exceptions.InvalidUserDataException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Implementación InMemory del repositorio de autenticación para testing
 * Simula las respuestas de la API de Comdirect basadas en la documentación
 */
@Repository
@Profile("dev") // Solo en perfil de desarrollo
public class ComdirectAuthRepositoryInMemory implements ComdirectAuthRepository {
    
    private static final Logger logger = LoggerFactory.getLogger(ComdirectAuthRepositoryInMemory.class);
    
    private final Map<String, AccessToken> tokens = new ConcurrentHashMap<>();
    private final Map<String, SessionInfo> sessions = new ConcurrentHashMap<>();
    
    // Datos de prueba basados en la documentación
    private static final String TEST_CLIENT_ID = "User_93FA2414B4B146969DFFDFEC21E8C845";
    private static final String TEST_CLIENT_SECRET = "BB0AC148B2874D0ABF9D34EB799A47FD";
    private static final String TEST_USERNAME = "1188651895"; // zugangsnummer
    private static final String TEST_PASSWORD = "testpin";
    
    @Override
    public AccessToken authenticateUser(String clientId, String clientSecret, 
                                      String username, String password) {
        logger.info("🔧 Usando ComdirectAuthRepositoryInMemory - simulación en memoria");
        
        // Validar credenciales de prueba
        if (!TEST_CLIENT_ID.equals(clientId) || 
            !TEST_CLIENT_SECRET.equals(clientSecret) ||
            !TEST_USERNAME.equals(username) ||
            !TEST_PASSWORD.equals(password)) {
            throw new InvalidUserDataException("Invalid credentials");
        }
        
        // Crear token inicial como en la documentación (paso 2.1)
        AccessToken token = AccessToken.builder()
                .accessToken("5375a37e-d7cd-4174-a73a-f9e1065f8df5")
                .tokenType("bearer")
                .refreshToken("f516b39a-1c1c-42bf-b3f4-492ec2e56897")
                .expiresIn(599)
                .scope(Set.of("TWO_FACTOR"))
                .kdnr("1188651895")
                .bpid(10320404L)
                .kontaktId(7221230038L)
                .build();
                
        tokens.put(token.getAccessToken(), token);
        return token;
    }
    
    @Override
    public Optional<SessionInfo> getSessionStatus(String accessToken) {
        // Simular respuesta del paso 2.2
        if (!tokens.containsKey(accessToken)) {
            return Optional.empty();
        }
        
        SessionInfo session = SessionInfo.builder()
                .identifier("198C3E54EA634BC7B0F4B73C7FFE75A0")
                .sessionTanActive(false)
                .activated2FA(false)
                .build();
                
        sessions.put(session.getIdentifier(), session);
        return Optional.of(session);
    }
    
    @Override
    public SessionInfo validateSessionTan(String accessToken, String sessionIdentifier) {
        // Simular respuesta del paso 2.3
        if (!tokens.containsKey(accessToken)) {
            throw new InvalidUserDataException("Invalid access token");
        }
        
        SessionInfo session = sessions.get(sessionIdentifier);
        if (session == null) {
            throw new InvalidUserDataException("Invalid session identifier");
        }
        
        // Crear nueva sesión validada
        SessionInfo validatedSession = SessionInfo.builder()
                .identifier(sessionIdentifier)
                .sessionTanActive(true)
                .activated2FA(true)
                .build();
                
        sessions.put(sessionIdentifier, validatedSession);
        return validatedSession;
    }
    
    @Override
    public SessionInfo activateSessionTan(String accessToken, String sessionIdentifier, 
                                        String tanCode, String authenticationId) {
        // Simular respuesta del paso 2.4
        if (!tokens.containsKey(accessToken)) {
            throw new InvalidUserDataException("Invalid access token");
        }
        
        // Validar código TAN (en realidad sería validado por el servidor)
        if (!"000000".equals(tanCode)) {
            throw new InvalidUserDataException("Invalid TAN code");
        }
        
        SessionInfo session = sessions.get(sessionIdentifier);
        if (session == null) {
            throw new InvalidUserDataException("Invalid session identifier");
        }
        
        // Activar sesión
        session.activateSessionTan();
        session.activate2FA();
        
        return session;
    }
    
    @Override
    public AccessToken getSecondaryToken(String clientId, String clientSecret, 
                                       String primaryToken) {
        // Simular respuesta del paso 2.5
        if (!TEST_CLIENT_ID.equals(clientId) || 
            !TEST_CLIENT_SECRET.equals(clientSecret)) {
            throw new InvalidUserDataException("Invalid client credentials");
        }
        
        AccessToken primaryAccessToken = tokens.get(primaryToken);
        if (primaryAccessToken == null || !primaryAccessToken.isValidForSecondaryFlow()) {
            throw new InvalidUserDataException("Invalid or expired primary token");
        }
        
        // Crear token secundario con permisos completos
        AccessToken secondaryToken = AccessToken.builder()
                .accessToken("b4d47dfe-4eb3-4a2e-ae7b-ca9b0a5200bc")
                .tokenType("bearer")
                .refreshToken("25ae8a23-eb2c-4567-a7f4-13c1ac2edfaa")
                .expiresIn(599)
                .scope(Set.of("BANKING_RW", "BROKERAGE_RW", "MESSAGES_RO", "REPORTS_RO", "SESSION_RW"))
                .kdnr("1188651895")
                .bpid(10320404L)
                .kontaktId(7221230038L)
                .build();
                
        tokens.put(secondaryToken.getAccessToken(), secondaryToken);
        return secondaryToken;
    }
    
    @Override
    public Optional<AccessToken> findTokenById(TokenId tokenId) {
        return tokens.values().stream()
                .filter(token -> token.getId().equals(tokenId))
                .findFirst();
    }
    
    @Override
    public Optional<SessionInfo> findSessionById(SessionId sessionId) {
        return sessions.values().stream()
                .filter(session -> session.getSessionId().equals(sessionId))
                .findFirst();
    }
    
    // Métodos auxiliares para testing
    public void clearAll() {
        tokens.clear();
        sessions.clear();
    }
    
    public int getTokenCount() {
        return tokens.size();
    }
    
    public int getSessionCount() {
        return sessions.size();
    }
}