# Interfaçage BNA – BNAC (WS1 à WS4)

Documentation technique du contrat d'interfaçage sortant vers BNA Capitaux (BNAC), section 8 du
cahier des charges. Complète le Swagger UI (`/swagger-ui.html`), qui documente uniquement l'API
REST **entrante** exposée par cette application, pas les web services **sortants** appelés côté
BNAC.

## Vue d'ensemble

- Contrat défini par l'interface [`BnacClient`](../backend/src/main/java/tn/bna/bnac/ws/BnacClient.java)
  (`tn.bna.bnac.ws`), avec deux implémentations :
  - [`BnacClientStub`](../backend/src/main/java/tn/bna/bnac/ws/BnacClientStub.java) : données
    simulées, aucun appel réseau. Active par défaut (`bnac.ws.stub-mode=true`).
  - [`BnacRestClient`](../backend/src/main/java/tn/bna/bnac/ws/BnacRestClient.java) : appels HTTP/JSON
    réels via `RestTemplate`. Active quand `bnac.ws.stub-mode=false`.
- **Aucune URL ni aucun contrat réel n'a été fourni par BNA Capitaux à ce jour.** Les chemins,
  noms de champs JSON et mécanisme d'authentification ci-dessous sont ceux retenus côté BNA en
  attendant confirmation — voir [Points à confirmer avant la bascule en mode réel](#points-à-confirmer-avant-la-bascule-en-mode-réel).
- Bascule stub → réel : `bnac.ws.stub-mode: false` dans `application.yml`.

### Configuration (`application.yml`, préfixe `bnac.ws`)

| Propriété | Rôle | Défaut |
|---|---|---|
| `stub-mode` | `true` = `BnacClientStub`, `false` = `BnacRestClient` | `true` |
| `base-url` | Racine des 4 endpoints | `https://bnac.example.tn/ws` |
| `timeout-ms` | Timeout connexion + lecture (aligné section 6.2 : réponse API ≤ 2s, WS BNAC inclus) | `2000` |
| `ws1-detail-client` | Chemin de WS1, concaténé à `base-url` | `/clients/detail` |
| `ws2-souscription` | Chemin de WS2 | `/souscriptions` |
| `ws3-demande-rachat` | Chemin de WS3 | `/rachats` |
| `ws4-ouverture-compte` | Chemin de WS4 | `/comptes` |
| `cache-ttl-seconds` | TTL du cache de la réponse WS1 (section 6.2, voir [Cache](#cache-ws1)) | `60` |

### Gestion des erreurs

Tout échec technique (timeout, indisponibilité, HTTP 5xx) lève une
[`BnacWebServiceException`](../backend/src/main/java/tn/bna/bnac/ws/BnacWebServiceException.java),
traduite par `GlobalExceptionHandler` en réponse **HTTP 502 Bad Gateway** côté API BNA. C'est
distinct des rejets **fonctionnels** renvoyés par BNAC dans le corps d'une réponse 200 (champs
`succes=false` / `messageErreur` pour WS2-WS4), qui remontent en HTTP 502 également côté appelant
mais avec le message métier de BNAC — voir le détail de chaque WS ci-dessous.

### Cache (WS1)

La réponse de WS1 est mise en cache (Caffeine, cache `detailClientBnac`) par critère de
recherche, TTL `bnac.ws.cache-ttl-seconds` (60s par défaut). S'applique aux deux implémentations
(stub et réelle). Voir [`CacheConfig`](../backend/src/main/java/tn/bna/bnac/config/CacheConfig.java).

---

## WS1 — Détail client BNAC

| | |
|---|---|
| **Déclencheur** | Étape 1 "recherche client", dans les 4 modules |
| **Méthode / URL** | `GET {base-url}{ws1-detail-client}?critere={critereRecherche}` |
| **DTO Java** | Requête : `String critereRecherche` (CIN, RNE, n° compte ou n° titre) — Réponse : [`ClientBnacDetailResponse`](../backend/src/main/java/tn/bna/bnac/dto/ClientBnacDetailResponse.java) |

### Réponse — `ClientBnacDetailResponse`

| Champ | Type | Description |
|---|---|---|
| `possedeCompteTitre` | boolean | `false` → le client n'a pas de compte titre BNAC ; `produits` est alors vide et l'appelant redirige vers le Module 3 (sauf en Module 3 lui-même, cf. RG3.1) |
| `identifiant` | string | Identifiant du client tel que résolu par BNAC |
| `typeIdentifiant` | string | Nature de l'identifiant (ex. `"CIN"`) |
| `nomPrenom` | string | |
| `adresse` | string | |
| `activite` | string | |
| `produits` | `ProduitBnacDto[]` | Liste des produits détenus/disponibles, voir ci-dessous |

### `ProduitBnacDto` (élément de `produits`)

| Champ | Type | Utilisé par |
|---|---|---|
| `numeroCompteTitre` | string | Modules 1, 2, 4 |
| `produit` | enum `ProduitFinancier` (`FCP_PROGRES`, `FCP_BNA_CAPITALISATION`, `PLACEMENT_OBLIGATOIRE_SICAV`, `SICAV`, `SICAV_BNA`) | Modules 1, 2, 4 |
| `valeurLiquidativeSouscription` | decimal | Module 1 (RG1 — calcul du montant de souscription) |
| `valeurLiquidativeRachat` | decimal | Module 2 (calcul du montant de rachat) |
| `nombreActionsEnProcession` | int | Modules 2 (RG2.2), 4 |
| `totalRachatsEnCours` | int | Module 2 (RG2.2) |
| `totalRachatsEnAttenteApprobationBnac` | int | Module 2 (RG2.2) |

### Exemple (mode stub, CIN `12345678`)

```json
{
  "possedeCompteTitre": true,
  "identifiant": "12345678",
  "typeIdentifiant": "CIN",
  "nomPrenom": "Client Simulé",
  "adresse": "Tunis, Tunisie",
  "activite": "Salarié",
  "produits": [
    {
      "numeroCompteTitre": "4047/155",
      "produit": "FCP_PROGRES",
      "valeurLiquidativeSouscription": 125.500,
      "valeurLiquidativeRachat": 124.900,
      "nombreActionsEnProcession": 10,
      "totalRachatsEnCours": 0,
      "totalRachatsEnAttenteApprobationBnac": 0
    }
  ]
}
```

Comportement stub particulier : le CIN `11112222` renvoie `possedeCompteTitre=false` et
`produits=[]`, pour tester RG3.3/RG4.2 (client BNA sans compte titre BNAC).

---

## WS2 — Souscription

| | |
|---|---|
| **Déclencheur** | Validation d'une PEC de souscription (Module 1, Étape 4) |
| **Méthode / URL** | `POST {base-url}{ws2-souscription}` |
| **DTO Java** | Requête : [`SouscriptionWsRequest`](../backend/src/main/java/tn/bna/bnac/dto/SouscriptionWsRequest.java) — Réponse : [`SouscriptionWsResponse`](../backend/src/main/java/tn/bna/bnac/dto/SouscriptionWsResponse.java) |

### Requête — `SouscriptionWsRequest`

| Champ | Type |
|---|---|
| `numeroSouscription` | string |
| `numeroCompteTitre` | string |
| `produit` | enum `ProduitFinancier` |
| `nombreActionsASouscrire` | int |
| `valeurLiquidative` | decimal |
| `montantSouscription` | decimal |
| `dateValeurComptable` | date (ISO 8601, RG1.6 : = date de l'opération) |

### Réponse — `SouscriptionWsResponse`

| Champ | Type | Description |
|---|---|---|
| `succes` | boolean | Rejet fonctionnel BNAC si `false` |
| `messageErreur` | string | Renseigné si `succes=false` |
| `referenceSouscription` | string | Référence BNAC, stockée sur la souscription (`referenceWs2`) |
| `nouveauNombreActions` | int | Nouveau solde d'actions après souscription |

---

## WS3 — Demande de rachat

| | |
|---|---|
| **Déclencheur** | Validation d'une PEC de rachat (Module 2, Étape 4) |
| **Méthode / URL** | `POST {base-url}{ws3-demande-rachat}` |
| **DTO Java** | Requête : [`RachatWsRequest`](../backend/src/main/java/tn/bna/bnac/dto/RachatWsRequest.java) — Réponse : [`RachatWsResponse`](../backend/src/main/java/tn/bna/bnac/dto/RachatWsResponse.java) |

### Requête — `RachatWsRequest`

| Champ | Type |
|---|---|
| `numeroRachat` | string |
| `numeroCompteTitre` | string |
| `produit` | enum `ProduitFinancier` |
| `nombreActionsAVendre` | int |
| `valeurLiquidativeRachat` | decimal |
| `montantRachat` | decimal |
| `dateValeurComptable` | date (ISO 8601, RG2.3 : = date de l'opération + 1 jour) |

### Réponse — `RachatWsResponse`

| Champ | Type | Description |
|---|---|---|
| `succes` | boolean | |
| `messageErreur` | string | Renseigné si `succes=false` |
| `referenceDemandeRachat` | string | Référence BNAC, stockée sur le rachat (`referenceWs3`) |

---

## WS4 — Ouverture de compte

| | |
|---|---|
| **Déclencheur** | Validation d'une PEC d'ouverture de compte titre (Module 3, Étape 4) |
| **Méthode / URL** | `POST {base-url}{ws4-ouverture-compte}` |
| **DTO Java** | Requête : [`OuvertureCompteWsRequest`](../backend/src/main/java/tn/bna/bnac/dto/OuvertureCompteWsRequest.java) — Réponse : [`OuvertureCompteWsResponse`](../backend/src/main/java/tn/bna/bnac/dto/OuvertureCompteWsResponse.java) |

### Requête — `OuvertureCompteWsRequest`

| Champ | Type |
|---|---|
| `numeroDemande` | string |
| `identifiantClient` | string |
| `typeIdentifiant` | string |
| `nomPrenom` | string |
| `adresse` | string |
| `activite` | string |
| `typeCompteSouhaite` | enum `ProduitFinancier` |

Envoyé pour tout client (avec ou sans compte BNAC préexistant) — RG3.3 : pour un client sans
compte BNAC, ce sont les données signalétiques du référentiel BNA qui sont transmises ici.

### Réponse — `OuvertureCompteWsResponse`

| Champ | Type | Description |
|---|---|---|
| `succes` | boolean | |
| `messageErreur` | string | Renseigné si `succes=false` |
| `numeroCompteTitre` | string | Nouveau numéro de compte titre créé par BNAC |
| `referenceOuverture` | string | Référence BNAC |

---

## Points à confirmer avant la bascule en mode réel

Ces éléments ne sont pas spécifiés dans le cahier des charges et devront être confirmés avec
BNA Capitaux avant de passer `bnac.ws.stub-mode` à `false` :

1. **Authentification** — `BnacRestClient` n'envoie aujourd'hui aucun en-tête d'authentification
   (`RestClientConfig`). À définir : clé API, OAuth2, mTLS, IP whitelisting...
2. **Format exact des URLs et noms de champs JSON** — les chemins et noms de champs ci-dessus
   sont ceux du contrat provisoire côté BNA ; à faire correspondre au contrat réel exposé par
   BNAC (probable besoin d'un mapping/adaptateur si les noms diffèrent).
3. **Codes d'erreur** — le contrat actuel suppose que BNAC répond toujours HTTP 200 avec
   `succes=false` pour un rejet fonctionnel, et un code HTTP d'erreur uniquement pour une panne
   technique. À confirmer que BNAC suit bien cette convention.
4. **Idempotence / rejeu** — en cas de timeout sur WS2/WS3/WS4 côté BNA sans certitude que BNAC a
   bien traité la demande, aucune stratégie de nouvelle tentative ou de vérification d'état n'est
   implémentée actuellement (l'opération reste au statut `EN_COURS_ENREGISTREMENT` si l'appel
   échoue avant validation).
