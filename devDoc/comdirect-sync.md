# comdirect-sync

De acuerdo a comdirect, se deben seguir los siguientes pasos

2 So geht es los
2.1 OAuth2 Resource Owner Password Credentials Flow
2.2 Session-Status
2.3 Anlage Validierung einer Session-TAN
2.4 Aktivierung einer Session-TAN
2.5 OAuth2 CD Secondary-Flow
5.1.1 Abruf Depots
5.1.2 Abruf Depotbestand und/oder Positionen
5.1.3 Abruf einer Position des Depots
5.1.4 Abruf Depotumsätze
9.1.1 Abruf PostBox

Antes de hacer una petición a la API real, me gustaría usar los datos en InmemoryRepository y vayamos verificando paso por paso.

Ya tengo los json de request y de response.

Nota; por el momento no quiero JavaFX, porque la última vez tuvimos muchos problemas, usaremos el modo consola porque allí creo tenemos más flexibilidad para mostrar información en pantalla.

También deberemos mandar los logs a un archivo de texto.

## Autentication OAuth2 process

### 2.1 OAuth2 Resource Owner Password Credentials Flow

We send

```bash
curl --location --globoff '{{oauth_url}}/oauth/token' \
--header 'Content-Type: application/x-www-form-urlencoded' \
--header 'Accept: application/json' \
--data-urlencode 'client_id={{client_id}}' \
--data-urlencode 'client_secret={{client_secret}}' \
--data-urlencode 'grant_type=password' \
--data-urlencode 'username={{zugangsnummer}}' \
--data-urlencode 'password={{pin}}'
```

Requested data

```json
{
    "access_token": "5375a37e-d7cd-4174-a73a-f9e1065f8df5",
    "token_type": "bearer",
    "refresh_token": "f516b39a-1c1c-42bf-b3f4-492ec2e56897",
    "expires_in": 599,
    "scope": "TWO_FACTOR",
    "kdnr": "1188651895",
    "bpid": 10320404,
    "kontaktId": 7221230038
}
```

### 2.2 Session-Status


```bash
curl --location 'https://api.comdirect.de/api/session/clients/user/v1/sessions' \
--header 'Accept: application/json' \
--header 'Authorization: Bearer 32734246-6015-4247-92fc-021ac8ce5e27' \
--header 'x-http-request-info: {"clientRequestId":{"sessionId":"f7af8fca-94a6-5772-7df3-663dd518fc78","requestId":"054471271"}}' \
--header 'Content-Type: application/json' \
--header 'Cookie: qSession=d18952e4.a6cdf558b2484d638f4f535'
```

Requested data

```json
[
    {
        "identifier": "198C3E54EA634BC7B0F4B73C7FFE75A0",
        "sessionTanActive": false,
        "activated2FA": false
    }
]
```

### 2.3 Anlage Validierung einer Session-TAN

En este punto debemos espera hasya que se haga la autenticacón por el FotoTan, no se si podamos hacer un comprotamiento como el de MS Authenticator, es decir que cuando el usuario acepte la petición en el FotoTan automáticamente pase al segundo paso.

```bash
curl --location 'https://api.comdirect.de/api/session/clients/user/v1/sessions/AC7AD7AB3C2D45779F95E73537B3BF12/validate' \
--header 'Accept: application/json' \
--header 'Authorization: Bearer 4a6bf91d-329e-4c08-8d61-eb7294a566f5' \
--header 'x-http-request-info: {"clientRequestId":{"sessionId":"411d0c3d-48cd-9618-9493-a53aaea14ce5","requestId":"061130511"}}' \
--header 'Content-Type: application/json' \
--header 'Cookie: qSession=d18952e4.a6cdf558b2484d638f4f535' \
--data '{
        "identifier" : "AC7AD7AB3C2D45779F95E73537B3BF12",
        "sessionTanActive": true,
        "activated2FA": true
}'
```

Requested data

```json
{
    "identifier": "198C3E54EA634BC7B0F4B73C7FFE75A0",
    "sessionTanActive": true,
    "activated2FA": true
}
```

### 2.4 Aktivierung einer Session-TAN

```bash
curl --location --request PATCH 'https://api.comdirect.de/api/session/clients/user/v1/sessions/08C166185549453F923AFA59343AA127' \
--header 'Accept: application/json' \
--header 'Authorization: Bearer 32734246-6015-4247-92fc-021ac8ce5e27' \
--header 'x-http-request-info: {"clientRequestId":{"sessionId":"f7af8fca-94a6-5772-7df3-663dd518fc78","requestId":"054471271"}}' \
--header 'Content-Type: application/json' \
--header 'x-once-authentication-info: {"id":"571116782"}' \
--header 'x-once-authentication: 000000' \
--header 'Cookie: qSession=d18952e4.a6cdf558b2484d638f4f535' \
--data '    {
        "identifier" : "08C166185549453F923AFA59343AA127",
        "sessionTanActive" : true,
        "activated2FA": true
    }
'
```

Requested data

```json
{
    "identifier": "198C3E54EA634BC7B0F4B73C7FFE75A0",
    "sessionTanActive": true,
    "activated2FA": true
}
```

### 2.5 OAuth2 CD Secondary-Flow

```bash
curl --location 'https://api.comdirect.de/oauth/token' \
--header 'Content-Type: application/x-www-form-urlencoded' \
--header 'Accept: application/json' \
--header 'Cookie: qSession=d18952e4.a6cdf558b2484d638f4f535' \
--data-urlencode 'client_id=User_93FA2414B4B146969DFFDFEC21E8C845' \
--data-urlencode 'client_secret=BB0AC148B2874D0ABF9D34EB799A47FD' \
--data-urlencode 'grant_type=cd_secondary' \
--data-urlencode 'token=32734246-6015-4247-92fc-021ac8ce5e27'
```

Requested data

```json
{
    "access_token": "b4d47dfe-4eb3-4a2e-ae7b-ca9b0a5200bc",
    "token_type": "bearer",
    "refresh_token": "25ae8a23-eb2c-4567-a7f4-13c1ac2edfaa",
    "expires_in": 599,
    "scope": "BANKING_RW BROKERAGE_RW MESSAGES_RO REPORTS_RO SESSION_RW",
    "kdnr": "1188651895",
    "bpid": 10320404,
    "kontaktId": 7221230038
}
```

### 9.1.1 Abruf PostBox

En este punto, debemos poder descargar los archivos PDF, en un archivo de configuración debo poner las cadenas a buscar, por ejemplo:



```
docuements: ["steuer","depot"]
```

Es decir que buscaré dentro de la respuesta todos los archivos PDF que concuerden con ese patrón


```bash
curl --location 'https://api.comdirect.de/api/messages/clients/user/v2/documents' \
--header 'Accept: application/json' \
--header 'Authorization: Bearer 32734246-6015-4247-92fc-021ac8ce5e27' \
--header 'x-http-request-info: {"clientRequestId":{"sessionId":"f7af8fca-94a6-5772-7df3-663dd518fc78","requestId":"054471271"}}' \
--header 'Content-Type: application/json' \
--header 'Cookie: qSession=d18952e4.a6cdf558b2484d638f4f535'
```

Requested data

```json
{
    "paging": {
        "index": 0,
        "matches": 1178
    },
    "aggregated": {
        "unreadMessages": 113,
        "dateOldestEntry": "2015-10-02",
        "matchesInThisResponse": 20,
        "allowedToSeeAllDocuments": true
    },
    "values": [
        {
            "documentId": "E31FE40510D98771B0CEBB2E1580A35C",
            "name": "Änderung der AGBs und des PLV zum 01.04.2021",
            "dateCreation": "2021-02-01",
            "mimeType": "text/html",
            "deletable": false,
            "advertisement": false,
            "documentMetaData": {
                "archived": false,
                "alreadyRead": false,
                "predocumentExists": false
            },
            "categoryId": 301
        },
        {
            "documentId": "D82DEDD7A51975828E496E4B9E7DFEF1",
            "name": "Ihre iTAN-Liste wird bald ungültig - jetzt schnell photoTAN aktivieren",
            "dateCreation": "2019-08-12",
            "mimeType": "text/html",
            "deletable": false,
            "advertisement": false,
            "documentMetaData": {
                "archived": false,
                "alreadyRead": false,
                "predocumentExists": false
            },
            "categoryId": 301
        },
        {
            "documentId": "BF9593DFD107525D5330DA080E52BD2D",
            "name": "Änderungen der Kundeninformationen zum Wertpapiergeschäft.",
            "dateCreation": "2019-07-15",
            "mimeType": "text/html",
            "deletable": false,
            "advertisement": false,
            "documentMetaData": {
                "archived": false,
                "alreadyRead": false,
                "predocumentExists": false
            },
            "categoryId": 301
        },
        {
            "documentId": "A4CE14205B504F03D7F4396F22D6F1CD",
            "name": "Ihre iTAN-Listen werden bald ungültig - jetzt zur photoTAN wechseln",
            "dateCreation": "2019-03-20",
            "mimeType": "text/html",
            "deletable": false,
            "advertisement": false,
            "documentMetaData": {
                "archived": false,
                "alreadyRead": false,
                "predocumentExists": false
            },
            "categoryId": 301
        },
        {
            "documentId": "B3BAED207A67002E1424882D5B043829",
            "name": "Änderung der AGB ab 01.09.2018",
            "dateCreation": "2018-08-27",
            "mimeType": "text/html",
            "deletable": false,
            "advertisement": false,
            "documentMetaData": {
                "archived": false,
                "alreadyRead": false,
                "predocumentExists": false
            },
            "categoryId": 301
        },
        {
            "documentId": "EF8B2A450D1E16A412961AF245E8746C",
            "name": "Änderung der AGB und des Preis- und Leistungsverzeichnisses ab 01.09.2018",
            "dateCreation": "2018-06-27",
            "mimeType": "text/html",
            "deletable": false,
            "advertisement": false,
            "documentMetaData": {
                "archived": false,
                "alreadyRead": false,
                "predocumentExists": false
            },
            "categoryId": 301
        },
        {
            "documentId": "23A0FD24AF5EC5EA57C67ED68DACB46A",
            "name": "Änderungen der AGB und des Preis- und Leistungsverzeichnisses zum 01.01.2018",
            "dateCreation": "2017-10-23",
            "mimeType": "text/html",
            "deletable": false,
            "advertisement": false,
            "documentMetaData": {
                "archived": false,
                "alreadyRead": false,
                "predocumentExists": false
            },
            "categoryId": 301
        },
        {
            "documentId": "8F9DC17C49C98822F0845D361BEB6624",
            "name": "Änderungen der AGB zum 1.10.2017",
            "dateCreation": "2017-08-01",
            "mimeType": "text/html",
            "deletable": false,
            "advertisement": false,
            "documentMetaData": {
                "archived": false,
                "alreadyRead": false,
                "predocumentExists": false
            },
            "categoryId": 301
        },
        {
            "documentId": "14CF898D6B0E8CDC087E24707CB42EC7",
            "name": "Senkung Ihres Limits bei telefonisch aufgegebenen Überweisungen",
            "dateCreation": "2017-05-10",
            "mimeType": "text/html",
            "deletable": false,
            "advertisement": false,
            "documentMetaData": {
                "archived": false,
                "alreadyRead": false,
                "predocumentExists": false
            },
            "categoryId": 301
        },
        {
            "documentId": "A5B31A44F02732F9AC6D828420F6B059",
            "name": "Jetzt informieren: Aktualisierte Kundeninformationen zum Wertpapiergeschäft",
            "dateCreation": "2016-02-08",
            "mimeType": "text/html",
            "deletable": false,
            "advertisement": false,
            "documentMetaData": {
                "archived": false,
                "alreadyRead": false,
                "predocumentExists": false
            },
            "categoryId": 301
        },
        {
            "documentId": "3B497AE77E259A33954808B9DADD6B30",
            "name": "Änderung der allgemeinen und produktbezogenen Geschäftsbedingungen zum 21.03.16",
            "dateCreation": "2016-01-19",
            "mimeType": "text/html",
            "deletable": false,
            "advertisement": false,
            "documentMetaData": {
                "archived": false,
                "alreadyRead": false,
                "predocumentExists": false
            },
            "categoryId": 301
        },
        {
            "documentId": "C9E0E17B71EA81FCE29D8D0A7A5861FC",
            "name": "Buchungsanzeige 885166 (HYUNDAI MOT.0,5N.VTG GDRS) vom 01.08.2025",
            "dateCreation": "2025-08-01",
            "mimeType": "application/pdf",
            "deletable": false,
            "advertisement": false,
            "documentMetaData": {
                "archived": false,
                "alreadyRead": false,
                "predocumentExists": false
            },
            "categoryId": 612
        },
        {
            "documentId": "EB96E7BBB5EF455B829F476D401E0EC4",
            "name": "Wichtige Fondsinformation - Änderung A1T8Z2 (BNPP RU EQU. CL.CAP)",
            "dateCreation": "2025-07-30",
            "mimeType": "application/pdf",
            "deletable": false,
            "advertisement": false,
            "documentMetaData": {
                "archived": false,
                "alreadyRead": false,
                "predocumentExists": false
            },
            "categoryId": 613
        },
        {
            "documentId": "349A1C88F5BF0015166FEC09EEE7D5F6",
            "name": "Wichtige Fondsinformation - Änderung A1T8Z2 (BNPP RU EQU. CL.CAP)",
            "dateCreation": "2025-05-13",
            "mimeType": "application/pdf",
            "deletable": false,
            "advertisement": false,
            "documentMetaData": {
                "archived": false,
                "alreadyRead": false,
                "predocumentExists": false
            },
            "categoryId": 613
        },
        {
            "documentId": "338B46358B1D671A6C1C7D00AEFACE69",
            "name": "Termingebundenes Umtausch oder Barabfindungsangebot 540811(AAREAL BANK AG) vom 20.01.2022",
            "dateCreation": "2022-01-20",
            "mimeType": "application/pdf",
            "deletable": false,
            "advertisement": false,
            "documentMetaData": {
                "archived": false,
                "alreadyRead": false,
                "predocumentExists": true
            },
            "categoryId": 611
        },
        {
            "documentId": "13D680FC0B963A8E03B95B28EFDB04CE",
            "name": "Termingebundenes Umtausch oder Barabfindungsangebot 540811(AAREAL BANK AG) vom 21.12.2021",
            "dateCreation": "2021-12-21",
            "mimeType": "application/pdf",
            "deletable": false,
            "advertisement": false,
            "documentMetaData": {
                "archived": false,
                "alreadyRead": false,
                "predocumentExists": true
            },
            "categoryId": 611
        },
        {
            "documentId": "A30EF0D824275A600BA9D5F54FE8923C",
            "name": "Finanzreport Nr. 08 per 01.09.2025",
            "dateCreation": "2025-09-03",
            "mimeType": "application/pdf",
            "deletable": false,
            "advertisement": false,
            "documentMetaData": {
                "archived": false,
                "alreadyRead": false,
                "predocumentExists": false
            },
            "categoryId": 201
        },
        {
            "documentId": "080AE60167A60F06CBC7229151A1BD4E",
            "name": "Ertragsgutschrift 3.100 St. A1H8BN (HSBC MSCI INDONES. UC.ETF) vom 29.08.2025",
            "dateCreation": "2025-08-30",
            "mimeType": "application/pdf",
            "deletable": false,
            "advertisement": false,
            "documentMetaData": {
                "archived": false,
                "alreadyRead": false,
                "predocumentExists": false
            },
            "categoryId": 700
        },
        {
            "documentId": "CA6F4ADEFF0B1789A181B49A5F87BCC7",
            "name": "Steuermitteilung A1H8BN (HSBC MSCI INDONES. UC.ETF) vom 29.08.2025",
            "dateCreation": "2025-08-30",
            "mimeType": "application/pdf",
            "deletable": false,
            "advertisement": false,
            "documentMetaData": {
                "archived": false,
                "alreadyRead": false,
                "predocumentExists": false
            },
            "categoryId": 252
        },
        {
            "documentId": "107607F6E9E2CC475A0C877C43529763",
            "name": "Steuermitteilung 853055 (CANON INC. SHARES O.N.) vom 25.08.2025",
            "dateCreation": "2025-08-26",
            "mimeType": "application/pdf",
            "deletable": false,
            "advertisement": false,
            "documentMetaData": {
                "archived": false,
                "alreadyRead": false,
                "predocumentExists": false
            },
            "categoryId": 252
        }
    ]
}
```

