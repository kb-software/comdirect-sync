# comdirect REST API - Schnittstellenspezifikation

## 2.3 Anlage Validierung einer Session-TAN

POST `{URL-Präfix}/session/clients/{clientId}/v1/sessions/{sessionId}/validate`

**Beschreibung**: Für das nun bekannte Session-Objekt wird in diesem Schritt eine TAN-Challenge angefordert.

> Bitte beachten:
> Das Abrufen von fünf TAN-Challenges ohne zwischenzeitliche Entwertung einer korrekten TAN führt zur Sperrung des Onlinebanking-Zugangs.

**REQUEST (= Anlage Validierung einer Session-TAN)**

Parameter Type | Parameter Name | Data Type | Description
-|-|-|-
Path|clientId|String|Literal „user“
 | sessionId|String|„identifier“ aus dem Session-Objekt
Body| | Session |Session-Objekt, in dem der identifier den Wert der sessionId hat und sessionTanActive und activated2FA den Wert „true“ erhält.

Beispiel Header:

```html
Accept:application/json
Authorization:Bearer 1234567890__Access-Token__1234567890
x-http-request-info:
{"clientRequestId":{"sessionId":"123_beliebige_ID_fuer_Session_12","
requestId":"123456789"}}
Content-Type:application/json
```

Beispiel Body:

```json
{
"identifier": "12345___identifier_der_session__1234"
"sessionTanActive": true,
"activated2FA": true
}
```

**RESPONSE (= Im Falle einer inaktiven Session-TAN wird eine TAN-Challenge erzeugt)**

Content-Type: application/json

JSON-Model:

Objects | Nested Objects | Keys | Description
-|-|-|-
Session| | JSON-Objekt Session

**Beispiel Response-Header (Auszug):**

```
x-once-authentication-info:
{ "id":"7654321",
"typ":"M_TAN",
"challenge":"+49-160-99XXXX",
"availableTypes":["P_TAN","M_TAN"]
}
```

Im Response-Header werden im "`x-once-authentication-info`"–Feld folgende Daten zurückgegeben:

- id: die ID der TAN-Challenge; diese ist in jedem Fall beim Aufruf der anschließenden Schnittstelle zur Aktivierung der Session-TAN zu übergeben
- typ: der TAN-Typ; mögliche Ausprägungen sind M_TAN, P_TAN, P_TAN_PUSH; ohne weitere Angaben im Request-Header dieser Schnittstelle wird immer eine TAN Ihres favorisierten TAN- Verfahrens erzeugt
- challenge: je TAN-Verfahren werden verschiedene Angaben gemacht; bei P_TAN wird die photoTAN-Grafik im PNG-Format (Base64-codiert) übergeben, bei MTAN die Telefonnummer, an die die mobileTAN gesendet wurde. Im Falle von P_TAN_PUSH entfällt die Challenge.

   - `x-once-authentication-info` Response Header im Falle P_TAN:

```json
{"id":"123456","typ":"P_TAN","challenge":"Base64Code","availableT ypes":["verfügbare TAN-Verfahren"]}
```

  - `x-once-authentication-info` Response Header im Falle M_TAN:

```json
{"id":"123456","typ":"M_TAN","challenge":"+49-1234-567XXXX","availableTypes":["verfügbare TAN-Verfahren"]}
```
- availableTypes: alle Ihre aktivierten TAN-Verfahren

Um auf ein anderes TAN-Verfahren zu wechseln (Beispiel: P_TAN), wird diese Validation-Schnittstelle erneut aufgerufen. Dabei muss im Header folgende Informationen hinzugefügt werden:

```
x-once-authentication-info: {"typ":"P_TAN"}
```

**HTTP Statuscodes**:

- 201 - CREATED
- 422 - UNPROCESSABLE ENTITY

## 2.4 Aktivierung einer Session-TAN

PATCH `{URL-Präfix}/session/clients/{clientId}/v1/sessions/{sessionId}`

**Beschreibung**: Die Schnittstelle dient zur Aktivierung der Session-TAN.

**REQUEST (= Aktivierung der Session-TAN)**

Parameter Type |Parameter Type | Data | Description
-|-|-|-
Path | clientId | String | Literal „user“
| | sessionId | String | „identifier“ aus dem Session-Objekt
Body | | Session | Session-Objekt, in dem der identifier den Wert der sessionId hat und sessionTanActive und activated2FA den Wert „true“ erhält.

**Beispiel Header** :

