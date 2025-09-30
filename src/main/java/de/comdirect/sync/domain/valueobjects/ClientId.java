package de.comdirect.sync.domain.valueobjects;

import java.util.Objects;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/**
 * Value Object que representa un identificador de cliente de Comdirect.
 * Siguiendo los principios de DDD, es inmutable y autovalidante.
 */
public final class ClientId {
    
    @NotBlank(message = "El ID de cliente no puede estar vacío")
    @Pattern(regexp = "^[A-Za-z0-9-_]{8,32}$", message = "El ID de cliente debe tener entre 8 y 32 caracteres alfanuméricos")
    private final String value;
    
    private ClientId(String value) {
        this.value = value;
    }
    
    /**
     * Factory method para crear un ClientId validado
     */
    public static ClientId of(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("El ID de cliente no puede estar vacío");
        }
        
        String trimmedValue = value.trim();
        if (!trimmedValue.matches("^[A-Za-z0-9-_]{8,32}$")) {
            throw new IllegalArgumentException("El ID de cliente debe tener entre 8 y 32 caracteres alfanuméricos");
        }
        
        return new ClientId(trimmedValue);
    }
    
    public String getValue() {
        return value;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        ClientId clientId = (ClientId) obj;
        return Objects.equals(value, clientId.value);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(value);
    }
    
    @Override
    public String toString() {
        return "ClientId{" + value + "}";
    }
}