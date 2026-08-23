# Rapport de tests et recette

État de la couverture de test à date, pour servir de base à la recette (section 9 du cahier des
charges). Couvre le backend (suite automatisée) ; voir §4 pour ce qui reste manuel ou non
couvert.

## 1. Résumé

- **52 tests** JUnit/Spring Boot (`backend/src/test/java`), **0 échec**.
- Lancement : `cd backend && mvn test` (profil de test dédié, base H2 en mémoire — voir
  `backend/src/test/resources/application.yml`).
- Tests d'intégration bout-en-bout via `MockMvc` : authentification JWT réelle, base de données
  réelle (H2), contrôleurs/services réels ; seul `BnacClient` (WS1-WS4) est simulé
  (`BnacClientStub`, voir [docs/ws-bnac.md](ws-bnac.md)) — aucun test n'appelle un vrai service
  BNAC.

## 2. Couverture par module

### Module 1 — Souscription (`SouscriptionModuleTest`, 14 tests)

| Test | Règle / comportement vérifié |
|---|---|
| `rechercheClient_nonBna_estBloquee` | RG1.1 — client non-BNA rejeté |
| `rechercheClient_bnaSansCompteTitre_neRetourneAucunProduit` | RG1.2 — pas de compte titre → pas de produits (redirection module 3 côté frontend) |
| `rechercheClient_bnaAvecCompteTitre_retourneLaFicheEtLesProduits` | RG1.2 — fiche + produits via WS1 |
| `creerSouscription_avecCompteNAppartenantPasAuClient_estRejetee` | RG1.3 — compte de débit invalide |
| `creerSouscription_montantSuperieurALaProvision_estRejetee` | RG1.5 — montant > provision |
| `creerSouscription_valide_appliqueRG16EtStatutInitial` | RG1.6 — date valeur comptable = date opération ; statut initial |
| `cycleDeVie_modifierEtSupprimerUniquementEnCoursDEnregistrement` | RG1.4 |
| `supprimer_uneSouscriptionEnCours_reussit` | RG1.4 |
| `valider_sansPiecesJointes_estBloquee` | Étape 4 — documents obligatoires (ordre de virement + bulletin signé) |
| `valider_avecPiecesJointes_appelleWS2EtPasseAuStatutValide` | Étape 4 — appel WS2, statut `VALIDE` |
| `valider_genereUneEcritureComptableDebitClientCreditProduit` | Écriture comptable à la validation |
| `role_chargeDeDossierNePeutPasValider` | Restriction de rôle (validation réservée `VALIDATEUR`/`ADMIN`) |
| `role_validateurNePeutPasCreerDePEC` | Restriction de rôle (PEC réservée `CHARGE_DE_DOSSIER`/`ADMIN`) |
| `role_adminPeutCreerEtValider` | `ADMIN` cumule les deux |

### Module 2 — Rachat (`RachatModuleTest`, 10 tests)

| Test | Règle vérifiée |
|---|---|
| `creerRachat_nombreActionsSuperieurAuDisponible_estRejete` | RG2.2 |
| `creerRachat_compteCreditNAppartenantPasAuClient_estRejete` | Validation du compte à créditer |
| `creerRachat_valide_appliqueRG23DateValeurPlusUnJour` | RG2.3 |
| `cycleDeVie_modifierUniquementEnCoursDEnregistrement` | RG2.1 |
| `supprimer_unRachatEnCours_reussit` | RG2.1 |
| `valider_sansBulletinSigne_estBloquee` | Étape 4 — bulletin signé obligatoire |
| `valider_avecBulletinSigne_appelleWS3EtPasseAuStatutValide` | Étape 4 — appel WS3, statut `VALIDE` |
| `valider_genereUneEcritureComptableDebitProduitCreditClient` | Écriture comptable à la validation |
| `role_chargeDeDossierNePeutPasValider` | Restriction de rôle |
| `role_validateurNePeutPasCreerDePEC` | Restriction de rôle |

