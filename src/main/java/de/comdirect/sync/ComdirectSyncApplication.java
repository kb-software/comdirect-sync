package de.comdirect.sync;

import de.comdirect.sync.application.config.CommandLineArgsParser;
import de.comdirect.sync.domain.valueobjects.DateRange;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.r2dbc.R2dbcAutoConfiguration;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;

import de.comdirect.sync.domain.entities.AccessToken;
import de.comdirect.sync.domain.entities.SessionInfo;
import de.comdirect.sync.domain.repositories.ComdirectAuthRepository;
import de.comdirect.sync.domain.repositories.ComdirectDocumentRepository;
import de.comdirect.sync.infrastructure.adapters.repositories.ComdirectDocumentRepositoryInMemory;
import de.comdirect.sync.infrastructure.adapters.repositories.ComdirectDocumentRepositoryHttp;
import de.comdirect.sync.application.usecases.auth.*;
import de.comdirect.sync.application.usecases.documents.DownloadDocumentsUseCase;
import de.comdirect.sync.application.usecases.auth.ActivateSessionUseCase;
import de.comdirect.sync.application.usecases.auth.ValidateSessionUseCase;
import de.comdirect.sync.application.services.UserInteractionService;
import de.comdirect.sync.infrastructure.services.DocumentStorageService;
import de.comdirect.sync.infrastructure.storage.DocumentHistoryService;
import de.comdirect.sync.infrastructure.services.MessageService;
import org.springframework.web.client.RestTemplate;
import de.comdirect.sync.configuration.ComdirectProperties;

/**
 * Clase principal de la aplicación Comdirect Sync - Versión Consola
 * Ejecuta el flujo completo OAuth2 y descarga de documentos
 */
@SpringBootApplication(exclude = {R2dbcAutoConfiguration.class})
public class ComdirectSyncApplication {

    private static final Logger logger = LoggerFactory.getLogger(ComdirectSyncApplication.class);

    public static void main(String[] args) {
        // Configurar para que no inicie servidor web
        System.setProperty("spring.main.web-application-type", "none");
        SpringApplication.run(ComdirectSyncApplication.class, args);
    }

    @Bean
    public ComdirectDocumentRepository documentRepository() {
        return new ComdirectDocumentRepositoryInMemory();
    }

    // El authRepository se configura automáticamente según el perfil activo
    // Ver RepositoryConfiguration.java

