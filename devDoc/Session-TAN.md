# Session-TAN

## 2.3 Anlage Validierung einer Session-TAN

POST `{URL-Präfix}/session/clients/{clientId}/v1/sessions/{sessionId}/validate`

**Beschreibung**: Für das nun bekannte Session-Objekt wird in diesem Schritt eine TAN-Challenge angefordert.

Bitte beachten:
Das Abrufen von fünf TAN-Challenges ohne zwischenzeitliche Entwertung einer korrekten TAN führt zur
Sperrung des Onlinebanking-Zugangs.
12/85
comdirect REST API - Schnittstellenspezifikation
REQUEST (= Anlage Validierung einer Session-TAN)# comdirect REST API - Schnittstellenspezifikation

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
Beispiel Body:
{
"identifier": "12345___identifier_der_session__1234"
"sessionTanActive": true,
"activated2FA": true
,
}
RESPONSE (= Im Falle einer inaktiven Session-TAN wird eine TAN-Challenge erzeugt)
Content-Type: application/json
JSON-Model:
Objects Nested Objects Keys Description
Session JSON-Objekt Session
Beispiel Response-Header (Auszug):
x-once-authentication-info:
{ "id":"7654321",
"typ":"M_TAN",
"challenge":"+49-160-99XXXX",
"availableTypes":["P_TAN","M_TAN"]
}
Im Response-Header werden im "x-once-authentication-info"–Feld folgende Daten
zurückgegeben:
 id: die ID der TAN-Challenge; diese ist in jedem Fall beim Aufruf der anschließenden Schnittstelle
zur Aktivierung der Session-TAN zu übergeben
13/85
comdirect REST API - Schnittstellenspezifikation
 typ: der TAN-Typ; mögliche Ausprägungen sind M_TAN, P_TAN, P_TAN_PUSH; ohne weitere
Angaben im Request-Header dieser Schnittstelle wird immer eine TAN Ihres favorisierten TAN-
Verfahrens erzeugt
 challenge: je TAN-Verfahren werden verschiedene Angaben gemacht; bei P_TAN wird die
