package ponts.physique;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.LinkedList;
import java.util.Map;

import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.World;

import ponts.ihm.Box2D;
import ponts.niveau.Niveau;
import ponts.physique.barres.Barre;
import ponts.physique.barres.BarreAcier;
import ponts.physique.barres.BarreAluminium;
import ponts.physique.barres.BarreBois;
import ponts.physique.barres.BarreCarbone;
import ponts.physique.barres.BarreGoudron;
import ponts.physique.barres.Materiau;
import ponts.physique.environnement.Bord;
import ponts.physique.liaisons.Liaison;
import ponts.physique.liaisons.LiaisonFixe;
import ponts.physique.liaisons.LiaisonMobile;

/**
 * Classe du pont, coeur du projet. Elle a la structure d'un graphe dont les
 * noeuds sont des liaisons et les arrêtes des barres
 */
public class Pont implements Serializable {

    private LinkedList<Barre> barres;
    private LinkedList<Liaison> liaisons;
    private Barre barreEnCreation;
    private Liaison liaisonEnCreation;
    private Liaison liaisonProche;
    private int limiteBarres;
    private boolean dernierPlacementValide;
    private int budget;
    private OutilConstruction outil = OutilConstruction.BARRE;
    private Map<Liaison, OutilConstruction> supportsUtilisateur;
    private Map<Liaison, Integer> coutsNoeuds;
    private Vec2 positionOutil = new Vec2();
    private boolean positionOutilValide;
    private Liaison selectionOutil;
    private Liaison supportEnDeplacement;
    private Liaison courbeDepart;
    private Liaison courbeArrivee;
    private Vec2 courbeControle;
    private boolean courbePrete;

    /**
     * Constructeur d'un pont
     * 
     * @param world
     * @param niveau
     */
    public Pont(World world, Niveau niveau) {

        barres = new LinkedList<Barre>();
        liaisons = new LinkedList<Liaison>();
        limiteBarres = niveau.getLimiteBarres();
        budget = niveau.getBudget();
        supportsUtilisateur = new IdentityHashMap<Liaison, OutilConstruction>();
        coutsNoeuds = new IdentityHashMap<Liaison, Integer>();

        for (Vec2 posLiaison : niveau.getPosLiaisons()) {
            liaisons.add(new LiaisonFixe(world, posLiaison));
        }

    }

    /**
     * Dessine le pont
     * 
     * @param g
     * @param box2d
     * @param posSouris
     * @param creationPont
     */
    public void dessiner(Graphics g, Box2D box2d, Vec2 posSouris, boolean creationPont) {
        if (creationPont) {
            dessinerApercuOutil((Graphics2D) g, box2d);
        }
        dessinerSupportsUtilisateur((Graphics2D) g, box2d);
        if (barreEnCreation != null && creationPont) {
            dessinerZoneConstruction((Graphics2D) g, box2d);
        }
        LinkedList<Barre> barresDesinees = new LinkedList<Barre>();
        for (Liaison liaison : liaisons) {
            for (Barre barre : liaison.getBarresLiees()) {
                if (!barresDesinees.contains(barre) && barre != barreEnCreation) {
                    barre.dessiner(g, box2d);
                    barresDesinees.add(barre);
                }
            }
            if (liaison != liaisonEnCreation) {
                liaison.dessiner(g, box2d, false);
            }
        }
        // On dessine les objets en creation a la fin pour qu'il apparaissent par dessus
        // les autres
        if (barreEnCreation != null) {
            barreEnCreation.dessiner(g, box2d);
            liaisonEnCreation.dessiner(g, box2d, false);
        }
        // On redessine la liaison proche pour quelle soit d'une autre couleur
        // et qu'elle se supperpose sur la liaison en création
        if (creationPont) {
            Liaison liaisonSurvolee = recupLiaisonProche(posSouris);
            if (liaisonSurvolee != null) {
                liaisonSurvolee.dessiner(g, box2d, true);
            }
        }
    }

