import { ProduitFinancier, StatutOperation } from './enums';
import { ClientBnaDto, ProduitBnacDto } from './referentiel.model';

export interface ConsultationClientResponse {
  ficheBna: ClientBnaDto | null;
  produits: ProduitBnacDto[];
}

export interface SouscriptionConsultationDto {
  numeroCompteTitre: string;
  produit: ProduitFinancier;
  idSouscription: string;
  dateSouscription: string;
  actionsASouscrire: number;
  valeurLiquidative: number;
  montantSouscription: number;
  actionsEnProcessionAvant: number;
  etatBna: StatutOperation;
  etatBnac: string;
  actionsApresApprobation: number | null;
  numeroCompte: string;
  idPourEdition: number;
}

export interface RachatConsultationDto {
  numeroCompteTitre: string;
  produit: ProduitFinancier;
  idRachat: string;
  dateRachat: string;
  actionsAVendre: number;
  valeurLiquidative: number;
  montantRachat: number;
  actionsEnProcessionAvant: number;
  etatBna: StatutOperation;
  etatBnac: string;
  numeroCompte: string;
  idPourEdition: number;
}

export interface ConsultationFiltres {
  produit?: ProduitFinancier | '';
  etat?: StatutOperation | '';
  dateDebut?: string;
  dateFin?: string;
}
