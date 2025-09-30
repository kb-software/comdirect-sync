package de.comdirect.sync.application.usecases.documents;

import de.comdirect.sync.domain.entities.DocumentInfo;
import de.comdirect.sync.domain.repositories.ComdirectDocumentRepository;
import de.comdirect.sync.domain.exceptions.InvalidUserDataException;
import de.comdirect.sync.infrastructure.services.DocumentStorageService;
import de.comdirect.sync.infrastructure.storage.DocumentHistoryService;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Caso de uso para descargar documentos filtrados por patrones
 * Corresponde al endpoint 9.1.1 Abruf PostBox y 9.1.2 Abruf eines Dokuments
 */
public class DownloadDocumentsUseCase {
    
    private final ComdirectDocumentRepository documentRepository;
    private final DocumentStorageService storageService;
    private final DocumentHistoryService historyService;
    
    public DownloadDocumentsUseCase(ComdirectDocumentRepository documentRepository, 
                                   DocumentStorageService storageService,
                                   DocumentHistoryService historyService) {
        this.documentRepository = documentRepository;
        this.storageService = storageService;
        this.historyService = historyService;
    }
    
    // Constructor backward compatible para casos sin storage y historial
    public DownloadDocumentsUseCase(ComdirectDocumentRepository documentRepository) {
        this.documentRepository = documentRepository;
        this.storageService = null;
        this.historyService = null;
    }
    
    public DownloadResult execute(DownloadRequest request) {
        validateRequest(request);
        
        LocalDateTime sessionStartTime = LocalDateTime.now();
        
        try {
            // Obtener documentos filtrados por patrones
            List<DocumentInfo> allDocuments = documentRepository.getDocuments(request.getAccessToken());
            List<DocumentInfo> filteredDocuments = filterDocuments(allDocuments, request.getPatterns());
            
            // Filtrar solo documentos descargables (PDF)
            List<DocumentInfo> downloadableDocuments = filteredDocuments.stream()
                    .filter(DocumentInfo::isDownloadable)
                    .collect(Collectors.toList());
            
            if (downloadableDocuments.isEmpty()) {
                recordSession(sessionStartTime, allDocuments.size(), 0, request.getPatterns());
                return DownloadResult.success(Collections.emptyList(), Collections.emptyMap(),
                    "No downloadable documents found matching the specified patterns");
            }
            
            // Descargar documentos (verificando historial para evitar re-descargas)
            Map<String, byte[]> downloadedContents = new HashMap<>();
            List<String> failedDownloads = new ArrayList<>();
            List<String> savedFiles = new ArrayList<>();
            List<String> skippedFiles = new ArrayList<>();
            
            for (DocumentInfo document : downloadableDocuments) {
                try {
                    String documentId = document.getDocumentId().getValue();
                    
                    // Verificar si el documento ya fue descargado
                    if (historyService != null && historyService.isDocumentAlreadyDownloaded(documentId)) {
                        skippedFiles.add(document.getName() + " (ya descargado)");
                        continue;
                    }
                    
                    byte[] content = documentRepository.downloadDocument(
                        request.getAccessToken(), 
                        document.getDocumentId()
                    );
                    downloadedContents.put(documentId, content);
                    
                    // Guardar archivo si storage service está disponible
                    if (storageService != null) {
                        String savedPath = storageService.saveDocument(
                            content,
                            document.getName(),
                            documentId,
                            document.getDateCreation()
                        );
                        savedFiles.add(savedPath);
                    }
                    
                    // Registrar en historial
                    if (historyService != null) {
                        String documentType = extractDocumentType(document.getName());
                        String company = extractCompany(document.getName());
                        String documentDate = document.getDateCreation() != null ? 
                                            document.getDateCreation().toString() : "unknown";
                        
                        historyService.recordDownload(
                            documentId,
                            generateFileName(document.getName(), documentId),
                            content,
                            documentType,
                            company,
                            documentDate
                        );
                    }
                    
                } catch (Exception e) {
                    failedDownloads.add(document.getName() + ": " + e.getMessage());
                }
            }
            
            // Registrar sesión en historial
            recordSession(sessionStartTime, allDocuments.size(), downloadedContents.size(), request.getPatterns());
            
            String message = buildResultMessage(downloadedContents, downloadableDocuments, 
                                              savedFiles, skippedFiles, failedDownloads);
            
            return DownloadResult.success(downloadableDocuments, downloadedContents, message);
            
        } catch (InvalidUserDataException e) {
            recordSession(sessionStartTime, 0, 0, request.getPatterns());
            return DownloadResult.failure("Download failed: " + e.getMessage());
        } catch (Exception e) {
            recordSession(sessionStartTime, 0, 0, request.getPatterns());
            return DownloadResult.failure("Unexpected error during download: " + e.getMessage());
        }
    }
    
