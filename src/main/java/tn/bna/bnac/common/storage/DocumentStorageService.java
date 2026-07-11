package tn.bna.bnac.common.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import tn.bna.bnac.domain.TypeOperation;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

/**
 * Stockage local des pieces jointes (ordre de virement, bulletins signes, CIN, ...).
 * Simple systeme de fichiers pour ce squelette ; a remplacer par une GED si besoin
 * (section 7.3 - Infrastructure du cahier des charges mentionne les deux options).
 */
@Service
@Slf4j
public class DocumentStorageService {

    private final Path racineStockage;

    public DocumentStorageService(@Value("${bnac.documents.storage-path:./documents}") String storagePath) {
        this.racineStockage = Path.of(storagePath).toAbsolutePath().normalize();
        try {
            Files.createDirectories(racineStockage);
        } catch (IOException e) {
            throw new UncheckedIOException("Impossible de creer le dossier de stockage des documents", e);
        }
    }

    /** Enregistre le fichier sous {@code <storage-path>/<typeOperation>/<operationId>/<uuid>-<nomOriginal>}. */
    public String enregistrer(TypeOperation typeOperation, Long operationId, MultipartFile fichier) {
        try {
            Path dossier = racineStockage.resolve(typeOperation.name().toLowerCase())
                    .resolve(String.valueOf(operationId));
            Files.createDirectories(dossier);

            String nomOriginal = fichier.getOriginalFilename() == null ? "document" : fichier.getOriginalFilename();
            String nomFichier = UUID.randomUUID() + "-" + nomOriginal.replaceAll("[^a-zA-Z0-9._-]", "_");
            Path cible = dossier.resolve(nomFichier);

            fichier.transferTo(cible);
            log.info("Document enregistre : {}", cible);
            return cible.toString();
        } catch (IOException e) {
            throw new UncheckedIOException("Echec de l'enregistrement du document", e);
        }
    }
}
