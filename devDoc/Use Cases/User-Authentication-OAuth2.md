# Use Case: User Authentication via OAuth2
**Use Case Code:** UC-2509-0001

## Actors

## Preconditions
- User has valid credentials: client_id, client_secret, zugangsnummer, pin
- External app is registered and ready for authorization

## Main Flow
1. User enters credentials in the login form
2. System sends credentials to the OAuth2 endpoint
3. System receives access and refresh tokens
4. System checks session status
5. If TAN/2FA required, user is prompted to authorize via FotoTAN app
6. System waits for user authorization
7. Upon authorization, session is validated and activated
8. System requests secondary access token for extended scopes

## Postconditions
- User is authenticated and receives access tokens
- Session is active and ready for further API requests

## Alternative Flows
- Invalid credentials: authentication fails
- User denies authorization: session remains inactive
- Network error: user is notified and can retry

## Example Data
```
POST /oauth/token
{
  "client_id": "User_93FA2414B4B146969DFFDFEC21E8C845",
  "client_secret": "BB0AC148B2874D0ABF9D34EB799A47FD",
  "grant_type": "password",
  "username": "1188651895",
  "password": "1234567890"
}
```
Response:
```
{
  "access_token": "5375a37e-d7cd-4174-a73a-f9e1065f8df5",
  "token_type": "bearer",
  "refresh_token": "f516b39a-1c1c-42bf-b3f4-492ec2e56897",
  "expires_in": 599,
  "scope": "TWO_FACTOR"
}
```

## Bibliography
- [Atlassian: How to write effective use cases](https://www.atlassian.com/software/confluence/templates/use-case)
- [Lucidchart: Use Case Examples](https://www.lucidchart.com/blog/use-case-diagram-examples)
- [Visual Paradigm: Writing Effective Use Cases](https://www.visual-paradigm.com/guide/uml-unified-modeling-language/how-to-write-effective-use-cases/)
