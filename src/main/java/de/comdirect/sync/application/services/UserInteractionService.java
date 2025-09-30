package de.comdirect.sync.application.services;

import de.comdirect.sync.infrastructure.services.MessageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Servicio para manejar la interacción con el usuario durante el proceso 2FA
 */
@Service
public class UserInteractionService {
    
    private static final Logger logger = LoggerFactory.getLogger(UserInteractionService.class);
    private final BufferedReader reader;
    private final MessageService messageService;
    
    public UserInteractionService(MessageService messageService) {
        this.reader = new BufferedReader(new InputStreamReader(System.in));
        this.messageService = messageService;
    }
    
    /**
     * Pausa la ejecución y espera confirmación del usuario
     * @param message Mensaje a mostrar al usuario
     */
    public void waitForUserConfirmation(String message) {
        try {
            System.out.println("\n" + "=".repeat(60));
            System.out.println("🔐 " + messageService.getMessage("2fa.title"));
            System.out.println("=".repeat(60));
            System.out.println(message);
            System.out.println("\n" + messageService.getMessage("2fa.instructions"));
            System.out.println(messageService.getMessage("2fa.step1"));
            System.out.println(messageService.getMessage("2fa.step2"));
            System.out.println(messageService.getMessage("2fa.step3"));
            System.out.println("\n" + messageService.getMessage("2fa.waiting.authorization"));
            System.out.print(messageService.getMessage("2fa.prompt"));
            
            // Esperar que el usuario presione ENTER
            reader.readLine();
            
            System.out.println(messageService.getMessage("2fa.continuing"));
            System.out.println("=".repeat(60) + "\n");
            
        } catch (IOException e) {
            logger.error(messageService.getMessage("2fa.error.reading"), e);
            // Si hay error, continuamos automáticamente después de un delay
            try {
                Thread.sleep(3000);
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
            }
        }
    }
    
    /**
     * Pausa con tiempo límite automático
     * @param message Mensaje a mostrar
     * @param timeoutSeconds Tiempo límite en segundos
     */
    public void waitForUserConfirmationWithTimeout(String message, int timeoutSeconds) {
        try {
            System.out.println("\n" + "=".repeat(60));
            System.out.println("🔐 " + messageService.getMessage("2fa.title"));
            System.out.println("=".repeat(60));
            System.out.println(message);
            System.out.println("\n" + messageService.getMessage("2fa.instructions"));
            System.out.println(messageService.getMessage("2fa.step1"));
            System.out.println(messageService.getMessage("2fa.step2"));
            System.out.println(messageService.getMessage("2fa.step3.timeout", timeoutSeconds));
            System.out.println("\n" + messageService.getMessage("2fa.waiting.authorization"));
            System.out.print(messageService.getMessage("2fa.prompt.timeout", timeoutSeconds));
            
            // Crear thread para timeout
            Thread timeoutThread = new Thread(() -> {
                try {
                    Thread.sleep(timeoutSeconds * 1000);
                    System.out.println("\n" + messageService.getMessage("2fa.timeout.expired"));
                } catch (InterruptedException e) {
                    // Thread interrumpido, el usuario presionó ENTER
                }
            });
            
            timeoutThread.start();
            reader.readLine(); // Esperar ENTER del usuario
            timeoutThread.interrupt(); // Cancelar timeout
            
            System.out.println(messageService.getMessage("2fa.continuing"));
            System.out.println("=".repeat(60) + "\n");
            
        } catch (IOException e) {
            logger.error(messageService.getMessage("2fa.error.reading"), e);
        }
    }
}