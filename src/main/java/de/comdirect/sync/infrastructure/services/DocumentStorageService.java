package de.comdirect.sync.infrastructure.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.regex.Pattern;

/**
 * Servicio para gestión de archivos y directorios de descarga multiplataforma
 * Implementa la estrategia híbrida con COMDIRECT_SYNC_HOME:
 * 1. Determina directorio base desde COMDIRECT_SYNC_HOME
 * 2. Combina con DOWNLOAD_DIRECTORY usando separadores correctos del OS
 * 3. Soporte completo para Mac, Linux y Windows
 * 4. Fallback: ~/comdirect-sync/downloads/
 */
@Service
public class DocumentStorageService {
    
    private static final Logger logger = LoggerFactory.getLogger(DocumentStorageService.class);
    private final String downloadDirectory;
    private final MessageService messageService;
    
    // Patrón para limpiar nombres de archivo
    private static final Pattern INVALID_FILENAME_CHARS = Pattern.compile("[<>:\"/\\\\|?*]");
    
    public DocumentStorageService(@Value("${COMDIRECT_SYNC_HOME:comdirect-sync}") String syncHome,
                                  @Value("${DOWNLOAD_DIRECTORY:downloads}") String downloadDir,
                                  MessageService messageService) {
        this.messageService = messageService;
        this.downloadDirectory = determineDownloadDirectory(syncHome, downloadDir);
        ensureDirectoryExists();
        logger.info(messageService.getStorageDirectoryConfiguredMessage(downloadDirectory));
    }
    
    /**
     * Determina el directorio de descarga usando la configuración híbrida
     * Soporte para Mac, Linux y Windows con separadores correctos
     */
    private String determineDownloadDirectory(String syncHome, String downloadDir) {
        try {
            Path basePath;
            
            // Si syncHome es una ruta absoluta
            if (Paths.get(syncHome).isAbsolute()) {
                basePath = Paths.get(syncHome);
            } else {
                // Ruta relativa al home del usuario (cross-platform)
                String userHome = System.getProperty("user.home");
                basePath = Paths.get(userHome, syncHome);
            }
            
            // Si downloadDir es absoluto, usarlo tal como está
            if (Paths.get(downloadDir).isAbsolute()) {
                return downloadDir;
            } else {
                // Combinar con basePath usando separadores correctos del OS
                return basePath.resolve(downloadDir).toString();
            }
            
        } catch (Exception e) {
            // Fallback a directorio por defecto
            String userHome = System.getProperty("user.home");
            String fallback = Paths.get(userHome, "comdirect-sync", "downloads").toString();
            logger.info(messageService.getMessage("storage.directory.fallback", fallback));
            return fallback;
        }
    }
    
    /**
     * Asegura que el directorio de descarga existe
     */
    private void ensureDirectoryExists() {
        try {
            Path dirPath = Paths.get(downloadDirectory);
            if (!Files.exists(dirPath)) {
                Files.createDirectories(dirPath);
                logger.info(messageService.getStorageDirectoryCreatedMessage(downloadDirectory));
            } else {
                logger.debug(messageService.getMessage("storage.directory.exists", downloadDirectory));
            }
        } catch (IOException e) {
            logger.error(messageService.getMessage("error.directory.create", downloadDirectory, e.getMessage()));
            throw new RuntimeException("Failed to create download directory: " + downloadDirectory, e);
        }
    }
    
    /**
     * Guarda un documento con nombre descriptivo
     * Formato: {fecha}_{tipo}_{empresa}_{id}.pdf
     */
    public String saveDocument(byte[] content, String documentName, String documentId, java.time.LocalDate dateCreation) {
        try {
            String dateStr = dateCreation != null ? dateCreation.toString() : getCurrentDate();
            String fileName = generateFileName(documentName, documentId, dateStr);
            Path filePath = Paths.get(downloadDirectory, fileName);
            
            // Escribir archivo
            Files.write(filePath, content, StandardOpenOption.CREATE, StandardOpenOption.WRITE);
            
            logger.info(messageService.getStorageDocumentSavedMessage(fileName, content.length));
            return filePath.toAbsolutePath().toString();
            
        } catch (IOException e) {
            logger.error(messageService.getMessage("error.storage.save", documentName, e.getMessage()));
            throw new RuntimeException("Failed to save document: " + documentName, e);
        }
    }
    
