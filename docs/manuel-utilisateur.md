# Manuel utilisateur — PEC et Validation

Destiné aux utilisateurs métier de l'application : **chargé de dossier** (agent en agence,
saisie/PEC) et **validateur** (responsable/chef d'agence, contrôle et validation). Pour
l'installation et l'administration des comptes, voir
[docs/guide-administration.md](guide-administration.md).

## 1. Connexion

Ouvrir l'application (`http://localhost:4200` en environnement de démonstration) et se connecter
avec le compte fourni par l'administrateur. Le rôle du compte (`CHARGE_DE_DOSSIER`,
`VALIDATEUR` ou `ADMIN`) détermine les actions disponibles :

- Un **chargé de dossier** peut rechercher un client, créer/modifier/supprimer une PEC et
  importer des pièces jointes, dans les modules 1, 2 et 3.
- Un **validateur** peut valider ou rejeter une PEC en cours d'enregistrement, et importer les
  documents requis pour la validation.
- Les deux rôles ont accès à la **consultation** (module 4), sans restriction.

Si une action affiche une erreur "accès refusé", c'est que le rôle du compte connecté ne le
permet pas (voir le tableau des rôles dans le guide d'administration).

## 2. Module 1 — Souscription d'actions

Menu **Souscription** (`/souscription`).

### 2.1 Rechercher un client

Saisir un critère de recherche (CIN, RNE, n° de compte ou n° de titre). Trois cas possibles :

| Résultat | Comportement |
|---|---|
| Le client n'est pas un client BNA | Message bloquant : *"La personne recherchée n'est pas un client BNA"* |
| Client BNA **sans** compte titre BNAC | Message invitant à créer un compte titre → redirection vers le module **Ouverture de compte** |
| Client BNA **avec** compte titre | Affichage de la fiche client + liste des produits disponibles, avec leur valeur liquidative de souscription |

### 2.2 Créer une nouvelle souscription

Bouton **Nouveau** (`/souscription/nouveau`). Sélectionner un produit dans la liste, puis
renseigner :

| Champ | Règle |
|---|---|
| Nombre d'actions à souscrire | Entier positif obligatoire |
| Montant de souscription | Calculé automatiquement (valeur liquidative × nombre d'actions) |
| Compte BNA à débiter | Doit être un compte du client d'un type éligible (101, 103, 109 ou 115) et dont la provision couvre le montant — sinon message bloquant |

**Enregistrer** crée la PEC avec un numéro de souscription généré automatiquement et le statut
*En cours d'enregistrement*, puis permet d'imprimer le bulletin de souscription (PDF).

### 2.3 Suivre et modifier les souscriptions en cours

La liste des souscriptions *En cours d'enregistrement* du client s'affiche automatiquement lors
de la recherche. Depuis une ligne (`/souscription/:id`) :

- **Modifier** / **Supprimer** : possible uniquement tant que le statut est *En cours
  d'enregistrement*.
- **Importer des documents** : requis avant validation (voir §2.4).
- **Imprimer** : bulletin de souscription à tout moment ; avis d'opération une fois validée.

### 2.4 Valider ou rejeter (rôle Validateur)

Depuis la fiche détail d'une souscription (`/souscription/:id`) :

- **Rejeter** : passe le statut à *Rejeté*, sans condition.
- **Valider** : nécessite d'avoir importé au préalable l'**ordre de virement** et le **bulletin
  de souscription signé** (client + chef d'agence) — sinon message bloquant listant les documents
  manquants. Une fois validée :
  - Le statut passe à *Validé*.
  - L'avis d'opération est généré automatiquement (imprimable depuis la fiche).
  - La demande est transmise à BNA Capitaux (WS2) ; la date valeur comptable est la date de
    l'opération.

## 3. Module 2 — Demande de rachat

Menu **Rachat** (`/rachat`). Fonctionnement identique au module 1 (recherche, création,
suivi/modification, validation), avec les différences suivantes :

- La recherche affiche la valeur liquidative de **rachat** de chaque produit, ainsi que le
  nombre d'actions déjà détenues et les rachats déjà en cours.
- Le nombre d'actions à vendre ne peut pas dépasser : *actions détenues − rachats déjà en cours
  côté BNA − rachats en attente d'approbation côté BNAC*. Le formulaire bloque toute saisie
  supérieure.
- Le compte BNA sélectionné est un compte à **créditer** (aucune restriction de type de compte,
  contrairement à la souscription) — le crédit effectif est réalisé par BNAC.
- La validation nécessite le **bulletin de rachat signé** (client + chef d'agence). Une fois
  validée : la **décharge** est générée automatiquement (imprimable), la demande est transmise à
  BNA Capitaux (WS3), et la date valeur comptable est la date de l'opération **+ 1 jour**.

## 4. Module 3 — Ouverture de compte titre

Menu **Ouverture de compte** (`/ouverture`).

### 4.1 Rechercher un client

| Résultat | Comportement |
|---|---|
| Le client n'est pas un client BNA | Message bloquant, comme pour les modules 1 et 2 |
| Client BNA avec un compte titre BNAC déjà existant | Affichage de sa fiche BNAC + produits (aucune redirection : ce module reste accessible même si un compte existe déjà, contrairement aux modules 1 et 2) |
| Client BNA sans compte titre | Affichage de sa fiche signalétique BNA uniquement, procédure d'ouverture activée |

### 4.2 Créer une demande d'ouverture

Sélectionner le type de compte souhaité (FCP Progrès, FCP BNA Capitalisation, Placement
obligatoire SICAV, SICAV, SICAV BNA), joindre la **demande signée du client**, puis
**Enregistrer** : génère un numéro de demande, statut *En cours d'enregistrement*, et le bulletin
d'ouverture (à faire signer par le client).

### 4.3 Modifier, supprimer

Comme pour les autres modules, possible uniquement tant que le statut est *En cours
d'enregistrement*.

### 4.4 Valider ou rejeter (rôle Validateur)

La validation nécessite l'import du **formulaire compte BNAC** et de la **CIN**. Une fois
validée : les données signalétiques sont transmises à BNA Capitaux (WS4), qui renvoie le nouveau
numéro de compte titre.

## 5. Module 4 — Consultation et édition

Menu **Consultation** (`/consultation`), accessible à tout utilisateur connecté, depuis n'importe
quelle agence.

1. Rechercher un client par CIN, RNE ou n° de compte.
2. Si le client n'a pas de compte titre BNAC actif : message *"Le client ne dispose pas un
   compte chez BNA Capitaux"*.
3. Sinon : la fiche BNAC et la liste des produits s'affichent, avec trois onglets :
   - **Portefeuille** : produits détenus.
   - **Souscriptions** : historique des souscriptions du client, filtrable par produit, état et
     plage de dates. Chaque ligne permet de retélécharger le bulletin de souscription.
   - **Rachats** : historique des rachats, mêmes filtres, avec accès au bulletin de rachat.

## 6. Gestion des comptes (rôle Admin uniquement)

Menu **Administration → Utilisateurs** (`/admin/utilisateurs`), visible uniquement pour un
compte `ADMIN`. Voir le détail dans le
[guide d'administration](guide-administration.md#3-comptes-utilisateurs).