    private List<DocumentInfo> filterDocuments(List<DocumentInfo> documents, Set<String> patterns) {
        if (patterns == null || patterns.isEmpty()) {
            return documents;
        }
        
        return documents.stream()
                .filter(doc -> doc.matchesPattern(patterns))
                .collect(Collectors.toList());
    }
    
    private void validateRequest(DownloadRequest request) {
        if (request == null) {
            throw new InvalidUserDataException("Download request cannot be null");
        }
        
        if (request.getAccessToken() == null || request.getAccessToken().trim().isEmpty()) {
            throw new InvalidUserDataException("Access token is required");
        }
    }
    
    /**
     * Registra la sesión de descarga en el historial
     */
    private void recordSession(LocalDateTime startTime, int documentsFound, int documentsDownloaded, Set<String> patterns) {
        if (historyService != null) {
            LocalDateTime endTime = LocalDateTime.now();
            List<String> patternList = patterns != null ? new ArrayList<>(patterns) : Collections.emptyList();
            String dateRange = "unknown"; // Sería mejor obtenerlo del request si está disponible
            
            historyService.recordSession(startTime, endTime, documentsFound, documentsDownloaded, dateRange, patternList);
        }
    }
    
    /**
     * Construye el mensaje de resultado con todas las estadísticas
     */
    private String buildResultMessage(Map<String, byte[]> downloadedContents, List<DocumentInfo> downloadableDocuments,
                                    List<String> savedFiles, List<String> skippedFiles, List<String> failedDownloads) {
        String message = String.format(
            "Downloaded %d of %d documents successfully", 
            downloadedContents.size(), 
            downloadableDocuments.size()
        );
        
        if (storageService != null && !savedFiles.isEmpty()) {
            message += String.format(". Saved %d files to: %s", 
                                   savedFiles.size(), 
                                   storageService.getDownloadDirectory());
        }
        
        if (!skippedFiles.isEmpty()) {
            message += String.format(". Skipped %d already downloaded files", skippedFiles.size());
        }
        
        if (!failedDownloads.isEmpty()) {
            message += ". Failed downloads: " + String.join(", ", failedDownloads);
        }
        
        return message;
    }
    
    /**
     * Extrae el tipo de documento del nombre
     */
    private String extractDocumentType(String documentName) {
        if (documentName == null) return "unknown";
        
        String[] keywords = {"Steuermitteilung", "Buchungsanzeige", "Finanzreport", 
                           "Dividendengutschrift", "Ertragsgutschrift"};
        
        for (String keyword : keywords) {
            if (documentName.toLowerCase().contains(keyword.toLowerCase())) {
                return keyword;
            }
        }
        
        return "unknown";
    }
    
