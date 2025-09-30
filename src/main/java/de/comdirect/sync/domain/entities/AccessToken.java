package de.comdirect.sync.domain.entities;

import de.comdirect.sync.domain.valueobjects.TokenId;
import de.comdirect.sync.domain.exceptions.InvalidUserDataException;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Set;

/**
 * Entidad que representa un token de acceso OAuth2 de Comdirect
 */
public class AccessToken {
    private final TokenId id;
    private final String accessToken;
    private final String tokenType;
    private final String refreshToken;
    private final int expiresIn;
    private final Set<String> scope;
    private final String kdnr;  // Kundennummer
    private final long bpid;    // Business Partner ID
    private final long kontaktId;
    private final LocalDateTime createdAt;
    private final LocalDateTime expiresAt;

    private AccessToken(Builder builder) {
        this.id = TokenId.of(builder.accessToken);
        this.accessToken = builder.accessToken;
        this.tokenType = builder.tokenType;
        this.refreshToken = builder.refreshToken;
        this.expiresIn = builder.expiresIn;
        this.scope = Set.copyOf(builder.scope);
        this.kdnr = builder.kdnr;
        this.bpid = builder.bpid;
        this.kontaktId = builder.kontaktId;
        this.createdAt = LocalDateTime.now();
        this.expiresAt = createdAt.plusSeconds(expiresIn);
    }

    public static Builder builder() {
        return new Builder();
    }

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }

    public boolean isValidForSecondaryFlow() {
        return scope.contains("TWO_FACTOR") && !isExpired();
    }

    public boolean isValidForApiCalls() {
        return (scope.contains("BANKING_RW") || 
                scope.contains("BROKERAGE_RW") ||
                scope.contains("MESSAGES_RO")) && !isExpired();
    }

    // Getters
    public TokenId getId() { return id; }
    public String getAccessToken() { return accessToken; }
    public String getTokenType() { return tokenType; }
    public String getRefreshToken() { return refreshToken; }
    public int getExpiresIn() { return expiresIn; }
    public Set<String> getScope() { return Set.copyOf(scope); }
    public String getKdnr() { return kdnr; }
    public long getBpid() { return bpid; }
    public long getKontaktId() { return kontaktId; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getExpiresAt() { return expiresAt; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AccessToken that = (AccessToken) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "AccessToken{" +
                "id=" + id +
                ", tokenType='" + tokenType + '\'' +
                ", scope=" + scope +
                ", kdnr='" + kdnr + '\'' +
                ", expiresAt=" + expiresAt +
                ", expired=" + isExpired() +
                '}';
    }

    public static class Builder {
        private String accessToken;
        private String tokenType;
        private String refreshToken;
        private int expiresIn;
        private Set<String> scope;
        private String kdnr;
        private long bpid;
        private long kontaktId;

        public Builder accessToken(String accessToken) {
            if (accessToken == null || accessToken.trim().isEmpty()) {
                throw new InvalidUserDataException("Access token cannot be null or empty");
            }
            this.accessToken = accessToken.trim();
            return this;
        }

        public Builder tokenType(String tokenType) {
            if (tokenType == null || tokenType.trim().isEmpty()) {
                throw new InvalidUserDataException("Token type cannot be null or empty");
            }
            this.tokenType = tokenType.trim();
            return this;
        }

        public Builder refreshToken(String refreshToken) {
            this.refreshToken = refreshToken;
            return this;
        }

        public Builder expiresIn(int expiresIn) {
            if (expiresIn <= 0) {
                throw new InvalidUserDataException("Expires in must be positive");
            }
            this.expiresIn = expiresIn;
            return this;
        }

        public Builder scope(Set<String> scope) {
            if (scope == null || scope.isEmpty()) {
                throw new InvalidUserDataException("Scope cannot be null or empty");
            }
            this.scope = scope;
            return this;
        }

        public Builder kdnr(String kdnr) {
            if (kdnr == null || kdnr.trim().isEmpty()) {
                throw new InvalidUserDataException("KDNR cannot be null or empty");
            }
            this.kdnr = kdnr.trim();
            return this;
        }

        public Builder bpid(long bpid) {
            if (bpid <= 0) {
                throw new InvalidUserDataException("BPID must be positive");
            }
            this.bpid = bpid;
            return this;
        }

        public Builder kontaktId(long kontaktId) {
            if (kontaktId <= 0) {
                throw new InvalidUserDataException("Kontakt ID must be positive");
            }
            this.kontaktId = kontaktId;
            return this;
        }

        public AccessToken build() {
            if (accessToken == null) {
                throw new InvalidUserDataException("Access token is required");
            }
            if (tokenType == null) {
                throw new InvalidUserDataException("Token type is required");
            }
            if (scope == null) {
                throw new InvalidUserDataException("Scope is required");
            }
            if (kdnr == null) {
                throw new InvalidUserDataException("KDNR is required");
            }
            return new AccessToken(this);
        }
    }
}