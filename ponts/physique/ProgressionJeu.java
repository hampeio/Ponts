package ponts.physique;

public final class ProgressionJeu {

    private ProgressionJeu() {
    }

    /**
     * Indexation zéro : la troisième关卡 est 2 et la quatrième est 3.
     */
    public static boolean volDisponible(int niveauMaxReussi, int niveauActuel) {
        return niveauMaxReussi >= 2 && niveauActuel >= 3;
    }
}
