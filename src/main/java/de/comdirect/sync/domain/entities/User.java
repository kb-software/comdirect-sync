package de.comdirect.sync.domain.entities;

import de.comdirect.sync.domain.valueobjects.ClientId;
import de.comdirect.sync.domain.valueobjects.UserId;
import de.comdirect.sync.domain.exceptions.InvalidUserDataException;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Entidad de dominio User siguiendo principios de DDD.
 * Representa un usuario del sistema con sus reglas de negocio.
 */
public class User {
    
    @NotNull(message = "El ID de usuario es obligatorio")
    private final UserId userId;
    
    @NotNull(message = "El ID de cliente es obligatorio") 
    private final ClientId clientId;
    
    @NotBlank(message = "El nombre de usuario no puede estar vacío")
    private String username;
    
    @Email(message = "El email debe tener un formato válido")
    @NotBlank(message = "El email es obligatorio")
    private String email;
    
    private boolean isActive;
    
    private final LocalDateTime createdAt;
    
    private LocalDateTime lastLoginAt;
    
    // Constructor privado para forzar el uso del Builder
    private User(Builder builder) {
        this.userId = builder.userId;
        this.clientId = builder.clientId;
        this.username = builder.username;
        this.email = builder.email;
        this.isActive = builder.isActive;
        this.createdAt = builder.createdAt != null ? builder.createdAt : LocalDateTime.now();
        this.lastLoginAt = builder.lastLoginAt;
        
        validate();
    }
    
    /**
     * Valida las reglas de negocio de la entidad
     */
    private void validate() {
        if (userId == null) {
            throw new InvalidUserDataException("El ID de usuario es obligatorio");
        }
        if (clientId == null) {
            throw new InvalidUserDataException("El ID de cliente es obligatorio");
        }
        if (username == null || username.trim().isEmpty()) {
            throw new InvalidUserDataException("El nombre de usuario no puede estar vacío");
        }
        if (email == null || email.trim().isEmpty()) {
            throw new InvalidUserDataException("El email es obligatorio");
        }
        if (!isValidEmail(email)) {
            throw new InvalidUserDataException("El email debe tener un formato válido");
        }
    }
    
    private boolean isValidEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        String trimmed = email.trim();
        // Validación más estricta del email
        return trimmed.contains("@") && 
               trimmed.contains(".") && 
               trimmed.indexOf("@") > 0 && 
               trimmed.indexOf("@") < trimmed.lastIndexOf(".") &&
               trimmed.lastIndexOf(".") < trimmed.length() - 1 &&
               !trimmed.startsWith("@") &&
               !trimmed.endsWith("@");
    }
    
    /**
     * Método de dominio para actualizar el último acceso
     */
    public void updateLastLogin() {
        this.lastLoginAt = LocalDateTime.now();
    }
    
    /**
     * Método de dominio para activar/desactivar usuario
     */
    public void setActive(boolean active) {
        this.isActive = active;
    }
    
    /**
     * Método de dominio para cambiar email
     */
    public void changeEmail(String newEmail) {
        if (newEmail == null || newEmail.trim().isEmpty()) {
            throw new InvalidUserDataException("El email es obligatorio");
        }
        if (!isValidEmail(newEmail)) {
            throw new InvalidUserDataException("El email debe tener un formato válido");
        }
        this.email = newEmail.trim();
    }
    
    /**
     * Método de dominio para cambiar nombre de usuario
     */
    public void changeUsername(String newUsername) {
        if (newUsername == null || newUsername.trim().isEmpty()) {
            throw new InvalidUserDataException("El nombre de usuario no puede estar vacío");
        }
        this.username = newUsername.trim();
    }
    
    // Getters
    public UserId getUserId() { return userId; }
    public ClientId getClientId() { return clientId; }
    public String getUsername() { return username; }
    public String getEmail() { return email; }
    public boolean isActive() { return isActive; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getLastLoginAt() { return lastLoginAt; }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        User user = (User) obj;
        return Objects.equals(userId, user.userId);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(userId);
    }
    
    @Override
    public String toString() {
        return "User{" +
                "userId=" + userId +
                ", clientId=" + clientId +
                ", username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", isActive=" + isActive +
                ", createdAt=" + createdAt +
                ", lastLoginAt=" + lastLoginAt +
                '}';
    }
    
    /**
     * Builder para crear instancias de User de forma fluida
     */
    public static class Builder {
        private UserId userId;
        private ClientId clientId;
        private String username;
        private String email;
        private boolean isActive = true;
        private LocalDateTime createdAt;
        private LocalDateTime lastLoginAt;
        
        public Builder withUserId(UserId userId) {
            this.userId = userId;
            return this;
        }
        
        public Builder withClientId(ClientId clientId) {
            this.clientId = clientId;
            return this;
        }
        
        public Builder withUsername(String username) {
            this.username = username;
            return this;
        }
        
        public Builder withEmail(String email) {
            this.email = email;
            return this;
        }
        
        public Builder withActive(boolean isActive) {
            this.isActive = isActive;
            return this;
        }
        
        public Builder withCreatedAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }
        
        public Builder withLastLoginAt(LocalDateTime lastLoginAt) {
            this.lastLoginAt = lastLoginAt;
            return this;
        }
        
        public User build() {
            return new User(this);
        }
    }
}