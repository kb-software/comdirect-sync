package de.comdirect.sync.application.usecases.auth;

import de.comdirect.sync.domain.entities.SessionInfo;
import de.comdirect.sync.domain.repositories.ComdirectAuthRepository;
import de.comdirect.sync.domain.exceptions.InvalidUserDataException;

/**
 * Caso de uso para activar una sesión TAN con código 2FA
 * Corresponde al paso 2.4 del flujo: Aktivierung einer Session-TAN
 */
public class ActivateSessionUseCase {
    
    private final ComdirectAuthRepository authRepository;
    
    public ActivateSessionUseCase(ComdirectAuthRepository authRepository) {
        this.authRepository = authRepository;
    }
    
    public ActivationResult execute(ActivationRequest request) {
        validateRequest(request);
        
        try {
            SessionInfo activatedSession = authRepository.activateSessionTan(
                request.getAccessToken(),
                request.getSessionIdentifier(),
                request.getTanCode(),
                request.getAuthenticationId()
            );
            
            if (activatedSession.isReadyForSecondaryFlow()) {
                return ActivationResult.success(activatedSession, 
                    "Session activated successfully. Ready for secondary token flow.");
            } else {
                return ActivationResult.failure("Session activation incomplete");
            }
            
        } catch (InvalidUserDataException e) {
            return ActivationResult.failure("Activation failed: " + e.getMessage());
        } catch (Exception e) {
            return ActivationResult.failure("Unexpected error during activation: " + e.getMessage());
        }
    }
    
    private void validateRequest(ActivationRequest request) {
        if (request == null) {
            throw new InvalidUserDataException("Activation request cannot be null");
        }
        
        if (request.getAccessToken() == null || request.getAccessToken().trim().isEmpty()) {
            throw new InvalidUserDataException("Access token is required");
        }
        
        if (request.getSessionIdentifier() == null || request.getSessionIdentifier().trim().isEmpty()) {
            throw new InvalidUserDataException("Session identifier is required");
        }
        
        if (request.getTanCode() == null || request.getTanCode().trim().isEmpty()) {
            throw new InvalidUserDataException("TAN code is required");
        }
        
        // Validar formato del código TAN (generalmente 6 dígitos)
        if (!request.getTanCode().matches("\\d{6}")) {
            throw new InvalidUserDataException("TAN code must be 6 digits");
        }
    }
    
    public static class ActivationRequest {
        private final String accessToken;
        private final String sessionIdentifier;
        private final String tanCode;
        private final String authenticationId;
        
        public ActivationRequest(String accessToken, String sessionIdentifier, 
                               String tanCode, String authenticationId) {
            this.accessToken = accessToken;
            this.sessionIdentifier = sessionIdentifier;
            this.tanCode = tanCode;
            this.authenticationId = authenticationId;
        }
        
        public String getAccessToken() { return accessToken; }
        public String getSessionIdentifier() { return sessionIdentifier; }
        public String getTanCode() { return tanCode; }
        public String getAuthenticationId() { return authenticationId; }
    }
    
    public static class ActivationResult {
        private final boolean success;
        private final SessionInfo session;
        private final String message;
        
        private ActivationResult(boolean success, SessionInfo session, String message) {
            this.success = success;
            this.session = session;
            this.message = message;
        }
        
        public static ActivationResult success(SessionInfo session, String message) {
            return new ActivationResult(true, session, message);
        }
        
        public static ActivationResult failure(String message) {
            return new ActivationResult(false, null, message);
        }
        
        public boolean isSuccess() { return success; }
        public SessionInfo getSession() { return session; }
        public String getMessage() { return message; }
    }
}