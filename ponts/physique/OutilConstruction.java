package ponts.physique;

public enum OutilConstruction {
    BARRE("单根构件"),
    ANCRAGE("新增锚点"),
    PILIER("新增桥墩"),
    LIGNE("直线填充"),
    COURBE("曲线填充"),
    DEPLACER("移动对象");

    private final String nom;

    OutilConstruction(String nom) {
        this.nom = nom;
    }

    @Override
    public String toString() {
        return nom;
    }
}
