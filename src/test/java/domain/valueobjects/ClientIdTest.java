package domain.valueobjects;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitarios para el Value Object ClientId
 * Valida las reglas de validación y comportamiento del Value Object
 */
@DisplayName("ClientId Value Object Tests")
class ClientIdTest {

    @Nested
    @DisplayName("ClientId Creation Tests")
    class ClientIdCreationTests {

        @Test
        @DisplayName("Should create ClientId with valid value")
        void shouldCreateClientIdWithValidValue() {
            // Arrange
            String validValue = "CLIENT123";

            // Act
            ClientId clientId = ClientId.of(validValue);

            // Assert
            assertNotNull(clientId);
            assertEquals(validValue, clientId.getValue());
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {"   ", "\t", "\n"})
        @DisplayName("Should throw exception when value is null or empty")
        void shouldThrowExceptionWhenValueIsNullOrEmpty(String invalidValue) {
            // Act & Assert
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
                ClientId.of(invalidValue);
            });
            assertEquals("El ID de cliente no puede estar vacío", exception.getMessage());
        }

        @ParameterizedTest
        @ValueSource(strings = {"1234567", "1234567890123456789012345678901234567"})
        @DisplayName("Should throw exception when value length is invalid")
        void shouldThrowExceptionWhenValueLengthIsInvalid(String invalidValue) {
            // Act & Assert
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
                ClientId.of(invalidValue);
            });
            assertEquals("El ID de cliente debe tener entre 8 y 32 caracteres alfanuméricos", exception.getMessage());
        }

        @ParameterizedTest
        @ValueSource(strings = {"CLIENT@123", "CLIENT 123", "CLIENT#123"})
        @DisplayName("Should throw exception when value contains invalid characters")
        void shouldThrowExceptionWhenValueContainsInvalidCharacters(String invalidValue) {
            // Act & Assert
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
                ClientId.of(invalidValue);
            });
            assertEquals("El ID de cliente debe tener entre 8 y 32 caracteres alfanuméricos", exception.getMessage());
        }
    }

    @Nested
    @DisplayName("ClientId Equality Tests")
    class ClientIdEqualityTests {

        @Test
        @DisplayName("Should be equal when ClientIds have same value")
        void shouldBeEqualWhenClientIdsHaveSameValue() {
            // Arrange
            ClientId clientId1 = ClientId.of("CLIENT123");
            ClientId clientId2 = ClientId.of("CLIENT123");

            // Assert
            assertEquals(clientId1, clientId2);
            assertEquals(clientId1.hashCode(), clientId2.hashCode());
        }

        @Test
        @DisplayName("Should not be equal when ClientIds have different values")
        void shouldNotBeEqualWhenClientIdsHaveDifferentValues() {
            // Arrange
            ClientId clientId1 = ClientId.of("CLIENT123");
            ClientId clientId2 = ClientId.of("CLIENT456");

            // Assert
            assertNotEquals(clientId1, clientId2);
        }
    }

    @Nested
    @DisplayName("ClientId String Tests")
    class ClientIdStringTests {

        @Test
        @DisplayName("Should have meaningful toString representation")
        void shouldHaveMeaningfulToStringRepresentation() {
            // Arrange
            ClientId clientId = ClientId.of("CLIENT123");

            // Act
            String result = clientId.toString();

            // Assert
            assertNotNull(result);
            assertTrue(result.contains("ClientId{"));
            assertTrue(result.contains("CLIENT123"));
        }
    }
}