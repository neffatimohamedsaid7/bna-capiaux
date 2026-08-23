# Guide d'administration

Destiné à la personne qui installe, exploite ou administre l'application (équipe technique BNA
ou administrateur fonctionnel `ADMIN`). Pour le contrat WS1-WS4 vers BNAC, voir
[docs/ws-bnac.md](ws-bnac.md). Pour l'usage métier au quotidien, voir
[docs/manuel-utilisateur.md](manuel-utilisateur.md).

## 1. Prérequis

| Outil | Version | Usage |
|---|---|---|
| Java (JDK) | 17 | Backend Spring Boot |
| Maven | 3.9+ | Build/run du backend |
| Node.js + npm | 18+ | Frontend Angular |
| Docker + Docker Compose | — | PostgreSQL local |

## 2. Démarrage

```powershell
# 1. Base de donnees (depuis la racine du repo)
docker compose up -d

# 2. Backend (profil par defaut = PostgreSQL), port 8081
cd backend
mvn spring-boot:run "-Dspring-boot.run.arguments=--server.port=8081"

# 3. Frontend, port 4200 (proxy /api -> localhost:8081, pas de config CORS necessaire)
cd frontend
npm install
npm start
```

- Swagger UI (API REST) : http://localhost:8081/swagger-ui.html
- Application : http://localhost:4200

### Profil `dev` (sans Docker/PostgreSQL)

Pour développer sans dépendre d'une base PostgreSQL locale, une base H2 en mémoire est
disponible (données perdues à chaque redémarrage) :

```powershell
cd backend
mvn spring-boot:run "-Dspring-boot.run.profiles=dev" "-Dspring-boot.run.arguments=--server.port=8081"
```

Console H2 : http://localhost:8081/h2-console (JDBC URL `jdbc:h2:mem:bnac_dev`, user `sa`, pas de
mot de passe).

### Docker Compose — pourquoi le port 5433

`docker-compose.yml` expose PostgreSQL sur le port hôte **5433** (→ 5432 dans le conteneur) au
lieu du 5432 par défaut, car ce dernier est déjà occupé par une instance PostgreSQL native sur la
machine de développement d'origine. `application.yml` pointe déjà vers `localhost:5433` ; à
adapter si ce n'est pas le cas dans votre environnement.

## 3. Comptes utilisateurs

### Comptes de démonstration (premier démarrage)

Si la table `utilisateur` est vide au démarrage, `UtilisateurSeeder` crée automatiquement 3
comptes :

| Username | Mot de passe | Rôle |
|---|---|---|
| `admin` | `Admin123!` | `ADMIN` |
| `agent1` | `Agent123!` | `CHARGE_DE_DOSSIER` |
| `validateur1` | `Valid123!` | `VALIDATEUR` |

**À retirer ou changer avant toute mise en production** — ce sont des mots de passe connus et
publics (présents dans ce dépôt Git).

### Rôles applicatifs

| Rôle | Peut faire |
|---|---|
| `CHARGE_DE_DOSSIER` | Rechercher un client, créer/modifier/supprimer une PEC (souscription, rachat, ouverture de compte), importer des pièces jointes |
| `VALIDATEUR` | Valider ou rejeter une PEC en cours d'enregistrement, importer les documents de validation |
| `ADMIN` | Tout ce qui précède, + gestion des comptes utilisateurs (`/api/utilisateurs`) |

La consultation (Module 4) et la lecture de l'audit/des écritures comptables ne sont pas
restreintes par rôle : tout utilisateur authentifié y a accès.

### Gérer les comptes (ADMIN)

Via l'écran d'administration du frontend (menu réservé aux comptes `ADMIN`) ou directement l'API :

| Action | Endpoint |
|---|---|
| Lister les comptes | `GET /api/utilisateurs` |
| Créer un compte | `POST /api/utilisateurs` — body : `username`, `password` (8 caractères min.), `nom`, `prenom`, `email` (optionnel), `role` |
| Activer/désactiver un compte | `PATCH /api/utilisateurs/{id}/statut` — body : `{ "actif": true/false }` |

