export type TypeOperationAudit = 'SOUSCRIPTION' | 'RACHAT' | 'OUVERTURE_COMPTE';

export type TypeAction = 'CREATION' | 'MODIFICATION' | 'SUPPRESSION' | 'IMPORT_DOCUMENT' | 'VALIDATION' | 'REJET' | 'APPEL_WS';

export const TYPE_ACTION_LABELS: Record<TypeAction, string> = {
  CREATION: 'PEC (création)',
  MODIFICATION: 'Modification',
  SUPPRESSION: 'Suppression',
  IMPORT_DOCUMENT: 'Import document',
  VALIDATION: 'Validation',
  REJET: 'Rejet',
  APPEL_WS: 'Appel web service BNAC',
};

export interface AuditEntry {
  dateAction: string;
  operateur: string;
  typeAction: TypeAction;
  details: string | null;
}