### 9.1.2 Abruf eines Dokuments

```bash
curl --location 'https://api.comdirect.de/api/messages/v2/documents/107607F6E9E2CC475A0C877C43529763' \
--header 'Accept: application/pdf' \
--header 'Authorization: Bearer 32734246-6015-4247-92fc-021ac8ce5e27' \
--header 'x-http-request-info: {"clientRequestId":{"sessionId":"f7af8fca-94a6-5772-7df3-663dd518fc78","requestId":"054471271"}}' \
--header 'Content-Type: application/json' \
--header 'Cookie: qSession=d18952e4.a6cdf558b2484d638f4f535'
```

Requested data

Desconocida

### 9.1.3 Abruf Dokument-Vorschaltseite

```bash
curl --location 'https://api.comdirect.de/api/messages/v2/documents/107607F6E9E2CC475A0C877C43529763/predocument' \
--header 'Accept: application/pdf' \
--header 'Authorization: Bearer 32734246-6015-4247-92fc-021ac8ce5e27' \
--header 'x-http-request-info: {"clientRequestId":{"sessionId":"f7af8fca-94a6-5772-7df3-663dd518fc78","requestId":"054471271"}}' \
--header 'Content-Type: application/json' \
--header 'Cookie: qSession=d18952e4.a6cdf558b2484d638f4f535'
```

Requested data

Desconocida

## 5 DEPOT

### 5.1.1 Abruf Depots

```bash
curl --location 'https://api.comdirect.de/api/brokerage/clients/user/v3/depots' \
--header 'Accept: application/json' \
--header 'Authorization: Bearer 32734246-6015-4247-92fc-021ac8ce5e27' \
--header 'x-http-request-info: {"clientRequestId":{"sessionId":"f7af8fca-94a6-5772-7df3-663dd518fc78","requestId":"054471271"}}' \
--header 'Content-Type: application/json' \
--header 'Cookie: qSession=d18952e4.a6cdf558b2484d638f4f535'
```

Requested data

```json
{
    "paging": {
        "index": 0,
        "matches": 1
    },
    "values": [
        {
            "depotId": "8873872174FD4B30A7CE1407893A0333",
            "depotDisplayId": "8651895",
            "clientId": "BC5001401982400F95C27906272235E0",
            "depotType": "STANDARD_DEPOT",
            "defaultSettlementAccountId": "FC9C31539CEF496993C0A36367FE9C77",
            "settlementAccountIds": [
                "4DDA009419FC4443A3EC926CB8CD8D0F",
                "0568A35A9556431BA67EDD87D10B1485",
                "1AD0D5B59FB54B8F871801231D4FE061",
                "7510BEA559654BE481BE20662EDE5705",
                "19124F41D0A24DDA99E428EFEC15D6AC"
            ],
            "targetMarket": "EXPERT_KNOWLEDGE"
        }
    ]
}
```

### 5.1.2 Abruf Depotbestand und/oder Positionen

```bash
curl --location 'https://api.comdirect.de/api/brokerage/v3/depots/8873872174FD4B30A7CE1407893A0333/positions' \
--header 'Accept: application/json' \
--header 'Authorization: Bearer 32734246-6015-4247-92fc-021ac8ce5e27' \
--header 'x-http-request-info: {"clientRequestId":{"sessionId":"f7af8fca-94a6-5772-7df3-663dd518fc78","requestId":"054471271"}}' \
--header 'Content-Type: application/json'
```

Requested data

