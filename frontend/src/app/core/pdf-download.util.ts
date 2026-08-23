/** Ouvre un blob PDF (recu via HttpClient, donc avec le header Authorization) dans un nouvel onglet. */
export function ouvrirPdf(blob: Blob): void {
  const url = window.URL.createObjectURL(blob);
  window.open(url, '_blank');
  setTimeout(() => window.URL.revokeObjectURL(url), 60_000);
}
