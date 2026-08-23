import { Role } from './auth.model';

export interface UtilisateurAdmin {
  id: number;
  username: string;
  nom: string;
  prenom: string;
  email: string | null;
  role: Role;
  actif: boolean;
}

export interface UtilisateurCreateRequest {
  username: string;
  password: string;
  nom: string;
  prenom: string;
  email?: string;
  role: Role;
}
