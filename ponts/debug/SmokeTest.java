package ponts.debug;

import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.World;

import ponts.niveau.Niveau;
import ponts.ihm.Box2D;
import ponts.physique.Pont;
import ponts.physique.OutilConstruction;
import ponts.physique.CoutsConstruction;
import ponts.physique.barres.Materiau;
import ponts.physique.barres.BarreBois;
import ponts.physique.environnement.Bord;
import ponts.physique.liaisons.LiaisonFixe;
import ponts.physique.voiture.Voiture;

/**
 * 无窗口内部冒烟测试。覆盖所有构件的创建、落点确认和短时物理运行。
 *
 * 运行：java -cp ".;lib/jbox2d-library-2.2.1.1.jar;lib/flatlaf-2.1.jar"
 * ponts.debug.SmokeTest
 */
public final class SmokeTest {

    private SmokeTest() {
    }

    public static void main(String[] args) {
        for (Materiau materiau : Materiau.values()) {
            testerPlacement(materiau);
        }
        testerVehiculeEtBoost();
        testerDirectionConstruction();
        testerDirectionSourisPixel();
        testerSupportsBudgetEtSuppression();
        testerRemplissages();
        System.out.println("SMOKE_OK materials=" + Materiau.values().length);
    }

    private static void testerDirectionConstruction() {
        World world = new World(new Vec2(0f, -9.81f));
        LiaisonFixe origineDroite = new LiaisonFixe(world, new Vec2(0f, 0f));
        LiaisonFixe cibleDroite = new LiaisonFixe(world, new Vec2(4f, 3f));
        BarreBois droite = new BarreBois(world, origineDroite, cibleDroite);
        verifier(Math.abs(droite.getAngle() - (float) Math.atan2(3f, 4f)) < 0.001f,
                "rightward construction angle is reversed");

        LiaisonFixe origineGauche = new LiaisonFixe(world, new Vec2(0f, 0f));
        LiaisonFixe cibleGauche = new LiaisonFixe(world, new Vec2(-4f, 3f));
        BarreBois gauche = new BarreBois(world, origineGauche, cibleGauche);
        verifier(Math.abs(gauche.getAngle() - (float) Math.atan2(3f, -4f)) < 0.001f,
                "leftward construction lost its quadrant");
    }

    private static void testerDirectionSourisPixel() {
        Niveau niveau = new Niveau();
        niveau.setBudget(120000);
        Vec2 origine = new Vec2(30f, 20f);
        niveau.ajouterLiaison(origine);
        niveau.ajouterPoint(new Vec2(45f, 5f));
        niveau.ajouterLiaison(new Vec2(60f, 20f));

        World world = new World(new Vec2(0f, -9.81f));
        Bord bord = new Bord(world, niveau);
        Pont pont = new Pont(world, niveau);
        pont.gererInput(world, origine, 1, true, Materiau.BOIS, bord);

        Box2D box2d = new Box2D(1000, 600);
        box2d.setCameraX(12f);
        int ox = box2d.worldToPixelX(origine.x);
        int oy = box2d.worldToPixelY(origine.y);

        Vec2 droite = pont.positionDepuisDirectionPixel(box2d, ox + 120, oy);
        Vec2 gauche = pont.positionDepuisDirectionPixel(box2d, ox - 120, oy);
        Vec2 haut = pont.positionDepuisDirectionPixel(box2d, ox, oy - 120);
        Vec2 bas = pont.positionDepuisDirectionPixel(box2d, ox, oy + 120);
        verifier(droite.x > origine.x && Math.abs(droite.y - origine.y) < 0.001f,
                "mouse right did not produce rightward direction");
        verifier(gauche.x < origine.x && Math.abs(gauche.y - origine.y) < 0.001f,
                "mouse left did not produce leftward direction");
        verifier(haut.y > origine.y && Math.abs(haut.x - origine.x) < 0.001f,
                "mouse above did not produce upward direction");
        verifier(bas.y < origine.y && Math.abs(bas.x - origine.x) < 0.001f,
                "mouse below did not produce downward direction");
    }

