# Use Case: View Account Information and Positions

**Use Case Code:** UC-2509-0003

## Actors

- User
- Comdirect API

## Preconditions

- User is authenticated and has a valid access token

## Main Flow

1. User selects the option to view account information
2. System requests depot (account) details from the API
3. System displays depot information (depotId, type, clientId, etc.)
4. User requests to view positions in the selected depot
5. System requests and displays positions (wkn, quantity, current price, value, etc.)

## Postconditions

- User can view account and position details

## Alternative Flows

- No depots or positions available: system displays a message
- Invalid access token: user is prompted to re-authenticate
- Network error: user is notified and can retry

## Example Data

Request:

```http
GET /api/brokerage/clients/user/v3/depots
Authorization: Bearer <access_token>
```

Response:

```json
{
  "values": [
    {
      "depotId": "8873872174FD4B30A7CE1407893A0333",
      "depotDisplayId": "8651895",
      "depotType": "STANDARD_DEPOT"
    }
  ]
}
```

Request:

```http
GET /api/brokerage/v3/depots/8873872174FD4B30A7CE1407893A0333/positions
Authorization: Bearer <access_token>
```

Response:

```json
{
  "values": [
    {
      "positionId": "CD60CD17AA77379787EF7C3D075C62B1",
      "wkn": "894983",
      "quantity": { "value": "210000", "unit": "XXX" },
      "currentPrice": { "price": { "value": "1.3845", "unit": "EUR" } },
      "currentValue": { "value": "290745.000", "unit": "EUR" }
    }
  ]
}
```

## Bibliography

- [Atlassian: How to write effective use cases](https://www.atlassian.com/software/confluence/templates/use-case)
- [Lucidchart: Use Case Examples](https://www.lucidchart.com/blog/use-case-diagram-examples)
- [Visual Paradigm: Writing Effective Use Cases](https://www.visual-paradigm.com/guide/uml-unified-modeling-language/how-to-write-effective-use-cases/)
