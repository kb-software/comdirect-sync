package de.comdirect.sync.infrastructure.storage;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import de.comdirect.sync.infrastructure.services.MessageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class DocumentHistoryService {
    
    private static final Logger logger = LoggerFactory.getLogger(DocumentHistoryService.class);
    private final ObjectMapper objectMapper;
    private final String historyDirectory;
    private final MessageService messageService;
    
    public DocumentHistoryService(@Value("${COMDIRECT_SYNC_HOME:comdirect-sync}") String syncHome,
                                  @Value("${HISTORY_DIRECTORY:history}") String historyDir,
                                  MessageService messageService) {
        this.messageService = messageService;
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
        this.historyDirectory = determineHistoryDirectory(syncHome, historyDir);
        initializeHistoryDirectory();
    }
    
    /**
     * Determina el directorio de historial usando la configuración híbrida
     * Soporte para Mac, Linux y Windows con separadores correctos
     */
    private String determineHistoryDirectory(String syncHome, String historyDir) {
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
            
            // Si historyDir es absoluto, usarlo tal como está
            if (Paths.get(historyDir).isAbsolute()) {
                return historyDir;
            } else {
                // Combinar con basePath usando separadores correctos del OS
                return basePath.resolve(historyDir).toString();
            }
            
        } catch (Exception e) {
            // Fallback a directorio por defecto
            String userHome = System.getProperty("user.home");
            String fallback = Paths.get(userHome, "comdirect-sync", "history").toString();
            logger.warn("🔄 Error determinando directorio de historial, usando fallback: {}", fallback);
            return fallback;
        }
    }
    
    /**
     * Inicializa el directorio de historial y archivos necesarios
     */
    private void initializeHistoryDirectory() {
        try {
            Path historyPath = Paths.get(historyDirectory);
            
            if (!Files.exists(historyPath)) {
                Files.createDirectories(historyPath);
                logger.info("✅ Directorio de historial creado: {}", historyDirectory);
            } else {
                logger.debug("📁 Directorio de historial ya existe: {}", historyDirectory);
            }
            
            // Crear archivos de historial si no existen
            initializeHistoryFiles();
            
            logger.info(messageService.getMessage("history.directory.configured", historyDirectory));
            
        } catch (IOException e) {
            logger.error(messageService.getMessage("error.history.directory.create", historyDirectory), e);
            throw new RuntimeException("No se pudo crear el directorio de historial", e);
        }
    }
    
    /**
     * Inicializa los archivos JSON de historial si no existen
     */
    private void initializeHistoryFiles() throws IOException {
        Path downloadsHistoryFile = Paths.get(historyDirectory, "downloads.json");
        Path sessionsHistoryFile = Paths.get(historyDirectory, "sessions.json");
        
        if (!Files.exists(downloadsHistoryFile)) {
            DownloadHistory emptyHistory = new DownloadHistory();
            saveDownloadHistory(emptyHistory);
            logger.info("✅ Archivo de historial de descargas creado: downloads.json");
        }
        
        if (!Files.exists(sessionsHistoryFile)) {
            SessionHistory emptySessionHistory = new SessionHistory();
            saveSessionHistory(emptySessionHistory);
            logger.info("✅ Archivo de historial de sesiones creado: sessions.json");
        }
    }
    
    /**
     * Verifica si un documento ya ha sido descargado
     */
    public boolean isDocumentAlreadyDownloaded(String documentId) {
        try {
            DownloadHistory history = loadDownloadHistory();
            return history.getDownloads().stream()
                    .anyMatch(download -> documentId.equals(download.getId()));
        } catch (Exception e) {
            logger.warn("⚠️ Error checking history for document {}: {}", documentId, e.getMessage());
            return false; // En caso de error, permitir descarga
        }
    }
    
    /**
     * Registra una nueva descarga en el historial
     */
    public void recordDownload(String documentId, String filename, byte[] content, 
                              String type, String company, String documentDate) {
        try {
            DownloadHistory history = loadDownloadHistory();
            
            DownloadRecord record = new DownloadRecord();
            record.setId(documentId);
            record.setFilename(filename);
            record.setDownloadDate(LocalDateTime.now());
            record.setSize(content.length);
            record.setType(type);
            record.setCompany(company);
            record.setDocumentDate(documentDate);
            record.setChecksum(calculateChecksum(content));
            
            history.getDownloads().add(record);
            saveDownloadHistory(history);
            
            logger.info("📝 Download has been registered into the history: {} ({})", filename, documentId);
            
        } catch (Exception e) {
            logger.error("❌ Error registering download into history: {}", documentId, e);
        }
    }
    
    /**
     * Registra una nueva sesión de descarga
     */
    public void recordSession(LocalDateTime startTime, LocalDateTime endTime, 
                             int documentsFound, int documentsDownloaded, 
                             String dateRange, List<String> patterns) {
        try {
            SessionHistory sessionHistory = loadSessionHistory();
            
            SessionRecord session = new SessionRecord();
            session.setStartTime(startTime);
            session.setEndTime(endTime);
            session.setDocumentsFound(documentsFound);
            session.setDocumentsDownloaded(documentsDownloaded);
            session.setDateRange(dateRange);
            session.setSearchPatterns(patterns);
            session.setDurationMinutes(calculateDurationMinutes(startTime, endTime));
            
            sessionHistory.getSessions().add(session);
            saveSessionHistory(sessionHistory);
            
            logger.info(messageService.getHistorySessionRecordedMessage(documentsDownloaded, documentsFound));
            
        } catch (Exception e) {
            logger.error(messageService.getMessage("error.history.session"));
        }
    }
    
    /**
     * Obtiene estadísticas de descargas
     */
    public DownloadStats getDownloadStats() {
        try {
            DownloadHistory downloadHistory = loadDownloadHistory();
            SessionHistory sessionHistory = loadSessionHistory();
            
            DownloadStats stats = new DownloadStats();
            stats.setTotalDownloads(downloadHistory.getDownloads().size());
            stats.setTotalSessions(sessionHistory.getSessions().size());
            
            // Calcular tamaño total
            long totalSize = downloadHistory.getDownloads().stream()
                    .mapToLong(DownloadRecord::getSize)
                    .sum();
            stats.setTotalSizeBytes(totalSize);
            
            // Obtener primera y última descarga
            downloadHistory.getDownloads().stream()
                    .map(DownloadRecord::getDownloadDate)
                    .min(LocalDateTime::compareTo)
                    .ifPresent(stats::setFirstDownload);
                    
            downloadHistory.getDownloads().stream()
                    .map(DownloadRecord::getDownloadDate)
                    .max(LocalDateTime::compareTo)
                    .ifPresent(stats::setLastDownload);
            
            return stats;
            
        } catch (Exception e) {
            logger.error("❌ Error obteniendo estadísticas de descarga", e);
            return new DownloadStats(); // Estadísticas vacías
        }
    }
    
    private DownloadHistory loadDownloadHistory() throws IOException {
        Path historyFile = Paths.get(historyDirectory, "downloads.json");
        if (Files.exists(historyFile)) {
            return objectMapper.readValue(historyFile.toFile(), DownloadHistory.class);
        }
        return new DownloadHistory();
    }
    
    private void saveDownloadHistory(DownloadHistory history) throws IOException {
        Path historyFile = Paths.get(historyDirectory, "downloads.json");
        objectMapper.writerWithDefaultPrettyPrinter()
                   .writeValue(historyFile.toFile(), history);
    }
    
    private SessionHistory loadSessionHistory() throws IOException {
        Path sessionFile = Paths.get(historyDirectory, "sessions.json");
        if (Files.exists(sessionFile)) {
            return objectMapper.readValue(sessionFile.toFile(), SessionHistory.class);
        }
        return new SessionHistory();
    }
    
    private void saveSessionHistory(SessionHistory sessionHistory) throws IOException {
        Path sessionFile = Paths.get(historyDirectory, "sessions.json");
        objectMapper.writerWithDefaultPrettyPrinter()
                   .writeValue(sessionFile.toFile(), sessionHistory);
    }
    
    private String calculateChecksum(byte[] content) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(content);
            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            logger.warn("⚠️ No se pudo calcular checksum SHA-256", e);
            return "unavailable";
        }
    }
    
    private long calculateDurationMinutes(LocalDateTime start, LocalDateTime end) {
        return java.time.Duration.between(start, end).toMinutes();
    }
    
    // Clases para el historial JSON
    public static class DownloadHistory {
        private List<DownloadRecord> downloads = new ArrayList<>();
        
        public List<DownloadRecord> getDownloads() { return downloads; }
        public void setDownloads(List<DownloadRecord> downloads) { this.downloads = downloads; }
    }
    
    public static class DownloadRecord {
        private String id;
        private String filename;
        private LocalDateTime downloadDate;
        private long size;
        private String type;
        private String company;
        private String documentDate;
        private String checksum;
        
        // Getters y setters
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        
        public String getFilename() { return filename; }
        public void setFilename(String filename) { this.filename = filename; }
        
        public LocalDateTime getDownloadDate() { return downloadDate; }
        public void setDownloadDate(LocalDateTime downloadDate) { this.downloadDate = downloadDate; }
        
        public long getSize() { return size; }
        public void setSize(long size) { this.size = size; }
        
        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
        
        public String getCompany() { return company; }
        public void setCompany(String company) { this.company = company; }
        
        public String getDocumentDate() { return documentDate; }
        public void setDocumentDate(String documentDate) { this.documentDate = documentDate; }
        
        public String getChecksum() { return checksum; }
        public void setChecksum(String checksum) { this.checksum = checksum; }
    }
    
    public static class SessionHistory {
        private List<SessionRecord> sessions = new ArrayList<>();
        
        public List<SessionRecord> getSessions() { return sessions; }
        public void setSessions(List<SessionRecord> sessions) { this.sessions = sessions; }
    }
    
    public static class SessionRecord {
        private LocalDateTime startTime;
        private LocalDateTime endTime;
        private int documentsFound;
        private int documentsDownloaded;
        private String dateRange;
        private List<String> searchPatterns;
        private long durationMinutes;
        
        // Getters y setters
        public LocalDateTime getStartTime() { return startTime; }
        public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }
        
        public LocalDateTime getEndTime() { return endTime; }
        public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }
        
        public int getDocumentsFound() { return documentsFound; }
        public void setDocumentsFound(int documentsFound) { this.documentsFound = documentsFound; }
        
        public int getDocumentsDownloaded() { return documentsDownloaded; }
        public void setDocumentsDownloaded(int documentsDownloaded) { this.documentsDownloaded = documentsDownloaded; }
        
        public String getDateRange() { return dateRange; }
        public void setDateRange(String dateRange) { this.dateRange = dateRange; }
        
        public List<String> getSearchPatterns() { return searchPatterns; }
        public void setSearchPatterns(List<String> searchPatterns) { this.searchPatterns = searchPatterns; }
        
        public long getDurationMinutes() { return durationMinutes; }
        public void setDurationMinutes(long durationMinutes) { this.durationMinutes = durationMinutes; }
    }
    
    public static class DownloadStats {
        private int totalDownloads;
        private int totalSessions;
        private long totalSizeBytes;
        private LocalDateTime firstDownload;
        private LocalDateTime lastDownload;
        
        // Getters y setters
        public int getTotalDownloads() { return totalDownloads; }
        public void setTotalDownloads(int totalDownloads) { this.totalDownloads = totalDownloads; }
        
        public int getTotalSessions() { return totalSessions; }
        public void setTotalSessions(int totalSessions) { this.totalSessions = totalSessions; }
        
        public long getTotalSizeBytes() { return totalSizeBytes; }
        public void setTotalSizeBytes(long totalSizeBytes) { this.totalSizeBytes = totalSizeBytes; }
        
        public LocalDateTime getFirstDownload() { return firstDownload; }
        public void setFirstDownload(LocalDateTime firstDownload) { this.firstDownload = firstDownload; }
        
        public LocalDateTime getLastDownload() { return lastDownload; }
        public void setLastDownload(LocalDateTime lastDownload) { this.lastDownload = lastDownload; }
    }
}