    /**
     * Méthode principale, gérant les entrées de l'utilisateur
     * 
     * @param world
     * @param posSouris
     * @param boutonSouris
     * @param clicSouris
     * @param materiau
     * @param bord
     */
    public void gererInput(World world, Vec2 posSouris, int boutonSouris, boolean clicSouris, Materiau materiau,
            Bord bord) {

        Vec2 posSourisMax = posSourisMax(barreEnCreation, posSouris);
        liaisonProche = recupLiaisonProche(posSourisMax);
        boolean sourisDansBord = bord.estDansBord(posSourisMax);

        if (barreEnCreation != null) {
            // Toujours placer d'abord l'aperçu sous la souris. L'ancienne séquence
            // validait encore sa longueur précédente (souvent zéro au deuxième clic).
            if (liaisonProche != null) {
                majPreview(liaisonProche.getPos());
            } else if (!barreEnCreation.inferieurLongeurMax(posSouris)) {
                majPreview(posSourisMax);
                liaisonProche = null;
            } else {
                majPreview(posSouris);
            }

        }
        boolean barreValide = barreValide(barreEnCreation, liaisonProche, sourisDansBord);
        dernierPlacementValide = barreValide;

        if (clicSouris) {

            switch (boutonSouris) {

                case 1: // clic gauche

                    // Teste si la barre vérifie des conditions
                    if (barreValide) {
                        if (liaisonProche != null) {
                            barreEnCreation.accrocher(world, liaisonProche);
                            liaisons.remove(liaisonEnCreation);
                        } else {
                            liaisonProche = liaisonEnCreation;
                        }
                        lacherBarre(world);
                    }

                    // Cas ou on doit creer une nouvelle barre
                    if (barreEnCreation == null && liaisonProche != null && getNombreBarres() < limiteBarres) {
                        creerBarre(world, posSouris, liaisonProche, materiau);
                        posSourisMax = posSourisMax(barreEnCreation, posSouris);
                        majPreview(posSourisMax);
                    }

                    break;

                case 3: // clic droit
                    if (barreEnCreation != null) {
                        arreterCreation(world);
                    } else {
                        supprimerBarresCliquees(world, posSouris);
                    }

                    break;

            }
        }

    }

    /**
     * Calcule la position maximale à laquelle devrait être la souris pour que la
     * taille de la barre soit inférieure à la taille maximale
     * 
     * @param barre
     * @param posSouris
     * @return
     */
    private Vec2 posSourisMax(Barre barre, Vec2 posSouris) {
        if (barre == null) {
            return posSouris;
        } else if (barre.inferieurLongeurMax(posSouris)) {
            return posSouris;
        } else {
            return barreEnCreation.posLiaisonMax(posSouris);
        }
    }

    /**
     * Détermine si une barre est valide
     * 
     * @param barre
     * @param liaisonProche
     * @param sourisDansBord
     * @return
     */
    private boolean barreValide(Barre barre, Liaison liaisonProche, boolean sourisDansBord) {
        // barre n'existe pas
        if (barre == null)
            return false;
        // taille minimum
        if (!barre.tailleMinimum())
            return false;
        // si elle est proche d'une liaison existante
        if (liaisonProche != null) {
            // la barre reliant les deux liaisons n'existe pas deja
            if (barreExisteDeja(barre, liaisonProche))
                return false;
            // la barre ne boucle pas sur elle même
            if (barre.getLiaisonsLiees().contains(liaisonProche))
                return false;
        } else {
            // Si la souris est dans le bord et proche d'aucune liaison
            if (sourisDansBord) {
                return false;
            }
        }
        return true;
    }