    /**
     * Extrae el nombre de la empresa del documento
     */
    private String extractCompany(String documentName) {
        if (documentName == null) return "unknown";
        
        // Buscar patrones comunes de empresa entre paréntesis
        if (documentName.contains("(") && documentName.contains(")")) {
            int start = documentName.indexOf("(") + 1;
            int end = documentName.indexOf(")", start);
            if (end > start) {
                return documentName.substring(start, end).trim();
            }
        }
        
        return "unknown";
    }
    
    /**
     * Genera un nombre de archivo simple para el historial
     */
    private String generateFileName(String documentName, String documentId) {
        String shortId = documentId.length() > 8 ? documentId.substring(0, 8) : documentId;
        String cleanName = documentName != null ? documentName.replaceAll("[^a-zA-Z0-9._-]", "_") : "document";
        return cleanName + "_" + shortId + ".pdf";
    }
    
    public static class DownloadRequest {
        private final String accessToken;
        private final Set<String> patterns;
        
        public DownloadRequest(String accessToken, Set<String> patterns) {
            this.accessToken = accessToken;
            this.patterns = patterns != null ? patterns : Collections.emptySet();
        }
        
        public String getAccessToken() { return accessToken; }
        public Set<String> getPatterns() { return patterns; }
        
        // Factory methods para patrones comunes
        public static DownloadRequest steuerDocuments(String accessToken) {
            return new DownloadRequest(accessToken, Set.of("steuer", "steuermitteilung"));
        }
        
        public static DownloadRequest depotDocuments(String accessToken) {
            return new DownloadRequest(accessToken, Set.of("depot", "buchungsanzeige", "ertragsgutschrift"));
        }
        
        public static DownloadRequest finanzreports(String accessToken) {
            return new DownloadRequest(accessToken, Set.of("finanzreport"));
        }
        
        public static DownloadRequest allDocuments(String accessToken) {
            return new DownloadRequest(accessToken, Collections.emptySet());
        }
        
        public static DownloadRequest customPatterns(String accessToken, String... patterns) {
            return new DownloadRequest(accessToken, Set.of(patterns));
        }
    }
    
    public static class DownloadResult {
        private final boolean success;
        private final List<DocumentInfo> documents;
        private final Map<String, byte[]> downloadedContents;
        private final String message;
        
        private DownloadResult(boolean success, List<DocumentInfo> documents, 
                             Map<String, byte[]> downloadedContents, String message) {
            this.success = success;
            this.documents = documents != null ? documents : Collections.emptyList();
            this.downloadedContents = downloadedContents != null ? downloadedContents : Collections.emptyMap();
            this.message = message;
        }
        
        public static DownloadResult success(List<DocumentInfo> documents, 
                                           Map<String, byte[]> downloadedContents, String message) {
            return new DownloadResult(true, documents, downloadedContents, message);
        }
        
        public static DownloadResult failure(String message) {
            return new DownloadResult(false, null, null, message);
        }
        
        public boolean isSuccess() { return success; }
        public List<DocumentInfo> getDocuments() { return documents; }
        public Map<String, byte[]> getDownloadedContents() { return downloadedContents; }
        public String getMessage() { return message; }
        
        public int getDocumentCount() { return documents.size(); }
        public int getDownloadedCount() { return downloadedContents.size(); }
        
        public boolean hasDownloads() { return !downloadedContents.isEmpty(); }
        
        public List<String> getDocumentNames() {
            return documents.stream()
                    .map(DocumentInfo::getName)
                    .collect(Collectors.toList());
        }
        
        public List<DocumentInfo> getSteuerDocuments() {
            return documents.stream()
                    .filter(DocumentInfo::isSteuerDocument)
                    .collect(Collectors.toList());
        }
        
        public List<DocumentInfo> getDepotDocuments() {
            return documents.stream()
                    .filter(DocumentInfo::isDepotDocument)
                    .collect(Collectors.toList());
        }
        
        public List<DocumentInfo> getFinanzreports() {
            return documents.stream()
                    .filter(DocumentInfo::isFinanzreport)
                    .collect(Collectors.toList());
        }
    }
}