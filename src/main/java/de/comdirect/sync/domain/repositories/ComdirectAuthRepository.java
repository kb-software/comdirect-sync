package de.comdirect.sync.domain.repositories;

import de.comdirect.sync.domain.entities.AccessToken;
import de.comdirect.sync.domain.entities.SessionInfo;
import de.comdirect.sync.domain.valueobjects.TokenId;
import de.comdirect.sync.domain.valueobjects.SessionId;

import java.util.Optional;

/**
 * Repositorio para manejar la autenticación OAuth2 con Comdirect
 */
public interface ComdirectAuthRepository {
    
    /**
     * Autentica un usuario con sus credenciales y obtiene el primer token
     * Corresponde al paso 2.1 del flujo OAuth2
     */
    AccessToken authenticateUser(String clientId, String clientSecret, 
                               String username, String password);
    
    /**
     * Obtiene el estado de la sesión actual
     * Corresponde al paso 2.2 del flujo OAuth2
     */
    Optional<SessionInfo> getSessionStatus(String accessToken);
    
    /**
     * Valida una sesión TAN
     * Corresponde al paso 2.3 del flujo OAuth2
     */
    SessionInfo validateSessionTan(String accessToken, String sessionIdentifier);
    
    /**
     * Activa una sesión TAN con el código 2FA
     * Corresponde al paso 2.4 del flujo OAuth2
     */
    SessionInfo activateSessionTan(String accessToken, String sessionIdentifier, 
                                 String tanCode, String authenticationId);
    
    /**
     * Obtiene el token secundario para acceso completo a la API
     * Corresponde al paso 2.5 del flujo OAuth2
     */
    AccessToken getSecondaryToken(String clientId, String clientSecret, 
                                String primaryToken);
    
    /**
     * Busca un token por su ID
     */
    Optional<AccessToken> findTokenById(TokenId tokenId);
    
    /**
     * Busca una sesión por su ID
     */
    Optional<SessionInfo> findSessionById(SessionId sessionId);
}