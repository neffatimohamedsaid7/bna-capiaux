# Base de connaissance - Assistant BNA Capitaux

Resume condense du cahier des charges et du manuel utilisateur, a l'usage de l'assistant
integre a l'application. Tenu volontairement court pour rester econome en tokens - le detail
complet vit dans docs/manuel-utilisateur.md et docs/ws-bnac.md.

## Roles

- CHARGE_DE_DOSSIER : recherche client, PEC (creation/modification/suppression d'une
  souscription, d'un rachat ou d'une ouverture de compte), import de pieces jointes.
- VALIDATEUR : valider ou rejeter une PEC en cours d'enregistrement, import des documents
  requis pour la validation.
- ADMIN : cumule les deux, + gestion des comptes utilisateurs.

## Module 1 - Souscription d'actions

- RG1.1 : seuls les clients BNA connus sont eligibles.
- RG1.2 : la PEC est reservee aux clients disposant deja d'un compte titre BNAC ; sinon,
  redirection vers le module Ouverture de compte.
- RG1.3 : seuls les comptes de type 101, 103, 109 et 115 sont eligibles au debit.
- RG1.4 : modification/suppression uniquement si le statut est "En cours d'enregistrement".
- RG1.5 : le montant de la souscription ne peut pas depasser la provision disponible du compte
  BNA choisi.
- RG1.6 : la date valeur comptable est la date de l'operation.
- Validation : necessite l'import de l'ordre de virement ET du bulletin de souscription signe.
  A la validation, appel WS2 vers BNAC, generation de l'avis d'operation.

## Module 2 - Demande de rachat

- RG2.1 : modification/suppression uniquement si le statut est "En cours d'enregistrement".
- RG2.2 : nombre d'actions a vendre <= (actions en procession - total des rachats deja en
  cours - total en attente d'approbation cote BNAC).
- RG2.3 : la date valeur comptable est la date de l'operation + 1 jour.
- Validation : necessite l'import du bulletin de rachat signe. A la validation, appel WS3 vers
  BNAC, generation de la decharge.

## Module 3 - Ouverture de compte titre

- RG3.1 : seuls les clients BNA connus sont eligibles ; contrairement aux modules 1 et 2, PAS de
  redirection meme si un compte titre existe deja.
- RG3.2 : modification/suppression uniquement si le statut est "En cours d'enregistrement".
- RG3.3 : pour un client sans compte BNAC, les donnees signaletiques BNA sont transmises via
  WS4 lors de la validation.
- Validation : necessite l'import du formulaire compte BNAC ET de la CIN.

## Module 4 - Consultation

- RG4.1 : consultable depuis n'importe quelle agence.
- RG4.2 : necessite un compte titre BNAC actif, sinon message bloquant.
- Vues Portefeuille / Souscriptions / Rachats, filtrables par produit, etat et plage de dates.

## Statuts d'une operation

En cours d'enregistrement -> Valide OU Rejete (transitions definitives une fois validee ou
rejetee ; on ne peut plus modifier/supprimer une operation validee ou rejetee).

## Erreurs frequentes et leur cause

- "La personne recherchee n'est pas un client BNA" : le CIN/RNE/compte/titre recherche ne
  correspond a aucun client du referentiel BNA (RG1.1/RG3.1).
- "Le client ne dispose pas d'un compte chez BNA Capitaux" : pas de compte titre BNAC ; pour
  les modules 1 et 2 cela redirige vers le module 3, pour la consultation c'est bloquant
  (RG4.2), pour le module 3 lui-meme ce n'est pas bloquant (c'est justement le cas d'usage).
- Rejet a la creation d'une souscription pour montant trop eleve : RG1.5, le montant depasse la
  provision disponible du compte BNA selectionne.
- Rejet a la creation d'un rachat pour nombre d'actions trop eleve : RG2.2, la quantite demandee
  depasse ce qui est reellement disponible une fois deduits les rachats deja en cours.
- "Documents manquants" au moment de valider : chaque module a sa propre liste de pieces
  obligatoires avant validation (voir sections Module 1/2/3 ci-dessus) - l'agent doit importer
  ces documents avant de pouvoir declencher la validation.
- 403 Acces refuse : le role du compte connecte ne permet pas cette action (voir section
  Roles ci-dessus).