    /**
     * Regarde si une barre n'existe pas déjà entre les deux liaisons sélectionnées
     * 
     * @param barreATester
     * @param liaison2
     * @return
     */
    private boolean barreExisteDeja(Barre barreATester, Liaison liaison2) {
        if (liaison2 == null) {
            return false;
        }
        Liaison liaison1 = barreATester.getLiaisonsLiees().get(0);
        for (Barre barre : liaison1.getBarresLiees()) {
            if (barre.getLiaisonsLiees().contains(liaison2)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Intérompt la création d'une barre en la supprimant
     * 
     * @param world
     */
    public void arreterCreation(World world) {
        if (barreEnCreation != null) {
            supprimerBarre(world, barreEnCreation);
            barreEnCreation = null;
            liaisonEnCreation = null;
        }
    }

    /**
     * Supprime une barre
     * 
     * @param world
     * @param barre
     */
    private void supprimerBarre(World world, Barre barre) {
        LinkedList<LiaisonMobile> liaisonASupprimer = barre.supprimer(world);
        barres.remove(barre);
        liaisons.removeAll(liaisonASupprimer);
        for (LiaisonMobile liaison : liaisonASupprimer) {
            coutsNoeuds.remove(liaison);
        }
    }

    /**
     * Termine la création d'une barre, pour que le curseur la lâche
     * 
     * @param world
     */
    private void lacherBarre(World world) {
        barreEnCreation.activerPhysique();

        for (Liaison liaison : barreEnCreation.getLiaisonsLiees()) {
            liaison.activerPhysique();
            barreEnCreation.lier(world, liaison);
        }

        liaisonEnCreation = null;
        barreEnCreation = null;

    }

    /**
     * Met à jour la preview d'une barre
     * 
     * @param posSouris
     */
    private void majPreview(Vec2 posSouris) {
        liaisonEnCreation.setPos(posSouris);
        barreEnCreation.ajusterPos();
    }

    /**
     * Récupère la liaison la plus proche de la position de la souris, s'il en
     * existe une dans un rayon donné
     * 
     * @param posSouris
     * @return liaison
     */
    private Liaison recupLiaisonProche(Vec2 posSouris) {
        Liaison liaisonPlusProche = null;
        float distanceMin = Float.POSITIVE_INFINITY;
        for (Liaison liaison : liaisons) {
            if (liaison.testLiaisonCliquee(posSouris) && liaison != liaisonEnCreation) {
                float distance = liaison.distancePoint(posSouris);
                if (distance < distanceMin) {
                    distanceMin = distance;
                    liaisonPlusProche = liaison;
                }
            }
        }
        return liaisonPlusProche;
    }

    /**
     * Créé une barre du matériau donné, ainsi que la liaison associée
     * 
     * @param world
     * @param posClic
     * @param liaisonCliquee
     * @param materiau
     */
    private void creerBarre(World world, Vec2 posClic, Liaison liaisonCliquee, Materiau materiau) {

        liaisonEnCreation = new LiaisonMobile(world, posClic);
        liaisons.add(liaisonEnCreation);

        Liaison liaison1 = liaisonCliquee;
        Liaison liaison2 = liaisonEnCreation;

        switch (materiau) {
            case GOUDRON:
                barreEnCreation = new BarreGoudron(world, liaison1, liaison2);
                break;
            case BOIS:
                barreEnCreation = new BarreBois(world, liaison1, liaison2);
                break;
            case ACIER:
                barreEnCreation = new BarreAcier(world, liaison1, liaison2);
                break;
            case ALUMINIUM:
                barreEnCreation = new BarreAluminium(world, liaison1, liaison2);
                break;
            case CARBONE:
                barreEnCreation = new BarreCarbone(world, liaison1, liaison2);
                break;
        }

        barres.add(barreEnCreation);

    }

    /**
     * Teste pour chaque barre si elle doit casser
     * 
     * @param world
     * @param dt
     */
    public void testCasse(World world, float dt) {
        for (Barre barre : barres) {
            if (barre != barreEnCreation) {
                LinkedList<Liaison> liaisonsCrees = barre.testCasse(world, dt);
                liaisons.addAll(liaisonsCrees);
            }
        }

    }

    /**
     * Supprime la barre cliquée (clic droit)
     * 
     * @param world
     * @param posClic
     */
    private void supprimerBarresCliquees(World world, Vec2 posClic) {
        LinkedList<Barre> barresASupprimer = new LinkedList<Barre>();
        for (Barre barre : barres) {
            if (barre.testBarreCliquee(posClic)) {
                supprimerBarre(world, barre);
                barresASupprimer.add(barre);
            }
        }
        barres.removeAll(barresASupprimer);
    }

    /**
     * Calcule le prix du pont
     * 
     * @return
     */
    public int prix() {
        int prix = 0;
        for (Barre barre : barres) {
            prix += barre.getPrix();
        }
        return prix + getCoutSupports();
    }

    public int getNombreBarres() {
        return barres.size() - (barreEnCreation == null ? 0 : 1);
    }

    public int getLimiteBarres() {
        return limiteBarres;
    }

    public boolean estEnConstruction() {
        return barreEnCreation != null;
    }

    /**
     * 节点接管鼠标后，直接用“节点屏幕位置 -> 鼠标屏幕位置”的向量计算目标。
     * 屏幕 Y 轴向下，因此转换为世界方向时需要取反。
     */
    public Vec2 positionDepuisDirectionPixel(Box2D box2d, int pixelX, int pixelY) {
        if (barreEnCreation == null) {
            return box2d.pixelToWorld(pixelX, pixelY);
        }
        Vec2 origine = barreEnCreation.getLiaisonsLiees().get(0).getPos();
        int originePixelX = box2d.worldToPixelX(origine.x);
        int originePixelY = box2d.worldToPixelY(origine.y);
        float deltaX = box2d.pixelToWorld(pixelX - originePixelX);
        float deltaY = box2d.pixelToWorld(originePixelY - pixelY);
        return new Vec2(origine.x + deltaX, origine.y + deltaY);
    }

    public boolean estProcheLiaison(Vec2 position) {
        return recupLiaisonProche(position) != null;
    }

    /**
     * 按节点在屏幕上的实际绘制位置选点，避免窗口边框、HUD 高度和镜头偏移
     * 造成的世界坐标误差。
     */
    public Vec2 accrocherPositionPixel(Box2D box2d, int pixelX, int pixelY, Vec2 positionLibre) {
        Liaison meilleure = null;
        int distanceCarreeMin = Integer.MAX_VALUE;
        int rayonPixels = Math.max(36, box2d.worldToPixel(Liaison.RAYON * 5f));

        for (Liaison liaison : liaisons) {
            if (liaison == liaisonEnCreation) {
                continue;
            }
            int dx = box2d.worldToPixelX(liaison.getX()) - pixelX;
            int dy = box2d.worldToPixelY(liaison.getY()) - pixelY;
            int distanceCarree = dx * dx + dy * dy;
            if (distanceCarree <= rayonPixels * rayonPixels && distanceCarree < distanceCarreeMin) {
                meilleure = liaison;
                distanceCarreeMin = distanceCarree;
            }
        }
        return meilleure == null ? positionLibre : meilleure.getPos().clone();
    }

    public boolean estProcheLiaisonPixel(Box2D box2d, int pixelX, int pixelY) {
        Vec2 libre = new Vec2(Float.NaN, Float.NaN);
        Vec2 resultat = accrocherPositionPixel(box2d, pixelX, pixelY, libre);
        return Float.isFinite(resultat.x);
    }

    public void setOutil(World world, OutilConstruction outil) {
        arreterCreation(world);
        this.outil = outil;
        selectionOutil = null;
        supportEnDeplacement = null;
        reinitialiserCourbe();
    }

    public OutilConstruction getOutil() {
        return outil;
    }

    public void actualiserOutil(Box2D box2d, Bord bord, int pixelX, int pixelY) {
        positionOutil = box2d.pixelToWorld(pixelX, pixelY);
        OutilConstruction typePlacement = supportEnDeplacement == null ? outil
                : supportsUtilisateur.getOrDefault(supportEnDeplacement, OutilConstruction.DEPLACER);
        boolean dansMonde = positionOutil.x >= 0f && positionOutil.x <= box2d.getLargeur()
                && positionOutil.y >= 0f && positionOutil.y <= box2d.getHauteur();
        boolean solSousPoint = bord.estDansBord(positionOutil.add(new Vec2(0f, -0.5f)));
        boolean zoneAutorisee = typePlacement == OutilConstruction.ANCRAGE ? solSousPoint
                : typePlacement != OutilConstruction.PILIER || !bord.estDansBord(positionOutil);
        int cout = typePlacement == OutilConstruction.ANCRAGE ? CoutsConstruction.ANCRAGE
                : typePlacement == OutilConstruction.PILIER ? CoutsConstruction.PILIER : 0;
        positionOutilValide = dansMonde && zoneAutorisee
                && !procheToutesLiaisons(positionOutil, 2.2f, supportEnDeplacement)
                && prix() + (supportEnDeplacement == null ? cout : 0) <= budget;

        if (outil == OutilConstruction.DEPLACER && supportEnDeplacement != null && positionOutilValide) {
            deplacerSupport(positionOutil);
        }
    }

    public void clicOutil(World world, Box2D box2d, Bord bord, int pixelX, int pixelY,
            int bouton, Materiau materiau) {
        actualiserOutil(box2d, bord, pixelX, pixelY);

        if (bouton == 3) {
            if (supprimerNoeudUtilisateurClique(world, positionOutil)) {
                return;
            }
            supprimerBarresCliquees(world, positionOutil);
            annulerOutil();
            return;
        }
        if (bouton != 1) {
            return;
        }

        switch (outil) {
            case ANCRAGE:
                ajouterSupport(world, positionOutil, OutilConstruction.ANCRAGE);
                break;
            case PILIER:
                ajouterSupport(world, positionOutil, OutilConstruction.PILIER);
                break;
            case LIGNE:
                clicLigne(world, box2d, pixelX, pixelY, materiau);
                break;
            case COURBE:
                clicCourbe(box2d, pixelX, pixelY);
                break;
            case DEPLACER:
                clicDeplacer(box2d, pixelX, pixelY);
                break;
            case BARRE:
            default:
                break;
        }
    }

    public boolean confirmerOutil(World world, Materiau materiau) {
        if (outil != OutilConstruction.COURBE || !courbePrete) {
            return false;
        }
        ArrayList<Vec2> points = new ArrayList<Vec2>();
        Vec2 debut = courbeDepart.getPos();
        Vec2 fin = courbeArrivee.getPos();
        float longueurApprox = debut.sub(courbeControle).length() + courbeControle.sub(fin).length();
        int segments = Math.max(2, (int) Math.ceil(longueurApprox / CoutsConstruction.ESPACEMENT_REMPLISSAGE));
        for (int i = 0; i <= segments; i++) {
            float t = i / (float) segments;
            float u = 1f - t;
            points.add(new Vec2(
                    u * u * debut.x + 2f * u * t * courbeControle.x + t * t * fin.x,
                    u * u * debut.y + 2f * u * t * courbeControle.y + t * t * fin.y));
        }
        boolean ok = genererChemin(world, courbeDepart, courbeArrivee, points, materiau);
        if (ok) {
            reinitialiserCourbe();
        }
        return ok;
    }

    public void annulerOutil() {
        selectionOutil = null;
        supportEnDeplacement = null;
        reinitialiserCourbe();
    }

    private void ajouterSupport(World world, Vec2 position, OutilConstruction type) {
        int cout = type == OutilConstruction.ANCRAGE ? CoutsConstruction.ANCRAGE : CoutsConstruction.PILIER;
        if (!positionOutilValide || prix() + cout > budget) {
            return;
        }
        LiaisonFixe support = new LiaisonFixe(world, position.clone());
        liaisons.add(support);
        supportsUtilisateur.put(support, type);
        coutsNoeuds.put(support, cout);
    }

    private void clicLigne(World world, Box2D box2d, int pixelX, int pixelY, Materiau materiau) {
        Liaison proche = recupLiaisonProchePixel(box2d, pixelX, pixelY);
        if (proche == null) {
            return;
        }
        if (selectionOutil == null) {
            selectionOutil = proche;
            return;
        }
        if (selectionOutil != proche) {
            ArrayList<Vec2> points = echantillonnerLigne(selectionOutil.getPos(), proche.getPos());
            genererChemin(world, selectionOutil, proche, points, materiau);
        }
        selectionOutil = null;
    }

    private void clicCourbe(Box2D box2d, int pixelX, int pixelY) {
        if (courbeDepart == null) {
            courbeDepart = recupLiaisonProchePixel(box2d, pixelX, pixelY);
        } else if (courbeControle == null) {
            courbeControle = positionOutil.clone();
        } else if (courbeArrivee == null) {
            courbeArrivee = recupLiaisonProchePixel(box2d, pixelX, pixelY);
            courbePrete = courbeArrivee != null && courbeArrivee != courbeDepart;
        }
    }

    private void clicDeplacer(Box2D box2d, int pixelX, int pixelY) {
        if (supportEnDeplacement == null) {
            Liaison candidat = recupLiaisonProchePixel(box2d, pixelX, pixelY);
            if (coutsNoeuds.containsKey(candidat)) {
                supportEnDeplacement = candidat;
            }
        } else {
            if (positionOutilValide) {
                deplacerSupport(positionOutil);
                supportEnDeplacement = null;
            }
        }
    }

    private void deplacerSupport(Vec2 position) {
        supportEnDeplacement.setPos(position);
        for (Barre barre : supportEnDeplacement.getBarresLiees()) {
            barre.ajusterPos();
        }
    }

    private boolean supprimerNoeudUtilisateurClique(World world, Vec2 position) {
        Liaison cible = null;
        for (Liaison liaison : coutsNoeuds.keySet()) {
            if (liaison.distancePoint(position) <= Liaison.RAYON * 5f) {
                cible = liaison;
                break;
            }
        }
        if (cible == null) {
            return false;
        }
        LinkedList<Barre> liees = new LinkedList<Barre>(cible.getBarresLiees());
        for (Barre barre : liees) {
            supprimerBarre(world, barre);
        }
        supportsUtilisateur.remove(cible);
        coutsNoeuds.remove(cible);
        if (liaisons.remove(cible)) {
            world.destroyBody(cible.getBody());
        }
        return true;
    }

    private ArrayList<Vec2> echantillonnerLigne(Vec2 debut, Vec2 fin) {
        int segments = Math.max(1,
                (int) Math.ceil(debut.sub(fin).length() / CoutsConstruction.ESPACEMENT_REMPLISSAGE));
        ArrayList<Vec2> points = new ArrayList<Vec2>();
        for (int i = 0; i <= segments; i++) {
            float t = i / (float) segments;
            points.add(new Vec2(debut.x + (fin.x - debut.x) * t, debut.y + (fin.y - debut.y) * t));
        }
        return points;
    }

    private boolean genererChemin(World world, Liaison debut, Liaison fin, ArrayList<Vec2> points,
            Materiau materiau) {
        int segments = points.size() - 1;
        int nouveauxNoeuds = Math.max(0, segments - 1);
        if (getNombreBarres() + segments > limiteBarres) {
            return false;
        }
        int cout = nouveauxNoeuds * CoutsConstruction.NOEUD_GENERE;
        for (int i = 0; i < segments; i++) {
            cout += estimerPrixBarre(materiau, points.get(i).sub(points.get(i + 1)).length());
        }
        if (prix() + cout > budget) {
            return false;
        }

        Liaison precedente = debut;
        for (int i = 1; i < points.size(); i++) {
            Liaison suivante;
            if (i == points.size() - 1) {
                suivante = fin;
            } else {
                LiaisonMobile noeud = new LiaisonMobile(world, points.get(i));
                liaisons.add(noeud);
                coutsNoeuds.put(noeud, CoutsConstruction.NOEUD_GENERE);
                suivante = noeud;
            }
            creerBarreDirecte(world, precedente, suivante, materiau);
            precedente = suivante;
        }
        return true;
    }

    private void creerBarreDirecte(World world, Liaison debut, Liaison fin, Materiau materiau) {
        Barre barre = instancierBarre(world, debut, fin, materiau);
        barres.add(barre);
        barre.activerPhysique();
        debut.activerPhysique();
        fin.activerPhysique();
        barre.lier(world, debut);
        barre.lier(world, fin);
    }

    private Barre instancierBarre(World world, Liaison debut, Liaison fin, Materiau materiau) {
        switch (materiau) {
            case GOUDRON:
                return new BarreGoudron(world, debut, fin);
            case BOIS:
                return new BarreBois(world, debut, fin);
            case ACIER:
                return new BarreAcier(world, debut, fin);
            case ALUMINIUM:
                return new BarreAluminium(world, debut, fin);
            case CARBONE:
            default:
                return new BarreCarbone(world, debut, fin);
        }
    }

    private int estimerPrixBarre(Materiau materiau, float longueur) {
        int base;
        switch (materiau) {
            case GOUDRON:
                base = 3000;
                break;
            case ACIER:
                base = 2000;
                break;
            case ALUMINIUM:
                base = 1600;
                break;
            case CARBONE:
                base = 3200;
                break;
            case BOIS:
            default:
                base = 1000;
                break;
        }
        return Math.round(longueur / Barre.LONGUEUR_MAX * base);
    }

    private Liaison recupLiaisonProchePixel(Box2D box2d, int pixelX, int pixelY) {
        Liaison meilleure = null;
        int distanceMin = Integer.MAX_VALUE;
        int rayon = Math.max(36, box2d.worldToPixel(Liaison.RAYON * 5f));
        for (Liaison liaison : liaisons) {
            int dx = box2d.worldToPixelX(liaison.getX()) - pixelX;
            int dy = box2d.worldToPixelY(liaison.getY()) - pixelY;
            int d = dx * dx + dy * dy;
            if (d <= rayon * rayon && d < distanceMin) {
                meilleure = liaison;
                distanceMin = d;
            }
        }
        return meilleure;
    }

    private boolean procheToutesLiaisons(Vec2 position, float rayon, Liaison ignorer) {
        for (Liaison liaison : liaisons) {
            if (liaison == ignorer) {
                continue;
            }
            if (liaison.distancePoint(position) < rayon) {
                return true;
            }
        }
        return false;
    }

    private void reinitialiserCourbe() {
        courbeDepart = null;
        courbeArrivee = null;
        courbeControle = null;
        courbePrete = false;
    }

    public int getNombreAncragesUtilisateur() {
        int total = 0;
        for (OutilConstruction type : supportsUtilisateur.values()) {
            if (type == OutilConstruction.ANCRAGE) {
                total++;
            }
        }
        return total;
    }

    public int getNombrePiliers() {
        int total = 0;
        for (OutilConstruction type : supportsUtilisateur.values()) {
            if (type == OutilConstruction.PILIER) {
                total++;
            }
        }
        return total;
    }

    public int getNombreNoeudsGeneres() {
        int total = 0;
        for (Map.Entry<Liaison, Integer> entree : coutsNoeuds.entrySet()) {
            if (!supportsUtilisateur.containsKey(entree.getKey())) {
                total++;
            }
        }
        return total;
    }

    public int getCoutSupports() {
        int total = 0;
        for (int cout : coutsNoeuds.values()) {
            total += cout;
        }
        return total;
    }

    private void dessinerSupportsUtilisateur(Graphics2D g, Box2D box2d) {
        for (Map.Entry<Liaison, OutilConstruction> entree : supportsUtilisateur.entrySet()) {
            Liaison liaison = entree.getKey();
            int x = box2d.worldToPixelX(liaison.getX());
            int y = box2d.worldToPixelY(liaison.getY());
            if (entree.getValue() == OutilConstruction.PILIER) {
                g.setColor(Color.decode("#8b7968"));
                g.fillRoundRect(x - 13, y, 26, Math.max(80, box2d.getHauteurPixels() - y), 8, 8);
                g.setColor(Color.decode("#51473e"));
                g.drawRoundRect(x - 13, y, 26, Math.max(80, box2d.getHauteurPixels() - y), 8, 8);
            } else {
                g.setColor(Color.decode("#f08b35"));
                g.setStroke(new BasicStroke(4f));
                g.drawOval(x - 13, y - 13, 26, 26);
            }
        }
    }

    private void dessinerApercuOutil(Graphics2D g, Box2D box2d) {
        if (outil == OutilConstruction.ANCRAGE || outil == OutilConstruction.PILIER) {
            int x = box2d.worldToPixelX(positionOutil.x);
            int y = box2d.worldToPixelY(positionOutil.y);
            g.setColor(positionOutilValide ? new Color(70, 190, 105, 150) : new Color(220, 65, 55, 150));
            if (outil == OutilConstruction.PILIER) {
                g.fillRoundRect(x - 13, y, 26, 100, 8, 8);
            } else {
                g.fillOval(x - 15, y - 15, 30, 30);
            }
        }
        if (outil == OutilConstruction.LIGNE && selectionOutil != null) {
            dessinerLignePointillee(g, box2d, selectionOutil.getPos(), positionOutil);
        }
        if (outil == OutilConstruction.COURBE && courbeDepart != null) {
            Vec2 controle = courbeControle == null ? positionOutil : courbeControle;
            Vec2 fin = courbeArrivee == null ? positionOutil : courbeArrivee.getPos();
            Vec2 precedent = courbeDepart.getPos();
            for (int i = 1; i <= 32; i++) {
                float t = i / 32f;
                float u = 1f - t;
                Vec2 point = new Vec2(
                        u * u * courbeDepart.getX() + 2f * u * t * controle.x + t * t * fin.x,
                        u * u * courbeDepart.getY() + 2f * u * t * controle.y + t * t * fin.y);
                dessinerLignePointillee(g, box2d, precedent, point);
                precedent = point;
            }
        }
    }

    private void dessinerLignePointillee(Graphics2D g, Box2D box2d, Vec2 debut, Vec2 fin) {
        Stroke ancien = g.getStroke();
        g.setColor(new Color(55, 180, 105, 190));
        g.setStroke(new BasicStroke(2.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND,
                10f, new float[] { 8f, 6f }, 0f));
        g.drawLine(box2d.worldToPixelX(debut.x), box2d.worldToPixelY(debut.y),
                box2d.worldToPixelX(fin.x), box2d.worldToPixelY(fin.y));
        g.setStroke(ancien);
    }

    private void dessinerZoneConstruction(Graphics2D g, Box2D box2d) {
        Vec2 origine = barreEnCreation.getLiaisonsLiees().get(0).getPos();
        Vec2 cible = liaisonEnCreation.getPos();
        int x = box2d.worldToPixelX(origine.x);
        int y = box2d.worldToPixelY(origine.y);
        int rayon = box2d.worldToPixel(Barre.LONGUEUR_MAX);
        int cibleX = box2d.worldToPixelX(cible.x);
        int cibleY = box2d.worldToPixelY(cible.y);

        Stroke ancienTrait = g.getStroke();
        Color couleur = dernierPlacementValide ? Color.decode("#59c36a") : Color.decode("#f0a33a");
        g.setColor(new Color(couleur.getRed(), couleur.getGreen(), couleur.getBlue(), 28));
        g.fillOval(x - rayon, y - rayon, rayon * 2, rayon * 2);
        g.setColor(new Color(couleur.getRed(), couleur.getGreen(), couleur.getBlue(), 205));
        g.setStroke(new BasicStroke(2.2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND,
                10f, new float[] { 9f, 7f }, 0f));
        g.drawOval(x - rayon, y - rayon, rayon * 2, rayon * 2);
        g.setStroke(new BasicStroke(2f));
        g.drawLine(x, y, cibleX, cibleY);
        g.fillOval(cibleX - 5, cibleY - 5, 10, 10);
        g.setStroke(ancienTrait);
    }

}
