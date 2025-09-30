# Use Case: Manage Application Configuration

**Use Case Code:** UC-2509-0006

## Actors

- User
- Application

## Preconditions

- Application is installed and running

## Main Flow

1. User opens the configuration settings in the application
2. System displays current configuration (e.g., client_id, client_secret, zugangsnummer, pin, output_path, xml_file, etc.)
3. User edits configuration values (e.g., updates credentials, changes output path)
4. System validates the input and saves the configuration to a file (XML, JSON, or INI)
5. System loads configuration from the file on startup

## Postconditions

- Configuration is updated and stored for future use

## Alternative Flows

- Invalid input: system displays error and prompts for correction
- File write error: system notifies user and allows retry

## Example Data

Example configuration (JSON):

```json
{
  "comdirect": {
    "client_id": "User_93FA2414B4B146969DFFDFEC21E8C845",
    "client_secret": "BB0AC148B2874D0ABF9D34EB799A47FD",
    "pin": "1234567890",
    "zugangsnummer": "1188651895",
    "oauth_url": "https://api.comdirect.de",
    "url": "https://api.comdirect.de/api",
    "document_types": [
      "Dividendengutschrift",
      "Buchungsanzeige",
      "Finanzreport",
      "Steuermitteilung",
      "Ertragsgutschrift"
    ]
  },
  "app": {
    "output_path": "/Users/username/Documents/ComdirectDocs",
    "xml_file": "sample.xml",
    "api": {
      "url": "https://otherapp/api",
      "api_key": "myapikey",
      "username": "user",
      "Password": "password"
    }
  }
}
```
