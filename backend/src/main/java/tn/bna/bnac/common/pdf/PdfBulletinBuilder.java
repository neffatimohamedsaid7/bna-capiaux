package tn.bna.bnac.common.pdf;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.util.List;

/**
 * Generateur commun des documents PDF du cahier des charges (bulletins de souscription/rachat/ouverture,
 * avis d'operation, decharge, listes de produits - section 7.2). Mise en page volontairement simple :
 * en-tete BNA Capitaux, bloc(s) libelle/valeur et/ou tableau multi-colonnes, bloc signatures (optionnel).
 */
public class PdfBulletinBuilder {

    private static final Font TITRE_FONT = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18);
    private static final Font SOUS_TITRE_FONT = FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, 11, Color.DARK_GRAY);
    private static final Font TITRE_DOCUMENT_FONT = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 13);
    private static final Font NUMERO_FONT = FontFactory.getFont(FontFactory.HELVETICA, 10, Color.DARK_GRAY);
    private static final Font SECTION_FONT = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11, Color.DARK_GRAY);
    private static final Font LABEL_FONT = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10);
    private static final Font VALEUR_FONT = FontFactory.getFont(FontFactory.HELVETICA, 10);
    private static final Font SIGNATURE_FONT = FontFactory.getFont(FontFactory.HELVETICA, 10, Color.DARK_GRAY);
    private static final Font ENTETE_TABLEAU_FONT = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, Color.WHITE);
    private static final Color ENTETE_TABLEAU_FOND = new Color(60, 90, 130);

    private final Document document = new Document(PageSize.A4, 50, 50, 60, 50);
    private final ByteArrayOutputStream out = new ByteArrayOutputStream();
    private PdfPTable table = nouvelleTableLabelValeur();
    private boolean avecSignatures = true;

    public PdfBulletinBuilder(String titreDocument, String numeroDocument) {
        try {
            PdfWriter.getInstance(document, out);
            document.open();

            Paragraph entete = new Paragraph("BNA CAPITAUX", TITRE_FONT);
            entete.setAlignment(Element.ALIGN_CENTER);
            document.add(entete);

            Paragraph sousEntete = new Paragraph("Souscription et Rachat des Actions", SOUS_TITRE_FONT);
            sousEntete.setAlignment(Element.ALIGN_CENTER);
            sousEntete.setSpacingAfter(24);
            document.add(sousEntete);

            Paragraph titre = new Paragraph(titreDocument, TITRE_DOCUMENT_FONT);
            titre.setAlignment(Element.ALIGN_CENTER);
            document.add(titre);

            Paragraph numero = new Paragraph("N° " + numeroDocument, NUMERO_FONT);
            numero.setAlignment(Element.ALIGN_CENTER);
            numero.setSpacingAfter(20);
            document.add(numero);
        } catch (DocumentException e) {
            throw new IllegalStateException("Erreur lors de l'initialisation du document PDF", e);
        }
    }

    private static PdfPTable nouvelleTableLabelValeur() {
        PdfPTable t = new PdfPTable(2);
        t.setWidthPercentage(100);
        try {
            t.setWidths(new float[]{35, 65});
        } catch (DocumentException e) {
            throw new IllegalStateException(e);
        }
        t.setSpacingBefore(6);
        return t;
    }

    /** Insere le tableau libelle/valeur accumule (s'il contient des lignes) et en recommence un nouveau. */
    private void flushTableLabelValeur() {
        if (!table.getRows().isEmpty()) {
            try {
                document.add(table);
            } catch (DocumentException e) {
                throw new IllegalStateException("Erreur lors de la generation du document PDF", e);
            }
        }
        table = nouvelleTableLabelValeur();
    }

    /** Documents purement informatifs (listes, annexes) : pas de bloc signatures a la fin. */
    public PdfBulletinBuilder sansSignatures() {
        this.avecSignatures = false;
        return this;
    }

    /** Ajoute un tableau multi-colonnes (ex : liste des produits detenus par un client). */
    public PdfBulletinBuilder tableau(String[] entetes, List<String[]> lignes) {
        flushTableLabelValeur();

        PdfPTable tableauProduits = new PdfPTable(entetes.length);
        tableauProduits.setWidthPercentage(100);
        tableauProduits.setSpacingBefore(10);

        for (String entete : entetes) {
            PdfPCell cell = new PdfPCell(new Phrase(entete, ENTETE_TABLEAU_FONT));
            cell.setBackgroundColor(ENTETE_TABLEAU_FOND);
            cell.setPadding(6);
            tableauProduits.addCell(cell);
        }
        for (String[] ligne : lignes) {
            for (String valeur : ligne) {
                PdfPCell cell = new PdfPCell(new Phrase(valeur == null ? "-" : valeur, VALEUR_FONT));
                cell.setPadding(6);
                tableauProduits.addCell(cell);
            }
        }

        try {
            document.add(tableauProduits);
        } catch (DocumentException e) {
            throw new IllegalStateException("Erreur lors de la generation du document PDF", e);
        }
        return this;
    }

    public PdfBulletinBuilder section(String titreSection) {
        PdfPCell cell = new PdfPCell(new Phrase(titreSection, SECTION_FONT));
        cell.setColspan(2);
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setPaddingTop(14);
        cell.setPaddingBottom(4);
        table.addCell(cell);
        return this;
    }

    public PdfBulletinBuilder ligne(String label, Object valeur) {
        PdfPCell labelCell = new PdfPCell(new Phrase(label, LABEL_FONT));
        labelCell.setBorder(Rectangle.BOTTOM);
        labelCell.setBorderColor(Color.LIGHT_GRAY);
        labelCell.setPaddingTop(6);
        labelCell.setPaddingBottom(6);

        PdfPCell valeurCell = new PdfPCell(new Phrase(valeur == null ? "-" : valeur.toString(), VALEUR_FONT));
        valeurCell.setBorder(Rectangle.BOTTOM);
        valeurCell.setBorderColor(Color.LIGHT_GRAY);
        valeurCell.setPaddingTop(6);
        valeurCell.setPaddingBottom(6);

        table.addCell(labelCell);
        table.addCell(valeurCell);
        return this;
    }

    public byte[] build() {
        flushTableLabelValeur();
        try {
            if (avecSignatures) {
                ajouterSignatures();
            }
            document.close();
            return out.toByteArray();
        } catch (DocumentException e) {
            throw new IllegalStateException("Erreur lors de la generation du document PDF", e);
        }
    }

    private void ajouterSignatures() throws DocumentException {
        Paragraph espace = new Paragraph(" ");
        espace.setSpacingBefore(50);
        document.add(espace);

        PdfPTable signatures = new PdfPTable(2);
        signatures.setWidthPercentage(100);
        signatures.addCell(celluleSignature("Signature du client"));
        signatures.addCell(celluleSignature("Signature du chef d'agence"));
        document.add(signatures);
    }

    private PdfPCell celluleSignature(String libelle) {
        PdfPCell cell = new PdfPCell(new Phrase(libelle, SIGNATURE_FONT));
        cell.setBorder(Rectangle.TOP);
        cell.setBorderColor(Color.DARK_GRAY);
        cell.setPaddingTop(40);
        cell.setFixedHeight(20);
        return cell;
    }
}