```http
Accept:application/json
Authorization:Bearer 1234567890__Access-Token__1234567890
x-http-request-info:
{"clientRequestId":{"sessionId":"123_beliebige_ID_fuer_Session_12","re
questId":"123456789"}}
Content-Type:application/json,
x-once-authentication-info:{"id":"7654321"}, //id der Challenge
x-once-authentication:123456 //die eigentliche TAN
```

Die „id“ wird dem Response-Header der Validation-Schnittstelle entnommen und im Header dieser Schnittstelle im „x-once-authentication-info-Parameter übergeben.

Über den Header-Parameter „x-once-authentication“ wird die aus der photoTAN-Grafik oder der mobileTAN-SMS ermittelte TAN in der Schnittstelle angegeben.

Wenn das TAN-Verfahren photoTAN-Push genutzt wird, erfolgt die Freigabe der TAN in der comdirect photoTAN-App. Eine TAN-Eingabe im Header der Schnittstelle ist damit nicht mehr erforderlich. Der Parameter wird deshalb in diesem Falle nicht im Header benötigt. Beispiel Body:

```json
{
"identifier" : "12345___identifier_der_session__1234",
"sessionTanActive": true,
"activated2FA": true
}
```

> Bitte beachten:
> Nach drei falschen TAN-Eingaben wird der Zugang zum Onlinebanking gesperrt. Nach zwei Fehleingaben über das API kann der Fehlerzähler über eine korrekte TAN-Eingabe auf der comdirect Website wieder zurückgesetzt werden.

**RESPONSE (= Session TAN wurde aktiviert, Zwei-Faktor-Authentifizierung wurde durchgeführt)**

Content-Type: application/json

JSON-Model:

Objects | Nested Objects |Keys | Description
-|-|-|-
Session| | | JSON-Objekt | Session

**HTTP Statuscodes**:

- 200 - OK
- 422 - UNPROCESSABLE ENTITY

> Bitte beachten:
> - Nach Aktivierung der Session TAN für eine Session wird bei TAN-pflichtigen Prozessen auf die Übergabe einer TAN verzichtet (vergleiche hierzu Kapitel 3).
> - Eine Session-TAN bleibt so lange gültig, bis das letzte Access/Refresh-Token seine Gültigkeit verliert

## 2.5 CD Secondary Flow

POST https://api.comdirect.de/oauth/token

**Beschreibung:**

Der comdirect-spezifische OAuth2 Authentication Flow „cd_secondary" ist ein Mix aus dem „client-credentials"-Flow und dem „resource-owner-password-credentials"-Flow. Über diesen Flow lassen Sie sich im letzten Schritt einen Access-Token ausstellen, der die Berechtigungen für die Banking- und Brokerage-Schnittstellen hat.

**REQUEST (= Generierung eines Access- und Refresh-Tokens)**

Parameter Type |Parameter Name| Data Type |Description
-|-|-|-
Path | - | -| -
Body | client_id |String |zugewiesene client_id
||client_secret | String | zugewiesenes client_secret
||grant_type |String |cd_secondary
||token |String | gültiger Access-Token

**Header:**

```http
Accept:application/json
Content-Type:application/x-www-form-urlencoded
```

Beispiel Body:

```http
client_id:"zugewiesene client_id"
client_secret:"zugewiesenes client_secret"
grant_type:cd_secondary
token:1234567890__Access-Token__1234567890
```

**RESPONSE (= Access- und Refresh-Token)**

Content-Type: application/json

JSON-Model:

Objects | Nested Objects | Keys | Description
-|-|-|-
String || access_token |Authentifikations-Token
String || token_type | Art des Tokens (Bearer)
String || refresh_token | Refresh-Token
String || expires_in | Angabe der Tokengültigkeit in Sekunden (599)
String || scope | Benennt die Zugriffsrechte (BANKING_RO, BROKERAGE_RW,SESSION_RW)
String || kdnr | Ihre Kundennummer
String || bpid | Interne Identifikationsnummer
String || kontaktId | Interne Identifikationsnummer

**Body:**

```http
{
"access_token": "1234567890__Access-Token__1234567890",
"token_type": "bearer",
"refresh_token": "1234567890_Refresh-Token__1234567890",
"expires_in": 599,
"scope": "BANKING_RO BROKERAGE_RW SESSION_RW",
"kdnr": "1234567890",
"bpid": "1234567",
"kontaktId": "1234567890"
}
```


## 9 Resource DOCUMENTS