### Module 3 — Ouverture de compte (`OuvertureCompteModuleTest`, 7 tests)

| Test | Règle vérifiée |
|---|---|
| `rechercheClient_nonBna_estBloquee` | RG3.1 |
| `rechercheClient_bnaSansCompteTitre_activeLaProcedureDOuverture` | RG3.1 — pas de redirection (contrairement aux modules 1/2) |
| `creerEtValiderDemande_pourClientSansCompteBnac_transmetLesDonneesSignaletiquesViaWS4` | RG3.3 — transmission WS4 |
| `valider_sansPiecesJointes_estBloquee` | Étape 4 — formulaire compte BNAC + CIN obligatoires |
| `cycleDeVie_modifierUniquementEnCoursDEnregistrement` | RG3.2 |
| `role_validateurNePeutPasCreerDePEC` | Restriction de rôle |
| `role_chargeDeDossierNePeutPasValider` | Restriction de rôle |

### Module 4 — Consultation (`ConsultationModuleTest`, 4 tests)

| Test | Règle vérifiée |
|---|---|
| `portefeuille_clientSansCompteTitre_estBloque` | RG4.2 |
| `portefeuille_clientAvecCompteTitre_retourneLesProduits` | RG4.1/RG4.2 |
| `historiqueSouscriptions_filtreParEtat_neRenvoiQueLesSouscriptionsCorrespondantes` | Filtres (état) |
| `historiqueSouscriptions_filtreParProduitAbsent_neRenvoieRien` | Filtres (produit) |

### Transverses

| Suite | Tests | Couvre |
|---|---|---|
| `AuthFlowTest` | 7 | Login (identifiants valides/invalides), `/api/auth/me`, accès protégé avec/sans token valide |
| `AuditTrailTest` | 3 | Traçabilité section 6.4 : ordre des actions, opérateur enregistré (y compris sur rejet et import de document) |
| `UtilisateurAdminTest` | 5 | Gestion des comptes réservée `ADMIN`, création avec anti-doublon, activation/désactivation |
| `BnacClientCacheTest` | 1 | Cache WS1 (section 6.2) — peuplement du cache par critère de recherche |
| `BnacBackendApplicationTests` | 1 | Démarrage du contexte Spring complet |

## 3. Résultats

```
$ mvn test
...
Tests run: 52, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

À rejouer avant toute mise en recette formelle pour confirmer la date/l'état à jour (les chiffres
ci-dessus sont ceux constatés lors de la rédaction de ce rapport).

## 4. Hors périmètre des tests automatisés

- **Frontend Angular** : aucune couverture automatisée réelle — seul le stub généré par défaut
  (`app.component.spec.ts`) existe. Les 4 modules + l'écran d'administration ont été vérifiés
  manuellement dans le navigateur pendant le développement, sans suite de tests formalisée
  (Karma/Jasmine ou e2e) à ce jour.
- **Intégration BNAC réelle** : par construction, `BnacClientStub` est utilisé dans tous les
  tests — aucun test ne couvre `BnacRestClient` contre un vrai service BNAC (indisponible à ce
  jour, voir [docs/ws-bnac.md](ws-bnac.md)). À couvrir dès qu'un environnement de test BNAC sera
  fourni.
- **Non-fonctionnel** : pas de test de charge/performance (section 6.2 : cible ≤ 2s par appel,
  utilisateurs simultanés), pas de test de sécurité applicative dédié au-delà des tests
  d'authentification/autorisation listés ci-dessus.
- **Recette manuelle antérieure** : les modules 1 à 4 ont initialement été validés manuellement
  via des scripts PowerShell (`Invoke-RestMethod`) pendant leur développement, avant l'écriture
  de la suite automatisée actuelle. Ces scripts n'ont pas été conservés dans le dépôt ; la suite
  `backend/src/test` ci-dessus est désormais la référence pour la non-régression.
