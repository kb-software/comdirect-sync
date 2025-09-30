# Use Case: Download JSON Data from Comdirect

**Use Case Code:** UC-2509-0005

## Actors

- User
- Comdirect API

## Preconditions

- User is authenticated and has a valid access token

## Main Flow

1. User selects the option to download JSON data
2. System sends a request to the relevant Comdirect API endpoint
3. System receives JSON response data
4. System saves the JSON data to the configured output path
5. User is notified of successful download

## Postconditions

- JSON data is downloaded and available to the user

## Alternative Flows

- Download fails: system notifies the user and allows retry
- Invalid access token: user is prompted to re-authenticate
- Network error: user is notified and can retry

## Example Data

Request:

```http
GET /api/messages/clients/user/v2/documents
Authorization: Bearer <access_token>
```

Response:

```json
{
  "paging": { "index": 0, "matches": 1178 },
  "values": [
    {
      "documentId": "E31FE40510D98771B0CEBB2E1580A35C",
      "name": "Änderung der AGBs und des PLV zum 01.04.2021",
      "dateCreation": "2021-02-01",
      "mimeType": "text/html"
    }
  ]
}
```

## Bibliography

- [Atlassian: How to write effective use cases](https://www.atlassian.com/software/confluence/templates/use-case)
- [Lucidchart: Use Case Examples](https://www.lucidchart.com/blog/use-case-diagram-examples)
- [Visual Paradigm: Writing Effective Use Cases](https://www.visual-paradigm.com/guide/uml-unified-modeling-language/how-to-write-effective-use-cases/)