Un compte désactivé (`actif=false`) ne peut plus se connecter (`POST /api/auth/login` échoue),
mais son historique (audit, opérations créées) reste intact.

## 4. Configuration (`backend/src/main/resources/application.yml`)

### Sécurité JWT (`security.jwt.*`)

| Propriété | Rôle | Défaut |
|---|---|---|
| `secret` | Clé de signature HMAC des tokens | `change_this_secret_in_production` |
| `expiration-ms` | Durée de validité d'un token, en ms | `3600000` (1h) |

**`secret` doit impérativement être changé avant mise en production** (valeur aléatoire longue,
stockée hors du dépôt Git — variable d'environnement ou secret manager).

### Interfaçage BNAC (`bnac.ws.*` et `bnac.documents.*`)

Voir le détail complet dans [docs/ws-bnac.md](ws-bnac.md#configuration-applicationyml-préfixe-bnacws).
Point le plus important en administration : `bnac.ws.stub-mode` doit rester à `true` tant que les
vraies URLs/contrats WS1-WS4 n'ont pas été fournis et validés par BNA Capitaux ; le passer à
`false` sans ça casse l'intégration (le `RestTemplate` ira frapper une URL factice
`https://bnac.example.tn/ws`).

`bnac.documents.storage-path` (défaut `./documents`, donc `backend/documents/` en lançant depuis
`backend/`) : dossier local où sont stockées les pièces jointes (ordre de virement, bulletins
signés, formulaires...) importées lors des PEC/validations. Ce dossier n'est **pas** versionné
dans Git (voir `.gitignore`) — à sauvegarder séparément en production (aucune stratégie de backup
n'est mise en place actuellement, à définir avant mise en production).

### Base de données

Profil par défaut : PostgreSQL (`spring.datasource.*`, voir §2). `spring.jpa.hibernate.ddl-auto`
est en `update` : le schéma est créé/complété automatiquement au démarrage à partir des entités
JPA — pas de scripts de migration (Flyway/Liquibase) à ce stade. À réévaluer avant mise en
production (une stratégie de migration versionnée est recommandée pour éviter les dérives de
schéma non tracées).

## 5. Journalisation et traçabilité

- Logs applicatifs : sortie console standard (format par défaut Spring Boot), aucune
  agrégation/centralisation (ELK, etc.) mise en place.
- Traçabilité fonctionnelle (section 6.4 du cahier des charges) : chaque action significative
  (PEC, modification, import, validation, rejet, appel WS) est enregistrée en base dans
  `journal_audit`, consultable via `GET /api/audit/{typeOperation}/{operationId}` — voir
  [CLAUDE.md](../CLAUDE.md#fonctionnalites-transverses).
- Écritures comptables (trace débit/crédit à la validation) : `GET /api/ecritures-comptables/{typeOperation}/{operationId}`.

## 6. Dépannage

| Symptôme | Cause probable | Action |
|---|---|---|
| Le backend ne démarre pas, erreur de connexion PostgreSQL | Conteneur `bnac_postgres` non démarré ou port différent | `docker compose up -d`, vérifier `docker ps` et le port dans `application.yml` |
| `401 Unauthorized` sur tous les appels `/api/**` sauf `/api/auth/login` | Token JWT absent, expiré (> `expiration-ms`) ou compte désactivé | Se reconnecter via `/api/auth/login` |
| `403 Forbidden` sur une action de PEC ou de validation | Rôle insuffisant (ex. `CHARGE_DE_DOSSIER` tentant de valider) | Vérifier le rôle du compte, voir §3 |
| `502 Bad Gateway` sur une recherche client ou une validation | Échec technique d'appel WS1-WS4 (mode réel) ou configuration `bnac.ws.base-url` incorrecte | Voir [docs/ws-bnac.md](ws-bnac.md#gestion-des-erreurs) |
| Port 8081 ou 4200 déjà utilisé | Une instance précédente tourne encore | Arrêter le processus existant avant de relancer |
