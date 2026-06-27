package ponts.physique.barres;

/**
 * Enum des différents matériaux du jeu
 */
public enum Materiau {
    GOUDRON("路面板"),
    BOIS("木材"),
    ACIER("钢材"),
    ALUMINIUM("轻型铝"),
    CARBONE("碳纤维");

    private final String nom;

    Materiau(String nom) {
        this.nom = nom;
    }

    @Override
    public String toString() {
        return nom;
    }
}