    @Bean
    public CommandLineRunner commandLineRunner(ComdirectAuthRepository authRepository, 
                                              UserInteractionService userInteractionService,
                                              ComdirectProperties properties,
                                              Environment environment,
                                              CommandLineArgsParser argsParser,
                                              MessageService messageService,
                                              DocumentStorageService storageService,
                                              DocumentHistoryService historyService) {
        return args -> {
            try {
                // Parsear argumentos de línea de comandos
                argsParser.parseArgs(args);
                
                // Verificar si se solicitó ayuda
                if (argsParser.isHelpRequested()) {
                    argsParser.printHelp();
                    return;
                }
                
                // Crear rango de fechas basado en argumentos
                DateRange dateRange = argsParser.createDateRange();
                logger.info(messageService.getMessage("cli.args.daterange.configured", dateRange));
                logger.info(messageService.getMessage("cli.args.parsed", argsParser.getArgsSummary()));
                
                logger.info(messageService.getMessage("auth.flow.title"));

                // Obtener credenciales según el perfil activo
                String clientId, clientSecret, username, password;
                String[] activeProfiles = environment.getActiveProfiles();
                boolean isDevProfile = java.util.Arrays.asList(activeProfiles).contains("dev");
                
                if (isDevProfile) {
                    // Perfil DEV: usar credenciales por defecto para InMemory
                    clientId = "User_93FA2414B4B146969DFFDFEC21E8C845";
                    clientSecret = "BB0AC148B2874D0ABF9D34EB799A47FD";
                    username = "1188651895";
                    password = "testpin";
                    logger.info(messageService.getMessage("auth.credentials.default"));
                } else {
                    // Perfil PROD: usar credenciales del .env
                    clientId = properties.getClientId();
                    clientSecret = properties.getClientSecret();
                    username = properties.getUsername();
                    password = properties.getPassword();
                    logger.info(messageService.getMessage("auth.credentials.env"));
                }

                // === Step 2.1: Initial Authentication ===
                logger.info(messageService.getAuthStepMessage("2.1"));
                AuthenticateUserUseCase authenticateUseCase = new AuthenticateUserUseCase(authRepository);
                AuthenticateUserUseCase.AuthRequest authRequest = new AuthenticateUserUseCase.AuthRequest(
                    clientId, clientSecret, username, password);
                var authResult = authenticateUseCase.execute(authRequest);
                
                if (!authResult.isSuccess()) {
                    logger.error("❌ Step 2.1 failed: {}", authResult.getMessage());
                    return;
                }
                
                AccessToken primaryToken = authResult.getToken();
                logger.info(messageService.getAuthSuccessMessage("2.1", primaryToken.getId().getValue()));

                // === Step 2.2: Validate Session Status ===
                logger.info(messageService.getAuthStepMessage("2.2"));
                ValidateSessionUseCase validateUseCase = new ValidateSessionUseCase(authRepository);
                ValidateSessionUseCase.ValidationRequest validationRequest = new ValidateSessionUseCase.ValidationRequest(
                    primaryToken.getAccessToken());
                var validationResult = validateUseCase.execute(validationRequest);
                
                if (!validationResult.isSuccess()) {
                    logger.error("❌ Step 2.2 failed: {}", validationResult.getMessage());
                    return;
                }
                
                SessionInfo sessionInfo = validationResult.getSession();
                logger.info(messageService.getAuthSuccessMessage("2.2", sessionInfo.getSessionId().getValue()));

                // === Step 2.3-2.4: Activate Session with 2FA Code ===
                logger.info(messageService.getAuthStepMessage("2.3-2.4"));

                // PAUSA INTERACTIVA PARA 2FA
                userInteractionService.waitForUserConfirmation(
                    messageService.getMessage("auth.2fa.waiting")
                );
                
                ActivateSessionUseCase activateUseCase = new ActivateSessionUseCase(authRepository);
                
                // En una aplicación real, el código TAN vendría del usuario
                // El repositorio InMemory espera "000000" como código válido
                String simulatedTanCode = "000000";
                String authenticationId = "AUTH_" + System.currentTimeMillis(); // Simular ID único
                
                ActivateSessionUseCase.ActivationRequest activationRequest = new ActivateSessionUseCase.ActivationRequest(
                    primaryToken.getAccessToken(),
                    sessionInfo.getIdentifier(),
                    simulatedTanCode,
                    authenticationId
                );
                
                var activationResult = activateUseCase.execute(activationRequest);
                
                if (!activationResult.isSuccess()) {
                    logger.error("❌ Step 2.3-2.4 failed: {}", activationResult.getMessage());
                    return;
                }
                
                logger.info(messageService.getAuthSuccessMessage("2.3-2.4", simulatedTanCode));

                // === Step 2.5: Get Secondary Token ===
                logger.info(messageService.getAuthStepMessage("2.5"));
                GetSecondaryTokenUseCase secondaryTokenUseCase = new GetSecondaryTokenUseCase(authRepository);
                GetSecondaryTokenUseCase.SecondaryTokenRequest tokenRequest = new GetSecondaryTokenUseCase.SecondaryTokenRequest(
                    clientId, clientSecret, primaryToken.getAccessToken());
                var tokenResult = secondaryTokenUseCase.execute(tokenRequest);
                
                if (!tokenResult.isSuccess()) {
                    logger.error("❌ Step 2.5 failed: {}", tokenResult.getMessage());
                    return;
                }
                
                AccessToken secondaryToken = tokenResult.getToken();
                logger.info(messageService.getAuthSuccessMessage("2.5", secondaryToken.getExpiresAt().toString()));

                logger.info(messageService.getMessage("auth.flow.completed"));

                // Now process documents with the valid token and date range
                logger.info(messageService.getMessage("docs.processing.start"));
                logger.info(messageService.getDocumentsSearchingMessage(dateRange.toString()));
                
                // Configurar repository según el perfil activo
                ComdirectDocumentRepository documentRepository;
                if (isDevProfile) {
                    // Modo DEV: usar InMemory con tokens hardcodeados
                    documentRepository = new ComdirectDocumentRepositoryInMemory(false);
                    logger.info(messageService.getMessage("docs.repository.dev"));
                } else {
                    // Modo PROD: usar HTTP con llamadas reales a la API
                    RestTemplate restTemplate = new RestTemplate();
                    documentRepository = new ComdirectDocumentRepositoryHttp(restTemplate);
                    logger.info(messageService.getMessage("docs.repository.prod"));
                }
                
                // Los servicios DocumentStorageService y DocumentHistoryService son inyectados por Spring
                
                DownloadDocumentsUseCase downloadUseCase = new DownloadDocumentsUseCase(
                    documentRepository, storageService, historyService);
                
                // Usar patrones del .env configurados por el usuario
                String documentPatterns = environment.getProperty("app.document.patterns", 
                    "Dividendengutschrift,Buchungsanzeige,Finanzreport,Steuermitteilung,Ertragsgutschrift");
                String[] patterns = documentPatterns.split(",");
                Set<String> patternSet = Set.of(patterns);
                
                logger.info(messageService.getDocumentsPatternsMessage(String.join(", ", patternSet)));
                
                DownloadDocumentsUseCase.DownloadRequest downloadRequest = new DownloadDocumentsUseCase.DownloadRequest(
                    secondaryToken.getAccessToken(), 
                    patternSet
                );
                
                var downloadResult = downloadUseCase.execute(downloadRequest);
                
                if (downloadResult.isSuccess()) {
                    logger.info(messageService.getDocumentsSuccessMessage(downloadResult.getMessage()));
                    logger.info(messageService.getMessage("docs.summary.range", 
                        dateRange.getDayCount(), 
                        dateRange.getInitDateAsString(), 
                        dateRange.getEndDateAsString()));
                    logger.info(messageService.getMessage("docs.summary.check"));
                } else {
                    logger.error("❌ Error: {}", downloadResult.getMessage());
                }

                logger.info(messageService.getMessage("app.process.completed"));
                
            } catch (Exception e) {
                logger.error(messageService.getMessage("app.error.execution"), e);
                System.exit(1);
            }
        };
    }
}