export type Role = 'CHARGE_DE_DOSSIER' | 'VALIDATEUR' | 'ADMIN';

export const ROLE_LABELS: Record<Role, string> = {
  CHARGE_DE_DOSSIER: 'Chargé de dossier',
  VALIDATEUR: 'Validateur',
  ADMIN: 'Administrateur',
};

export interface LoginRequest {
  username: string;
  password: string;
}

export interface LoginResponse {
  token: string;
  tokenType: string;
  expiresInMs: number;
  username: string;
  nom: string;
  prenom: string;
  role: Role;
}

export interface UtilisateurCourant {
  username: string;
  nom: string;
  prenom: string;
  role: Role;
}