photoTAN-Grafik im PNG-Format (Base64-codiert) übergeben, bei MTAN die Telefonnummer, an
die die mobileTAN gesendet wurde. Im Falle von P_TAN_PUSH entfällt die Challenge.
o x-once-authentication-info Response Header im Falle P_TAN:
{"id":"123456","typ":"P_TAN","challenge":"Base64Code","availableT
ypes":["verfügbare TAN-Verfahren"]}
o x-once-authentication-info Response Header im Falle M_TAN:
{"id":"123456","typ":"M_TAN","challenge":"+49-1234-
567XXXX","availableTypes":["verfügbare TAN-Verfahren"]}
 availableTypes: alle Ihre aktivierten TAN-Verfahren
Um auf ein anderes TAN-Verfahren zu wechseln (Beispiel: P_TAN), wird diese Validation-Schnittstelle
erneut aufgerufen. Dabei muss im Header folgende Informationen hinzugefügt werden:
x-once-authentication-info: {"typ":"P_TAN"}
HTTP Statuscodes:
 201 - CREATED
 422 - UNPROCESSABLE ENTITY
2.4 Aktivierung einer Session-TAN
PATCH URL-Präfix/session/clients/{clientId}/v1/sessions/{sessionId}
Beschreibung: Die Schnittstelle dient zur Aktivierung der Session-TAN.
REQUEST (= Aktivierung der Session-TAN)
Parameter
Parameter
Data
Description
Type
Name
Type
Path clientId String Literal „user“
sessionId String „identifier“ aus dem Session-Objekt
Body Session Session-Objekt, in dem der identifier den Wert der sessionId hat
und sessionTanActive und activated2FA den Wert „true“ erhält.
Beispiel Header:
Accept:application/json
Authorization:Bearer 1234567890__Access-Token__1234567890
14/85
comdirect REST API - Schnittstellenspezifikation
x-http-request-info:
{"clientRequestId":{"sessionId":"123_beliebige_ID_fuer_Session_12","re
questId":"123456789"}}
Content-Type:application/json,
x-once-authentication-info:{"id":"7654321"}, //id der Challenge
x-once-authentication:123456 //die eigentliche TAN
Die „id“ wird dem Response-Header der Validation-Schnittstelle entnommen und im Header dieser
Schnittstelle im „x-once-authentication-info“
-Parameter übergeben.
Über den Header-Parameter „x-once-authentication“ wird die aus der photoTAN-Grafik oder der
mobileTAN-SMS ermittelte TAN in der Schnittstelle angegeben.
Wenn das TAN-Verfahren photoTAN-Push genutzt wird, erfolgt die Freigabe der TAN in der comdirect
photoTAN-App. Eine TAN-Eingabe im Header der Schnittstelle ist damit nicht mehr erforderlich. Der
Parameter wird deshalb in diesem Falle nicht im Header benötigt.
Beispiel Body:
{
"identifier" : "12345___identifier_der_session__1234",
"sessionTanActive": true,
"activated2FA": true
}
Bitte beachten:
Nach drei falschen TAN-Eingaben wird der Zugang zum Onlinebanking gesperrt. Nach zwei
Fehleingaben über das API kann der Fehlerzähler über eine korrekte TAN-Eingabe auf der comdirect
Website wieder zurückgesetzt werden.
RESPONSE (= Session TAN wurde aktiviert, Zwei-Faktor-Authentifizierung wurde durchgeführt)
Content-Type: application/json
JSON-Model:
Objects Nested Objects Keys Description
Session JSON-Objekt Session
HTTP Statuscodes:
 200 - OK
 422 - UNPROCESSABLE ENTITY
Bitte beachten:
 Nach Aktivierung der Session TAN für eine Session wird bei TAN-pflichtigen Prozessen auf die
Übergabe einer TAN verzichtet (vergleiche hierzu Kapitel 3).
 Eine Session-TAN bleibt so lange gültig, bis das letzte Access/Refresh-Token seine Gültigkeit verliert
Parameter
Parameter
Data
Description
Type
Name
Type
Path clientId String Literal „user“
sessionId String „identifier“ aus dem Session-Objekt
Body Session Session-Objekt, in dem der identifier den Wert der sessionId hat
und sessionTanActive und activated2FA den Wert „true“ erhält.
Beispiel Header:
Accept:application/json
Authorization:Bearer 1234567890__Access-Token__1234567890
x-http-request-info:
{"clientRequestId":{"sessionId":"123_beliebige_ID_fuer_Session_12","
requestId":"123456789"}}
Content-Type:application/json
Beispiel Body:
{
"identifier": "12345___identifier_der_session__1234"
"sessionTanActive": true,
"activated2FA": true
,
}
RESPONSE (= Im Falle einer inaktiven Session-TAN wird eine TAN-Challenge erzeugt)
Content-Type: application/json
JSON-Model:
Objects Nested Objects Keys Description
Session JSON-Objekt Session
Beispiel Response-Header (Auszug):
x-once-authentication-info:
{ "id":"7654321",
"typ":"M_TAN",
"challenge":"+49-160-99XXXX",
"availableTypes":["P_TAN","M_TAN"]
}
Im Response-Header werden im "x-once-authentication-info"–Feld folgende Daten
zurückgegeben:
 id: die ID der TAN-Challenge; diese ist in jedem Fall beim Aufruf der anschließenden Schnittstelle
zur Aktivierung der Session-TAN zu übergeben
13/85
comdirect REST API - Schnittstellenspezifikation
 typ: der TAN-Typ; mögliche Ausprägungen sind M_TAN, P_TAN, P_TAN_PUSH; ohne weitere
Angaben im Request-Header dieser Schnittstelle wird immer eine TAN Ihres favorisierten TAN-
Verfahrens erzeugt
 challenge: je TAN-Verfahren werden verschiedene Angaben gemacht; bei P_TAN wird die
photoTAN-Grafik im PNG-Format (Base64-codiert) übergeben, bei MTAN die Telefonnummer, an
die die mobileTAN gesendet wurde. Im Falle von P_TAN_PUSH entfällt die Challenge.
o x-once-authentication-info Response Header im Falle P_TAN:
{"id":"123456","typ":"P_TAN","challenge":"Base64Code","availableT
ypes":["verfügbare TAN-Verfahren"]}
o x-once-authentication-info Response Header im Falle M_TAN:
{"id":"123456","typ":"M_TAN","challenge":"+49-1234-
567XXXX","availableTypes":["verfügbare TAN-Verfahren"]}
 availableTypes: alle Ihre aktivierten TAN-Verfahren
Um auf ein anderes TAN-Verfahren zu wechseln (Beispiel: P_TAN), wird diese Validation-Schnittstelle
erneut aufgerufen. Dabei muss im Header folgende Informationen hinzugefügt werden:
x-once-authentication-info: {"typ":"P_TAN"}
HTTP Statuscodes:
 201 - CREATED
 422 - UNPROCESSABLE ENTITY
2.4 Aktivierung einer Session-TAN
PATCH URL-Präfix/session/clients/{clientId}/v1/sessions/{sessionId}
Beschreibung: Die Schnittstelle dient zur Aktivierung der Session-TAN.
REQUEST (= Aktivierung der Session-TAN)
Parameter
Parameter
Data
Description
Type
Name
Type
Path clientId String Literal „user“
sessionId String „identifier“ aus dem Session-Objekt
Body Session Session-Objekt, in dem der identifier den Wert der sessionId hat
und sessionTanActive und activated2FA den Wert „true“ erhält.
Beispiel Header:
Accept:application/json
Authorization:Bearer 1234567890__Access-Token__1234567890
14/85
comdirect REST API - Schnittstellenspezifikation
x-http-request-info:
{"clientRequestId":{"sessionId":"123_beliebige_ID_fuer_Session_12","re
questId":"123456789"}}
Content-Type:application/json,
x-once-authentication-info:{"id":"7654321"}, //id der Challenge
x-once-authentication:123456 //die eigentliche TAN
Die „id“ wird dem Response-Header der Validation-Schnittstelle entnommen und im Header dieser
Schnittstelle im „x-once-authentication-info“
-Parameter übergeben.
Über den Header-Parameter „x-once-authentication“ wird die aus der photoTAN-Grafik oder der
mobileTAN-SMS ermittelte TAN in der Schnittstelle angegeben.
Wenn das TAN-Verfahren photoTAN-Push genutzt wird, erfolgt die Freigabe der TAN in der comdirect
photoTAN-App. Eine TAN-Eingabe im Header der Schnittstelle ist damit nicht mehr erforderlich. Der
Parameter wird deshalb in diesem Falle nicht im Header benötigt.
Beispiel Body:
{
"identifier" : "12345___identifier_der_session__1234",
"sessionTanActive": true,
"activated2FA": true
}
Bitte beachten:
Nach drei falschen TAN-Eingaben wird der Zugang zum Onlinebanking gesperrt. Nach zwei
Fehleingaben über das API kann der Fehlerzähler über eine korrekte TAN-Eingabe auf der comdirect
Website wieder zurückgesetzt werden.
RESPONSE (= Session TAN wurde aktiviert, Zwei-Faktor-Authentifizierung wurde durchgeführt)
Content-Type: application/json
JSON-Model:
Objects Nested Objects Keys Description
Session JSON-Objekt Session
HTTP Statuscodes:
 200 - OK
 422 - UNPROCESSABLE ENTITY
Bitte beachten:
 Nach Aktivierung der Session TAN für eine Session wird bei TAN-pflichtigen Prozessen auf die
Übergabe einer TAN verzichtet (vergleiche hierzu Kapitel 3).
 Eine Session-TAN bleibt so lange gültig, bis das letzte Access/Refresh-Token seine Gültigkeit verliert