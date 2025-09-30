package domain.valueobjects;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitarios para el Value Object UserId
 * Valida las reglas de validación y comportamiento del Value Object
 */
@DisplayName("UserId Value Object Tests")
class UserIdTest {

    @Nested
    @DisplayName("UserId Creation Tests")
    class UserIdCreationTests {

        @Test
        @DisplayName("Should create UserId with valid value")
        void shouldCreateUserIdWithValidValue() {
            // Arrange
            String validValue = "user123";

            // Act
            UserId userId = UserId.of(validValue);

            // Assert
            assertNotNull(userId);
            assertEquals(validValue, userId.getValue());
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {"   ", "\t", "\n"})
        @DisplayName("Should throw exception when value is null or empty")
        void shouldThrowExceptionWhenValueIsNullOrEmpty(String invalidValue) {
            // Act & Assert
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
                UserId.of(invalidValue);
            });
            assertEquals("El ID de usuario no puede estar vacío", exception.getMessage());
        }

        @ParameterizedTest
        @ValueSource(strings = {"ab", "12345678901234567890123456789012345678901234567890x"})
        @DisplayName("Should throw exception when value length is invalid")
        void shouldThrowExceptionWhenValueLengthIsInvalid(String invalidValue) {
            // Act & Assert
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
                UserId.of(invalidValue);
            });
            assertEquals("El ID de usuario debe tener entre 3 y 50 caracteres", exception.getMessage());
        }
    }

    @Nested
    @DisplayName("UserId Equality Tests")
    class UserIdEqualityTests {

        @Test
        @DisplayName("Should be equal when UserIds have same value")
        void shouldBeEqualWhenUserIdsHaveSameValue() {
            // Arrange
            UserId userId1 = UserId.of("user123");
            UserId userId2 = UserId.of("user123");

            // Assert
            assertEquals(userId1, userId2);
            assertEquals(userId1.hashCode(), userId2.hashCode());
        }

        @Test
        @DisplayName("Should not be equal when UserIds have different values")
        void shouldNotBeEqualWhenUserIdsHaveDifferentValues() {
            // Arrange
            UserId userId1 = UserId.of("user123");
            UserId userId2 = UserId.of("user456");

            // Assert
            assertNotEquals(userId1, userId2);
        }
    }

    @Nested
    @DisplayName("UserId String Tests")
    class UserIdStringTests {

        @Test
        @DisplayName("Should have meaningful toString representation")
        void shouldHaveMeaningfulToStringRepresentation() {
            // Arrange
            UserId userId = UserId.of("user123");

            // Act
            String result = userId.toString();

            // Assert
            assertNotNull(result);
            assertTrue(result.contains("UserId{"));
            assertTrue(result.contains("user123"));
        }
    }
}