    private static void testerSupportsBudgetEtSuppression() {
        Niveau niveau = niveauOutils(20000);
        World world = new World(new Vec2(0f, -9.81f));
        Bord bord = new Bord(world, niveau);
        Pont pont = new Pont(world, niveau);
        Box2D box2d = new Box2D(1000, 600);

        Vec2 position = new Vec2(15f, 9f);
        int px = box2d.worldToPixelX(position.x);
        int py = box2d.worldToPixelY(position.y);
        pont.setOutil(world, OutilConstruction.ANCRAGE);
        pont.clicOutil(world, box2d, bord, px, py, 1, Materiau.BOIS);
        verifier(pont.getNombreAncragesUtilisateur() == 1, "user anchor was not created");
        verifier(pont.prix() == CoutsConstruction.ANCRAGE, "anchor cost missing");

        pont.clicOutil(world, box2d, bord, px, py, 3, Materiau.BOIS);
        verifier(pont.getNombreAncragesUtilisateur() == 0, "user anchor was not deleted");
        verifier(pont.prix() == 0, "anchor deletion did not refund cost");

        Vec2 pilier = new Vec2(25f, 15f);
        int pilierX = box2d.worldToPixelX(pilier.x);
        int pilierY = box2d.worldToPixelY(pilier.y);
        pont.setOutil(world, OutilConstruction.PILIER);
        pont.clicOutil(world, box2d, bord, pilierX, pilierY, 1, Materiau.BOIS);
        verifier(pont.getNombrePiliers() == 1, "pier was not created");
        verifier(pont.prix() == CoutsConstruction.PILIER, "pier cost missing");
        pont.clicOutil(world, box2d, bord, pilierX, pilierY, 3, Materiau.BOIS);
        verifier(pont.getNombrePiliers() == 0 && pont.prix() == 0,
                "pier deletion did not refund cost");

        Niveau pauvre = niveauOutils(CoutsConstruction.ANCRAGE - 1);
        World worldPauvre = new World(new Vec2(0f, -9.81f));
        Bord bordPauvre = new Bord(worldPauvre, pauvre);
        Pont pontPauvre = new Pont(worldPauvre, pauvre);
        pontPauvre.setOutil(worldPauvre, OutilConstruction.ANCRAGE);
        pontPauvre.clicOutil(worldPauvre, box2d, bordPauvre, px, py, 1, Materiau.BOIS);
        verifier(pontPauvre.getNombreAncragesUtilisateur() == 0,
                "anchor was created despite insufficient budget");
    }

    private static void testerRemplissages() {
        Niveau niveau = niveauOutils(120000);
        World world = new World(new Vec2(0f, -9.81f));
        Bord bord = new Bord(world, niveau);
        Pont pont = new Pont(world, niveau);
        Box2D box2d = new Box2D(1000, 600);
        Vec2 debut = niveau.getPosLiaisons().getFirst();
        Vec2 fin = niveau.getPosLiaisons().getLast();

        pont.setOutil(world, OutilConstruction.LIGNE);
        pont.clicOutil(world, box2d, bord, box2d.worldToPixelX(debut.x), box2d.worldToPixelY(debut.y),
                1, Materiau.BOIS);
        pont.clicOutil(world, box2d, bord, box2d.worldToPixelX(fin.x), box2d.worldToPixelY(fin.y),
                1, Materiau.BOIS);
        verifier(pont.getNombreBarres() > 1, "line fill did not generate segmented bars");
        verifier(pont.getNombreNoeudsGeneres() > 0, "line fill did not account generated nodes");

        Niveau niveauCourbe = niveauOutils(120000);
        World worldCourbe = new World(new Vec2(0f, -9.81f));
        Bord bordCourbe = new Bord(worldCourbe, niveauCourbe);
        Pont pontCourbe = new Pont(worldCourbe, niveauCourbe);
        Vec2 cd = niveauCourbe.getPosLiaisons().getFirst();
        Vec2 cf = niveauCourbe.getPosLiaisons().getLast();
        pontCourbe.setOutil(worldCourbe, OutilConstruction.COURBE);
        pontCourbe.clicOutil(worldCourbe, box2d, bordCourbe, box2d.worldToPixelX(cd.x),
                box2d.worldToPixelY(cd.y), 1, Materiau.ACIER);
        Vec2 controle = new Vec2(25f, 28f);
        pontCourbe.clicOutil(worldCourbe, box2d, bordCourbe, box2d.worldToPixelX(controle.x),
                box2d.worldToPixelY(controle.y), 1, Materiau.ACIER);
        pontCourbe.clicOutil(worldCourbe, box2d, bordCourbe, box2d.worldToPixelX(cf.x),
                box2d.worldToPixelY(cf.y), 1, Materiau.ACIER);
        verifier(pontCourbe.confirmerOutil(worldCourbe, Materiau.ACIER), "curve confirmation failed");
        verifier(pontCourbe.getNombreBarres() > 1, "curve fill did not generate bars");
        for (int i = 0; i < 30; i++) {
            world.step(1f / 60f, 10, 8);
            pont.testCasse(world, 1f / 60f);
            worldCourbe.step(1f / 60f, 10, 8);
            pontCourbe.testCasse(worldCourbe, 1f / 60f);
        }
    }

