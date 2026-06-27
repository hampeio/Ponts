package ponts;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Résout les ressources depuis le dossier de l'EXE jpackage ou, en développement,
 * depuis le dossier de travail du projet.
 */
public final class Chemins {

    private Chemins() {
    }

    public static Path ressource(String... parties) {
        String appPath = System.getProperty("jpackage.app-path");
        Path base = appPath == null || appPath.isBlank()
                ? Paths.get("").toAbsolutePath()
                : Paths.get(appPath).toAbsolutePath().getParent();
        Path chemin = base.resolve("res");
        for (String partie : parties) {
            chemin = chemin.resolve(partie);
        }
        return chemin;
    }
}
