package de.comdirect.sync.application.usecases.auth;

import de.comdirect.sync.domain.entities.AccessToken;
import de.comdirect.sync.domain.repositories.ComdirectAuthRepository;
import de.comdirect.sync.domain.exceptions.InvalidUserDataException;

/**
 * Caso de uso para obtener el token secundario con permisos completos
 * Corresponde al paso 2.5 del flujo: OAuth2 CD Secondary-Flow
 */
public class GetSecondaryTokenUseCase {
    
    private final ComdirectAuthRepository authRepository;
    
    public GetSecondaryTokenUseCase(ComdirectAuthRepository authRepository) {
        this.authRepository = authRepository;
    }
    
    public SecondaryTokenResult execute(SecondaryTokenRequest request) {
        validateRequest(request);
        
        try {
            AccessToken secondaryToken = authRepository.getSecondaryToken(
                request.getClientId(),
                request.getClientSecret(),
                request.getPrimaryToken()
            );
            
            if (secondaryToken.isValidForApiCalls()) {
                return SecondaryTokenResult.success(secondaryToken, 
                    "Secondary token obtained successfully. Full API access granted.");
            } else {
                return SecondaryTokenResult.failure("Secondary token is invalid or has insufficient permissions");
            }
            
        } catch (InvalidUserDataException e) {
            return SecondaryTokenResult.failure("Secondary token request failed: " + e.getMessage());
        } catch (Exception e) {
            return SecondaryTokenResult.failure("Unexpected error during secondary token request: " + e.getMessage());
        }
    }
    
    private void validateRequest(SecondaryTokenRequest request) {
        if (request == null) {
            throw new InvalidUserDataException("Secondary token request cannot be null");
        }
        
        if (request.getClientId() == null || request.getClientId().trim().isEmpty()) {
            throw new InvalidUserDataException("Client ID is required");
        }
        
        if (request.getClientSecret() == null || request.getClientSecret().trim().isEmpty()) {
            throw new InvalidUserDataException("Client secret is required");
        }
        
        if (request.getPrimaryToken() == null || request.getPrimaryToken().trim().isEmpty()) {
            throw new InvalidUserDataException("Primary token is required");
        }
    }
    
    public static class SecondaryTokenRequest {
        private final String clientId;
        private final String clientSecret;
        private final String primaryToken;
        
        public SecondaryTokenRequest(String clientId, String clientSecret, String primaryToken) {
            this.clientId = clientId;
            this.clientSecret = clientSecret;
            this.primaryToken = primaryToken;
        }
        
        public String getClientId() { return clientId; }
        public String getClientSecret() { return clientSecret; }
        public String getPrimaryToken() { return primaryToken; }
    }
    
    public static class SecondaryTokenResult {
        private final boolean success;
        private final AccessToken token;
        private final String message;
        
        private SecondaryTokenResult(boolean success, AccessToken token, String message) {
            this.success = success;
            this.token = token;
            this.message = message;
        }
        
        public static SecondaryTokenResult success(AccessToken token, String message) {
            return new SecondaryTokenResult(true, token, message);
        }
        
        public static SecondaryTokenResult failure(String message) {
            return new SecondaryTokenResult(false, null, message);
        }
        
        public boolean isSuccess() { return success; }
        public AccessToken getToken() { return token; }
        public String getMessage() { return message; }
    }
}