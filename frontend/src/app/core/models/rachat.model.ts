import { ProduitFinancier, StatutOperation } from './enums';
import { ClientBnaDto, CompteBnaDto } from './referentiel.model';

export interface ProduitRachatDto {
  numeroCompteTitre: string;
  produit: ProduitFinancier;
  valeurLiquidativeRachat: number;
  actionsEnProcession: number;
  totalRachatsEnCours: number;
  totalRachatsEnAttenteApprobationBnac: number;
}

export interface RechercheClientRachatResponse {
  ficheBna: ClientBnaDto;
  possedeCompteTitre: boolean;
  produits: ProduitRachatDto[];
  comptesCredit: CompteBnaDto[];
}

export interface NouveauRachatRequest {
  cinRneClient: string;
  numeroCompteTitre: string;
  produit: ProduitFinancier | null;
  nombreActionsAVendre: number | null;
  numeroCompteBnaCredit: string;
}

export interface RachatResponse {
  id: number;
  numeroRachat: string;
  cinRneClient: string;
  numeroCompteTitre: string;
  produit: ProduitFinancier;
  valeurLiquidativeRachat: number;
  nombreActionsAVendre: number;
  montantRachat: number;
  numeroCompteBnaCredit: string;
  actionsEnProcessionAvantRachat: number;
  dateRachat: string;
  dateValeurComptable: string;
  statut: StatutOperation;
  referenceWs3: string | null;
}
