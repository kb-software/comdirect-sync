package de.comdirect.sync.domain.valueobjects;

import de.comdirect.sync.domain.exceptions.InvalidUserDataException;

import java.util.Objects;

/**
 * Value Object que representa un identificador único de token OAuth2
 */
public class TokenId {
    private final String value;

    private TokenId(String value) {
        this.value = value;
    }

    public static TokenId of(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new InvalidUserDataException("Token ID cannot be null or empty");
        }
        
        String trimmedValue = value.trim();
        if (trimmedValue.length() < 8) {
            throw new InvalidUserDataException("Token ID must be at least 8 characters long");
        }
        
        return new TokenId(trimmedValue);
    }

    public String getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TokenId tokenId = (TokenId) o;
        return Objects.equals(value, tokenId.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return "TokenId{value='" + value + "'}";
    }
}