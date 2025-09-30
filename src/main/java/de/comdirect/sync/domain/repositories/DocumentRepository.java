package de.comdirect.sync.domain.repositories;

import de.comdirect.sync.domain.entities.Document;
import de.comdirect.sync.domain.valueobjects.DateRange;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio para operaciones con documentos de Comdirect.
 * Define el contrato para acceder a los documentos a través de la API.
 */
public interface DocumentRepository {
    
    /**
     * Lista documentos en un rango de fechas específico
     * 
     * @param dateRange Rango de fechas para filtrar documentos
     * @param paging Información de paginación (índice inicial)
     * @param maxResults Máximo número de resultados por página
     * @return Lista de documentos encontrados
     */
    List<Document> findDocumentsByDateRange(DateRange dateRange, int paging, int maxResults);
    
    /**
     * Descarga el contenido de un documento específico
     * 
     * @param documentId ID del documento a descargar
     * @param acceptMimeType Tipo MIME esperado (ej: application/pdf)
     * @return Contenido del documento como array de bytes
     */
    Optional<byte[]> downloadDocument(String documentId, String acceptMimeType);
    
    /**
     * Obtiene la página previa de un documento (si existe)
     * 
     * @param documentId ID del documento
     * @return Contenido HTML de la página previa
     */
    Optional<String> getDocumentPredocument(String documentId);
    
    /**
     * Busca documentos que coincidan con patrones específicos en el nombre
     * 
     * @param dateRange Rango de fechas para filtrar
     * @param patterns Patrones a buscar en el nombre del documento
     * @param mimeType Tipo MIME a filtrar (ej: application/pdf)
     * @param maxResults Máximo número de resultados
     * @return Lista de documentos que coinciden con los criterios
     */
    List<Document> findDocumentsByPatterns(DateRange dateRange, String[] patterns, 
                                         String mimeType, int maxResults);
    
    /**
     * Verifica la conectividad con el servicio de documentos
     * 
     * @return true si el servicio está disponible
     */
    boolean isServiceAvailable();
}