package de.comdirect.sync.domain.entities;

import de.comdirect.sync.domain.valueobjects.DocumentId;
import de.comdirect.sync.domain.exceptions.InvalidUserDataException;

import java.time.LocalDate;
import java.util.Objects;
import java.util.Set;

/**
 * Entidad que representa información de un documento de Comdirect
 */
public class DocumentInfo {
    private final DocumentId documentId;
    private final String name;
    private final LocalDate dateCreation;
    private final String mimeType;
    private final boolean deletable;
    private final boolean advertisement;
    private final DocumentMetaData documentMetaData;
    private final int categoryId;

    private DocumentInfo(Builder builder) {
        this.documentId = DocumentId.of(builder.documentId);
        this.name = builder.name;
        this.dateCreation = builder.dateCreation;
        this.mimeType = builder.mimeType;
        this.deletable = builder.deletable;
        this.advertisement = builder.advertisement;
        this.documentMetaData = builder.documentMetaData;
        this.categoryId = builder.categoryId;
    }

    public static Builder builder() {
        return new Builder();
    }

    public boolean matchesPattern(Set<String> patterns) {
        if (patterns == null || patterns.isEmpty()) {
            return true;
        }
        
        String nameLower = name.toLowerCase();
        return patterns.stream()
                .anyMatch(pattern -> nameLower.contains(pattern.toLowerCase()));
    }

    public boolean isPdf() {
        return "application/pdf".equals(mimeType);
    }

    public boolean isHtml() {
        return "text/html".equals(mimeType);
    }

    public boolean isDownloadable() {
        return isPdf() && !advertisement;
    }

    public boolean isSteuerDocument() {
        return matchesPattern(Set.of("steuer", "steuermitteilung"));
    }

    public boolean isDepotDocument() {
        return matchesPattern(Set.of("depot", "buchungsanzeige", "ertragsgutschrift"));
    }

    public boolean isFinanzreport() {
        return matchesPattern(Set.of("finanzreport"));
    }

    // Getters
    public DocumentId getDocumentId() { return documentId; }
    public String getName() { return name; }
    public LocalDate getDateCreation() { return dateCreation; }
    public String getMimeType() { return mimeType; }
    public boolean isDeletable() { return deletable; }
    public boolean isAdvertisement() { return advertisement; }
    public DocumentMetaData getDocumentMetaData() { return documentMetaData; }
    public int getCategoryId() { return categoryId; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DocumentInfo that = (DocumentInfo) o;
        return Objects.equals(documentId, that.documentId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(documentId);
    }

    @Override
    public String toString() {
        return "DocumentInfo{" +
                "documentId=" + documentId +
                ", name='" + name + '\'' +
                ", dateCreation=" + dateCreation +
                ", mimeType='" + mimeType + '\'' +
                ", categoryId=" + categoryId +
                ", downloadable=" + isDownloadable() +
                '}';
    }

    public static class Builder {
        private String documentId;
        private String name;
        private LocalDate dateCreation;
        private String mimeType;
        private boolean deletable = false;
        private boolean advertisement = false;
        private DocumentMetaData documentMetaData;
        private int categoryId;

        public Builder documentId(String documentId) {
            if (documentId == null || documentId.trim().isEmpty()) {
                throw new InvalidUserDataException("Document ID cannot be null or empty");
            }
            this.documentId = documentId.trim();
            return this;
        }

        public Builder name(String name) {
            if (name == null || name.trim().isEmpty()) {
                throw new InvalidUserDataException("Document name cannot be null or empty");
            }
            this.name = name.trim();
            return this;
        }

        public Builder dateCreation(String dateCreation) {
            if (dateCreation == null || dateCreation.trim().isEmpty()) {
                throw new InvalidUserDataException("Date creation cannot be null or empty");
            }
            try {
                this.dateCreation = LocalDate.parse(dateCreation.trim());
            } catch (Exception e) {
                throw new InvalidUserDataException("Invalid date format: " + dateCreation);
            }
            return this;
        }

        public Builder dateCreation(LocalDate dateCreation) {
            if (dateCreation == null) {
                throw new InvalidUserDataException("Date creation cannot be null");
            }
            this.dateCreation = dateCreation;
            return this;
        }

        public Builder mimeType(String mimeType) {
            if (mimeType == null || mimeType.trim().isEmpty()) {
                throw new InvalidUserDataException("MIME type cannot be null or empty");
            }
            this.mimeType = mimeType.trim();
            return this;
        }

        public Builder deletable(boolean deletable) {
            this.deletable = deletable;
            return this;
        }

        public Builder advertisement(boolean advertisement) {
            this.advertisement = advertisement;
            return this;
        }

        public Builder documentMetaData(DocumentMetaData documentMetaData) {
            this.documentMetaData = documentMetaData;
            return this;
        }

        public Builder categoryId(int categoryId) {
            if (categoryId <= 0) {
                throw new InvalidUserDataException("Category ID must be positive");
            }
            this.categoryId = categoryId;
            return this;
        }

        public DocumentInfo build() {
            if (documentId == null) {
                throw new InvalidUserDataException("Document ID is required");
            }
            if (name == null) {
                throw new InvalidUserDataException("Document name is required");
            }
            if (dateCreation == null) {
                throw new InvalidUserDataException("Date creation is required");
            }
            if (mimeType == null) {
                throw new InvalidUserDataException("MIME type is required");
            }
            if (documentMetaData == null) {
                this.documentMetaData = DocumentMetaData.builder().build();
            }
            return new DocumentInfo(this);
        }
    }
}