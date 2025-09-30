package domain.entities;

import domain.valueobjects.ClientId;
import domain.valueobjects.UserId;
import domain.exceptions.InvalidUserDataException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitarios para la entidad User del dominio
 * Valida las reglas de negocio y comportamientos de la entidad
 */
@DisplayName("User Entity Tests")
class UserTest {

    private UserId validUserId;
    private ClientId validClientId;
    private User.Builder validUserBuilder;

    @BeforeEach
    void setUp() {
        validUserId = UserId.of("user123");
        validClientId = ClientId.of("CLIENT123");
        validUserBuilder = new User.Builder()
                .withUserId(validUserId)
                .withClientId(validClientId)
                .withUsername("john_doe")
                .withEmail("john@example.com");
    }

    @Nested
    @DisplayName("User Creation Tests")
    class UserCreationTests {

        @Test
        @DisplayName("Should create user with valid data")
        void shouldCreateUserWithValidData() {
            // Act
            User user = validUserBuilder.build();

            // Assert
            assertNotNull(user);
            assertEquals("john_doe", user.getUsername());
            assertEquals("john@example.com", user.getEmail());
            assertEquals(validUserId, user.getUserId());
            assertEquals(validClientId, user.getClientId());
            assertTrue(user.isActive());
            assertNotNull(user.getCreatedAt());
        }

        @Test
        @DisplayName("Should throw exception when userId is null")
        void shouldThrowExceptionWhenUserIdIsNull() {
            // Arrange & Act & Assert
            InvalidUserDataException exception = assertThrows(InvalidUserDataException.class, () -> {
                validUserBuilder.withUserId(null).build();
            });
            assertEquals("El ID de usuario es obligatorio", exception.getMessage());
        }

        @Test
        @DisplayName("Should throw exception when clientId is null")
        void shouldThrowExceptionWhenClientIdIsNull() {
            // Arrange & Act & Assert
            InvalidUserDataException exception = assertThrows(InvalidUserDataException.class, () -> {
                validUserBuilder.withClientId(null).build();
            });
            assertEquals("El ID de cliente es obligatorio", exception.getMessage());
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {"   ", "\t", "\n"})
        @DisplayName("Should throw exception when username is null or empty")
        void shouldThrowExceptionWhenUsernameIsNullOrEmpty(String username) {
            // Arrange & Act & Assert
            InvalidUserDataException exception = assertThrows(InvalidUserDataException.class, () -> {
                validUserBuilder.withUsername(username).build();
            });
            assertEquals("El nombre de usuario no puede estar vacío", exception.getMessage());
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {"   ", "\t", "\n", "invalid-email", "test@", "@domain.com"})
        @DisplayName("Should throw exception when email is invalid")
        void shouldThrowExceptionWhenEmailIsInvalid(String email) {
            // Arrange & Act & Assert
            InvalidUserDataException exception = assertThrows(InvalidUserDataException.class, () -> {
                validUserBuilder.withEmail(email).build();
            });
            assertTrue(exception.getMessage().contains("email"));
        }
    }

    @Nested
    @DisplayName("User Behavior Tests")
    class UserBehaviorTests {

        private User user;

        @BeforeEach
        void setUp() {
            user = validUserBuilder.build();
        }

        @Test
        @DisplayName("Should update last login when updateLastLogin is called")
        void shouldUpdateLastLoginWhenUpdateLastLoginIsCalled() {
            // Arrange
            LocalDateTime beforeLogin = user.getLastLoginAt();

            // Act
            user.updateLastLogin();

            // Assert
            LocalDateTime afterLogin = user.getLastLoginAt();
            assertNotEquals(beforeLogin, afterLogin);
            assertNotNull(afterLogin);
        }

        @Test
        @DisplayName("Should deactivate user when setActive(false) is called")
        void shouldDeactivateUserWhenSetActiveFalseIsCalled() {
            // Act
            user.setActive(false);

            // Assert
            assertFalse(user.isActive());
        }

        @Test
        @DisplayName("Should activate user when setActive(true) is called")
        void shouldActivateUserWhenSetActiveTrueIsCalled() {
            // Arrange
            user.setActive(false);

            // Act
            user.setActive(true);

            // Assert
            assertTrue(user.isActive());
        }

        @Test
        @DisplayName("Should change email when valid email is provided")
        void shouldChangeEmailWhenValidEmailIsProvided() {
            // Arrange
            String newEmail = "newemail@example.com";

            // Act
            user.changeEmail(newEmail);

            // Assert
            assertEquals(newEmail, user.getEmail());
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {"   ", "invalid-email", "test@", "@domain.com"})
        @DisplayName("Should throw exception when changing to invalid email")
        void shouldThrowExceptionWhenChangingToInvalidEmail(String invalidEmail) {
            // Act & Assert
            InvalidUserDataException exception = assertThrows(InvalidUserDataException.class, () -> {
                user.changeEmail(invalidEmail);
            });
            assertTrue(exception.getMessage().contains("email"));
        }

        @Test
        @DisplayName("Should change username when valid username is provided")
        void shouldChangeUsernameWhenValidUsernameIsProvided() {
            // Arrange
            String newUsername = "new_username";

            // Act
            user.changeUsername(newUsername);

            // Assert
            assertEquals(newUsername, user.getUsername());
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {"   ", "\t", "\n"})
        @DisplayName("Should throw exception when changing to invalid username")
        void shouldThrowExceptionWhenChangingToInvalidUsername(String invalidUsername) {
            // Act & Assert
            InvalidUserDataException exception = assertThrows(InvalidUserDataException.class, () -> {
                user.changeUsername(invalidUsername);
            });
            assertEquals("El nombre de usuario no puede estar vacío", exception.getMessage());
        }
    }

    @Nested
    @DisplayName("User Equality Tests")
    class UserEqualityTests {

        @Test
        @DisplayName("Should be equal when users have same userId")
        void shouldBeEqualWhenUsersHaveSameUserId() {
            // Arrange
            UserId userId = UserId.of("sameId");
            User user1 = validUserBuilder.withUserId(userId).build();
            User user2 = validUserBuilder.withUserId(userId).build();

            // Assert
            assertEquals(user1, user2);
            assertEquals(user1.hashCode(), user2.hashCode());
        }

        @Test
        @DisplayName("Should not be equal when users have different userId")
        void shouldNotBeEqualWhenUsersHaveDifferentUserId() {
            // Arrange
            User user1 = validUserBuilder.withUserId(UserId.of("id1")).build();
            User user2 = validUserBuilder.withUserId(UserId.of("id2")).build();

            // Assert
            assertNotEquals(user1, user2);
        }
    }

    @Nested
    @DisplayName("User String Representation Tests")
    class UserStringRepresentationTests {

        @Test
        @DisplayName("Should have meaningful toString representation")
        void shouldHaveMeaningfulToStringRepresentation() {
            // Arrange
            User user = validUserBuilder.build();

            // Act
            String result = user.toString();

            // Assert
            assertNotNull(result);
            assertTrue(result.contains("User{"));
            assertTrue(result.contains("userId="));
            assertTrue(result.contains("clientId="));
            assertTrue(result.contains("username="));
            assertTrue(result.contains("email="));
        }
    }
}