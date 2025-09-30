# Use Case: Handle Session Timeout and Refresh

**Use Case Code:** UC-2509-0004

## Actors

- User
- Comdirect API

## Preconditions

- User is authenticated and using the application

## Main Flow

1. User performs an action after a period of inactivity
2. System sends an API request and receives a timeout or 404 response
3. System displays a popup notifying the user of the session timeout
4. User clicks the "continue" button to refresh the session
5. System sends a request to refresh the session (using refresh token or re-authentication)
6. If successful, user can continue using the application

## Postconditions

- Session is refreshed and user can continue

## Alternative Flows

- Session refresh fails: user is prompted to log in again
- Network error: user is notified and can retry

## Example Data

Response:

```json
{
  "error": "session_timeout",
  "error_description": "Session expired due to inactivity."
}
```

Popup:

```text
Session expired. Please click 'continue' to refresh your session.
[Continue]
```

Refresh request:

```http
POST /oauth/token
{
  "grant_type": "refresh_token",
  "refresh_token": "f516b39a-1c1c-42bf-b3f4-492ec2e56897"
}
```
