package de.comdirect.sync.domain.valueobjects;

import java.util.Objects;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Value Object que representa un identificador único de usuario.
 * Siguiendo los principios de DDD, es inmutable y autovalidante.
 */
public final class UserId {
    
    @NotBlank(message = "El ID de usuario no puede estar vacío")
    @Size(min = 3, max = 50, message = "El ID de usuario debe tener entre 3 y 50 caracteres")
    private final String value;
    
    private UserId(String value) {
        this.value = value;
    }
    
    /**
     * Factory method para crear un UserId validado
     */
    public static UserId of(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("El ID de usuario no puede estar vacío");
        }
        
        String trimmedValue = value.trim();
        if (trimmedValue.length() < 3 || trimmedValue.length() > 50) {
            throw new IllegalArgumentException("El ID de usuario debe tener entre 3 y 50 caracteres");
        }
        
        return new UserId(trimmedValue);
    }
    
    public String getValue() {
        return value;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        UserId userId = (UserId) obj;
        return Objects.equals(value, userId.value);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(value);
    }
    
    @Override
    public String toString() {
        return "UserId{" + value + "}";
    }
}