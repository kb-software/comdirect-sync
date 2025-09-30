package de.comdirect.sync.infrastructure.adapters.repositories;

import de.comdirect.sync.domain.entities.DocumentInfo;
import de.comdirect.sync.domain.entities.DocumentMetaData;
import de.comdirect.sync.domain.repositories.ComdirectDocumentRepository;
import de.comdirect.sync.domain.valueobjects.DocumentId;
import de.comdirect.sync.domain.exceptions.InvalidUserDataException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Implementación HTTP del repositorio de documentos
 * Realiza llamadas reales a la API de Comdirect para obtener y descargar documentos
 * 
 * Endpoints implementados:
 * - GET /api/messages/clients/user/v2/documents (9.1.1 Abruf PostBox)
 * - GET /api/messages/v2/documents/{id} (9.1.2 Abruf eines Dokuments)
 */
public class ComdirectDocumentRepositoryHttp implements ComdirectDocumentRepository {
    
    private static final Logger logger = LoggerFactory.getLogger(ComdirectDocumentRepositoryHttp.class);
    private final RestTemplate restTemplate;
    private final String baseUrl;
    
    public ComdirectDocumentRepositoryHttp(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
        this.baseUrl = "https://api.comdirect.de";
    }
    
    public ComdirectDocumentRepositoryHttp(RestTemplate restTemplate, String baseUrl) {
        this.restTemplate = restTemplate;
        this.baseUrl = baseUrl;
    }
    
