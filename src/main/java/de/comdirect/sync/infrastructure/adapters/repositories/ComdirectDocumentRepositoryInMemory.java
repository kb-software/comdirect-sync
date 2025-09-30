package de.comdirect.sync.infrastructure.adapters.repositories;

import de.comdirect.sync.domain.entities.DocumentInfo;
import de.comdirect.sync.domain.entities.DocumentMetaData;
import de.comdirect.sync.domain.repositories.ComdirectDocumentRepository;
import de.comdirect.sync.domain.valueobjects.DocumentId;
import de.comdirect.sync.domain.exceptions.InvalidUserDataException;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Implementación InMemory del repositorio de documentos para testing
 * Contiene datos de ejemplo basados en la documentación de Comdirect
 */
public class ComdirectDocumentRepositoryInMemory implements ComdirectDocumentRepository {
    
    private final Map<String, DocumentInfo> documents = new ConcurrentHashMap<>();
    private final Map<String, byte[]> documentContents = new ConcurrentHashMap<>();
    private final boolean isProductionMode;
    
    public ComdirectDocumentRepositoryInMemory() {
        this(false); // Por defecto es modo desarrollo
    }
    
    public ComdirectDocumentRepositoryInMemory(boolean isProductionMode) {
        this.isProductionMode = isProductionMode;
        initializeTestDocuments();
    }
    
    @Override
    public List<DocumentInfo> getDocuments(String accessToken) {
        validateAccessToken(accessToken);
        return new ArrayList<>(documents.values());
    }
    
    @Override
    public List<DocumentInfo> getDocumentsByPatterns(String accessToken, Set<String> patterns) {
        validateAccessToken(accessToken);
        
        return documents.values().stream()
                .filter(doc -> doc.matchesPattern(patterns))
                .collect(Collectors.toList());
    }
    
    @Override
    public byte[] downloadDocument(String accessToken, DocumentId documentId) {
        validateAccessToken(accessToken);
        
        DocumentInfo document = documents.get(documentId.getValue());
        if (document == null) {
            throw new InvalidUserDataException("Document not found: " + documentId);
        }
        
        if (!document.isPdf()) {
            throw new InvalidUserDataException("Document is not downloadable (not PDF): " + documentId);
        }
        
        // Simular contenido PDF
        byte[] content = documentContents.get(documentId.getValue());
        if (content == null) {
            // Generar contenido simulado
            content = generateMockPdfContent(document);
            documentContents.put(documentId.getValue(), content);
        }
        
        return content;
    }
    
    @Override
    public Optional<DocumentInfo> findDocumentById(DocumentId documentId) {
        return Optional.ofNullable(documents.get(documentId.getValue()));
    }
    
