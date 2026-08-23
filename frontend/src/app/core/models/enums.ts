export type ProduitFinancier =
  | 'FCP_PROGRES'
  | 'FCP_BNA_CAPITALISATION'
  | 'PLACEMENT_OBLIGATOIRE_SICAV'
  | 'SICAV'
  | 'SICAV_BNA';

export const PRODUITS_FINANCIERS: { value: ProduitFinancier; label: string }[] = [
  { value: 'FCP_PROGRES', label: 'FCP Progrès' },
  { value: 'FCP_BNA_CAPITALISATION', label: 'FCP BNA Capitalisation' },
  { value: 'PLACEMENT_OBLIGATOIRE_SICAV', label: 'Placement obligatoire SICAV' },
  { value: 'SICAV', label: 'SICAV' },
  { value: 'SICAV_BNA', label: 'SICAV BNA' },
];

export function libelleProduit(produit: ProduitFinancier | null | undefined): string {
  return PRODUITS_FINANCIERS.find((p) => p.value === produit)?.label ?? produit ?? '-';
}

export type StatutOperation = 'EN_COURS_ENREGISTREMENT' | 'VALIDE' | 'REJETE';

export const STATUTS_OPERATION: { value: StatutOperation; label: string }[] = [
  { value: 'EN_COURS_ENREGISTREMENT', label: 'En cours d\'enregistrement' },
  { value: 'VALIDE', label: 'Validé' },
  { value: 'REJETE', label: 'Rejeté' },
];

export function libelleStatut(statut: StatutOperation | null | undefined): string {
  return STATUTS_OPERATION.find((s) => s.value === statut)?.label ?? statut ?? '-';
}

export type TypeDocument =
  | 'ORDRE_VIREMENT'
  | 'BULLETIN_SOUSCRIPTION_SIGNE'
  | 'BULLETIN_RACHAT_SIGNE'
  | 'FORMULAIRE_COMPTE_BNAC'
  | 'DEMANDE_OUVERTURE_SIGNEE'
  | 'CIN'
  | 'AUTRE';

export const TYPES_DOCUMENT: { value: TypeDocument; label: string }[] = [
  { value: 'ORDRE_VIREMENT', label: 'Ordre de virement' },
  { value: 'BULLETIN_SOUSCRIPTION_SIGNE', label: 'Bulletin de souscription signé' },
  { value: 'BULLETIN_RACHAT_SIGNE', label: 'Bulletin de rachat signé' },
  { value: 'FORMULAIRE_COMPTE_BNAC', label: 'Formulaire compte BNAC' },
  { value: 'DEMANDE_OUVERTURE_SIGNEE', label: "Demande d'ouverture signée" },
  { value: 'CIN', label: 'CIN' },
  { value: 'AUTRE', label: 'Autre' },
];

export type TypeCompteBna = 'TYPE_101' | 'TYPE_103' | 'TYPE_109' | 'TYPE_115';