```json
{
    "paging": {
        "index": 0,
        "matches": 35
    },
    "aggregated": {
        "depot": {
            "depotId": "8873872174FD4B30A7CE1407893A0333",
            "depotDisplayId": "8651895",
            "clientId": "BC5001401982400F95C27906272235E0",
            "depotType": "STANDARD_DEPOT",
            "defaultSettlementAccountId": "FC9C31539CEF496993C0A36367FE9C77",
            "settlementAccountIds": [
                "4DDA009419FC4443A3EC926CB8CD8D0F",
                "0568A35A9556431BA67EDD87D10B1485",
                "1AD0D5B59FB54B8F871801231D4FE061",
                "7510BEA559654BE481BE20662EDE5705",
                "19124F41D0A24DDA99E428EFEC15D6AC"
            ],
            "targetMarket": "EXPERT_KNOWLEDGE"
        },
        "prevDayValue": {
            "value": "19365021.62",
            "unit": "EUR"
        },
        "currentValue": {
            "value": "13429368.976",
            "unit": "EUR"
        },
        "purchaseValue": {
            "value": "9792776.35",
            "unit": "EUR"
        },
        "profitLossPurchaseAbs": {
            "value": "3636592.626",
            "unit": "EUR"
        },
        "profitLossPurchaseRel": "37.135",
        "profitLossPrevDayAbs": {
            "value": "68241.545",
            "unit": "EUR"
        },
        "profitLossPrevDayRel": "0.511"
    },
    "values": [
        {
            "depotId": "8873872174FD4B30A7CE1407893A0333",
            "positionId": "CD60CD17AA77379787EF7C3D075C62B1",
            "wkn": "894983",
            "custodyType": "850",
            "quantity": {
                "value": "210000",
                "unit": "XXX"
            },
            "availableQuantity": {
                "value": "210000.0",
                "unit": "XXX"
            },
            "currentPrice": {
                "price": {
                    "value": "1.3845",
                    "unit": "EUR"
                },
                "priceDateTime": "2024-05-30T17:59:41+02",
                "venue": {
                    "name": "Tradegate",
                    "venueId": "C3E017C9A9464A688F57E57BD7661C12",
                    "country": "DE",
                    "type": "EXCHANGE"
                }
            },
            "purchasePrice": {
                "value": "0.47800",
                "unit": "EUR"
            },
            "prevDayPrice": {
                "price": {
                    "value": "1.367",
                    "unit": "EUR"
                },
                "priceDateTime": "2024-05-29T22:26:25+02",
                "venue": {
                    "name": "Tradegate",
                    "venueId": "C3E017C9A9464A688F57E57BD7661C12",
                    "country": "DE",
                    "type": "EXCHANGE"
                }
            },
            "currentValue": {
                "value": "290745.000",
                "unit": "EUR"
            },
            "purchaseValue": {
                "value": "100380",
                "unit": "EUR"
            },
            "profitLossPurchaseAbs": {
                "value": "190365.000",
                "unit": "EUR"
            },
            "profitLossPurchaseRel": "189.644",
            "profitLossPrevDayAbs": {
                "value": "0.018",
                "unit": "EUR"
            },
            "profitLossPrevDayRel": "1.317",
            "profitLossPrevDayTotalAbs": {
                "value": "3675.000",
                "unit": "EUR"
            },
            "version": null,
            "hedgeability": "NOT_HEDGEABLE",
            "availableQuantityToHedge": {
                "value": "0",
                "unit": "XXX"
            },
            "currentPriceDeterminable": true
        },
        {
            "depotId": "8873872174FD4B30A7CE1407893A0333",
            "positionId": "52588055157036CEB6DE3491DBDA751D",
            "wkn": "A0Z2ZZ",
            "custodyType": "004",
            "quantity": {
                "value": "3000",
                "unit": "XXX"
            },
            "availableQuantity": {
                "value": "3000.0",
                "unit": "XXX"
            },
            "currentPrice": {
                "price": {
                    "value": "23.7",
                    "unit": "EUR"
                },
                "priceDateTime": "2024-05-30T17:35:23+02",
                "venue": {
                    "name": "Xetra",
                    "venueId": "72274630AEA34CF2949911ED078A39FF",
                    "country": "DE",
                    "type": "EXCHANGE"
                }
            },
            "purchasePrice": {
                "value": "18.01987",
                "unit": "EUR"
            },
            "prevDayPrice": {
                "price": {
                    "value": "23.4",
                    "unit": "EUR"
                },
                "priceDateTime": "2024-05-29T17:40:30+02",
                "venue": {
                    "name": "Xetra",
                    "venueId": "72274630AEA34CF2949911ED078A39FF",
                    "country": "DE",
                    "type": "EXCHANGE"
                }
            },
            "currentValue": {
                "value": "71100.000",
                "unit": "EUR"
            },
            "purchaseValue": {
                "value": "54059.61",
                "unit": "EUR"
            },
            "profitLossPurchaseAbs": {
                "value": "17040.390",
                "unit": "EUR"
            },
            "profitLossPurchaseRel": "31.521",
            "profitLossPrevDayAbs": {
                "value": "0.300",
                "unit": "EUR"
            },
            "profitLossPrevDayRel": "1.282",
            "profitLossPrevDayTotalAbs": {
                "value": "900.000",
                "unit": "EUR"
            },
            "version": null,
            "hedgeability": "HEDGEABLE",
            "availableQuantityToHedge": {
                "value": "3000",
                "unit": "XXX"
            },
            "currentPriceDeterminable": true
        },
        {
            "depotId": "8873872174FD4B30A7CE1407893A0333",
            "positionId": "849129B99E4D3C999B1728307167F634",
            "wkn": "BASF11",
            "custodyType": "004",
            "quantity": {
                "value": "8905",
                "unit": "XXX"
            },
            "availableQuantity": {
                "value": "8905.0",
                "unit": "XXX"
            },
            "currentPrice": {
                "price": {
                    "value": "48.08",
                    "unit": "EUR"
                },
                "priceDateTime": "2024-05-30T17:38:46+02",
                "venue": {
                    "name": "Xetra",
                    "venueId": "72274630AEA34CF2949911ED078A39FF",
                    "country": "DE",
                    "type": "EXCHANGE"
                }
            },
            "purchasePrice": {
                "value": "67.12040",
                "unit": "EUR"
            },
            "prevDayPrice": {
                "price": {
                    "value": "47.24",
                    "unit": "EUR"
                },
                "priceDateTime": "2024-05-29T17:35:25+02",
                "venue": {
                    "name": "Xetra",
                    "venueId": "72274630AEA34CF2949911ED078A39FF",
                    "country": "DE",
                    "type": "EXCHANGE"
                }
            },
            "currentValue": {
                "value": "428152.400",
                "unit": "EUR"
            },
            "purchaseValue": {
                "value": "597707.16",
                "unit": "EUR"
            },
            "profitLossPurchaseAbs": {
                "value": "-169554.760",
                "unit": "EUR"
            },
            "profitLossPurchaseRel": "-28.368",
            "profitLossPrevDayAbs": {
                "value": "0.840",
                "unit": "EUR"
            },
            "profitLossPrevDayRel": "1.778",
            "profitLossPrevDayTotalAbs": {
                "value": "7480.200",
                "unit": "EUR"
            },
            "version": null,
            "hedgeability": "HEDGEABLE",
            "availableQuantityToHedge": {
                "value": "8905",
                "unit": "XXX"
            },
            "currentPriceDeterminable": true
        },
        {
            "depotId": "8873872174FD4B30A7CE1407893A0333",
            "positionId": "BB1B53D049BE3F2986C00787A52B2B31",
            "wkn": "519000",
            "custodyType": "004",
            "quantity": {
                "value": "4705",
                "unit": "XXX"
            },
            "availableQuantity": {
                "value": "4705.0",
                "unit": "XXX"
            },
            "currentPrice": {
                "price": {
                    "value": "92.6",
                    "unit": "EUR"
                },
                "priceDateTime": "2024-05-30T17:35:10+02",
                "venue": {
                    "name": "Xetra",
                    "venueId": "72274630AEA34CF2949911ED078A39FF",
                    "country": "DE",
                    "type": "EXCHANGE"
                }
            },
            "purchasePrice": {
                "value": "77.08378",
                "unit": "EUR"
            },
            "prevDayPrice": {
                "price": {
                    "value": "91.82",
                    "unit": "EUR"
                },
                "priceDateTime": "2024-05-29T17:35:29+02",
                "venue": {
                    "name": "Xetra",
                    "venueId": "72274630AEA34CF2949911ED078A39FF",
                    "country": "DE",
                    "type": "EXCHANGE"
                }
            },
            "currentValue": {
                "value": "435683.000",
                "unit": "EUR"
            },
            "purchaseValue": {
                "value": "362679.18",
                "unit": "EUR"
            },
            "profitLossPurchaseAbs": {
                "value": "73003.820",
                "unit": "EUR"
            },
            "profitLossPurchaseRel": "20.129",
            "profitLossPrevDayAbs": {
                "value": "0.780",
                "unit": "EUR"
            },
            "profitLossPrevDayRel": "0.849",
            "profitLossPrevDayTotalAbs": {
                "value": "3669.900",
                "unit": "EUR"
            },
            "version": null,
            "hedgeability": "HEDGEABLE",
            "availableQuantityToHedge": {
                "value": "4705",
                "unit": "XXX"
            },
            "currentPriceDeterminable": true
        },
        {
            "depotId": "8873872174FD4B30A7CE1407893A0333",
            "positionId": "BE5FA85A9B983B3EA1CE3D9A9BB7CAC0",
            "wkn": "853055",
            "custodyType": "831",
            "quantity": {
                "value": "7300",
                "unit": "XXX"
            },
            "availableQuantity": {
                "value": "7300.0",
                "unit": "XXX"
            },
            "currentPrice": {
                "price": {
                    "value": "26.23",
                    "unit": "EUR"
                },
                "priceDateTime": "2024-05-30T17:25:02+02",
                "venue": {
                    "name": "Tradegate",
                    "venueId": "C3E017C9A9464A688F57E57BD7661C12",
                    "country": "DE",
                    "type": "EXCHANGE"
                }
            },
            "purchasePrice": {
                "value": "27.49002",
                "unit": "EUR"
            },
            "prevDayPrice": {
                "price": {
                    "value": "26.23",
                    "unit": "EUR"
                },
                "priceDateTime": "2024-05-29T22:26:25+02",
                "venue": {
                    "name": "Tradegate",
                    "venueId": "C3E017C9A9464A688F57E57BD7661C12",
                    "country": "DE",
                    "type": "EXCHANGE"
                }
            },
            "currentValue": {
                "value": "191479.000",
                "unit": "EUR"
            },
            "purchaseValue": {
                "value": "200677.15",
                "unit": "EUR"
            },
            "profitLossPurchaseAbs": {
                "value": "-9198.150",
                "unit": "EUR"
            },
            "profitLossPurchaseRel": "-4.584",
            "profitLossPrevDayAbs": {
                "value": "0.000",
                "unit": "EUR"
            },
            "profitLossPrevDayRel": "0.000",
            "profitLossPrevDayTotalAbs": {
                "value": "0.000",
                "unit": "EUR"
            },
            "version": null,
            "hedgeability": "NOT_HEDGEABLE",
            "availableQuantityToHedge": {
                "value": "0",
                "unit": "XXX"
            },
            "currentPriceDeterminable": true
        },
        {
            "depotId": "8873872174FD4B30A7CE1407893A0333",
            "positionId": "743CDE12B76033DD8721A7433AABE3A9",
            "wkn": "858326",
            "custodyType": "701",
            "quantity": {
                "value": "2100",
                "unit": "XXX"
            },
            "availableQuantity": {
                "value": "2100.0",
                "unit": "XXX"
            },
            "currentPrice": {
                "price": {
                    "value": "33.325",
                    "unit": "USD"
                },
                "priceDateTime": "2024-05-30T18:12:20+02",
                "venue": {
                    "name": "NYSE",
                    "venueId": "D0F1EA54E9764B409F006977C643FB6C",
                    "country": "US",
                    "type": "EXCHANGE"
                }
            },
            "purchasePrice": {
                "value": "23.97387",
                "unit": "EUR"
            },
            "prevDayPrice": {
                "price": {
                    "value": "32.79",
                    "unit": "USD"
                },
                "priceDateTime": "2024-05-29T22:00:02+02",
                "venue": {
                    "name": "NYSE",
                    "venueId": "D0F1EA54E9764B409F006977C643FB6C",
                    "country": "US",
                    "type": "EXCHANGE"
                }
            },
            "currentValue": {
                "value": "64579.757",
                "unit": "EUR"
            },
            "purchaseValue": {
                "value": "50345.13",
                "unit": "EUR"
            },
            "profitLossPurchaseAbs": {
                "value": "14234.627",
                "unit": "EUR"
            },
            "profitLossPurchaseRel": "28.274",
            "profitLossPrevDayAbs": {
                "value": "0.535",
                "unit": "EUR"
            },
            "profitLossPrevDayRel": "1.632",
            "profitLossPrevDayTotalAbs": {
                "value": "1036.764",
                "unit": "EUR"
            },
            "version": null,
            "hedgeability": "NOT_HEDGEABLE",
            "availableQuantityToHedge": {
                "value": "0",
                "unit": "XXX"
            },
            "currentPriceDeterminable": true
        },
        {
            "depotId": "8873872174FD4B30A7CE1407893A0333",
            "positionId": "1C35B194CA643C4987FD58A04A59A455",
            "wkn": "514000",
            "custodyType": "004",
            "quantity": {
                "value": "41465",
                "unit": "XXX"
            },
            "availableQuantity": {
                "value": "41465.0",
                "unit": "XXX"
            },
            "currentPrice": {
                "price": {
                    "value": "15.43",
                    "unit": "EUR"
                },
                "priceDateTime": "2024-05-30T17:35:23+02",
                "venue": {
                    "name": "Xetra",
                    "venueId": "72274630AEA34CF2949911ED078A39FF",
                    "country": "DE",
                    "type": "EXCHANGE"
                }
            },
            "purchasePrice": {
                "value": "11.13753",
                "unit": "EUR"
            },
            "prevDayPrice": {
                "price": {
                    "value": "15.336",
                    "unit": "EUR"
                },
                "priceDateTime": "2024-05-29T17:40:33+02",
                "venue": {
                    "name": "Xetra",
                    "venueId": "72274630AEA34CF2949911ED078A39FF",
                    "country": "DE",
                    "type": "EXCHANGE"
                }
            },
            "currentValue": {
                "value": "639804.950",
                "unit": "EUR"
            },
            "purchaseValue": {
                "value": "461817.68",
                "unit": "EUR"
            },
            "profitLossPurchaseAbs": {
                "value": "177987.270",
                "unit": "EUR"
            },
            "profitLossPurchaseRel": "38.541",
            "profitLossPrevDayAbs": {
                "value": "0.094",
                "unit": "EUR"
            },
            "profitLossPrevDayRel": "0.613",
            "profitLossPrevDayTotalAbs": {
                "value": "3897.710",
                "unit": "EUR"
            },
            "version": null,
            "hedgeability": "HEDGEABLE",
            "availableQuantityToHedge": {
                "value": "41465",
                "unit": "XXX"
            },
            "currentPriceDeterminable": true
        },
        {
            "depotId": "8873872174FD4B30A7CE1407893A0333",
            "positionId": "491BCDFC7952301C85BB899381F5AB14",
            "wkn": "A3DJ6W",
            "custodyType": "621",
            "quantity": {
                "value": "15200",
                "unit": "XXX"
            },
            "availableQuantity": {
                "value": "15200.0",
                "unit": "XXX"
            },
            "currentPrice": {
                "price": {
                    "value": "4.79",
                    "unit": "GBP"
                },
                "priceDateTime": "2024-05-30T17:35:06+02",
                "venue": {
                    "name": "London Stock Exchange",
                    "venueId": "0655F8A39E74464190EA2D03E8F5845D",
                    "country": "GB",
                    "type": "EXCHANGE"
                }
            },
            "purchasePrice": {
                "value": "6.29307",
                "unit": "EUR"
            },
            "prevDayPrice": {
                "price": {
                    "value": "4.80",
                    "unit": "GBP"
                },
                "priceDateTime": "2024-05-29T17:35:02+02",
                "venue": {
                    "name": "London Stock Exchange",
                    "venueId": "0655F8A39E74464190EA2D03E8F5845D",
                    "country": "GB",
                    "type": "EXCHANGE"
                }
            },
            "currentValue": {
                "value": "85568.888",
                "unit": "EUR"
            },
            "purchaseValue": {
                "value": "95654.62",
                "unit": "EUR"
            },
            "profitLossPurchaseAbs": {
                "value": "-10085.732",
                "unit": "EUR"
            },
            "profitLossPurchaseRel": "-10.544",
            "profitLossPrevDayAbs": {
                "value": "-0.010",
                "unit": "EUR"
            },
            "profitLossPrevDayRel": "-0.208",
            "profitLossPrevDayTotalAbs": {
                "value": "-178.641",
                "unit": "EUR"
            },
            "version": null,
            "hedgeability": "NOT_HEDGEABLE",
            "availableQuantityToHedge": {
                "value": "0",
                "unit": "XXX"
            },
            "currentPriceDeterminable": true
        },
        {
            "depotId": "8873872174FD4B30A7CE1407893A0333",
            "positionId": "B24B20F6CE7F3A679AF4D7F6F1D7E93B",
            "wkn": "A0Q87R",
            "custodyType": "701",
            "quantity": {
                "value": "5000",
                "unit": "XXX"
            },
            "availableQuantity": {
                "value": "5000.0",
                "unit": "XXX"
            },
            "currentPrice": {
                "price": {
                    "value": "29.97",
                    "unit": "USD"
                },
                "priceDateTime": "2024-05-30T18:10:46+02",
                "venue": {
                    "name": "NYSE",
                    "venueId": "D0F1EA54E9764B409F006977C643FB6C",
                    "country": "US",
                    "type": "EXCHANGE"
                }
            },
            "purchasePrice": {
                "value": "16.72250",
                "unit": "EUR"
            },
            "prevDayPrice": {
                "price": {
                    "value": "29.61",
                    "unit": "USD"
                },
                "priceDateTime": "2024-05-29T22:00:02+02",
                "venue": {
                    "name": "NYSE",
                    "venueId": "D0F1EA54E9764B409F006977C643FB6C",
                    "country": "US",
                    "type": "EXCHANGE"
                }
            },
            "currentValue": {
                "value": "138281.380",
                "unit": "EUR"
            },
            "purchaseValue": {
                "value": "83612.49",
                "unit": "EUR"
            },
            "profitLossPurchaseAbs": {
                "value": "54668.890",
                "unit": "EUR"
            },
            "profitLossPurchaseRel": "65.384",
            "profitLossPrevDayAbs": {
                "value": "0.360",
                "unit": "EUR"
            },
            "profitLossPrevDayRel": "1.216",
            "profitLossPrevDayTotalAbs": {
                "value": "1661.038",
                "unit": "EUR"
            },
            "version": null,
            "hedgeability": "NOT_HEDGEABLE",
            "availableQuantityToHedge": {
                "value": "0",
                "unit": "XXX"
            },
            "currentPriceDeterminable": true
        },
        {
            "depotId": "8873872174FD4B30A7CE1407893A0333",
            "positionId": "610C97AAD4733A48AAE25AA1A81A025C",
            "wkn": "853510",
            "custodyType": "831",
            "quantity": {
                "value": "10750",
                "unit": "XXX"
            },
            "availableQuantity": {
                "value": "10750.0",
                "unit": "XXX"
            },
            "currentPrice": {
                "price": {
                    "value": "19.656",
                    "unit": "EUR"
                },
                "priceDateTime": "2024-05-30T18:01:50+02",
                "venue": {
                    "name": "Tradegate",
                    "venueId": "C3E017C9A9464A688F57E57BD7661C12",
                    "country": "DE",
                    "type": "EXCHANGE"
                }
            },
            "purchasePrice": {
                "value": "9.30016",
                "unit": "EUR"
            },
            "prevDayPrice": {
                "price": {
                    "value": "19.804",
                    "unit": "EUR"
                },
                "priceDateTime": "2024-05-29T22:26:25+02",
                "venue": {
                    "name": "Tradegate",
                    "venueId": "C3E017C9A9464A688F57E57BD7661C12",
                    "country": "DE",
                    "type": "EXCHANGE"
                }
            },
            "currentValue": {
                "value": "211302.000",
                "unit": "EUR"
            },
            "purchaseValue": {
                "value": "99976.67",
                "unit": "EUR"
            },
            "profitLossPurchaseAbs": {
                "value": "111325.330",
                "unit": "EUR"
            },
            "profitLossPurchaseRel": "111.351",
            "profitLossPrevDayAbs": {
                "value": "-0.148",
                "unit": "EUR"
            },
            "profitLossPrevDayRel": "-0.747",
            "profitLossPrevDayTotalAbs": {
                "value": "-1591.000",
                "unit": "EUR"
            },
            "version": null,
            "hedgeability": "NOT_HEDGEABLE",
            "availableQuantityToHedge": {
                "value": "0",
                "unit": "XXX"
            },
            "currentPriceDeterminable": true
        },
        {
            "depotId": "8873872174FD4B30A7CE1407893A0333",
            "positionId": "5869BB6DB35130FC808A4BF61374B1B1",
            "wkn": "850517",
            "custodyType": "620",
            "quantity": {
                "value": "55257",
                "unit": "XXX"
            },
            "availableQuantity": {
                "value": "55257.0",
                "unit": "XXX"
            },
            "currentPrice": {
                "price": {
                    "value": "5.708",
                    "unit": "EUR"
                },
                "priceDateTime": "2024-05-30T17:35:59+02",
                "venue": {
                    "name": "Xetra",
                    "venueId": "72274630AEA34CF2949911ED078A39FF",
                    "country": "DE",
                    "type": "EXCHANGE"
                }
            },
            "purchasePrice": {
                "value": "5.49517",
                "unit": "EUR"
            },
            "prevDayPrice": {
                "price": {
                    "value": "5.751",
                    "unit": "EUR"
                },
                "priceDateTime": "2024-05-29T17:35:45+02",
                "venue": {
                    "name": "Xetra",
                    "venueId": "72274630AEA34CF2949911ED078A39FF",
                    "country": "DE",
                    "type": "EXCHANGE"
                }
            },
            "currentValue": {
                "value": "315406.956",
                "unit": "EUR"
            },
            "purchaseValue": {
                "value": "303646.61",
                "unit": "EUR"
            },
            "profitLossPurchaseAbs": {
                "value": "11760.346",
                "unit": "EUR"
            },
            "profitLossPurchaseRel": "3.873",
            "profitLossPrevDayAbs": {
                "value": "-0.043",
                "unit": "EUR"
            },
            "profitLossPrevDayRel": "-0.748",
            "profitLossPrevDayTotalAbs": {
                "value": "-2376.051",
                "unit": "EUR"
            },
            "version": null,
            "hedgeability": "NOT_HEDGEABLE",
            "availableQuantityToHedge": {
                "value": "0",
                "unit": "XXX"
            },
            "currentPriceDeterminable": true
        },
        {
            "depotId": "8873872174FD4B30A7CE1407893A0333",
            "positionId": "F2B1AA80EF8235839FC798DA4D6EF946",
            "wkn": "903276",
            "custodyType": "700",
            "quantity": {
                "value": "137100",
                "unit": "XXX"
            },
            "availableQuantity": {
                "value": "137100.0",
                "unit": "XXX"
            },
            "currentPrice": {
                "price": {
                    "value": "0",
                    "unit": "EUR"
                },
                "priceDateTime": null
            },
            "purchasePrice": {
                "value": "3.65124",
                "unit": "EUR"
            },
            "prevDayPrice": {
                "price": {
                    "value": "0",
                    "unit": "EUR"
                },
                "priceDateTime": null
            },
            "currentValue": {
                "value": "0.000",
                "unit": "EUR"
            },
            "purchaseValue": {
                "value": "500585",
                "unit": "EUR"
            },
            "profitLossPurchaseAbs": {
                "value": "-500585.000",
                "unit": "EUR"
            },
            "profitLossPurchaseRel": "-100.000",
            "profitLossPrevDayAbs": {
                "value": "0",
                "unit": "EUR"
            },
            "profitLossPrevDayRel": "0.000",
            "profitLossPrevDayTotalAbs": {
                "value": "0.000",
                "unit": "EUR"
            },
            "version": null,
            "hedgeability": "NOT_HEDGEABLE",
            "availableQuantityToHedge": {
                "value": "0",
                "unit": "XXX"
            },
            "currentPriceDeterminable": false
        },
        {
            "depotId": "8873872174FD4B30A7CE1407893A0333",
            "positionId": "49EDED12C5723485B066450BBB76A147",
            "wkn": "DTR0CK",
            "custodyType": "004",
            "quantity": {
                "value": "6797",
                "unit": "XXX"
            },
            "availableQuantity": {
                "value": "6797.0",
                "unit": "XXX"
            },
            "currentPrice": {
                "price": {
                    "value": "38.97",
                    "unit": "EUR"
                },
                "priceDateTime": "2024-05-30T17:35:25+02",
                "venue": {
                    "name": "Xetra",
                    "venueId": "72274630AEA34CF2949911ED078A39FF",
                    "country": "DE",
                    "type": "EXCHANGE"
                }
            },
            "purchasePrice": {
                "value": "0.00001",
                "unit": "EUR"
            },
            "prevDayPrice": {
                "price": {
                    "value": "38.84",
                    "unit": "EUR"
                },
                "priceDateTime": "2024-05-29T17:37:57+02",
                "venue": {
                    "name": "Xetra",
                    "venueId": "72274630AEA34CF2949911ED078A39FF",
                    "country": "DE",
                    "type": "EXCHANGE"
                }
            },
            "currentValue": {
                "value": "264879.090",
                "unit": "EUR"
            },
            "purchaseValue": {
                "value": "0.07",
                "unit": "EUR"
            },
            "profitLossPurchaseAbs": {
                "value": "264879.020",
                "unit": "EUR"
            },
            "profitLossPurchaseRel": "378398600.000",
            "profitLossPrevDayAbs": {
                "value": "0.130",
                "unit": "EUR"
            },
            "profitLossPrevDayRel": "0.335",
            "profitLossPrevDayTotalAbs": {
                "value": "883.610",
                "unit": "EUR"
            },
            "version": null,
            "hedgeability": "HEDGEABLE",
            "availableQuantityToHedge": {
                "value": "6797",
                "unit": "XXX"
            },
            "currentPriceDeterminable": true
        },
        {
            "depotId": "8873872174FD4B30A7CE1407893A0333",
            "positionId": "DBE52F06A9ED30D5B73EB347C86E2BA9",
            "wkn": "A1X3YY",
            "custodyType": "004",
            "quantity": {
                "value": "8600",
                "unit": "XXX"
            },
            "availableQuantity": {
                "value": "8600.0",
                "unit": "XXX"
            },
            "currentPrice": {
                "price": {
                    "value": "7.2",
                    "unit": "EUR"
                },
                "priceDateTime": "2024-05-30T16:17:07+02",
                "venue": {
                    "name": "Tradegate",
                    "venueId": "C3E017C9A9464A688F57E57BD7661C12",
                    "country": "DE",
                    "type": "EXCHANGE"
                }
            },
            "purchasePrice": {
                "value": "5.78071",
                "unit": "EUR"
            },
            "prevDayPrice": {
                "price": {
                    "value": "7.35",
                    "unit": "EUR"
                },
                "priceDateTime": "2024-05-29T22:26:31+02",
                "venue": {
                    "name": "Tradegate",
                    "venueId": "C3E017C9A9464A688F57E57BD7661C12",
                    "country": "DE",
                    "type": "EXCHANGE"
                }
            },
            "currentValue": {
                "value": "61920.000",
                "unit": "EUR"
            },
            "purchaseValue": {
                "value": "49714.11",
                "unit": "EUR"
            },
            "profitLossPurchaseAbs": {
                "value": "12205.890",
                "unit": "EUR"
            },
            "profitLossPurchaseRel": "24.552",
            "profitLossPrevDayAbs": {
                "value": "-0.150",
                "unit": "EUR"
            },
            "profitLossPrevDayRel": "-2.041",
            "profitLossPrevDayTotalAbs": {
                "value": "-1290.000",
                "unit": "EUR"
            },
            "version": null,
            "hedgeability": "HEDGEABLE",
            "availableQuantityToHedge": {
                "value": "8600",
                "unit": "XXX"
            },
            "currentPriceDeterminable": true
        },
        {
            "depotId": "8873872174FD4B30A7CE1407893A0333",
            "positionId": "E4ED697E1956360480464CE20DE7D9A1",
            "wkn": "A3ETYB",
            "custodyType": "611",
            "quantity": {
                "value": "940",
                "unit": "XXX"
            },
            "availableQuantity": {
                "value": "940.0",
                "unit": "XXX"
            },
            "currentPrice": {
                "price": {
                    "value": "31.73",
                    "unit": "CHF"
                },
                "priceDateTime": "2024-05-30T17:30:39+02",
                "venue": {
                    "name": "SIX Swiss Exchange",
                    "venueId": "F29D82478F454DA1BB919EBC8377DA63",
                    "country": "CH",
                    "type": "EXCHANGE"
                }
            },
            "purchasePrice": {
                "value": "0.00001",
                "unit": "EUR"
            },
            "prevDayPrice": {
                "price": {
                    "value": "31.57",
                    "unit": "CHF"
                },
                "priceDateTime": "2024-05-29T17:31:03+02",
                "venue": {
                    "name": "SIX Swiss Exchange",
                    "venueId": "F29D82478F454DA1BB919EBC8377DA63",
                    "country": "CH",
                    "type": "EXCHANGE"
                }
            },
            "currentValue": {
                "value": "30453.232",
                "unit": "EUR"
            },
            "purchaseValue": {
                "value": "0.01",
                "unit": "EUR"
            },
            "profitLossPurchaseAbs": {
                "value": "30453.222",
                "unit": "EUR"
            },
            "profitLossPurchaseRel": "304532220.000",
            "profitLossPrevDayAbs": {
                "value": "0.160",
                "unit": "EUR"
            },
            "profitLossPrevDayRel": "0.507",
            "profitLossPrevDayTotalAbs": {
                "value": "153.562",
                "unit": "EUR"
            },
            "version": null,
            "hedgeability": "NOT_HEDGEABLE",
            "availableQuantityToHedge": {
                "value": "0",
                "unit": "XXX"
            },
            "currentPriceDeterminable": true
        },
        {
            "depotId": "8873872174FD4B30A7CE1407893A0333",
            "positionId": "69B83A52141B35F6AEA756F9E3479842",
            "wkn": "DBX0AN",
            "custodyType": "645",
            "quantity": {
                "value": "4250",
                "unit": "XXX"
            },
            "availableQuantity": {
                "value": "4250.0",
                "unit": "XXX"
            },
            "currentPrice": {
                "price": {
                    "value": "141.8999",
                    "unit": "EUR"
                },
                "priceDateTime": "2024-05-30T18:25:58+02",
                "venue": {
                    "name": "Tradegate",
                    "venueId": "C3E017C9A9464A688F57E57BD7661C12",
                    "country": "DE",
                    "type": "EXCHANGE"
                }
            },
            "purchasePrice": {
                "value": "141.34600",
                "unit": "EUR"
            },
            "prevDayPrice": {
                "price": {
                    "value": "141.851",
                    "unit": "EUR"
                },
                "priceDateTime": "2024-05-29T22:25:55+02",
                "venue": {
                    "name": "Tradegate",
                    "venueId": "C3E017C9A9464A688F57E57BD7661C12",
                    "country": "DE",
                    "type": "EXCHANGE"
                }
            },
            "currentValue": {
                "value": "603074.575",
                "unit": "EUR"
            },
            "purchaseValue": {
                "value": "600720.5",
                "unit": "EUR"
            },
            "profitLossPurchaseAbs": {
                "value": "2354.075",
                "unit": "EUR"
            },
            "profitLossPurchaseRel": "0.392",
            "profitLossPrevDayAbs": {
                "value": "0.049",
                "unit": "EUR"
            },
            "profitLossPrevDayRel": "0.035",
            "profitLossPrevDayTotalAbs": {
                "value": "207.825",
                "unit": "EUR"
            },
            "version": null,
            "hedgeability": "HEDGEABLE",
            "availableQuantityToHedge": {
                "value": "4250",
                "unit": "XXX"
            },
            "currentPriceDeterminable": true
        },
        {
            "depotId": "8873872174FD4B30A7CE1407893A0333",
            "positionId": "CBBE5745EB2A3117AB4FF8D7A2A21B9D",
            "wkn": "A37FT9",
            "custodyType": "004",
            "quantity": {
                "value": "4000",
                "unit": "XXX"
            },
            "availableQuantity": {
                "value": "4000.0",
                "unit": "XXX"
            },
            "currentPrice": {
                "price": {
                    "value": "34.1",
                    "unit": "EUR"
                },
                "priceDateTime": "2024-05-30T13:29:19+02",
                "venue": {
                    "name": "Hamburg",
                    "venueId": "820EDB86332F43B29AA288A042C6B87B",
                    "country": "DE",
                    "type": "EXCHANGE"
                }
            },
            "purchasePrice": {
                "value": "24.97100",
                "unit": "EUR"
            },
            "prevDayPrice": {
                "price": {
                    "value": "34.0",
                    "unit": "EUR"
                },
                "priceDateTime": "2024-05-29T17:05:12+02",
                "venue": {
                    "name": "Hamburg",
                    "venueId": "820EDB86332F43B29AA288A042C6B87B",
                    "country": "DE",
                    "type": "EXCHANGE"
                }
            },
            "currentValue": {
                "value": "136400.000",
                "unit": "EUR"
            },
            "purchaseValue": {
                "value": "99884",
                "unit": "EUR"
            },
            "profitLossPurchaseAbs": {
                "value": "36516.000",
                "unit": "EUR"
            },
            "profitLossPurchaseRel": "36.558",
            "profitLossPrevDayAbs": {
                "value": "0.100",
                "unit": "EUR"
            },
            "profitLossPrevDayRel": "0.294",
            "profitLossPrevDayTotalAbs": {
                "value": "400.000",
                "unit": "EUR"
            },
            "version": null,
            "hedgeability": "HEDGEABLE",
            "availableQuantityToHedge": {
                "value": "4000",
                "unit": "XXX"
            },
            "currentPriceDeterminable": true
        },
        {
            "depotId": "8873872174FD4B30A7CE1407893A0333",
            "positionId": "BCAE228FC628330C8BD904FE3AF0F054",
            "wkn": "579919",
            "custodyType": "611",
            "quantity": {
                "value": "425",
                "unit": "XXX"
            },
            "availableQuantity": {
                "value": "425.0",
                "unit": "XXX"
            },
            "currentPrice": {
                "price": {
                    "value": "469.2",
                    "unit": "CHF"
                },
                "priceDateTime": "2024-05-30T17:30:39+02",
                "venue": {
                    "name": "SIX Swiss Exchange",
                    "venueId": "F29D82478F454DA1BB919EBC8377DA63",
                    "country": "CH",
                    "type": "EXCHANGE"
                }
            },
            "purchasePrice": {
                "value": "234.22341",
                "unit": "EUR"
            },
            "prevDayPrice": {
                "price": {
                    "value": "471.0",
                    "unit": "CHF"
                },
                "priceDateTime": "2024-05-29T17:31:03+02",
                "venue": {
                    "name": "SIX Swiss Exchange",
                    "venueId": "F29D82478F454DA1BB919EBC8377DA63",
                    "country": "CH",
                    "type": "EXCHANGE"
                }
            },
            "currentValue": {
                "value": "203602.169",
                "unit": "EUR"
            },
            "purchaseValue": {
                "value": "99544.95",
                "unit": "EUR"
            },
            "profitLossPurchaseAbs": {
                "value": "104057.219",
                "unit": "EUR"
            },
            "profitLossPurchaseRel": "104.533",
            "profitLossPrevDayAbs": {
                "value": "-1.800",
                "unit": "EUR"
            },
            "profitLossPrevDayRel": "-0.382",
            "profitLossPrevDayTotalAbs": {
                "value": "-781.082",
                "unit": "EUR"
            },
            "version": null,
            "hedgeability": "NOT_HEDGEABLE",
            "availableQuantityToHedge": {
                "value": "0",
                "unit": "XXX"
            },
            "currentPriceDeterminable": true
        },
        {
            "depotId": "8873872174FD4B30A7CE1407893A0333",
            "positionId": "1F71CB6413083FB18248DB1AEB2ACF9E",
            "wkn": "766403",
            "custodyType": "004",
            "quantity": {
                "value": "2835",
                "unit": "XXX"
            },
            "availableQuantity": {
                "value": "2835.0",
                "unit": "XXX"
            },
            "currentPrice": {
                "price": {
                    "value": "114.5",
                    "unit": "EUR"
                },
                "priceDateTime": "2024-05-30T17:43:47+02",
                "venue": {
                    "name": "Xetra",
                    "venueId": "72274630AEA34CF2949911ED078A39FF",
                    "country": "DE",
                    "type": "EXCHANGE"
                }
            },
            "purchasePrice": {
                "value": "141.86028",
                "unit": "EUR"
            },
            "prevDayPrice": {
                "price": {
                    "value": "120.8",
                    "unit": "EUR"
                },
                "priceDateTime": "2024-05-29T17:44:39+02",
                "venue": {
                    "name": "Xetra",
                    "venueId": "72274630AEA34CF2949911ED078A39FF",
                    "country": "DE",
                    "type": "EXCHANGE"
                }
            },
            "currentValue": {
                "value": "324607.500",
                "unit": "EUR"
            },
            "purchaseValue": {
                "value": "402173.89",
                "unit": "EUR"
            },
            "profitLossPurchaseAbs": {
                "value": "-77566.390",
                "unit": "EUR"
            },
            "profitLossPurchaseRel": "-19.287",
            "profitLossPrevDayAbs": {
                "value": "-6.300",
                "unit": "EUR"
            },
            "profitLossPrevDayRel": "-5.215",
            "profitLossPrevDayTotalAbs": {
                "value": "-17860.500",
                "unit": "EUR"
            },
            "version": null,
            "hedgeability": "HEDGEABLE",
            "availableQuantityToHedge": {
                "value": "2835",
                "unit": "XXX"
            },
            "currentPriceDeterminable": true
        },
        {
            "depotId": "8873872174FD4B30A7CE1407893A0333",
            "positionId": "A3E893DA569639D489B7117F877B41C6",
            "wkn": "852147",
            "custodyType": "620",
            "quantity": {
                "value": "6225",
                "unit": "XXX"
            },
            "availableQuantity": {
                "value": "6225.0",
                "unit": "XXX"
            },
            "currentPrice": {
                "price": {
                    "value": "64.27",
                    "unit": "EUR"
                },
                "priceDateTime": "2024-05-30T17:35:40+02",
                "venue": {
                    "name": "Xetra",
                    "venueId": "72274630AEA34CF2949911ED078A39FF",
                    "country": "DE",
                    "type": "EXCHANGE"
                }
            },
            "purchasePrice": {
                "value": "32.17693",
                "unit": "EUR"
            },
            "prevDayPrice": {
                "price": {
                    "value": "64.51",
                    "unit": "EUR"
                },
                "priceDateTime": "2024-05-29T17:35:43+02",
                "venue": {
                    "name": "Xetra",
                    "venueId": "72274630AEA34CF2949911ED078A39FF",
                    "country": "DE",
                    "type": "EXCHANGE"
                }
            },
            "currentValue": {
                "value": "400080.750",
                "unit": "EUR"
            },
            "purchaseValue": {
                "value": "200301.39",
                "unit": "EUR"
            },
            "profitLossPurchaseAbs": {
                "value": "199779.360",
                "unit": "EUR"
            },
            "profitLossPurchaseRel": "99.739",
            "profitLossPrevDayAbs": {
                "value": "-0.240",
                "unit": "EUR"
            },
            "profitLossPrevDayRel": "-0.372",
            "profitLossPrevDayTotalAbs": {
                "value": "-1494.000",
                "unit": "EUR"
            },
            "version": null,
            "hedgeability": "HEDGEABLE",
            "availableQuantityToHedge": {
                "value": "6225",
                "unit": "XXX"
            },
            "currentPriceDeterminable": true
        },
        {
            "depotId": "8873872174FD4B30A7CE1407893A0333",
            "positionId": "CF2FBDD77AC435FBBCA6640820408228",
            "wkn": "850517",
            "custodyType": "621",
            "quantity": {
                "value": "26200",
                "unit": "XXX"
            },
            "availableQuantity": {
                "value": "26200.0",
                "unit": "XXX"
            },
            "currentPrice": {
                "price": {
                    "value": "4.85",
                    "unit": "GBP"
                },
                "priceDateTime": "2024-05-30T17:35:02+02",
                "venue": {
                    "name": "London Stock Exchange",
                    "venueId": "0655F8A39E74464190EA2D03E8F5845D",
                    "country": "GB",
                    "type": "EXCHANGE"
                }
            },
            "purchasePrice": {
                "value": "4.85360",
                "unit": "EUR"
            },
            "prevDayPrice": {
                "price": {
                    "value": "4.89",
                    "unit": "GBP"
                },
                "priceDateTime": "2024-05-29T17:35:21+02",
                "venue": {
                    "name": "London Stock Exchange",
                    "venueId": "0655F8A39E74464190EA2D03E8F5845D",
                    "country": "GB",
                    "type": "EXCHANGE"
                }
            },
            "currentValue": {
                "value": "149341.262",
                "unit": "EUR"
            },
            "purchaseValue": {
                "value": "127164.39",
                "unit": "EUR"
            },
            "profitLossPurchaseAbs": {
                "value": "22176.872",
                "unit": "EUR"
            },
            "profitLossPurchaseRel": "17.440",
            "profitLossPrevDayAbs": {
                "value": "-0.040",
                "unit": "EUR"
            },
            "profitLossPrevDayRel": "-0.818",
            "profitLossPrevDayTotalAbs": {
                "value": "-1231.681",
                "unit": "EUR"
            },
            "version": null,
            "hedgeability": "NOT_HEDGEABLE",
            "availableQuantityToHedge": {
                "value": "0",
                "unit": "XXX"
            },
            "currentPriceDeterminable": true
        },
        {
            "depotId": "8873872174FD4B30A7CE1407893A0333",
            "positionId": "2AFF623B51EE3CD5BB37DD1229C4246E",
            "wkn": "710000",
            "custodyType": "004",
            "quantity": {
                "value": "13595",
                "unit": "XXX"
            },
            "availableQuantity": {
                "value": "13595.0",
                "unit": "XXX"
            },
            "currentPrice": {
                "price": {
                    "value": "65.93",
                    "unit": "EUR"
                },
                "priceDateTime": "2024-05-30T17:35:16+02",
                "venue": {
                    "name": "Xetra",
                    "venueId": "72274630AEA34CF2949911ED078A39FF",
                    "country": "DE",
                    "type": "EXCHANGE"
                }
            },
            "purchasePrice": {
                "value": "58.99635",
                "unit": "EUR"
            },
            "prevDayPrice": {
                "price": {
                    "value": "65.47",
                    "unit": "EUR"
                },
                "priceDateTime": "2024-05-29T17:40:37+02",
                "venue": {
                    "name": "Xetra",
                    "venueId": "72274630AEA34CF2949911ED078A39FF",
                    "country": "DE",
                    "type": "EXCHANGE"
                }
            },
            "currentValue": {
                "value": "896318.350",
                "unit": "EUR"
            },
            "purchaseValue": {
                "value": "802055.38",
                "unit": "EUR"
            },
            "profitLossPurchaseAbs": {
                "value": "94262.970",
                "unit": "EUR"
            },
            "profitLossPurchaseRel": "11.753",
            "profitLossPrevDayAbs": {
                "value": "0.460",
                "unit": "EUR"
            },
            "profitLossPrevDayRel": "0.703",
            "profitLossPrevDayTotalAbs": {
                "value": "6253.700",
                "unit": "EUR"
            },
            "version": null,
            "hedgeability": "HEDGEABLE",
            "availableQuantityToHedge": {
                "value": "13595",
                "unit": "XXX"
            },
            "currentPriceDeterminable": true
        },
        {
            "depotId": "8873872174FD4B30A7CE1407893A0333",
            "positionId": "2335E6AF58CD3CDFA628F1A59BCF4F6C",
            "wkn": "A1T8Z2",
            "custodyType": "650",
            "quantity": {
                "value": "2900",
                "unit": "XXX"
            },
            "availableQuantity": {
                "value": "2900.0",
                "unit": "XXX"
            },
            "currentPrice": {
                "price": {
                    "value": "85.32",
                    "unit": "EUR"
                },
                "priceDateTime": "2022-03-01T08:16:56+01",
                "venue": {
                    "name": "Berlin",
                    "venueId": "B751308ADC2F4DD0BF7A332BA1BD4791",
                    "country": "DE",
                    "type": "EXCHANGE"
                }
            },
            "purchasePrice": {
                "value": "70.71000",
                "unit": "EUR"
            },
            "prevDayPrice": {
                "price": {
                    "value": "83.91",
                    "unit": "EUR"
                },
                "priceDateTime": "2022-02-28T20:53:31+01",
                "venue": {
                    "name": "Berlin",
                    "venueId": "B751308ADC2F4DD0BF7A332BA1BD4791",
                    "country": "DE",
                    "type": "EXCHANGE"
                }
            },
            "currentValue": {
                "value": "247428.000",
                "unit": "EUR"
            },
            "purchaseValue": {
                "value": "205059",
                "unit": "EUR"
            },
            "profitLossPurchaseAbs": {
                "value": "42369.000",
                "unit": "EUR"
            },
            "profitLossPurchaseRel": "20.662",
            "profitLossPrevDayAbs": {
                "value": "1.410",
                "unit": "EUR"
            },
            "profitLossPrevDayRel": "1.680",
            "profitLossPrevDayTotalAbs": {
                "value": "4089.000",
                "unit": "EUR"
            },
            "version": null,
            "hedgeability": "NOT_HEDGEABLE",
            "availableQuantityToHedge": {
                "value": "0",
                "unit": "XXX"
            },
            "currentPriceDeterminable": true
        },
        {
            "depotId": "8873872174FD4B30A7CE1407893A0333",
            "positionId": "D44D4B6C1F24302882F8AA6EB9D84F85",
            "wkn": "904278",
            "custodyType": "611",
            "quantity": {
                "value": "4700",
                "unit": "XXX"
            },
            "availableQuantity": {
                "value": "4700.0",
                "unit": "XXX"
            },
            "currentPrice": {
                "price": {
                    "value": "91.23",
                    "unit": "CHF"
                },
                "priceDateTime": "2024-05-30T17:30:39+02",
                "venue": {
                    "name": "SIX Swiss Exchange",
                    "venueId": "F29D82478F454DA1BB919EBC8377DA63",
                    "country": "CH",
                    "type": "EXCHANGE"
                }
            },
            "purchasePrice": {
                "value": "79.01214",
                "unit": "EUR"
            },
            "prevDayPrice": {
                "price": {
                    "value": "90.51",
                    "unit": "CHF"
                },
                "priceDateTime": "2024-05-29T17:31:03+02",
                "venue": {
                    "name": "SIX Swiss Exchange",
                    "venueId": "F29D82478F454DA1BB919EBC8377DA63",
                    "country": "CH",
                    "type": "EXCHANGE"
                }
            },
            "currentValue": {
                "value": "437795.203",
                "unit": "EUR"
            },
            "purchaseValue": {
                "value": "371357.04",
                "unit": "EUR"
            },
            "profitLossPurchaseAbs": {
                "value": "66438.163",
                "unit": "EUR"
            },
            "profitLossPurchaseRel": "17.891",
            "profitLossPrevDayAbs": {
                "value": "0.720",
                "unit": "EUR"
            },
            "profitLossPrevDayRel": "0.795",
            "profitLossPrevDayTotalAbs": {
                "value": "3455.141",
                "unit": "EUR"
            },
            "version": null,
            "hedgeability": "NOT_HEDGEABLE",
            "availableQuantityToHedge": {
                "value": "0",
                "unit": "XXX"
            },
            "currentPriceDeterminable": true
        },
        {
            "depotId": "8873872174FD4B30A7CE1407893A0333",
            "positionId": "44E00A2A4819389AA6FF8AFFE10BE9DB",
            "wkn": "DBX1MR",
            "custodyType": "645",
            "quantity": {
                "value": "8850",
                "unit": "XXX"
            },
            "availableQuantity": {
                "value": "8850.0",
                "unit": "XXX"
            },
            "currentPrice": {
                "price": {
                    "value": "44.255",
                    "unit": "EUR"
                },
                "priceDateTime": "2024-05-30T17:36:07+02",
                "venue": {
                    "name": "Xetra",
                    "venueId": "72274630AEA34CF2949911ED078A39FF",
                    "country": "DE",
                    "type": "EXCHANGE"
                }
            },
            "purchasePrice": {
                "value": "22.61561",
                "unit": "EUR"
            },
            "prevDayPrice": {
                "price": {
                    "value": "44.255",
                    "unit": "EUR"
                },
                "priceDateTime": "2024-05-29T17:36:14+02",
                "venue": {
                    "name": "Xetra",
                    "venueId": "72274630AEA34CF2949911ED078A39FF",
                    "country": "DE",
                    "type": "EXCHANGE"
                }
            },
            "currentValue": {
                "value": "391656.750",
                "unit": "EUR"
            },
            "purchaseValue": {
                "value": "200148.15",
                "unit": "EUR"
            },
            "profitLossPurchaseAbs": {
                "value": "191508.600",
                "unit": "EUR"
            },
            "profitLossPurchaseRel": "95.683",
            "profitLossPrevDayAbs": {
                "value": "0.000",
                "unit": "EUR"
            },
            "profitLossPrevDayRel": "0.000",
            "profitLossPrevDayTotalAbs": {
                "value": "0.000",
                "unit": "EUR"
            },
            "version": null,
            "hedgeability": "HEDGEABLE",
            "availableQuantityToHedge": {
                "value": "8850",
                "unit": "XXX"
            },
            "currentPriceDeterminable": true
        },
        {
            "depotId": "8873872174FD4B30A7CE1407893A0333",
            "positionId": "DB84D54799C334A0B041A3C034B8F124",
            "wkn": "894983",
            "custodyType": "851",
            "quantity": {
                "value": "602000",
                "unit": "XXX"
            },
            "availableQuantity": {
                "value": "602000.0",
                "unit": "XXX"
            },
            "currentPrice": {
                "price": {
                    "value": "11.12",
                    "unit": "HKD"
                },
                "priceDateTime": "2024-05-29T00:00:00+02"
            },
            "purchasePrice": {
                "value": "0.66740",
                "unit": "EUR"
            },
            "prevDayPrice": {
                "price": {
                    "value": "11.12",
                    "unit": "HKD"
                },
                "priceDateTime": "2024-05-29T00:00:00+02"
            },
            "currentValue": {
                "value": "790146.539",
                "unit": "EUR"
            },
            "purchaseValue": {
                "value": "401774.81",
                "unit": "EUR"
            },
            "profitLossPurchaseAbs": {
                "value": "388371.729",
                "unit": "EUR"
            },
            "profitLossPurchaseRel": "96.664",
            "profitLossPrevDayAbs": {
                "value": "0.000",
                "unit": "EUR"
            },
            "profitLossPrevDayRel": "0.000",
            "profitLossPrevDayTotalAbs": {
                "value": "0.000",
                "unit": "EUR"
            },
            "version": null,
            "hedgeability": "NOT_HEDGEABLE",
            "availableQuantityToHedge": {
                "value": "0",
                "unit": "XXX"
            },
            "currentPriceDeterminable": false
        },
        {
            "depotId": "8873872174FD4B30A7CE1407893A0333",
            "positionId": "10F3531C563C367FA77286C1DF47F5B2",
            "wkn": "840400",
            "custodyType": "004",
            "quantity": {
                "value": "5030",
                "unit": "XXX"
            },
            "availableQuantity": {
                "value": "5030.0",
                "unit": "XXX"
            },
            "currentPrice": {
                "price": {
                    "value": "265.7",
                    "unit": "EUR"
                },
                "priceDateTime": "2024-05-30T17:35:14+02",
                "venue": {
                    "name": "Xetra",
                    "venueId": "72274630AEA34CF2949911ED078A39FF",
                    "country": "DE",
                    "type": "EXCHANGE"
                }
            },
            "purchasePrice": {
                "value": "139.12786",
                "unit": "EUR"
            },
            "prevDayPrice": {
                "price": {
                    "value": "263.9",
                    "unit": "EUR"
                },
                "priceDateTime": "2024-05-29T17:35:20+02",
                "venue": {
                    "name": "Xetra",
                    "venueId": "72274630AEA34CF2949911ED078A39FF",
                    "country": "DE",
                    "type": "EXCHANGE"
                }
            },
            "currentValue": {
                "value": "1336471.000",
                "unit": "EUR"
            },
            "purchaseValue": {
                "value": "699813.14",
                "unit": "EUR"
            },
            "profitLossPurchaseAbs": {
                "value": "636657.860",
                "unit": "EUR"
            },
            "profitLossPurchaseRel": "90.975",
            "profitLossPrevDayAbs": {
                "value": "1.800",
                "unit": "EUR"
            },
            "profitLossPrevDayRel": "0.682",
            "profitLossPrevDayTotalAbs": {
                "value": "9054.000",
                "unit": "EUR"
            },
            "version": null,
            "hedgeability": "HEDGEABLE",
            "availableQuantityToHedge": {
                "value": "5030",
                "unit": "XXX"
            },
            "currentPriceDeterminable": true
        },
        {
            "depotId": "8873872174FD4B30A7CE1407893A0333",
            "positionId": "3792B51BAC40344EAE6FF79A25F8F59E",
            "wkn": "885166",
            "custodyType": "620",
            "quantity": {
                "value": "5400",
                "unit": "XXX"
            },
            "availableQuantity": {
                "value": "5400.0",
                "unit": "XXX"
            },
            "currentPrice": {
                "price": {
                    "value": "51.4",
                    "unit": "EUR"
                },
                "priceDateTime": "2024-05-30T17:44:14+02",
                "venue": {
                    "name": "Tradegate",
                    "venueId": "C3E017C9A9464A688F57E57BD7661C12",
                    "country": "DE",
                    "type": "EXCHANGE"
                }
            },
            "purchasePrice": {
                "value": "37.26133",
                "unit": "EUR"
            },
            "prevDayPrice": {
                "price": {
                    "value": "52.6",
                    "unit": "EUR"
                },
                "priceDateTime": "2024-05-29T22:26:29+02",
                "venue": {
                    "name": "Tradegate",
                    "venueId": "C3E017C9A9464A688F57E57BD7661C12",
                    "country": "DE",
                    "type": "EXCHANGE"
                }
            },
            "currentValue": {
                "value": "277560.000",
                "unit": "EUR"
            },
            "purchaseValue": {
                "value": "201211.18",
                "unit": "EUR"
            },
            "profitLossPurchaseAbs": {
                "value": "76348.820",
                "unit": "EUR"
            },
            "profitLossPurchaseRel": "37.945",
            "profitLossPrevDayAbs": {
                "value": "-1.200",
                "unit": "EUR"
            },
            "profitLossPrevDayRel": "-2.281",
            "profitLossPrevDayTotalAbs": {
                "value": "-6480.000",
                "unit": "EUR"
            },
            "version": null,
            "hedgeability": "HEDGEABLE",
            "availableQuantityToHedge": {
                "value": "5400",
                "unit": "XXX"
            },
            "currentPriceDeterminable": true
        },
        {
            "depotId": "8873872174FD4B30A7CE1407893A0333",
            "positionId": "F28844593AE1376DA4DB7EF3ECC14E9B",
            "wkn": "CBK100",
            "custodyType": "004",
            "quantity": {
                "value": "125000",
                "unit": "XXX"
            },
            "availableQuantity": {
                "value": "125000.0",
                "unit": "XXX"
            },
            "currentPrice": {
                "price": {
                    "value": "15.705",
                    "unit": "EUR"
                },
                "priceDateTime": "2024-05-30T17:35:10+02",
                "venue": {
                    "name": "Xetra",
                    "venueId": "72274630AEA34CF2949911ED078A39FF",
                    "country": "DE",
                    "type": "EXCHANGE"
                }
            },
            "purchasePrice": {
                "value": "8.17918",
                "unit": "EUR"
            },
            "prevDayPrice": {
                "price": {
                    "value": "15.395",
                    "unit": "EUR"
                },
                "priceDateTime": "2024-05-29T17:36:48+02",
                "venue": {
                    "name": "Xetra",
                    "venueId": "72274630AEA34CF2949911ED078A39FF",
                    "country": "DE",
                    "type": "EXCHANGE"
                }
            },
            "currentValue": {
                "value": "1963125.000",
                "unit": "EUR"
            },
            "purchaseValue": {
                "value": "1022397.5",
                "unit": "EUR"
            },
            "profitLossPurchaseAbs": {
                "value": "940727.500",
                "unit": "EUR"
            },
            "profitLossPurchaseRel": "92.012",
            "profitLossPrevDayAbs": {
                "value": "0.310",
                "unit": "EUR"
            },
            "profitLossPrevDayRel": "2.014",
            "profitLossPrevDayTotalAbs": {
                "value": "38750.000",
                "unit": "EUR"
            },
            "version": null,
            "hedgeability": "HEDGEABLE",
            "availableQuantityToHedge": {
                "value": "125000",
                "unit": "XXX"
            },
            "currentPriceDeterminable": true
        },
        {
            "depotId": "8873872174FD4B30A7CE1407893A0333",
            "positionId": "1B5CF6190DC53612BA65934784D35014",
            "wkn": "855182",
            "custodyType": "831",
            "quantity": {
                "value": "48000",
                "unit": "XXX"
            },
            "availableQuantity": {
                "value": "48000.0",
                "unit": "XXX"
            },
            "currentPrice": {
                "price": {
                    "value": "13.45",
                    "unit": "EUR"
                },
                "priceDateTime": "2024-05-30T16:36:34+02",
                "venue": {
                    "name": "Tradegate",
                    "venueId": "C3E017C9A9464A688F57E57BD7661C12",
                    "country": "DE",
                    "type": "EXCHANGE"
                }
            },
            "purchasePrice": {
                "value": "4.26377",
                "unit": "EUR"
            },
            "prevDayPrice": {
                "price": {
                    "value": "13.2",
                    "unit": "EUR"
                },
                "priceDateTime": "2024-05-29T22:26:25+02",
                "venue": {
                    "name": "Tradegate",
                    "venueId": "C3E017C9A9464A688F57E57BD7661C12",
                    "country": "DE",
                    "type": "EXCHANGE"
                }
            },
            "currentValue": {
                "value": "645600.000",
                "unit": "EUR"
            },
            "purchaseValue": {
                "value": "204660.74",
                "unit": "EUR"
            },
            "profitLossPurchaseAbs": {
                "value": "440939.260",
                "unit": "EUR"
            },
            "profitLossPurchaseRel": "215.449",
            "profitLossPrevDayAbs": {
                "value": "0.250",
                "unit": "EUR"
            },
            "profitLossPrevDayRel": "1.894",
            "profitLossPrevDayTotalAbs": {
                "value": "12000.000",
                "unit": "EUR"
            },
            "version": null,
            "hedgeability": "NOT_HEDGEABLE",
            "availableQuantityToHedge": {
                "value": "0",
                "unit": "XXX"
            },
            "currentPriceDeterminable": true
        },
        {
            "depotId": "8873872174FD4B30A7CE1407893A0333",
            "positionId": "66416ABF8FC63A9A993650A736F99AC2",
            "wkn": "886455",
            "custodyType": "620",
            "quantity": {
                "value": "1710",
                "unit": "XXX"
            },
            "availableQuantity": {
                "value": "1710.0",
                "unit": "XXX"
            },
            "currentPrice": {
                "price": {
                    "value": "141.5",
                    "unit": "EUR"
                },
                "priceDateTime": "2024-05-30T17:35:35+02",
                "venue": {
                    "name": "Xetra",
                    "venueId": "72274630AEA34CF2949911ED078A39FF",
                    "country": "DE",
                    "type": "EXCHANGE"
                }
            },
            "purchasePrice": {
                "value": "58.60216",
                "unit": "EUR"
            },
            "prevDayPrice": {
                "price": {
                    "value": "140.6",
                    "unit": "EUR"
                },
                "priceDateTime": "2024-05-29T17:35:42+02",
                "venue": {
                    "name": "Xetra",
                    "venueId": "72274630AEA34CF2949911ED078A39FF",
                    "country": "DE",
                    "type": "EXCHANGE"
                }
            },
            "currentValue": {
                "value": "241965.000",
                "unit": "EUR"
            },
            "purchaseValue": {
                "value": "100209.69",
                "unit": "EUR"
            },
            "profitLossPurchaseAbs": {
                "value": "141755.310",
                "unit": "EUR"
            },
            "profitLossPurchaseRel": "141.459",
            "profitLossPrevDayAbs": {
                "value": "0.900",
                "unit": "EUR"
            },
            "profitLossPrevDayRel": "0.640",
            "profitLossPrevDayTotalAbs": {
                "value": "1539.000",
                "unit": "EUR"
            },
            "version": null,
            "hedgeability": "HEDGEABLE",
            "availableQuantityToHedge": {
                "value": "1710",
                "unit": "XXX"
            },
            "currentPriceDeterminable": true
        },
        {
            "depotId": "8873872174FD4B30A7CE1407893A0333",
            "positionId": "39BEAACD40BF39518DDA6CB84D72BBF3",
            "wkn": "DBX0ES",
            "custodyType": "645",
            "quantity": {
                "value": "14200",
                "unit": "XXX"
            },
            "availableQuantity": {
                "value": "14200.0",
                "unit": "XXX"
            },
            "currentPrice": {
                "price": {
                    "value": "6.351",
                    "unit": "EUR"
                },
                "priceDateTime": "2024-05-30T17:36:22+02",
                "venue": {
                    "name": "Xetra",
                    "venueId": "72274630AEA34CF2949911ED078A39FF",
                    "country": "DE",
                    "type": "EXCHANGE"
                }
            },
            "purchasePrice": {
                "value": "3.47250",
                "unit": "EUR"
            },
            "prevDayPrice": {
                "price": {
                    "value": "6.325",
                    "unit": "EUR"
                },
                "priceDateTime": "2024-05-29T17:36:13+02",
                "venue": {
                    "name": "Xetra",
                    "venueId": "72274630AEA34CF2949911ED078A39FF",
                    "country": "DE",
                    "type": "EXCHANGE"
                }
            },
            "currentValue": {
                "value": "90184.200",
                "unit": "EUR"
            },
            "purchaseValue": {
                "value": "49309.5",
                "unit": "EUR"
            },
            "profitLossPurchaseAbs": {
                "value": "40874.700",
                "unit": "EUR"
            },
            "profitLossPurchaseRel": "82.894",
            "profitLossPrevDayAbs": {
                "value": "0.026",
                "unit": "EUR"
            },
            "profitLossPrevDayRel": "0.411",
            "profitLossPrevDayTotalAbs": {
                "value": "369.200",
                "unit": "EUR"
            },
            "version": null,
            "hedgeability": "HEDGEABLE",
            "availableQuantityToHedge": {
                "value": "14200",
                "unit": "XXX"
            },
            "currentPriceDeterminable": true
        },
        {
            "depotId": "8873872174FD4B30A7CE1407893A0333",
            "positionId": "5C971BEC703B37A49485DCD6199FA528",
            "wkn": "592353",
            "custodyType": "700",
            "quantity": {
                "value": "1725",
                "unit": "XXX"
            },
            "availableQuantity": {
                "value": "1725.0",
                "unit": "XXX"
            },
            "currentPrice": {
                "price": {
                    "value": "187.16",
                    "unit": "EUR"
                },
                "priceDateTime": "2024-05-29T22:02:07+02",
                "venue": {
                    "name": "Tradegate",
                    "venueId": "C3E017C9A9464A688F57E57BD7661C12",
                    "country": "DE",
                    "type": "EXCHANGE"
                }
            },
            "purchasePrice": {
                "value": "115.20684",
                "unit": "EUR"
            },
            "prevDayPrice": {
                "price": {
                    "value": "188.94",
                    "unit": "EUR"
                },
                "priceDateTime": "2024-05-28T22:02:06+02",
                "venue": {
                    "name": "Tradegate",
                    "venueId": "C3E017C9A9464A688F57E57BD7661C12",
                    "country": "DE",
                    "type": "EXCHANGE"
                }
            },
            "currentValue": {
                "value": "322851.000",
                "unit": "EUR"
            },
            "purchaseValue": {
                "value": "198731.8",
                "unit": "EUR"
            },
            "profitLossPurchaseAbs": {
                "value": "124119.200",
                "unit": "EUR"
            },
            "profitLossPurchaseRel": "62.456",
            "profitLossPrevDayAbs": {
                "value": "-1.780",
                "unit": "EUR"
            },
            "profitLossPrevDayRel": "-0.942",
            "profitLossPrevDayTotalAbs": {
                "value": "-3070.500",
                "unit": "EUR"
            },
            "version": null,
            "hedgeability": "HEDGEABLE",
            "availableQuantityToHedge": {
                "value": "1725",
                "unit": "XXX"
            },
            "currentPriceDeterminable": true
        },
        {
            "depotId": "8873872174FD4B30A7CE1407893A0333",
            "positionId": "9648328540FB3E9BBF2C9398F16FCAB1",
            "wkn": "BAY001",
            "custodyType": "004",
            "quantity": {
                "value": "5775",
                "unit": "XXX"
            },
            "availableQuantity": {
                "value": "5775.0",
                "unit": "XXX"
            },
            "currentPrice": {
                "price": {
                    "value": "28.055",
                    "unit": "EUR"
                },
                "priceDateTime": "2024-05-30T17:35:01+02",
                "venue": {
                    "name": "Xetra",
                    "venueId": "72274630AEA34CF2949911ED078A39FF",
                    "country": "DE",
                    "type": "EXCHANGE"
                }
            },
            "purchasePrice": {
                "value": "76.41216",
                "unit": "EUR"
            },
            "prevDayPrice": {
                "price": {
                    "value": "26.925",
                    "unit": "EUR"
                },
                "priceDateTime": "2024-05-29T17:40:31+02",
                "venue": {
                    "name": "Xetra",
                    "venueId": "72274630AEA34CF2949911ED078A39FF",
                    "country": "DE",
                    "type": "EXCHANGE"
                }
            },
            "currentValue": {
                "value": "162017.625",
                "unit": "EUR"
            },
            "purchaseValue": {
                "value": "441280.22",
                "unit": "EUR"
            },
            "profitLossPurchaseAbs": {
                "value": "-279262.595",
                "unit": "EUR"
            },
            "profitLossPurchaseRel": "-63.285",
            "profitLossPrevDayAbs": {
                "value": "1.130",
                "unit": "EUR"
            },
            "profitLossPrevDayRel": "4.197",
            "profitLossPrevDayTotalAbs": {
                "value": "6525.750",
                "unit": "EUR"
            },
            "version": null,
            "hedgeability": "HEDGEABLE",
            "availableQuantityToHedge": {
                "value": "5775",
                "unit": "XXX"
            },
            "currentPriceDeterminable": true
        },
        {
            "depotId": "8873872174FD4B30A7CE1407893A0333",
            "positionId": "AFA34CC71F8C3F38992C6705ED35F64D",
            "wkn": "A3C99G",
            "custodyType": "004",
            "quantity": {
                "value": "17580",
                "unit": "XXX"
            },
            "availableQuantity": {
                "value": "17580.0",
                "unit": "XXX"
            },
            "currentPrice": {
                "price": {
                    "value": "32.98",
                    "unit": "EUR"
                },
                "priceDateTime": "2024-05-30T18:25:54+02",
                "venue": {
                    "name": "Tradegate",
                    "venueId": "C3E017C9A9464A688F57E57BD7661C12",
                    "country": "DE",
                    "type": "EXCHANGE"
                }
            },
            "purchasePrice": {
                "value": "22.98769",
                "unit": "EUR"
            },
            "prevDayPrice": {
                "price": {
                    "value": "33.06",
                    "unit": "EUR"
                },
                "priceDateTime": "2024-05-29T22:26:52+02",
                "venue": {
                    "name": "Tradegate",
                    "venueId": "C3E017C9A9464A688F57E57BD7661C12",
                    "country": "DE",
                    "type": "EXCHANGE"
                }
            },
            "currentValue": {
                "value": "579788.400",
                "unit": "EUR"
            },
            "purchaseValue": {
                "value": "404123.59",
                "unit": "EUR"
            },
            "profitLossPurchaseAbs": {
                "value": "175664.810",
                "unit": "EUR"
            },
            "profitLossPurchaseRel": "43.468",
            "profitLossPrevDayAbs": {
                "value": "-0.080",
                "unit": "EUR"
            },
            "profitLossPrevDayRel": "-0.242",
            "profitLossPrevDayTotalAbs": {
                "value": "-1406.400",
                "unit": "EUR"
            },
            "version": null,
            "hedgeability": "HEDGEABLE",
            "availableQuantityToHedge": {
                "value": "17580",
                "unit": "XXX"
            },
            "currentPriceDeterminable": true
        }
    ]
}

```