    @Override
    public List<DocumentInfo> getDownloadableDocuments(String accessToken) {
        validateAccessToken(accessToken);
        
        return documents.values().stream()
                .filter(DocumentInfo::isDownloadable)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<DocumentInfo> getDocumentsByCategory(String accessToken, int categoryId) {
        validateAccessToken(accessToken);
        
        return documents.values().stream()
                .filter(doc -> doc.getCategoryId() == categoryId)
                .collect(Collectors.toList());
    }
    
    private void validateAccessToken(String accessToken) {
        // Para testing, aceptamos tokens válidos
        if (accessToken == null || accessToken.trim().isEmpty()) {
            throw new InvalidUserDataException("Access token cannot be null or empty");
        }
        
        // Simular validación de token - en realidad se haría contra el auth repository
        if (!isValidTestToken(accessToken)) {
            throw new InvalidUserDataException("Invalid or expired access token");
        }
    }
    
    private boolean isValidTestToken(String accessToken) {
        // En modo producción: aceptar cualquier token válido (no vacío)
        if (isProductionMode) {
            return accessToken != null && !accessToken.trim().isEmpty() && accessToken.length() > 10;
        }
        
        // En modo desarrollo: usar tokens hardcodeados para testing
        return "5375a37e-d7cd-4174-a73a-f9e1065f8df5".equals(accessToken) ||
               "b4d47dfe-4eb3-4a2e-ae7b-ca9b0a5200bc".equals(accessToken) ||
               "32734246-6015-4247-92fc-021ac8ce5e27".equals(accessToken);
    }
    
    private byte[] generateMockPdfContent(DocumentInfo document) {
        // Generar contenido PDF simulado
        String content = "Mock PDF Content for: " + document.getName() + "\n" +
                        "Document ID: " + document.getDocumentId().getValue() + "\n" +
                        "Date: " + document.getDateCreation() + "\n" +
                        "Category: " + document.getCategoryId();
        return content.getBytes();
    }
    
    private void initializeTestDocuments() {
        // Documentos basados en los ejemplos de la documentación
        
        // Documentos de impuestos (Steuer)
        addDocument("107607F6E9E2CC475A0C877C43529763",
                   "Steuermitteilung 853055 (CANON INC. SHARES O.N.) vom 25.08.2025",
                   "2025-08-26", "application/pdf", 252);
                   
        addDocument("CA6F4ADEFF0B1789A181B49A5F87BCC7",
                   "Steuermitteilung A1H8BN (HSBC MSCI INDONES. UC.ETF) vom 29.08.2025",
                   "2025-08-30", "application/pdf", 252);
        
        // Documentos de depot
        addDocument("C9E0E17B71EA81FCE29D8D0A7A5861FC",
                   "Buchungsanzeige 885166 (HYUNDAI MOT.0,5N.VTG GDRS) vom 01.08.2025",
                   "2025-08-01", "application/pdf", 612);
                   
        addDocument("080AE60167A60F06CBC7229151A1BD4E",
                   "Ertragsgutschrift 3.100 St. A1H8BN (HSBC MSCI INDONES. UC.ETF) vom 29.08.2025",
                   "2025-08-30", "application/pdf", 700);
        
        // Finanzreports
        addDocument("A30EF0D824275A600BA9D5F54FE8923C",
                   "Finanzreport Nr. 08 per 01.09.2025",
                   "2025-09-03", "application/pdf", 201);
        
        // Documentos HTML (no descargables)
        addDocument("E31FE40510D98771B0CEBB2E1580A35C",
                   "Änderung der AGBs und des PLV zum 01.04.2021",
                   "2021-02-01", "text/html", 301);
                   
        addDocument("D82DEDD7A51975828E496E4B9E7DFEF1",
                   "Ihre iTAN-Liste wird bald ungültig - jetzt schnell photoTAN aktivieren",
                   "2019-08-12", "text/html", 301);
        
        // Más documentos de fondos
        addDocument("EB96E7BBB5EF455B829F476D401E0EC4",
                   "Wichtige Fondsinformation - Änderung A1T8Z2 (BNPP RU EQU. CL.CAP)",
                   "2025-07-30", "application/pdf", 613);
                   
        addDocument("349A1C88F5BF0015166FEC09EEE7D5F6",
                   "Wichtige Fondsinformation - Änderung A1T8Z2 (BNPP RU EQU. CL.CAP)",
                   "2025-05-13", "application/pdf", 613);
    }
    
    private void addDocument(String id, String name, String dateCreation, 
                           String mimeType, int categoryId) {
        DocumentInfo document = DocumentInfo.builder()
                .documentId(id)
                .name(name)
                .dateCreation(dateCreation)
                .mimeType(mimeType)
                .deletable(false)
                .advertisement(false)
                .categoryId(categoryId)
                .documentMetaData(DocumentMetaData.builder()
                        .archived(false)
                        .alreadyRead(false)
                        .predocumentExists(false)
                        .build())
                .build();
                
        documents.put(id, document);
    }
    
    // Métodos auxiliares para testing
    public void clearAll() {
        documents.clear();
        documentContents.clear();
        initializeTestDocuments();
    }
    
    public int getDocumentCount() {
        return documents.size();
    }
    
    public List<DocumentInfo> getSteuerDocuments() {
        return documents.values().stream()
                .filter(DocumentInfo::isSteuerDocument)
                .collect(Collectors.toList());
    }
    
    public List<DocumentInfo> getDepotDocuments() {
        return documents.values().stream()
                .filter(DocumentInfo::isDepotDocument)
                .collect(Collectors.toList());
    }
}