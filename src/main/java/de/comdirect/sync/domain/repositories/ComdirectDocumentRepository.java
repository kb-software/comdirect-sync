package de.comdirect.sync.domain.repositories;

import de.comdirect.sync.domain.entities.DocumentInfo;
import de.comdirect.sync.domain.valueobjects.DocumentId;

import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Repositorio para manejar documentos de Comdirect
 */
public interface ComdirectDocumentRepository {
    
    /**
     * Obtiene la lista de documentos disponibles
     * Corresponde al endpoint 9.1.1 Abruf PostBox
     */
    List<DocumentInfo> getDocuments(String accessToken);
    
    /**
     * Obtiene la lista de documentos filtrados por patrones
     */
    List<DocumentInfo> getDocumentsByPatterns(String accessToken, Set<String> patterns);
    
    /**
     * Descarga un documento específico
     * Corresponde al endpoint 9.1.2 Abruf eines Dokuments
     */
    byte[] downloadDocument(String accessToken, DocumentId documentId);
    
    /**
     * Obtiene un documento por su ID
     */
    Optional<DocumentInfo> findDocumentById(DocumentId documentId);
    
    /**
     * Obtiene solo documentos PDF descargables
     */
    List<DocumentInfo> getDownloadableDocuments(String accessToken);
    
    /**
     * Obtiene documentos por categoría
     */
    List<DocumentInfo> getDocumentsByCategory(String accessToken, int categoryId);
}