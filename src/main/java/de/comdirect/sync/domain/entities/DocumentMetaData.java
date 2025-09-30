package de.comdirect.sync.domain.entities;

import java.util.Objects;

/**
 * Value Object que representa metadatos de un documento
 */
public class DocumentMetaData {
    private final boolean archived;
    private final boolean alreadyRead;
    private final boolean predocumentExists;

    private DocumentMetaData(Builder builder) {
        this.archived = builder.archived;
        this.alreadyRead = builder.alreadyRead;
        this.predocumentExists = builder.predocumentExists;
    }

    public static Builder builder() {
        return new Builder();
    }

    public boolean isArchived() {
        return archived;
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
        DocumentMetaData that = (DocumentMetaData) o;
        return archived == that.archived &&
                alreadyRead == that.alreadyRead &&
                predocumentExists == that.predocumentExists;
    }

    @Override
    public int hashCode() {
        return Objects.hash(archived, alreadyRead, predocumentExists);
    }

    @Override
    public String toString() {
        return "DocumentMetaData{" +
                "archived=" + archived +
                ", alreadyRead=" + alreadyRead +
                ", predocumentExists=" + predocumentExists +
                '}';
    }

    public static class Builder {
        private boolean archived = false;
        private boolean alreadyRead = false;
        private boolean predocumentExists = false;

        public Builder archived(boolean archived) {
            this.archived = archived;
            return this;
        }

        public Builder alreadyRead(boolean alreadyRead) {
            this.alreadyRead = alreadyRead;
            return this;
        }

        public Builder predocumentExists(boolean predocumentExists) {
            this.predocumentExists = predocumentExists;
            return this;
        }

        public DocumentMetaData build() {
            return new DocumentMetaData(this);
        }
    }
}