### 5.1.3 Abruf einer Position des Depots

```bash
curl --location 'https://api.comdirect.de/api/brokerage/v3/depots/8873872174FD4B30A7CE1407893A0333/positions/CD60CD17AA77379787EF7C3D075C62B1' \
--header 'Accept: application/json' \
--header 'Authorization: Bearer 32734246-6015-4247-92fc-021ac8ce5e27' \
--header 'x-http-request-info: {"clientRequestId":{"sessionId":"f7af8fca-94a6-5772-7df3-663dd518fc78","requestId":"054471271"}}' \
--header 'Content-Type: application/json'
```

Requested data

```json
{
    "depotId": "8873872174FD4B30A7CE1407893A0333",
    "positionId": "CD60CD17AA77379787EF7C3D075C62B1",
    "wkn": "894983",
    "custodyType": "850",
    "quantity": {
        "value": "210000",
        "unit": "XXX"
    },
    "availableQuantity": {
        "value": "210000.0",
        "unit": "XXX"
    },
    "currentPrice": {
        "price": {
            "value": "1.3845",
            "unit": "EUR"
        },
        "priceDateTime": "2024-05-30T17:59:41+02",
        "venue": {
            "name": "Tradegate",
            "venueId": "C3E017C9A9464A688F57E57BD7661C12",
            "country": "DE",
            "type": "EXCHANGE"
        }
    },
    "purchasePrice": {
        "value": "0.47800",
        "unit": "EUR"
    },
    "prevDayPrice": {
        "price": {
            "value": "1.367",
            "unit": "EUR"
        },
        "priceDateTime": "2024-05-29T22:26:25+02",
        "venue": {
            "name": "Tradegate",
            "venueId": "C3E017C9A9464A688F57E57BD7661C12",
            "country": "DE",
            "type": "EXCHANGE"
        }
    },
    "currentValue": {
        "value": "290745.000",
        "unit": "EUR"
    },
    "purchaseValue": {
        "value": "100380",
        "unit": "EUR"
    },
    "profitLossPurchaseAbs": {
        "value": "190365.000",
        "unit": "EUR"
    },
    "profitLossPurchaseRel": "189.644",
    "profitLossPrevDayAbs": {
        "value": "0.018",
        "unit": "EUR"
    },
    "profitLossPrevDayRel": "1.317",
    "profitLossPrevDayTotalAbs": {
        "value": "3675.000",
        "unit": "EUR"
    },
    "version": null,
    "hedgeability": "NOT_HEDGEABLE",
    "availableQuantityToHedge": {
        "value": "0",
        "unit": "XXX"
    },
    "currentPriceDeterminable": true
}


```
### 5.1.4 Abruf Depotumsätze

