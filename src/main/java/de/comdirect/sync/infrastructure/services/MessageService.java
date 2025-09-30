package de.comdirect.sync.infrastructure.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;

import java.util.Locale;

/**
 * Servicio centralizado para gestión de mensajes internacionalizados
 * Proporciona acceso a mensajes en múltiples idiomas usando Spring Boot MessageSource
 * 
 * Idiomas soportados:
 * - Español (es)
 * - Alemán (de) 
 * - Inglés (en) - por defecto
 */
@Service
public class MessageService {
    
    private final MessageSource messageSource;
    private final Locale currentLocale;
    
    public MessageService(MessageSource messageSource,
                         @Value("${COMDIRECT_LANGUAGE:auto}") String configuredLanguage) {
        this.messageSource = messageSource;
        this.currentLocale = determineLocale(configuredLanguage);
    }
    
    /**
     * Obtiene un mensaje usando el locale actual del sistema
     */
    public String getMessage(String key, Object... args) {
        return messageSource.getMessage(key, args, currentLocale);
    }
    
    /**
     * Obtiene un mensaje usando un locale específico
     */
    public String getMessage(String key, Locale locale, Object... args) {
        return messageSource.getMessage(key, args, locale);
    }
    
    /**
     * Obtiene un mensaje en español específicamente
     */
    public String getMessageEs(String key, Object... args) {
        return getMessage(key, Locale.of("es"), args);
    }
    
    /**
     * Obtiene un mensaje en alemán específicamente
     */
    public String getMessageDe(String key, Object... args) {
        return getMessage(key, Locale.of("de"), args);
    }
    
    /**
     * Obtiene un mensaje en inglés específicamente
     */
    public String getMessageEn(String key, Object... args) {
        return getMessage(key, Locale.ENGLISH, args);
    }
    
    /**
     * Determina el locale a usar basado en configuración del sistema y variables de entorno
     */
    private Locale determineLocale(String configuredLanguage) {
        // 1. Verificar configuración de Spring Boot desde .env
        if (configuredLanguage != null && !configuredLanguage.trim().isEmpty() && !configuredLanguage.equals("auto")) {
            return parseLocale(configuredLanguage);
        }
        
        // 2. Verificar variable de entorno LANGUAGE específica de la aplicación
        String appLanguage = System.getenv("COMDIRECT_LANGUAGE");
        if (appLanguage != null && !appLanguage.trim().isEmpty() && !appLanguage.equals("auto")) {
            return parseLocale(appLanguage);
        }
        
        // 3. Verificar propiedad del sistema
        String systemLang = System.getProperty("user.language");
        if (systemLang != null && !systemLang.trim().isEmpty()) {
            return parseLocale(systemLang);
        }
        
        // 4. Verificar variable de entorno LANG (Unix/Mac)
        String envLang = System.getenv("LANG");
        if (envLang != null && !envLang.trim().isEmpty()) {
            String langCode = envLang.split("_")[0]; // Extraer solo el código de idioma
            return parseLocale(langCode);
        }
        
        // 4. Usar locale por defecto del sistema
        Locale systemDefault = Locale.getDefault();
        String language = systemDefault.getLanguage();
        
        // Si el idioma del sistema está soportado, usarlo
        if (isSupportedLanguage(language)) {
            return Locale.of(language);
        }
        
        // 5. Fallback a inglés
        return Locale.ENGLISH;
    }
    
    /**
     * Parsea un string de idioma a Locale
     */
    private Locale parseLocale(String languageCode) {
        if (languageCode == null || languageCode.trim().isEmpty()) {
            return Locale.ENGLISH;
        }
        
        String lang = languageCode.toLowerCase().trim();
        
        // Manejar códigos comunes
        switch (lang) {
            case "es":
            case "esp":
            case "spanish":
            case "español":
                return Locale.of("es");
            case "de":
            case "deu":
            case "ger":
            case "german":
            case "deutsch":
                return Locale.of("de");
            case "en":
            case "eng":
            case "english":
            default:
                return Locale.ENGLISH;
        }
    }
    
    /**
     * Verifica si un idioma está soportado por la aplicación
     */
    private boolean isSupportedLanguage(String language) {
        return "es".equals(language) || "de".equals(language) || "en".equals(language);
    }
    
    /**
     * Obtiene el locale actual
     */
    public Locale getCurrentLocale() {
        return currentLocale;
    }
    
    /**
     * Obtiene información sobre el locale actual
     */
    public String getLocaleInfo() {
        return String.format("Current locale: %s (%s)", 
                           currentLocale.getLanguage(), 
                           currentLocale.getDisplayLanguage(currentLocale));
    }
    
    /**
     * Métodos de conveniencia para mensajes comunes
     */
    
    public String getAppStartupMessage(String javaVersion) {
        return getMessage("app.startup.java.version", javaVersion);
    }
    
    public String getProfileActiveMessage(String profile) {
        return getMessage("app.startup.profile.active", profile);
    }
    
    public String getAuthStepMessage(String stepNumber) {
        return getMessage("auth.step." + stepNumber);
    }
    
    public String getAuthSuccessMessage(String stepNumber, String data) {
        return getMessage("auth.success." + stepNumber, data);
    }
    
    public String getDocumentsProcessingTitle() {
        return getMessage("docs.processing.title");
    }
    
    public String getDocumentsSearchingMessage(String range) {
        return getMessage("docs.searching", range);
    }
    
    public String getDocumentsPatternsMessage(String patterns) {
        return getMessage("docs.patterns", patterns);
    }
    
    public String getDocumentsSuccessMessage(String result) {
        return getMessage("docs.success", result);
    }
    
    public String getStorageDirectoryConfiguredMessage(String directory) {
        return getMessage("storage.directory.configured", directory);
    }
    
    public String getStorageDirectoryCreatedMessage(String directory) {
        return getMessage("storage.directory.created", directory);
    }
    
    public String getStorageDocumentSavedMessage(String filename, long bytes) {
        return getMessage("storage.document.saved", filename, bytes);
    }
    
    public String getHistoryDownloadRecordedMessage(String filename, String id) {
        return getMessage("history.download.recorded", filename, id);
    }
    
    public String getHistorySessionRecordedMessage(int downloaded, int found) {
        return getMessage("history.session.recorded", downloaded, found);
    }
    
    public String get2FATitle() {
        return getMessage("auth.2fa.title");
    }
    
    public String get2FAInstructions() {
        return getMessage("auth.2fa.instructions");
    }
    
    public String get2FAWaiting() {
        return getMessage("auth.2fa.waiting");
    }
    
    public String get2FAPrompt() {
        return getMessage("auth.2fa.prompt");
    }
    
    public String get2FAContinuing() {
        return getMessage("auth.2fa.continuing");
    }
}