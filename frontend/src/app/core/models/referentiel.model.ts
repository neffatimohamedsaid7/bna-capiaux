import { ProduitFinancier, TypeCompteBna } from './enums';

export interface CompteBnaDto {
  numeroCompte: string;
  typeCompte: TypeCompteBna;
  provisionDisponible: number;
}

export interface ClientBnaDto {
  cinRne: string;
  typePiece: string;
  numeroPiece: string;
  nomPrenom: string;
  relation: string;
  activite: string;
  adresse: string;
  comptes: CompteBnaDto[];
}

export interface ProduitBnacDto {
  numeroCompteTitre: string;
  produit: ProduitFinancier;
  valeurLiquidativeSouscription: number | null;
  valeurLiquidativeRachat: number | null;
  nombreActionsEnProcession: number | null;
  totalRachatsEnCours: number | null;
  totalRachatsEnAttenteApprobationBnac: number | null;
}
