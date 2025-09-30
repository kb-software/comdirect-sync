package de.comdirect.sync.domain.valueobjects;

import de.comdirect.sync.domain.exceptions.InvalidUserDataException;

import java.util.Objects;

/**
 * Value Object que representa un identificador único de documento
 */
public class DocumentId {
    private final String value;

    private DocumentId(String value) {
        this.value = value;
    }

    public static DocumentId of(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new InvalidUserDataException("Document ID cannot be null or empty");
        }
        
        String trimmedValue = value.trim();
        if (trimmedValue.length() < 8) {
            throw new InvalidUserDataException("Document ID must be at least 8 characters long");
        }
        
        return new DocumentId(trimmedValue);
    }

    public String getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DocumentId that = (DocumentId) o;
        return Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return "DocumentId{value='" + value + "'}";
    }
}