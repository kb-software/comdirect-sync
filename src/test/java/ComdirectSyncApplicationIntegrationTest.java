import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * Test de integración para validar que la aplicación Spring Boot se inicia correctamente
 */
@SpringBootTest(classes = ComdirectSyncApplication.class)
@ActiveProfiles("test")
class ComdirectSyncApplicationIntegrationTest {

    @Test
    void contextLoads() {
        // Este test verifica que el contexto de Spring Boot se carga correctamente
        // Si el contexto no se puede cargar, el test fallará
    }
}