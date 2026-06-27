package ponts.physique;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Polygon;

import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.World;

import ponts.ihm.Box2D;
import ponts.ihm.Jeu;
import ponts.niveau.Niveau;
import ponts.physique.barres.Materiau;
import ponts.physique.environnement.Bord;
import ponts.physique.voiture.Voiture;

/**
 * Classe gérant une partie
 */
public class Partie {

    private Jeu jeu;
    private Box2D box2d;
    private World world;
    private Pont pont;
    private Bord bord;
    private Voiture voiture;
    private int budget;
    private Vec2 objectif;

    private boolean simulationPhysique = false;
    private boolean creationPont = true;
    private boolean terminee = false;
    private Materiau materiau = Materiau.GOUDRON;

    /**
     * Constructeur d'une partie
     * 
     * @param jeu
     * @param box2d
     * @param niveau
     */
    public Partie(Jeu jeu, Box2D box2d, Niveau niveau, Voiture.Charge charge, Voiture.Style style) {
        this.jeu = jeu;
        this.box2d = box2d;

        niveau.centrer(box2d);
        niveau.ajouterExtremitees(box2d);

        Vec2 gravity = new Vec2(0.0f, -9.81f);
        world = new World(gravity);

        bord = new Bord(world, niveau);
        pont = new Pont(world, niveau);
        voiture = new Voiture(world, niveau, charge, style);
        budget = niveau.getBudget();
        objectif = niveau.calculerObjectif();
        box2d.setCameraX(Math.max(0f, niveau.calculerDepart() - 18f));

    }

    public boolean getSimulationPhysique() {
        return simulationPhysique;
    }

    public void setSimulationPhsyique(boolean simulationPhysique) {
        this.simulationPhysique = simulationPhysique;
        pont.arreterCreation(world);
        if (creationPont) {
            creationPont = false;
        }
    }

    public void toggleSimulationPhysique() {
        setSimulationPhsyique(!getSimulationPhysique());
    }

    /**
     * Dessine une partie
     * 
     * @param g
     * @param box2d
     * @param posSouris
     */
    public void dessiner(Graphics2D g, Box2D box2d, Vec2 posSouris) {
        bord.dessiner(g, box2d);
        dessinerObjectif(g, box2d);
        pont.dessiner(g, box2d, posSouris, creationPont);
        voiture.dessiner(g, box2d);
    }

    /**
     * Effectue les calculs de la physique qui s'est écoulée pendant le temps dt
     * 
     * @param posSouris
     * @param boutonSouris
     * @param clicSouris
     * @param dt
     */
    public void tickPhysique(Vec2 posSouris, int boutonSouris, boolean clicSouris, float dt) {

        if (simulationPhysique) {
            world.step(dt, 10, 8);
            pont.testCasse(world, dt);
            voiture.tick(dt);
            voiture.arreter();
            suivreVoiture();
            if (!terminee && voiture.objectifAtteint()) {
                finPartie();
            }
        }
        // 建造输入由 mousePressed/mouseMoved 即时驱动。不要在物理 timer 中
        // 再次更新预览，否则新旧坐标路径会交替覆盖并造成虚影左右闪烁。

    }

    /**
     * Méthode appelée en fin de partie
     */
    private void finPartie() {
        boolean niveauReussi = getPrix() <= getBuget();
        jeu.finPartie(niveauReussi, getPrix());
        terminee = true;
    }

    /**
     * Méthode pour changer de matériau sélectionné
     * 
     * @param materiau
     */
    public void changementMateriau(Materiau materiau) {
        this.materiau = materiau;
        pont.arreterCreation(world);

    }

    public int getPrix() {
        return pont.prix();
    }

    public int getBuget() {
        return budget;
    }

    public int getNombreBarres() {
        return pont.getNombreBarres();
    }

    public int getLimiteBarres() {
        return pont.getLimiteBarres();
    }

    public boolean activerBoost() {
        return simulationPhysique && voiture.activerBoost();
    }

    public boolean boostDisponible() {
        return voiture.boostDisponible();
    }

    /**
     * Traite immédiatement un clic de construction depuis Swing. Cela évite de
     * perdre un clic court entre deux impulsions du timer physique.
     */
    public void clicConstruction(Vec2 position, int bouton) {
        if (!simulationPhysique && creationPont) {
            pont.gererInput(world, position, bouton, true, materiau, bord);
        }
    }

    public void clicConstructionPixel(int pixelX, int pixelY, int bouton) {
        if (!simulationPhysique && creationPont) {
            if (pont.getOutil() != OutilConstruction.BARRE) {
                pont.clicOutil(world, box2d, bord, pixelX, pixelY, bouton, materiau);
                return;
            }
            Vec2 positionLibre = box2d.pixelToWorld(pixelX, pixelY);
            Vec2 positionAccrochee = pont.estEnConstruction()
                    ? pont.positionDepuisDirectionPixel(box2d, pixelX, pixelY)
                    : pont.accrocherPositionPixel(box2d, pixelX, pixelY, positionLibre);
            pont.gererInput(world, positionAccrochee, bouton, true, materiau, bord);
        }
    }

    public void deplacerConstructionPixel(int pixelX, int pixelY) {
        if (!simulationPhysique && creationPont) {
            if (pont.getOutil() == OutilConstruction.BARRE && pont.estEnConstruction()) {
                Vec2 position = pont.positionDepuisDirectionPixel(box2d, pixelX, pixelY);
                pont.gererInput(world, position, 0, false, materiau, bord);
            } else {
                pont.actualiserOutil(box2d, bord, pixelX, pixelY);
            }
        }
    }

    public boolean noeudSousCurseur(Vec2 position) {
        return creationPont && pont.estProcheLiaison(position);
    }

    public boolean noeudSousCurseurPixel(int pixelX, int pixelY) {
        return creationPont && pont.estProcheLiaisonPixel(box2d, pixelX, pixelY);
    }

    public void changementOutil(OutilConstruction outil) {
        pont.setOutil(world, outil);
    }

    public boolean confirmerOutil() {
        return pont.confirmerOutil(world, materiau);
    }

    public void annulerOutil() {
        pont.annulerOutil();
    }

    public int getNombreAncragesUtilisateur() {
        return pont.getNombreAncragesUtilisateur();
    }

    public int getNombrePiliers() {
        return pont.getNombrePiliers();
    }

    public int getNombreNoeudsGeneres() {
        return pont.getNombreNoeudsGeneres();
    }

    public int getCoutSupports() {
        return pont.getCoutSupports();
    }

    private void suivreVoiture() {
        float positionEcran = voiture.getX() - box2d.getCameraX();
        if (positionEcran > 72f) {
            box2d.setCameraX(voiture.getX() - 72f);
        }
    }

    private void dessinerObjectif(Graphics2D g, Box2D box2d) {
        int x = box2d.worldToPixelX(objectif.x);
        int y = box2d.worldToPixelY(objectif.y);
        int hauteur = Math.max(55, box2d.worldToPixel(7f));

        g.setStroke(new BasicStroke(4f));
        g.setColor(Color.decode("#f8efe0"));
        g.drawLine(x, y, x, y - hauteur);
        Polygon drapeau = new Polygon();
        drapeau.addPoint(x, y - hauteur);
        drapeau.addPoint(x + Math.max(34, box2d.worldToPixel(4f)), y - hauteur + 12);
        drapeau.addPoint(x, y - hauteur + 25);
        g.setColor(Color.decode("#ef6a3b"));
        g.fillPolygon(drapeau);
        g.setColor(Color.decode("#6f3d27"));
        g.drawPolygon(drapeau);
    }

}