```bash
curl --location 'https://api.comdirect.de/api/brokerage/v3/depots/8873872174FD4B30A7CE1407893A0333/transactions' \
--header 'Accept: application/json' \
--header 'Authorization: Bearer 32734246-6015-4247-92fc-021ac8ce5e27' \
--header 'x-http-request-info: {"clientRequestId":{"sessionId":"f7af8fca-94a6-5772-7df3-663dd518fc78","requestId":"054471271"}}' \
--header 'Content-Type: application/json' \
--header 'Cookie: qSession=d18952e4.a6cdf558b2484d638f4f535'
```

Requested data

```json
{
    "paging": {
        "index": 0,
        "matches": 1
    },
    "values": [
        {
            "transactionId": "D9833A99583047FD9CFD332FE056DBBE",
            "bookingStatus": "BOOKED",
            "bookingDate": "2024-04-26",
            "businessDate": "2024-04-25",
            "quantity": {
                "value": "4250",
                "unit": "XXX"
            },
            "instrumentId": "E9B28398CCAC47B39A57F5CE3B86AECD",
            "instrument": {
                "instrumentId": "E9B28398CCAC47B39A57F5CE3B86AECD",
                "wkn": "DBX0AN",
                "isin": "LU0290358497",
                "mnemonic": "XEON",
                "name": "Xtrackers II EUR Over.Rate Sw. Inhaber-Anteile 1C o.N.",
                "shortName": "XTR.II EUR OV.RATE SW. 1C",
                "staticData": {
                    "notation": "XXX",
                    "currency": "XXX",
                    "instrumentType": "ETF",
                    "priipsRelevant": true,
                    "kidAvailable": true,
                    "shippingWaiverRequired": true,
                    "fundRedemptionLimited": false,
                    "savingsPlanEligibility": "ELIGIBLE"
                }
            },
            "executionPrice": {
                "value": "141.346",
                "unit": "EUR"
            },
            "transactionValue": {
                "value": "600768.32",
                "unit": "EUR"
            },
            "transactionDirection": "IN",
            "transactionType": "BUY"
        }
    ]
}
```