Die Schnittstellen der Document-Resource ermöglichen Ihnen den Abruf Ihrer PostBox-Dokumente.

### 9.1 REST-API Resourcen

Meth. | URI (Endpunkt) | Bemerkung
-|-|-
GET | `/messages/clients/{clientId}/v2/documents` | Abruf der PostBox-Inhalte
GET | `/messages/v2/documents/{documentId}`|  Dokumentenabruf. MimeType gemäß Suchergebnis
GET | `/messages/v2/documents/{documentId}/predocument` | Abruf Vorschaltseite zu einem Dokument (falls vorhanden)

### 9.1.1 Abruf PostBox

GET /messages/clients/{clientId}/v2/documents

**REQUEST (= Abruf einer Dokumentenliste mit den Inhalten der PostBox)**

Parameter Type | Parameter Name | Data Type | Description
-|-|-|-
Path |clientId |String |Literal "user" oder die UUID des clients

**RESPONSE (= Liefert eine Liste von Metadaten zu Dokumenten)**

Content-Type: application/json

JSON-Model:

Objects | Nested Objects | Keys | Description
-|-|-|-
paging || index | Index erstes Dokument
matches || Anzahl | Dokumente (gesamt)
values | `$Document[]`| Liste der Dokumente

**HTTP Statuscodes:**

- 200 - OK
- 404 - NOT FOUND
- 422 – UNPROCESSABLE


### 9.1.2 Abruf eines Dokuments

GET `/messages/v2/documents/{documentId}`

**Anmerkung:**

Bitte beachten Sie, dass ein ungelesenes Dokument nach dem Abruf über diese Schnittstelle als gelesen markiert wird.

**REQUEST (= Aufruf eines Dokuments)**

Parameter Type | Parameter Name | Data Type | Description
-|-|-|-
Path | documentId | String | DokumentId (UUID)

Je nach MIME-Type des angeforderten Dokuments muss der Standardheader (vergleiche Kapitel 3.3) angepasst werden. Das Feld „Accept“ muss dabei den erwarteten MIME-Type erhalten.

Beispiel-Header (Auszug):

`Accept:application/pdf`

**RESPONSE (= Liefert das angefragte Dokument)**

Content-Type: application/pdf oder text/html (gemäß des im Document-JSON angegebenem mimeTypes)

**HTTP Statuscodes:**

- 200 – OK
- 404 - NOT FOUND
- 406 – NOT ACCEPTABLE (der Content-Type des Responses entspricht nicht dem, der im Request-Header akzeptiert wird (Angabe im Feld „Accept“)

### 9.1.3 Abruf Dokument-Vorschaltseite

GET `/messages/v2/documents/{documentId}/predocument`

**REQUEST (= Aufruf zur Anzeige der Dokument-Vorschaltseite)**

Parameter Type | Parameter Name | Data Type | Description
-|-|-|-
Path |documentId |String |DokumentId (UUID)

Das Header-Feld „Accept“ muss den erwarteten MIME-Type text/html erhalten.

**Beispiel-Header (Auszug):**

`Accept:text/html`

**RESPONSE (= Liefert die gewünschte Dokument-Vorschaltseite)**

Content-Type: text/html

**HTTP Statuscodes:**

- 200 - OK
- 404 - NOT FOUND

## 9.2 REST-Objekte

### 9.2.1 Document

Parameter | Datentyp | Schreibbarkeit | Beschreibung
-|-|-|-
documentId | String | read-only UUID des Dokuments
name | String | read-only | Betreff/Titel des Dokuments
dateCreation | $DateString | read-only | Eingangsdatum/Erstellungsdatum
mimeType | String | read-only | MimeType des Dokuments
deleteable | Boolean | read-only | TRUE, wenn ein Dokument löschbar ist
advertisement | Boolean | read-only | TRUE, wenn es sich bei dem Dokument um Werbung handelt
documentMetadata|  $DocumentMetaData | read-only | Metadaten zum Dokument

### 9.2.2 DocumentMetadata

Parameter | Datentyp | Schreibbarkeit |Beschreibung
-|-|-|-
archived |Boolean |read-only |TRUE, wenn das Dokument in das Archiv verschoben wurde
dateRead | $DateString+NULL | read-only |Datum, an dem das Dokument gelesen wurde
alreadyRead |Boolean |read-only | TRUE, wenn das Dokument gelesen wurde
predocumentExists | Boolean | read-only TRUE, wenn für das Dokument eine Vorschaltseite im HTML-Format verfügbar ist