package de.comdirect.sync.domain.entities;

import de.comdirect.sync.domain.valueobjects.DocumentMetadata;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Entidad de dominio que representa un documento de Comdirect.
 * Basada en la especificación oficial de la API REST de Comdirect (sección 9.2.1).
 */
public class Document {
    
    private final String documentId;        // UUID del documento
    private final String name;              // Betreff/Título del documento
    private final LocalDateTime dateCreation; // Eingangsdatum/Erstellungsdatum
    private final String mimeType;          // MimeType del documento
    private final boolean deleteable;       // TRUE si el documento es eliminable
    private final boolean advertisement;    // TRUE si es publicidad
    private final DocumentMetadata documentMetadata;
    
    public Document(String documentId, String name, LocalDateTime dateCreation, 
                   String mimeType, boolean deleteable, boolean advertisement,
                   DocumentMetadata documentMetadata) {
        this.documentId = Objects.requireNonNull(documentId, "documentId no puede ser null");
        this.name = Objects.requireNonNull(name, "name no puede ser null");
        this.dateCreation = Objects.requireNonNull(dateCreation, "dateCreation no puede ser null");
        this.mimeType = Objects.requireNonNull(mimeType, "mimeType no puede ser null");
        this.deleteable = deleteable;
        this.advertisement = advertisement;
        this.documentMetadata = documentMetadata;
    }
    
    /**
     * Verifica si el documento coincide con alguno de los patrones especificados
     */
    public boolean matchesPattern(String[] patterns) {
        if (patterns == null || patterns.length == 0) {
            return true; // Sin filtros, acepta todos
        }
        
        String nameToCheck = name.toLowerCase();
        for (String pattern : patterns) {
            if (nameToCheck.contains(pattern.toLowerCase().trim())) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Verifica si el documento es un PDF
     */
    public boolean isPdf() {
        return "application/pdf".equalsIgnoreCase(mimeType);
    }
    
    /**
     * Verifica si el documento ya fue leído
     */
    public boolean isRead() {
        return documentMetadata != null && documentMetadata.isAlreadyRead();
    }
    
    /**
     * Verifica si el documento está archivado
     */
    public boolean isArchived() {
        return documentMetadata != null && documentMetadata.isArchived();
    }
    
    /**
     * Obtiene el nombre de archivo sugerido para la descarga
     */
    public String getSuggestedFileName() {
        String cleanName = name.replaceAll("[^a-zA-Z0-9äöüÄÖÜß\\-_\\s]", "")
                              .replaceAll("\\s+", "_")
                              .trim();
        
        String extension = getFileExtension();
        return String.format("%s_%s%s", 
            dateCreation.toLocalDate().toString(),
            cleanName,
            extension
        );
    }
    
    private String getFileExtension() {
        return switch (mimeType.toLowerCase()) {
            case "application/pdf" -> ".pdf";
            case "text/html" -> ".html";
            case "text/plain" -> ".txt";
            default -> "";
        };
    }
    
    // Getters
    public String getDocumentId() {
        return documentId;
    }
    
    public String getName() {
        return name;
    }
    
    public LocalDateTime getDateCreation() {
        return dateCreation;
    }
    
    public String getMimeType() {
        return mimeType;
    }
    
    public boolean isDeleteable() {
        return deleteable;
    }
    
    public boolean isAdvertisement() {
        return advertisement;
    }
    
    public DocumentMetadata getDocumentMetadata() {
        return documentMetadata;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Document document = (Document) o;
        return Objects.equals(documentId, document.documentId);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(documentId);
    }
    
    @Override
    public String toString() {
        return String.format("Document{id='%s', name='%s', date=%s, mimeType='%s', read=%s}", 
            documentId, name, dateCreation.toLocalDate(), mimeType, isRead());
    }
}