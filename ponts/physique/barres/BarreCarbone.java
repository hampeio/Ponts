package ponts.physique.barres;

import java.awt.Color;

import org.jbox2d.collision.shapes.Shape;
import org.jbox2d.dynamics.FixtureDef;
import org.jbox2d.dynamics.World;

import ponts.physique.liaisons.Liaison;

/**
 * 碳纤维支撑：轻且强，但价格昂贵。
 */
public class BarreCarbone extends Barre {

    public static final int CATEGORY = 0b100000000;
    public static final int MASK = Barre.MASK;

    public BarreCarbone(World world, Liaison liaison1, Liaison liaison2) {
        super(world, liaison1, liaison2);
        couleurRemplissage = Color.decode("#384452");
        prixMateriau = Math.round(prixMateriau * 3.2f);
        forceMax *= 2.2f;
    }

    @Override
    protected FixtureDef creerFixtureDef(Shape shape) {
        return creerFixtureDef(FRICTION, ELASTICITE, DENSITE * 0.7f, CATEGORY, MASK);
    }
}