    private static Niveau niveauOutils(int budget) {
        Niveau niveau = new Niveau();
        niveau.setBudget(budget);
        niveau.ajouterPoint(new Vec2(-10f, -10f));
        niveau.ajouterLiaison(new Vec2(10f, 12f));
        niveau.ajouterPoint(new Vec2(25f, 3f));
        niveau.ajouterLiaison(new Vec2(40f, 12f));
        niveau.ajouterPoint(new Vec2(60f, -10f));
        return niveau;
    }

    private static void testerVehiculeEtBoost() {
        Niveau niveau = new Niveau();
        niveau.ajouterPoint(new Vec2(-20f, -10f));
        niveau.ajouterPoint(new Vec2(-20f, 10f));
        niveau.ajouterPoint(new Vec2(10f, 10f));
        niveau.ajouterPoint(new Vec2(60f, 10f));
        niveau.ajouterPoint(new Vec2(90f, 10f));
        niveau.ajouterPoint(new Vec2(90f, -10f));

        World world = new World(new Vec2(0f, -9.81f));
        new Bord(world, niveau);
        Voiture voiture = new Voiture(world, niveau, Voiture.Charge.CAISSES, Voiture.Style.OCEAN);

        verifier(voiture.boostDisponible(), "boost should initially be available");
        verifier(voiture.activerBoost(), "boost activation failed");
        verifier(!voiture.boostDisponible(), "boost was not consumed");

        for (int i = 0; i < 180; i++) {
            float dt = 1f / 60f;
            world.step(dt, 10, 8);
            voiture.tick(dt);
            voiture.arreter();
            verifier(Float.isFinite(voiture.getX()), "vehicle position became invalid");
        }
    }

    private static void testerPlacement(Materiau materiau) {
        Niveau niveau = new Niveau();
        niveau.setBudget(120000);
        Vec2 ancrageGauche = new Vec2(10f, 10f);
        Vec2 fond = new Vec2(25f, 2f);
        Vec2 ancrageDroit = new Vec2(40f, 10f);
        niveau.ajouterLiaison(ancrageGauche);
        niveau.ajouterPoint(fond);
        niveau.ajouterLiaison(ancrageDroit);

        World world = new World(new Vec2(0f, -9.81f));
        Bord bord = new Bord(world, niveau);
        Pont pont = new Pont(world, niveau);

        // Premier clic : sélectionner l'ancrage. Second clic : poser une extrémité libre.
        pont.gererInput(world, ancrageGauche, 1, true, materiau, bord);
        pont.gererInput(world, new Vec2(14f, 14f), 1, true, materiau, bord);

        verifier(pont.getNombreBarres() == 1, materiau + " placement impossible");
        verifier(pont.prix() > 0, materiau + " price was not finalized");

        for (int i = 0; i < 30; i++) {
            world.step(1f / 60f, 10, 8);
            pont.testCasse(world, 1f / 60f);
        }
    }

    private static void verifier(boolean condition, String message) {
        if (!condition) {
            throw new IllegalStateException(message);
        }
    }
}
