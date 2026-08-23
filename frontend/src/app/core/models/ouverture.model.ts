import { ProduitFinancier, StatutOperation } from './enums';
import { ClientBnaDto } from './referentiel.model';
import { ProduitBnacDto } from './referentiel.model';

export interface RechercheClientOuvertureResponse {
  ficheBna: ClientBnaDto;
  possedeCompteTitre: boolean;
  produitsExistants: ProduitBnacDto[];
}

export interface NouvelleOuvertureRequest {
  cinRneClient: string;
  typeCompteSouhaite: ProduitFinancier | null;
}

export interface OuvertureCompteResponse {
  id: number;
  numeroDemande: string;
  cinRneClient: string;
  nomPrenomClient: string;
  adresseClient: string;
  activiteClient: string;
  typeCompteSouhaite: ProduitFinancier;
  dateDemande: string;
  statut: StatutOperation;
  referenceWs4: string | null;
  numeroCompteTitreGenere: string | null;
}
