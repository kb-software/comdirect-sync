# Use Case: User Authentication

## Actors

- User  
- External Application (Comdirect FotoTAN)

## Preconditions

- User has valid credentials: `client_id`, `client_secret`, `username` (zugangsnummer), and `password` (pin).
- External Application is registered and authorized to request access.

## Main Flow

1. User initiates authentication by providing:
   - `client_id`
   - `client_secret`
   - `username` (zugangsnummer)
   - `password` (pin)
2. System sends these credentials to the External Application (Authorization Server).
3. External Application prompts the user to allow access.
4. User reviews and grants authorization.
5. System waits for the user’s authorization response.
6. Upon approval, authentication proceeds and access token is issued.

## Postconditions

- User is authenticated and receives an access token.
- System can now access protected resources on behalf of the user.

## Alternative Flow

- If credentials are invalid or user denies access, authentication fails and no token is issued