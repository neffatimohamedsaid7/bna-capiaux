# BNA Capitaux - Souscription et Rachat des Actions

Backend Spring Boot pour la gestion des souscriptions/rachats d'actions, ouvertures de compte
et consultation, en interfacage avec le systeme externe **BNAC** (4 web services WS1-WS4).

## Structure du projet

Monorepo avec backend et frontend separes en deux dossiers a la racine :

```
bnac-backend/
├── backend/            # API REST Spring Boot (Java 17, Maven) - voir Stack technique
├── frontend/           # SPA Angular 17 standalone
└── docker-compose.yml  # Postgres local (partage par les deux)
```

## Stack technique

- Spring Boot 3.2.5, Java 17, Jakarta EE (jakarta.persistence, jakarta.validation)
- Spring Data JPA + Hibernate 6.4.4
- PostgreSQL (prod, via Docker Compose fourni) / H2 en memoire (profil `dev`, pour tests rapides sans DB)
- Spring Security + JWT (authentification par token Bearer, voir section Authentification)
- springdoc-openapi (Swagger UI sur `/swagger-ui.html`)
- Lombok
- RestTemplate pour les appels sortants vers BNAC (stub/reel toggle via `bnac.ws.stub-mode`)

Frontend : Angular 17 standalone (`frontend/`), connecte aux APIs des 4 modules via un
proxy de dev (`ng serve` -> `/api` redirige vers `localhost:8081`, voir `proxy.conf.json`).

## Etat d'avancement

Les 4 modules fonctionnels du cahier des charges sont **implementes et testes manuellement**
(via PowerShell `Invoke-RestMethod`) :

| Module | Package | Statut |
|---|---|---|
| 1. Souscription | `tn.bna.bnac.souscription` | Fait + teste |
| 2. Rachat | `tn.bna.bnac.rachat` | Fait + teste |
| 3. Ouverture de compte | `tn.bna.bnac.ouverture` | Fait + teste |
| 4. Consultation | `tn.bna.bnac.consultation` | Fait + teste |

## Regles metier (RG) implementees

**Module 1 - Souscription**
- RG1.1 : le client doit etre un client BNA connu (sinon `ClientNonBnaException`)
- RG1.2 : recherche via WS1 (`detailClient`) - si `possedeCompteTitre=true`, redirige vers les produits existants
- RG1.3 : le compte BNA de debit doit etre d'un type eligible (`TypeCompteNonEligibleException` sinon)
- RG1.4 : modification/suppression uniquement si statut = `EN_COURS_ENREGISTREMENT`
- RG1.5 : montant souscription <= provision disponible (`MontantSuperieurProvisionException` sinon)
- RG1.6 : dateValeurComptable = dateSouscription

**Module 2 - Rachat**
- RG2.1 : modification uniquement si statut = `EN_COURS_ENREGISTREMENT`
- RG2.2 : nombre actions a vendre <= (actionsEnProcession - totalRachatsEnCours - totalEnAttenteApprobationBnac), ces 3 valeurs venant de WS1 (pas de la BDD locale)
- RG2.3 : dateValeurComptable = dateRachat + 1 jour

**Module 3 - Ouverture de compte**
- RG3.1 : le client doit etre un client BNA connu (`ClientNonBnaException` sinon), mais PAS de redirection meme si un compte titre existe deja (contrairement aux modules 1/2)
- RG3.2 : modification/suppression uniquement si statut = `EN_COURS_ENREGISTREMENT`
- RG3.3 : validation envoie les donnees signaletiques completes a WS4 (`ouvrirCompte`)

**Module 4 - Consultation**
- RG4.1 : consultable depuis n'importe quelle agence
- RG4.2 : necessite un compte titre BNAC actif (`CompteTitreInexistantException` sinon, via WS1)
- Vues Souscriptions/Rachats filtrables par produit, etat, plage de dates

## Authentification

Login JWT (`tn.bna.bnac.auth`, `tn.bna.bnac.security`) : `POST /api/auth/login` (username/password)
retourne un token Bearer, a envoyer sur toutes les autres routes `/api/**` (`Authorization: Bearer <token>`).
`GET /api/auth/me` retourne l'utilisateur courant a partir du token (utilise par le frontend pour
restaurer la session apres rechargement). Session STATELESS (pas de cookie), secret/duree de vie
du token configures dans `security.jwt.*` (`application.yml`).

Roles (`Role`) : `CHARGE_DE_DOSSIER`, `VALIDATEUR`, `ADMIN`. Restriction fine par role en place
via `@PreAuthorize` sur les controllers des 3 modules a workflow (souscription/rachat/ouverture) :
creation/modification/suppression/depot de documents reserves a `CHARGE_DE_DOSSIER`/`ADMIN`,
`/valider` et `/rejeter` reserves a `VALIDATEUR`/`ADMIN`.

`UtilisateurSeeder` cree 3 comptes de demonstration au premier demarrage (table `utilisateur`
vide) : `admin` / `Admin123!` (ADMIN), `agent1` / `Agent123!` (CHARGE_DE_DOSSIER),
`validateur1` / `Valid123!` (VALIDATEUR). A retirer/adapter avant mise en production.

