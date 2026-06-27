package ponts.physique.barres;

import java.awt.Color;

import org.jbox2d.collision.shapes.Shape;
import org.jbox2d.dynamics.FixtureDef;
import org.jbox2d.dynamics.World;

import ponts.physique.liaisons.Liaison;

/**
 * 轻型铝合金支撑：重量低、强度适中，适合长跨结构减重。
 */
public class BarreAluminium extends Barre {

    public static final int CATEGORY = 0b10000000;
    public static final int MASK = Barre.MASK;

    public BarreAluminium(World world, Liaison liaison1, Liaison liaison2) {
        super(world, liaison1, liaison2);
        couleurRemplissage = Color.decode("#bdd5df");
        prixMateriau = Math.round(prixMateriau * 1.6f);
        forceMax *= 0.9f;
    }

    @Override
    protected FixtureDef creerFixtureDef(Shape shape) {
        return creerFixtureDef(FRICTION, ELASTICITE, DENSITE * 0.55f, CATEGORY, MASK);
    }
}
