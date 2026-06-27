package ponts.debug;

import java.util.ArrayList;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.World;

import ponts.niveau.Niveau;
import ponts.niveau.ConfigurationVoiture;
import ponts.ihm.Box2D;
import ponts.physique.Pont;
import ponts.physique.OutilConstruction;
import ponts.physique.CoutsConstruction;
import ponts.physique.ProgressionJeu;
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
        testerConversionSourisEtCamera();
        testerLimitesEtLongueurLibre();
        testerConfigurationVoitureEtVol();
        verifier(!ProgressionJeu.volDisponible(1, 3), "flight unlocked before clearing level three");
        verifier(!ProgressionJeu.volDisponible(2, 2), "flight was available before level four");
        verifier(ProgressionJeu.volDisponible(2, 3), "flight did not unlock for level four");
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
        verifier(pont.getNombreBarres() == 0, "line fill generated before confirmation");
        verifier(pont.confirmerOutil(world, Materiau.BOIS), "line confirmation failed");
        verifier(pont.getNombreBarres() > 1, "line fill did not generate segmented bars");
        verifier(pont.getNombreNoeudsGeneres() > 0, "line fill did not account generated nodes");
        int prixLigne = pont.prix();
        pont.annulerOutil(world);
        verifier(pont.getNombreBarres() == 0 && pont.getNombreNoeudsGeneres() == 0 && pont.prix() < prixLigne,
                "line fill undo did not roll back structure and budget");

        Niveau niveauCourbe = niveauOutils(120000);
        World worldCourbe = new World(new Vec2(0f, -9.81f));
        Bord bordCourbe = new Bord(worldCourbe, niveauCourbe);
        Pont pontCourbe = new Pont(worldCourbe, niveauCourbe);
        Vec2 cd = niveauCourbe.getPosLiaisons().getFirst();
        Vec2 cf = niveauCourbe.getPosLiaisons().getLast();
        pontCourbe.setOutil(worldCourbe, OutilConstruction.COURBE);
        pontCourbe.clicOutil(worldCourbe, box2d, bordCourbe, box2d.worldToPixelX(cd.x),
                box2d.worldToPixelY(cd.y), 1, Materiau.ACIER);
        pontCourbe.clicOutil(worldCourbe, box2d, bordCourbe, box2d.worldToPixelX(cf.x),
                box2d.worldToPixelY(cf.y), 1, Materiau.ACIER);
        ArrayList<Vec2> poignees = pontCourbe.getPoigneesCourbe();
        verifier(poignees.size() == 2, "cubic bezier handles were not initialized");
        Vec2 poignee = poignees.get(0);
        pontCourbe.clicOutil(worldCourbe, poignee, box2d, bordCourbe,
                box2d.worldToPixelX(poignee.x), box2d.worldToPixelY(poignee.y), 1, Materiau.ACIER);
        Vec2 controleDeplace = new Vec2(20f, 28f);
        pontCourbe.glisserOutil(controleDeplace);
        pontCourbe.relacherOutil();
        verifier(pontCourbe.getPoigneesCourbe().get(0).sub(controleDeplace).length() < 0.001f,
                "bezier handle drag did not update preview control");
        verifier(pontCourbe.getNombreBarres() == 0, "curve generated before confirmation");
        verifier(pontCourbe.confirmerOutil(worldCourbe, Materiau.ACIER), "curve confirmation failed");
        verifier(pontCourbe.getNombreBarres() > 1, "curve fill did not generate bars");
        for (int i = 0; i < 30; i++) {
            world.step(1f / 60f, 10, 8);
            pont.testCasse(world, 1f / 60f);
            worldCourbe.step(1f / 60f, 10, 8);
            pontCourbe.testCasse(worldCourbe, 1f / 60f);
        }

        Niveau niveauPauvre = niveauOutils(1000);
        World worldPauvre = new World(new Vec2(0f, -9.81f));
        Bord bordPauvre = new Bord(worldPauvre, niveauPauvre);
        Pont pontPauvre = new Pont(worldPauvre, niveauPauvre);
        Vec2 pd = niveauPauvre.getPosLiaisons().getFirst();
        Vec2 pf = niveauPauvre.getPosLiaisons().getLast();
        pontPauvre.setOutil(worldPauvre, OutilConstruction.LIGNE);
        pontPauvre.clicOutil(worldPauvre, box2d, bordPauvre, box2d.worldToPixelX(pd.x),
                box2d.worldToPixelY(pd.y), 1, Materiau.ACIER);
        pontPauvre.clicOutil(worldPauvre, box2d, bordPauvre, box2d.worldToPixelX(pf.x),
                box2d.worldToPixelY(pf.y), 1, Materiau.ACIER);
        verifier(!pontPauvre.confirmerOutil(worldPauvre, Materiau.ACIER),
                "fill ignored insufficient budget");
        verifier(pontPauvre.getNombreBarres() == 0 && pontPauvre.getMessageOutil().contains("预算不足"),
                "budget failure was not atomic or visible");
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

    private static void testerConversionSourisEtCamera() {
        Box2D box2d = new Box2D(1379, 823);
        box2d.setCameraX(47.25f);
        int[][] pixels = {
                { 0, 0 },
                { 1378, 822 },
                { 231, 417 },
                { 902, 126 }
        };
        for (int[] pixel : pixels) {
            Vec2 monde = box2d.pixelToWorld(pixel[0], pixel[1]);
            verifier(Math.abs(box2d.worldToPixelX(monde.x) - pixel[0]) <= 1,
                    "camera X coordinate round-trip drift");
            verifier(Math.abs(box2d.worldToPixelY(monde.y) - pixel[1]) <= 1,
                    "viewport Y coordinate round-trip drift");
        }

        Niveau niveau = niveauOutils(20000);
        World world = new World(new Vec2(0f, -9.81f));
        Bord bord = new Bord(world, niveau);
        Pont pont = new Pont(world, niveau);
        pont.setOutil(world, OutilConstruction.ANCRAGE);
        Vec2 cibleMonde = new Vec2(15f, 9f);
        // Les pixels volontairement faux ne doivent plus déplacer l'outil.
        pont.clicOutil(world, cibleMonde, box2d, bord, 1200, 40, 1, Materiau.BOIS);
        verifier(pont.getNombreAncragesUtilisateur() == 1, "authoritative world cursor was ignored");
        Vec2 cree = pont.getPositionsSupportsUtilisateur().get(0);
        verifier(cree.sub(cibleMonde).length() < 0.001f, "support generation drifted from preview");
    }

    private static void testerLimitesEtLongueurLibre() {
        Box2D box2d = new Box2D(1000, 600);
        Niveau limite = niveauOutils(120000);
        limite.setLimiteBarres(2);
        World worldLimite = new World(new Vec2(0f, -9.81f));
        Bord bordLimite = new Bord(worldLimite, limite);
        Pont pontLimite = new Pont(worldLimite, limite);
        Vec2 debut = limite.getPosLiaisons().getFirst();
        Vec2 fin = limite.getPosLiaisons().getLast();
        pontLimite.setOutil(worldLimite, OutilConstruction.LIGNE);
        pontLimite.clicOutil(worldLimite, debut, box2d, bordLimite, box2d.worldToPixelX(debut.x),
                box2d.worldToPixelY(debut.y), 1, Materiau.BOIS);
        pontLimite.clicOutil(worldLimite, fin, box2d, bordLimite, box2d.worldToPixelX(fin.x),
                box2d.worldToPixelY(fin.y), 1, Materiau.BOIS);
        verifier(!pontLimite.confirmerOutil(worldLimite, Materiau.BOIS)
                && pontLimite.getMessageOutil().contains("杆件额度不足"),
                "configured bar limit was not enforced");

        Niveau libre = niveauOutils(120000);
        libre.setLimiteBarres(0);
        World worldLibre = new World(new Vec2(0f, -9.81f));
        Bord bordLibre = new Bord(worldLibre, libre);
        Pont pontLibre = new Pont(worldLibre, libre);
        Vec2 origine = libre.getPosLiaisons().getFirst();
        Vec2 cibleLointaine = new Vec2(35f, 32f);
        pontLibre.gererInput(worldLibre, origine, 1, true, Materiau.BOIS, bordLibre);
        pontLibre.gererInput(worldLibre, cibleLointaine, 1, true, Materiau.BOIS, bordLibre);
        verifier(pontLibre.getNombreBarres() == 1 && pontLibre.prix() > 1000,
                "unlimited mode still clamped long free-angle bars");
    }

    private static void testerConfigurationVoitureEtVol() {
        try {
            Niveau niveau = niveauVoiture();
            niveau.setLimiteBarres(50);
            ConfigurationVoiture config = niveau.getConfigurationVoiture();
            config.masse = 1.8f;
            config.vitesseInitiale = 3f;
            config.vitesseMax = 14f;
            config.acceleration = 240f;
            config.boostCooldown = 0.5f;
            config.boostDuree = 0.8f;
            config.boostIntensite = 2.8f;
            config.volAutorise = true;
            config.volCooldown = 0.5f;
            config.volDuree = 0.8f;
            config.volPousseeVerticale = 260f;
            config.volPousseeHorizontale = 35f;
            config.volHauteurMax = 20f;
            config.volSubitGravite = false;

            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            ObjectOutputStream sortie = new ObjectOutputStream(bytes);
            sortie.writeObject(niveau);
            sortie.close();
            ObjectInputStream entree = new ObjectInputStream(new ByteArrayInputStream(bytes.toByteArray()));
            Niveau recharge = (Niveau) entree.readObject();
            entree.close();
            verifier(Math.abs(recharge.getConfigurationVoiture().boostIntensite - 2.8f) < 0.001f,
                    "vehicle configuration was not serialized");
            verifier(recharge.getLimiteBarres() == 50, "level bar limit was not serialized");

            World world = new World(new Vec2(0f, -9.81f));
            new Bord(world, recharge);
            Voiture voiture = new Voiture(world, recharge, Voiture.Charge.STANDARD,
                    Voiture.Style.CLASSIQUE, true);
            verifier(voiture.getMasse() > 1f, "configured mass did not affect physics body");
            verifier(Math.abs(voiture.getVitesseX() - 3f) < 0.01f, "initial speed was ignored");
            verifier(voiture.activerBoost(), "configured boost did not activate");
            verifier(voiture.activerVol(), "unlocked configured flight did not activate");
            float yInitial = voiture.getY();
            float vitesseMaxObservee = 0f;
            for (int i = 0; i < 90; i++) {
                float dt = 1f / 60f;
                voiture.tick(dt);
                world.step(dt, 10, 8);
                vitesseMaxObservee = Math.max(vitesseMaxObservee, Math.abs(voiture.getVitesseX()));
            }
            verifier(voiture.getY() > yInitial + 0.2f, "flight force did not lift the vehicle");
            verifier(vitesseMaxObservee > config.vitesseMax * 1.1f,
                    "boost was too weak to produce a noticeable speed increase");
            verifier(vitesseMaxObservee <= config.vitesseMax * config.boostIntensite + 0.5f,
                    "boost ignored configured maximum speed");
            verifier(voiture.boostDisponible() && voiture.volDisponible(),
                    "configured cooldowns did not recover");
        } catch (Exception ex) {
            throw new IllegalStateException("vehicle configuration test failed", ex);
        }
    }

    private static Niveau niveauVoiture() {
        Niveau niveau = new Niveau();
        niveau.ajouterPoint(new Vec2(-20f, -10f));
        niveau.ajouterPoint(new Vec2(-20f, 10f));
        niveau.ajouterPoint(new Vec2(10f, 10f));
        niveau.ajouterPoint(new Vec2(60f, 10f));
        niveau.ajouterPoint(new Vec2(90f, 10f));
        niveau.ajouterPoint(new Vec2(90f, -10f));
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
