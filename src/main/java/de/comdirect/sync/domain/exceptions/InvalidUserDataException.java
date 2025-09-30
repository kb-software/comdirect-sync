package de.comdirect.sync.domain.exceptions;

/**
 * Excepción personalizada para datos de usuario inválidos en el dominio.
 * Siguiendo los principios de DDD, representa reglas de negocio violadas.
 */
public class InvalidUserDataException extends RuntimeException {
    
    public InvalidUserDataException(String message) {
        super(message);
    }
    
    public InvalidUserDataException(String message, Throwable cause) {
        super(message, cause);
    }
}