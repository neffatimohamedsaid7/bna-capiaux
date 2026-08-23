import { ProduitFinancier, StatutOperation } from './enums';
import { ClientBnaDto, CompteBnaDto } from './referentiel.model';

export interface ProduitSouscriptionDto {
  numeroCompteTitre: string;
  produit: ProduitFinancier;
  valeurLiquidativeSouscription: number;
  actionsEnProcession: number;
}

export interface RechercheClientSouscriptionResponse {
  ficheBna: ClientBnaDto;
  possedeCompteTitre: boolean;
  produits: ProduitSouscriptionDto[];
  comptesEligiblesDebit: CompteBnaDto[];
}

export interface NouvelleSouscriptionRequest {
  cinRneClient: string;
  numeroCompteTitre: string;
  produit: ProduitFinancier | null;
  nombreActionsASouscrire: number | null;
  numeroCompteBnaDebit: string;
}

export interface SouscriptionResponse {
  id: number;
  numeroSouscription: string;
  cinRneClient: string;
  numeroCompteTitre: string;
  produit: ProduitFinancier;
  valeurLiquidative: number;
  nombreActionsASouscrire: number;
  montantSouscription: number;
  numeroCompteBnaDebit: string;
  actionsEnProcessionAvant: number;
  dateSouscription: string;
  dateValeurComptable: string;
  statut: StatutOperation;
  referenceWs2: string | null;
  nouveauNombreActions: number | null;
}
