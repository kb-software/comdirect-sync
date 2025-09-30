package de.comdirect.sync.domain.valueobjects;

import de.comdirect.sync.domain.exceptions.InvalidUserDataException;

import java.util.Objects;

/**
 * Value Object que representa un identificador único de sesión
 */
public class SessionId {
    private final String value;

    private SessionId(String value) {
        this.value = value;
    }

    public static SessionId of(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new InvalidUserDataException("Session ID cannot be null or empty");
        }
        
        String trimmedValue = value.trim();
        if (trimmedValue.length() < 16) {
            throw new InvalidUserDataException("Session ID must be at least 16 characters long");
        }
        
        return new SessionId(trimmedValue);
    }

    public String getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SessionId sessionId = (SessionId) o;
        return Objects.equals(value, sessionId.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return "SessionId{value='" + value + "'}";
    }
}