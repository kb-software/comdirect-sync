package de.comdirect.sync.application.usecases.auth;

import de.comdirect.sync.domain.entities.AccessToken;
import de.comdirect.sync.domain.repositories.ComdirectAuthRepository;
import de.comdirect.sync.domain.exceptions.InvalidUserDataException;

/**
 * Caso de uso para autenticar un usuario con Comdirect OAuth2
 * Corresponde al paso 2.1 del flujo: OAuth2 Resource Owner Password Credentials Flow
 */
public class AuthenticateUserUseCase {
    
    private final ComdirectAuthRepository authRepository;
    
    public AuthenticateUserUseCase(ComdirectAuthRepository authRepository) {
        this.authRepository = authRepository;
    }
    
    public AuthResult execute(AuthRequest request) {
        validateRequest(request);
        
        try {
            AccessToken token = authRepository.authenticateUser(
                request.getClientId(),
                request.getClientSecret(),
                request.getUsername(),
                request.getPassword()
            );
            
            return AuthResult.success(token, "User authenticated successfully");
            
        } catch (InvalidUserDataException e) {
            return AuthResult.failure("Authentication failed: " + e.getMessage());
        } catch (Exception e) {
            return AuthResult.failure("Unexpected error during authentication: " + e.getMessage());
        }
    }
    
    private void validateRequest(AuthRequest request) {
        if (request == null) {
            throw new InvalidUserDataException("Authentication request cannot be null");
        }
        
        if (request.getClientId() == null || request.getClientId().trim().isEmpty()) {
            throw new InvalidUserDataException("Client ID is required");
        }
        
        if (request.getClientSecret() == null || request.getClientSecret().trim().isEmpty()) {
            throw new InvalidUserDataException("Client secret is required");
        }
        
        if (request.getUsername() == null || request.getUsername().trim().isEmpty()) {
            throw new InvalidUserDataException("Username (Zugangsnummer) is required");
        }
        
        if (request.getPassword() == null || request.getPassword().trim().isEmpty()) {
            throw new InvalidUserDataException("Password (PIN) is required");
        }
    }
    
    public static class AuthRequest {
        private final String clientId;
        private final String clientSecret;
        private final String username;  // Zugangsnummer
        private final String password;  // PIN
        
        public AuthRequest(String clientId, String clientSecret, String username, String password) {
            this.clientId = clientId;
            this.clientSecret = clientSecret;
            this.username = username;
            this.password = password;
        }
        
        public String getClientId() { return clientId; }
        public String getClientSecret() { return clientSecret; }
        public String getUsername() { return username; }
        public String getPassword() { return password; }
    }
    
    public static class AuthResult {
        private final boolean success;
        private final AccessToken token;
        private final String message;
        
        private AuthResult(boolean success, AccessToken token, String message) {
            this.success = success;
            this.token = token;
            this.message = message;
        }
        
        public static AuthResult success(AccessToken token, String message) {
            return new AuthResult(true, token, message);
        }
        
        public static AuthResult failure(String message) {
            return new AuthResult(false, null, message);
        }
        
        public boolean isSuccess() { return success; }
        public AccessToken getToken() { return token; }
        public String getMessage() { return message; }
    }
}