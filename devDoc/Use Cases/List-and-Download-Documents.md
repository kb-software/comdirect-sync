# Use Case: List and Download Documents

**Use Case Code:** UC-2509-0002

## Actors

- User
- Comdirect API

## Preconditions

- User is authenticated and has a valid access token

## Main Flow

1. User selects the option to view documents
2. System requests the list of available documents from the API
3. System displays the list of documents with metadata (name, date, type)
4. User selects one or more documents to download
5. System requests the selected documents from the API
6. System downloads and saves the documents to the configured output path

## Postconditions

- Selected documents are downloaded and available to the user

## Alternative Flows

- No documents available: system displays a message
- Download fails: system notifies the user and allows retry
- Invalid access token: user is prompted to re-authenticate

## Example Data

Request:

```http
GET /api/messages/clients/user/v2/documents
Authorization: Bearer <access_token>
```

Response:

```json
{
  "values": [
    {
      "documentId": "E31FE40510D98771B0CEBB2E1580A35C",
      "name": "Änderung der AGBs und des PLV zum 01.04.2021",
      "dateCreation": "2021-02-01",
      "mimeType": "text/html"
    },
    {
      "documentId": "A30EF0D824275A600BA9D5F54FE8923C",
      "name": "Finanzreport Nr. 08 per 01.09.2025",
      "dateCreation": "2025-09-03",
      "mimeType": "application/pdf"
    }
  ]
}
```

## Bibliography

- [Atlassian: How to write effective use cases](https://www.atlassian.com/software/confluence/templates/use-case)
- [Lucidchart: Use Case Examples](https://www.lucidchart.com/blog/use-case-diagram-examples)
- [Visual Paradigm: Writing Effective Use Cases](https://www.visual-paradigm.com/guide/uml-unified-modeling-language/how-to-write-effective-use-cases/)
