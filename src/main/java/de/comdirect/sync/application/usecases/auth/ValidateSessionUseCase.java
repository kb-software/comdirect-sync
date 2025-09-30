package de.comdirect.sync.application.usecases.auth;

import de.comdirect.sync.domain.entities.SessionInfo;
import de.comdirect.sync.domain.repositories.ComdirectAuthRepository;
import de.comdirect.sync.domain.exceptions.InvalidUserDataException;

import java.util.Optional;

/**
 * Caso de uso para validar una sesión TAN
 * Corresponde al paso 2.3 del flujo: Anlage Validierung einer Session-TAN
 */
public class ValidateSessionUseCase {
    
    private final ComdirectAuthRepository authRepository;
    
    public ValidateSessionUseCase(ComdirectAuthRepository authRepository) {
        this.authRepository = authRepository;
    }
    
    public ValidationResult execute(ValidationRequest request) {
        validateRequest(request);
        
        try {
            // Primero obtener el estado de la sesión
            Optional<SessionInfo> currentSession = authRepository.getSessionStatus(request.getAccessToken());
            
            if (currentSession.isEmpty()) {
                return ValidationResult.failure("No active session found");
            }
            
            SessionInfo session = currentSession.get();
            
            // Si ya está validada, no necesita validación adicional
            if (session.isReadyForSecondaryFlow()) {
                return ValidationResult.success(session, "Session already validated");
            }
            
            // Validar la sesión TAN
            SessionInfo validatedSession = authRepository.validateSessionTan(
                request.getAccessToken(),
                session.getIdentifier()
            );
            
            return ValidationResult.success(validatedSession, "Session validated successfully. Ready for 2FA activation.");
            
        } catch (InvalidUserDataException e) {
            return ValidationResult.failure("Validation failed: " + e.getMessage());
        } catch (Exception e) {
            return ValidationResult.failure("Unexpected error during validation: " + e.getMessage());
        }
    }
    
    private void validateRequest(ValidationRequest request) {
        if (request == null) {
            throw new InvalidUserDataException("Validation request cannot be null");
        }
        
        if (request.getAccessToken() == null || request.getAccessToken().trim().isEmpty()) {
            throw new InvalidUserDataException("Access token is required");
        }
    }
    
    public static class ValidationRequest {
        private final String accessToken;
        
        public ValidationRequest(String accessToken) {
            this.accessToken = accessToken;
        }
        
        public String getAccessToken() { return accessToken; }
    }
    
    public static class ValidationResult {
        private final boolean success;
        private final SessionInfo session;
        private final String message;
        
        private ValidationResult(boolean success, SessionInfo session, String message) {
            this.success = success;
            this.session = session;
            this.message = message;
        }
        
        public static ValidationResult success(SessionInfo session, String message) {
            return new ValidationResult(true, session, message);
        }
        
        public static ValidationResult failure(String message) {
            return new ValidationResult(false, null, message);
        }
        
        public boolean isSuccess() { return success; }
        public SessionInfo getSession() { return session; }
        public String getMessage() { return message; }
    }
}