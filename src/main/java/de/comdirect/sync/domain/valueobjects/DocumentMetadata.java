package de.comdirect.sync.domain.valueobjects;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Value Object que representa los metadatos de un documento de Comdirect.
 * Basada en la especificación oficial de la API REST de Comdirect (sección 9.2.2).
 */
public class DocumentMetadata {
    
    private final boolean archived;                    // TRUE si está archivado
    private final LocalDateTime dateRead;             // Fecha de lectura (nullable)
    private final boolean alreadyRead;                // TRUE si ya fue leído
    private final boolean predocumentExists;          // TRUE si existe página previa HTML
    
    public DocumentMetadata(boolean archived, LocalDateTime dateRead, 
                           boolean alreadyRead, boolean predocumentExists) {
        this.archived = archived;
        this.dateRead = dateRead;
        this.alreadyRead = alreadyRead;
        this.predocumentExists = predocumentExists;
    }
    
    /**
     * Factory method para crear metadatos de un documento no leído
     */
    public static DocumentMetadata unread() {
        return new DocumentMetadata(false, null, false, false);
    }
    
    /**
     * Factory method para crear metadatos de un documento leído
     */
    public static DocumentMetadata read(LocalDateTime readDate) {
        return new DocumentMetadata(false, readDate, true, false);
    }
    
    /**
     * Factory method para crear metadatos de un documento archivado
     */
    public static DocumentMetadata archived(LocalDateTime readDate) {
        return new DocumentMetadata(true, readDate, true, false);
    }
    
    /**
     * Verifica si el documento tiene página previa HTML disponible
     */
    public boolean hasPredocument() {
        return predocumentExists;
    }
    
    /**
     * Obtiene el estado del documento como texto descriptivo
     */
    public String getStatusDescription() {
        if (archived) {
            return "Archivado";
        } else if (alreadyRead) {
            return "Leído";
        } else {
            return "No leído";
        }
    }
    
    // Getters
    public boolean isArchived() {
        return archived;
    }
    
    public LocalDateTime getDateRead() {
        return dateRead;
    }
    
    public boolean isAlreadyRead() {
        return alreadyRead;
    }
    
    public boolean isPredocumentExists() {
        return predocumentExists;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DocumentMetadata that = (DocumentMetadata) o;
        return archived == that.archived &&
               alreadyRead == that.alreadyRead &&
               predocumentExists == that.predocumentExists &&
               Objects.equals(dateRead, that.dateRead);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(archived, dateRead, alreadyRead, predocumentExists);
    }
    
    @Override
    public String toString() {
        return String.format("DocumentMetadata{status='%s', dateRead=%s, predocument=%s}", 
            getStatusDescription(), 
            dateRead != null ? dateRead.toLocalDate() : "null",
            predocumentExists);
    }
}