    /**
     * Genera un nombre de archivo descriptivo y limpio
     * Formato: {fecha}_{tipo}_{empresa}_{id}.pdf
     */
    private String generateFileName(String documentName, String documentId, String dateCreation) {
        // Limpiar nombre del documento
        String cleanName = cleanFileName(documentName);
        
        // Extraer tipo de documento (primera palabra significativa)
        String documentType = extractDocumentType(cleanName);
        
        // Extraer empresa/instrumento (si está disponible)
        String company = extractCompany(cleanName);
        
        // Formato de fecha
        String date = dateCreation != null ? dateCreation : getCurrentDate();
        
        // ID corto (primeros 8 caracteres)
        String shortId = documentId.length() > 8 ? documentId.substring(0, 8) : documentId;
        
        // Construir nombre: fecha_tipo_empresa_id.pdf
        StringBuilder fileName = new StringBuilder();
        fileName.append(date);
        
        if (!documentType.isEmpty()) {
            fileName.append("_").append(documentType);
        }
        
        if (!company.isEmpty()) {
            fileName.append("_").append(company);
        }
        
        fileName.append("_").append(shortId).append(".pdf");
        
        return fileName.toString();
    }
    
    /**
     * Limpia caracteres inválidos del nombre de archivo
     */
    private String cleanFileName(String fileName) {
        if (fileName == null) return "document";
        
        // Reemplazar caracteres inválidos y espacios múltiples
        String clean = INVALID_FILENAME_CHARS.matcher(fileName).replaceAll("_");
        clean = clean.replaceAll("\\s+", "_");
        clean = clean.replaceAll("_{2,}", "_");
        
        // Remover guiones bajos al inicio y final
        clean = clean.replaceAll("^_+|_+$", "");
        
        return clean.isEmpty() ? "document" : clean;
    }
    
    /**
     * Extrae el tipo de documento (Steuermitteilung, Buchungsanzeige, etc.)
     */
    private String extractDocumentType(String documentName) {
        String[] keywords = {"Steuermitteilung", "Buchungsanzeige", "Finanzreport", 
                           "Dividendengutschrift", "Ertragsgutschrift"};
        
        for (String keyword : keywords) {
            if (documentName.toLowerCase().contains(keyword.toLowerCase())) {
                return keyword;
            }
        }
        
        // Fallback: primera palabra del documento
        String[] words = documentName.split("_");
        return words.length > 0 ? words[0] : "";
    }
    
    /**
     * Extrae el nombre de la empresa o instrumento
     */
    private String extractCompany(String documentName) {
        // Buscar patrones comunes de empresa entre paréntesis
        if (documentName.contains("(") && documentName.contains(")")) {
            int start = documentName.indexOf("(") + 1;
            int end = documentName.indexOf(")", start);
            if (end > start) {
                String company = documentName.substring(start, end);
                return cleanFileName(company).replace("_", "");
            }
        }
        
        // Fallback: buscar palabras que parezcan nombres de empresa
        String[] words = documentName.split("_");
        for (String word : words) {
            if (word.length() > 3 && word.matches(".*[A-Z].*")) {
                return word;
            }
        }
        
        return "";
    }
    
    /**
     * Obtiene la fecha actual en formato YYYY-MM-DD
     */
    private String getCurrentDate() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
    }
    
    /**
     * Retorna el directorio de descarga configurado
     */
    public String getDownloadDirectory() {
        return downloadDirectory;
    }
    
    /**
     * Verifica si el directorio está disponible y escribible
     */
    public boolean isDirectoryWritable() {
        Path dirPath = Paths.get(downloadDirectory);
        return Files.exists(dirPath) && Files.isWritable(dirPath);
    }
    
    /**
     * Obtiene información del directorio de descarga
     */
    public String getDirectoryInfo() {
        Path dirPath = Paths.get(downloadDirectory);
        return String.format("Directory: %s | Exists: %s | Writable: %s", 
                            downloadDirectory, 
                            Files.exists(dirPath), 
                            Files.isWritable(dirPath));
    }
}