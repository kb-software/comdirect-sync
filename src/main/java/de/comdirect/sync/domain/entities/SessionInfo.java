package de.comdirect.sync.domain.entities;

import de.comdirect.sync.domain.valueobjects.SessionId;
import de.comdirect.sync.domain.exceptions.InvalidUserDataException;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Entidad que representa información de sesión para 2FA de Comdirect
 */
public class SessionInfo {
    private final SessionId sessionId;
    private final String identifier;
    private boolean sessionTanActive;
    private boolean activated2FA;
    private final LocalDateTime createdAt;
    private LocalDateTime lastValidatedAt;

    private SessionInfo(Builder builder) {
        this.sessionId = SessionId.of(builder.identifier);
        this.identifier = builder.identifier;
        this.sessionTanActive = builder.sessionTanActive;
        this.activated2FA = builder.activated2FA;
        this.createdAt = LocalDateTime.now();
    }

    public static Builder builder() {
        return new Builder();
    }

    public void activateSessionTan() {
        this.sessionTanActive = true;
        this.lastValidatedAt = LocalDateTime.now();
    }

    public void activate2FA() {
        this.activated2FA = true;
        this.lastValidatedAt = LocalDateTime.now();
    }

    public void validate() {
        this.sessionTanActive = true;
        this.activated2FA = true;
        this.lastValidatedAt = LocalDateTime.now();
    }

    public boolean isReadyForSecondaryFlow() {
        return sessionTanActive && activated2FA;
    }

    public boolean needsValidation() {
        return !sessionTanActive || !activated2FA;
    }

    // Getters
    public SessionId getSessionId() { return sessionId; }
    public String getIdentifier() { return identifier; }
    public boolean isSessionTanActive() { return sessionTanActive; }
    public boolean isActivated2FA() { return activated2FA; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getLastValidatedAt() { return lastValidatedAt; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SessionInfo that = (SessionInfo) o;
        return Objects.equals(sessionId, that.sessionId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(sessionId);
    }

    @Override
    public String toString() {
        return "SessionInfo{" +
                "sessionId=" + sessionId +
                ", identifier='" + identifier + '\'' +
                ", sessionTanActive=" + sessionTanActive +
                ", activated2FA=" + activated2FA +
                ", readyForSecondaryFlow=" + isReadyForSecondaryFlow() +
                '}';
    }

    public static class Builder {
        private String identifier;
        private boolean sessionTanActive = false;
        private boolean activated2FA = false;

        public Builder identifier(String identifier) {
            if (identifier == null || identifier.trim().isEmpty()) {
                throw new InvalidUserDataException("Session identifier cannot be null or empty");
            }
            this.identifier = identifier.trim();
            return this;
        }

        public Builder sessionTanActive(boolean sessionTanActive) {
            this.sessionTanActive = sessionTanActive;
            return this;
        }

        public Builder activated2FA(boolean activated2FA) {
            this.activated2FA = activated2FA;
            return this;
        }

        public SessionInfo build() {
            if (identifier == null) {
                throw new InvalidUserDataException("Session identifier is required");
            }
            return new SessionInfo(this);
        }
    }
}