    @Override
    public List<DocumentInfo> getDocuments(String accessToken) {
        logger.info("=== OBTENER DOCUMENTOS HTTP ===");
        String url = baseUrl + "/api/messages/clients/user/v2/documents";
        logger.info("Enviando petición a: {}", url);
        
        try {
            HttpHeaders headers = createAuthHeaders(accessToken);
            HttpEntity<String> entity = new HttpEntity<>(headers);
            
            logger.debug("HTTP GET {}", url);
            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, entity, Map.class);
            
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                return parseDocumentsResponse(response.getBody());
            } else {
                logger.error("Error en respuesta HTTP: {}", response.getStatusCode());
                throw new InvalidUserDataException("Failed to get documents: HTTP " + response.getStatusCode());
            }
            
        } catch (Exception e) {
            logger.error("Error obteniendo documentos: {}", e.getMessage());
            throw new InvalidUserDataException("Failed to get documents: " + e.getMessage());
        }
    }
    
    @Override
    public List<DocumentInfo> getDocumentsByPatterns(String accessToken, Set<String> patterns) {
        List<DocumentInfo> allDocuments = getDocuments(accessToken);
        
        if (patterns == null || patterns.isEmpty()) {
            return allDocuments;
        }
        
        return allDocuments.stream()
                .filter(doc -> doc.matchesPattern(patterns))
                .collect(Collectors.toList());
    }
    
    @Override
    public byte[] downloadDocument(String accessToken, DocumentId documentId) {
        logger.info("=== DESCARGAR DOCUMENTO HTTP ===");
        String url = baseUrl + "/api/messages/v2/documents/" + documentId.getValue();
        logger.info("Enviando petición a: {}", url);
        
        try {
            HttpHeaders headers = createAuthHeaders(accessToken);
            headers.setAccept(Arrays.asList(MediaType.APPLICATION_PDF));
            HttpEntity<String> entity = new HttpEntity<>(headers);
            
            logger.debug("HTTP GET {}", url);
            ResponseEntity<byte[]> response = restTemplate.exchange(url, HttpMethod.GET, entity, byte[].class);
            
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                logger.info("✅ Document downloaded successfully: {} bytes", response.getBody().length);
                return response.getBody();
            } else {
                logger.error("Error downloading document: HTTP {}", response.getStatusCode());
                throw new InvalidUserDataException("Failed to download document: HTTP " + response.getStatusCode());
            }
            
        } catch (Exception e) {
            logger.error("Error downloading document {}: {}", documentId.getValue(), e.getMessage());
            throw new InvalidUserDataException("Failed to download document: " + e.getMessage());
        }
    }
    
    @Override
    public Optional<DocumentInfo> findDocumentById(DocumentId documentId) {
        List<DocumentInfo> allDocuments = getDocuments(""); // This will require a valid token
        return allDocuments.stream()
                .filter(doc -> doc.getDocumentId().equals(documentId))
                .findFirst();
    }
    
    @Override
    public List<DocumentInfo> getDownloadableDocuments(String accessToken) {
        return getDocuments(accessToken).stream()
                .filter(DocumentInfo::isDownloadable)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<DocumentInfo> getDocumentsByCategory(String accessToken, int categoryId) {
        return getDocuments(accessToken).stream()
                .filter(doc -> doc.getCategoryId() == categoryId)
                .collect(Collectors.toList());
    }
    
    /**
     * Crea headers HTTP con autenticación y metadata requerida por Comdirect
     */
    private HttpHeaders createAuthHeaders(String accessToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(accessToken);
        
        // Header requerido por Comdirect API
        String requestInfo = String.format(
            "{\"clientRequestId\":{\"sessionId\":\"%s\",\"requestId\":\"%s\"}}",
            generateSessionId(),
            generateRequestId()
        );
        headers.set("x-http-request-info", requestInfo);
        
        return headers;
    }
    
    /**
     * Parsea la respuesta JSON de la API de documentos a objetos DocumentInfo
     */
    @SuppressWarnings("unchecked")
    private List<DocumentInfo> parseDocumentsResponse(Map<String, Object> responseBody) {
        List<DocumentInfo> documents = new ArrayList<>();
        
        try {
            // La respuesta tiene estructura: {"paging": {...}, "values": [...]}
            List<Map<String, Object>> documentList = (List<Map<String, Object>>) responseBody.get("values");
            
            if (documentList != null) {
                for (Map<String, Object> docData : documentList) {
                    DocumentInfo document = parseDocumentInfo(docData);
                    if (document != null) {
                        documents.add(document);
                    }
                }
            }

            logger.info("✅ Parsed {} documents from response", documents.size());

        } catch (Exception e) {
            logger.error("Error parsing documents response: {}", e.getMessage());
            throw new InvalidUserDataException("Failed to parse documents response: " + e.getMessage());
        }
        
        return documents;
    }
    
    /**
     * Convierte un Map JSON a objeto DocumentInfo
     */
    @SuppressWarnings("unchecked")
    private DocumentInfo parseDocumentInfo(Map<String, Object> docData) {
        try {
            String documentId = (String) docData.get("documentId");
            String name = (String) docData.get("name");
            String dateCreation = (String) docData.get("dateCreation");
            String mimeType = (String) docData.get("mimeType");
            Boolean deletable = (Boolean) docData.get("deletable");
            Boolean advertisement = (Boolean) docData.get("advertisement");
            Integer categoryId = (Integer) docData.get("categoryId");
            
            // Parsear metadata si existe
            DocumentMetaData metaData = null;
            Map<String, Object> metaDataMap = (Map<String, Object>) docData.get("documentMetaData");
            if (metaDataMap != null) {
                Boolean archived = (Boolean) metaDataMap.get("archived");
                Boolean alreadyRead = (Boolean) metaDataMap.get("alreadyRead");
                Boolean predocumentExists = (Boolean) metaDataMap.get("predocumentExists");
                
                metaData = DocumentMetaData.builder()
                        .archived(archived != null ? archived : false)
                        .alreadyRead(alreadyRead != null ? alreadyRead : false)
                        .predocumentExists(predocumentExists != null ? predocumentExists : false)
                        .build();
            }
            
            return DocumentInfo.builder()
                    .documentId(documentId)
                    .name(name)
                    .dateCreation(dateCreation)
                    .mimeType(mimeType)
                    .deletable(deletable != null ? deletable : false)
                    .advertisement(advertisement != null ? advertisement : false)
                    .categoryId(categoryId != null ? categoryId : 0)
                    .documentMetaData(metaData)
                    .build();
                    
        } catch (Exception e) {
            logger.warn("Error parsing individual document: {}", e.getMessage());
            return null;
        }
    }
    
    /**
     * Genera un session ID único para las peticiones HTTP
     */
    private String generateSessionId() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 32);
    }
    
    /**
     * Genera un request ID único para las peticiones HTTP
     */
    private String generateRequestId() {
        return String.valueOf(System.currentTimeMillis() % 1000000000L);
    }
}