Gestion des comptes utilisateurs (`tn.bna.bnac.utilisateur`) : `UtilisateurController`
(`/api/utilisateurs`, reserve au role `ADMIN`) permet de lister, creer et activer/desactiver des
comptes au-dela des 3 comptes seedes. Cote frontend : `features/admin/utilisateurs.component.*`.

Frontend : `frontend/src/app/features/auth/login.component.*` (page de connexion),
`core/services/auth.service.ts` (session, token en `localStorage`), `core/auth.interceptor.ts`
(ajoute le header Authorization, deconnecte sur 401), `core/auth.guard.ts` (protege toutes les
routes sauf `/login`).

## Fonctionnalites transverses

- **Audit** (`tn.bna.bnac.audit`, entite `JournalAudit`) : trace les actions (creation,
  modification, validation, rejet...) sur les operations. Consultable via
  `GET /api/audit/{typeOperation}/{operationId}`.
- **Ecritures comptables** (`tn.bna.bnac.comptabilite`, entite `EcritureComptable`) : a la
  validation d'une souscription ou d'un rachat, le cahier des charges (sections 1.3/2.3) demande
  une "ecriture comptable : debit compte client / credit compte produit". Aucun WS n'etant defini
  vers le core banking BNA pour un mouvement reel (seuls WS1-WS4 vers BNAC existent), cette entite
  se contente d'enregistrer la trace (compte debite, compte/produit credite, montant) sans tenue
  de solde ni grand livre - a l'image du journal d'audit. Consultable via
  `GET /api/ecritures-comptables/{typeOperation}/{operationId}`.
- **Dashboard** (`tn.bna.bnac.dashboard`) : statistiques agregees via `GET /api/dashboard/stats`.
- **Generation PDF** (`tn.bna.bnac.common.pdf.PdfBulletinBuilder`, + `SouscriptionPdfService`,
  `RachatPdfService`, `OuverturePdfService`) : bulletins/ordres PDF generes et stockes via
  `DocumentStorageService` pour les 3 modules a workflow.

## Donnees de test simulees (stubs)

`ClientBnaServiceStub` (referentiel BNA, `tn.bna.bnac.referentiel`) simule 3 clients :
- `12345678` - Ahmed Ben Salah, 2 comptes BNA (TYPE_101, TYPE_103)
- `87654321` - Fatma Trabelsi, 1 compte BNA (TYPE_109)
- `11112222` - Sami Gharbi, 1 compte BNA (TYPE_115), **sans compte titre BNAC** (utile pour tester RG3.3/RG4.2)

`BnacClientStub` (WS BNAC, `tn.bna.bnac.ws`) simule les reponses WS1-WS4 sans appel reseau reel.
Le CIN `11112222` renvoie `possedeCompteTitre=false` cote WS1.

## A faire (TODO)

Fait : securite JWT + restriction par role, gestion des comptes utilisateurs, frontend Angular
(4 modules + admin), tests automatises (49 tests JUnit/integration, `mvn test`), PostgreSQL via
Docker Compose, audit trail, dashboard, generation PDF. Voir sections ci-dessus pour le detail
de chacun.

Reste a faire :

1. **Integration reelle BNAC** - passer `bnac.ws.stub-mode` de `true` a `false` dans
   `application.yml` une fois les vraies URLs/contrats WS1-WS4 fournis par BNA Capitaux. Seul
   point reellement bloquant, en attente d'une dependance externe.
2. **Commit du travail en cours** - une grande partie du code ci-dessus (securite par role,
   audit, dashboard, PDF, gestion utilisateurs, tests, frontend, restructuration
   backend/frontend, docker-compose.yml) est presente sur disque mais pas encore commitee
   (dernier commit : module 4 consultation). A commiter en chunks logiques.

## Lancer le projet

```powershell
# Base de donnees (PostgreSQL via Docker) - depuis la racine du repo
docker compose up -d

# Backend (profil par defaut = PostgreSQL ; profil dev = H2 en memoire)
cd backend
mvn spring-boot:run "-Dspring-boot.run.arguments=--server.port=8081"
# ou, pour H2 sans Docker :
mvn spring-boot:run "-Dspring-boot.run.profiles=dev" "-Dspring-boot.run.arguments=--server.port=8081"

# Frontend (depuis frontend/, backend attendu sur le port 8081)
cd frontend
npm install
npm start   # ng serve, proxy /api -> localhost:8081, ouvre sur http://localhost:4200
```

Swagger UI : http://localhost:8081/swagger-ui.html

## Conventions de code

Chaque module suit la meme structure en couches : `dto/` (requetes/reponses), `*Mapper.java`
(entite <-> DTO), `*Service.java` (logique metier + regles RG), `*Controller.java` (REST, prefixe
`/api/<module>`). Les exceptions metier heritent de `BusinessException`
(`tn.bna.bnac.common.exception`) et sont mappees en HTTP via `GlobalExceptionHandler`.
