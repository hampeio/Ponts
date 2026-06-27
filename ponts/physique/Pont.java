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
    private Liaison selectionOutilFin;
    private Liaison supportEnDeplacement;
    private Liaison courbeDepart;
    private Liaison courbeArrivee;
    private ArrayList<Vec2> courbeControles = new ArrayList<Vec2>();
    private boolean courbePrete;
    private int poigneeCourbeActive = -1;
    private String messageOutil = "请选择工具";
    private LinkedList<Barre> dernierLotBarres = new LinkedList<Barre>();
    private LinkedList<Liaison> dernierLotNoeuds = new LinkedList<Liaison>();

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
                    if (barreEnCreation == null && liaisonProche != null && peutAjouterBarres(1)) {
                        creerBarre(world, posSouris, liaisonProche, materiau);
                        posSourisMax = posSourisMax(barreEnCreation, posSouris);
                        majPreview(posSourisMax);
                    } else if (barreEnCreation == null && liaisonProche != null && limiteBarres > 0) {
                        messageOutil = "已达到本关最大构件数量：" + limiteBarres;
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
        if (barre == null || barre.inferieurLongeurMax(posSouris)) {
            return posSouris;
        }
        return barre.posLiaisonMax(posSouris);
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

    private boolean peutAjouterBarres(int quantite) {
        return limiteBarres <= 0 || getNombreBarres() + quantite <= limiteBarres;
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
        selectionOutilFin = null;
        supportEnDeplacement = null;
        reinitialiserCourbe();
        messageOutil = instructionsOutil(outil);
    }

    public OutilConstruction getOutil() {
        return outil;
    }

    public void actualiserOutil(Box2D box2d, Bord bord, int pixelX, int pixelY) {
        actualiserOutil(box2d.pixelToWorld(pixelX, pixelY), box2d, bord);
    }

    public void actualiserOutil(Vec2 positionSourisMonde, Box2D box2d, Bord bord) {
        positionOutil = positionSourisMonde.clone();
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
        clicOutil(world, box2d.pixelToWorld(pixelX, pixelY), box2d, bord,
                pixelX, pixelY, bouton, materiau);
    }

    public void clicOutil(World world, Vec2 positionSourisMonde, Box2D box2d, Bord bord,
            int pixelX, int pixelY, int bouton, Materiau materiau) {
        actualiserOutil(positionSourisMonde, box2d, bord);

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
                clicLigne(world, materiau);
                break;
            case COURBE:
                clicCourbe();
                break;
            case DEPLACER:
                clicDeplacer();
                break;
            case BARRE:
            default:
                break;
        }
    }

    public boolean confirmerOutil(World world, Materiau materiau) {
        if (outil == OutilConstruction.LIGNE) {
            if (selectionOutil == null || selectionOutilFin == null) {
                messageOutil = "请先选择直线的起点和终点";
                return false;
            }
            ArrayList<Vec2> points = echantillonnerLigne(selectionOutil.getPos(), selectionOutilFin.getPos());
            boolean ok = genererChemin(world, selectionOutil, selectionOutilFin, points, materiau);
            if (ok) {
                selectionOutil = null;
                selectionOutilFin = null;
                messageOutil = "直线填充已生成";
            }
            return ok;
        }
        if (outil != OutilConstruction.COURBE || !courbePrete) {
            messageOutil = "请先选择贝塞尔曲线的起点和终点";
            return false;
        }
        ArrayList<Vec2> points = echantillonnerCourbe(courbeDepart.getPos(), courbeControles,
                courbeArrivee.getPos());
        boolean ok = genererChemin(world, courbeDepart, courbeArrivee, points, materiau);
        if (ok) {
            reinitialiserCourbe();
            messageOutil = "曲线填充已生成";
        }
        return ok;
    }

    public void annulerOutil() {
        selectionOutil = null;
        selectionOutilFin = null;
        supportEnDeplacement = null;
        reinitialiserCourbe();
        messageOutil = "当前操作已取消";
    }

    public void annulerOutil(World world) {
        boolean operationEnCours = selectionOutil != null || courbeDepart != null || supportEnDeplacement != null;
        if (operationEnCours) {
            annulerOutil();
            return;
        }
        if (!dernierLotBarres.isEmpty()) {
            LinkedList<Barre> copie = new LinkedList<Barre>(dernierLotBarres);
            for (Barre barre : copie) {
                if (barres.contains(barre)) {
                    supprimerBarre(world, barre);
                }
            }
            dernierLotBarres.clear();
            dernierLotNoeuds.clear();
            messageOutil = "已撤销上一次自动填充";
        } else {
            messageOutil = "没有可撤销的自动填充";
        }
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

    private void clicLigne(World world, Materiau materiau) {
        Liaison proche = recupLiaisonProche(positionOutil);
        if (proche == null) {
            messageOutil = "直线端点必须选择已有节点";
            return;
        }
        if (selectionOutil == null) {
            selectionOutil = proche;
            messageOutil = "已选择起点，请选择终点";
        } else if (selectionOutil != proche) {
            selectionOutilFin = proche;
            messageOutil = "直线预览完成，点击“确认”生成";
        } else {
            messageOutil = "终点不能与起点相同";
        }
    }

    private void clicCourbe() {
        if (courbeDepart == null) {
            courbeDepart = recupLiaisonProche(positionOutil);
            messageOutil = courbeDepart == null ? "曲线起点必须选择已有节点"
                    : "已选择起点，请选择终点节点";
            return;
        }
        if (courbeArrivee == null) {
            Liaison proche = recupLiaisonProche(positionOutil);
            if (proche == null || proche == courbeDepart) {
                messageOutil = "曲线终点必须选择另一个已有节点";
                return;
            }
            courbeArrivee = proche;
            Vec2 debut = courbeDepart.getPos();
            Vec2 fin = courbeArrivee.getPos();
            Vec2 difference = fin.sub(debut);
            courbeControles.clear();
            courbeControles.add(debut.add(difference.mul(1f / 3f)));
            courbeControles.add(debut.add(difference.mul(2f / 3f)));
            courbePrete = true;
            messageOutil = "拖动蓝色手柄调整贝塞尔曲线，完成后点击“确认”";
            return;
        }
        for (int i = 0; i < courbeControles.size(); i++) {
            if (courbeControles.get(i).sub(positionOutil).length() <= 2.5f) {
                poigneeCourbeActive = i;
                messageOutil = "正在拖动控制手柄 " + (i + 1);
                return;
            }
        }
        messageOutil = "请拖动蓝色控制手柄，或点击“确认”生成";
    }

    public void glisserOutil(Vec2 positionSourisMonde) {
        if (outil == OutilConstruction.COURBE && poigneeCourbeActive >= 0
                && poigneeCourbeActive < courbeControles.size()) {
            courbeControles.set(poigneeCourbeActive, positionSourisMonde.clone());
            positionOutil = positionSourisMonde.clone();
            messageOutil = "控制手柄 " + (poigneeCourbeActive + 1) + "：" + Math.round(positionSourisMonde.x)
                    + ", " + Math.round(positionSourisMonde.y);
        }
    }

    public void relacherOutil() {
        if (poigneeCourbeActive >= 0) {
            poigneeCourbeActive = -1;
            messageOutil = "贝塞尔曲线已调整，点击“确认”生成";
        }
    }

    private void clicDeplacer() {
        if (supportEnDeplacement == null) {
            Liaison candidat = recupLiaisonProche(positionOutil);
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

    /**
     * 三次贝塞尔：P0 为起点，P1/P2 为手柄，P3 为终点。手柄只影响曲线，
     * 不会成为最终物理节点。
     */
    private ArrayList<Vec2> echantillonnerCourbe(Vec2 debut, ArrayList<Vec2> controles, Vec2 fin) {
        ArrayList<Vec2> dense = new ArrayList<Vec2>();
        Vec2 p1 = controles.size() > 0 ? controles.get(0) : debut;
        Vec2 p2 = controles.size() > 1 ? controles.get(1) : fin;
        for (int i = 0; i <= 64; i++) {
            float t = i / 64f;
            float u = 1f - t;
            float x = u * u * u * debut.x + 3f * u * u * t * p1.x
                    + 3f * u * t * t * p2.x + t * t * t * fin.x;
            float y = u * u * u * debut.y + 3f * u * u * t * p1.y
                    + 3f * u * t * t * p2.y + t * t * t * fin.y;
            dense.add(new Vec2(x, y));
        }
        return reechantillonnerEspacementFixe(dense, CoutsConstruction.ESPACEMENT_REMPLISSAGE);
    }

    private ArrayList<Vec2> reechantillonnerEspacementFixe(ArrayList<Vec2> dense, float espacement) {
        ArrayList<Vec2> resultat = new ArrayList<Vec2>();
        Vec2 dernierEchantillon = dense.get(0).clone();
        resultat.add(dernierEchantillon.clone());
        float restant = espacement;

        for (int i = 1; i < dense.size(); i++) {
            Vec2 debutSegment = dense.get(i - 1).clone();
            Vec2 finSegment = dense.get(i);
            Vec2 direction = finSegment.sub(debutSegment);
            float longueur = direction.length();
            if (longueur <= 0.0001f) {
                continue;
            }
            direction.mulLocal(1f / longueur);
            while (longueur >= restant) {
                debutSegment = debutSegment.add(direction.mul(restant));
                resultat.add(debutSegment.clone());
                longueur -= restant;
                restant = espacement;
            }
            restant -= longueur;
        }
        Vec2 fin = dense.get(dense.size() - 1);
        if (resultat.get(resultat.size() - 1).sub(fin).length() > 0.01f) {
            resultat.add(fin.clone());
        }
        return resultat;
    }

    private boolean genererChemin(World world, Liaison debut, Liaison fin, ArrayList<Vec2> points,
            Materiau materiau) {
        int segments = points.size() - 1;
        int nouveauxNoeuds = Math.max(0, segments - 1);
        if (!peutAjouterBarres(segments)) {
            messageOutil = "杆件额度不足：需要 " + segments + "，剩余 "
                    + Math.max(0, limiteBarres - getNombreBarres());
            return false;
        }
        int cout = nouveauxNoeuds * CoutsConstruction.NOEUD_GENERE;
        for (int i = 0; i < segments; i++) {
            cout += estimerPrixBarre(materiau, points.get(i).sub(points.get(i + 1)).length());
        }
        if (prix() + cout > budget) {
            messageOutil = "预算不足：本次需要 " + cout + " 元，剩余 "
                    + Math.max(0, budget - prix()) + " 元";
            return false;
        }

        dernierLotBarres.clear();
        dernierLotNoeuds.clear();
        Liaison precedente = debut;
        for (int i = 1; i < points.size(); i++) {
            Liaison suivante;
            if (i == points.size() - 1) {
                suivante = fin;
            } else {
                LiaisonMobile noeud = new LiaisonMobile(world, points.get(i));
                liaisons.add(noeud);
                coutsNoeuds.put(noeud, CoutsConstruction.NOEUD_GENERE);
                dernierLotNoeuds.add(noeud);
                suivante = noeud;
            }
            dernierLotBarres.add(creerBarreDirecte(world, precedente, suivante, materiau));
            precedente = suivante;
        }
        messageOutil = "已生成 " + segments + " 根构件和 " + nouveauxNoeuds + " 个节点";
        return true;
    }

    private Barre creerBarreDirecte(World world, Liaison debut, Liaison fin, Materiau materiau) {
        Barre barre = instancierBarre(world, debut, fin, materiau);
        barres.add(barre);
        barre.activerPhysique();
        debut.activerPhysique();
        fin.activerPhysique();
        barre.lier(world, debut);
        barre.lier(world, fin);
        return barre;
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
        courbeControles.clear();
        courbePrete = false;
        poigneeCourbeActive = -1;
    }

    private String instructionsOutil(OutilConstruction outil) {
        switch (outil) {
            case LIGNE:
                return "请选择直线起点";
            case COURBE:
                return "请选择曲线起点";
            case ANCRAGE:
                return "在地形表面放置锚点";
            case PILIER:
                return "在沟谷区域放置桥墩";
            case DEPLACER:
                return "选择用户创建的节点进行移动";
            case BARRE:
            default:
                return "选择节点开始建造构件";
        }
    }

    public String getMessageOutil() {
        return messageOutil;
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

    public Vec2 getPositionOutil() {
        return positionOutil.clone();
    }

    public ArrayList<Vec2> getPositionsSupportsUtilisateur() {
        ArrayList<Vec2> positions = new ArrayList<Vec2>();
        for (Liaison liaison : supportsUtilisateur.keySet()) {
            positions.add(liaison.getPos().clone());
        }
        return positions;
    }

    public ArrayList<Vec2> getPoigneesCourbe() {
        ArrayList<Vec2> poignees = new ArrayList<Vec2>();
        for (Vec2 controle : courbeControles) {
            poignees.add(controle.clone());
        }
        return poignees;
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
            Vec2 fin = selectionOutilFin == null ? positionOutil : selectionOutilFin.getPos();
            ArrayList<Vec2> points = echantillonnerLigne(selectionOutil.getPos(), fin);
            dessinerCheminApercu(g, box2d, points);
        }
        if (outil == OutilConstruction.COURBE && courbeDepart != null) {
            Vec2 fin = courbeArrivee == null ? positionOutil : courbeArrivee.getPos();
            ArrayList<Vec2> points = echantillonnerCourbe(courbeDepart.getPos(), courbeControles, fin);
            dessinerCheminApercu(g, box2d, points);
            g.setColor(new Color(45, 135, 220, 190));
            if (courbeControles.size() == 2 && courbeArrivee != null) {
                Stroke ancien = g.getStroke();
                g.setStroke(new BasicStroke(1.8f));
                g.drawLine(box2d.worldToPixelX(courbeDepart.getX()), box2d.worldToPixelY(courbeDepart.getY()),
                        box2d.worldToPixelX(courbeControles.get(0).x),
                        box2d.worldToPixelY(courbeControles.get(0).y));
                g.drawLine(box2d.worldToPixelX(courbeArrivee.getX()), box2d.worldToPixelY(courbeArrivee.getY()),
                        box2d.worldToPixelX(courbeControles.get(1).x),
                        box2d.worldToPixelY(courbeControles.get(1).y));
                g.setStroke(ancien);
            }
            for (Vec2 controle : courbeControles) {
                int x = box2d.worldToPixelX(controle.x);
                int y = box2d.worldToPixelY(controle.y);
                g.fillOval(x - 8, y - 8, 16, 16);
                g.setColor(Color.WHITE);
                g.drawOval(x - 8, y - 8, 16, 16);
                g.setColor(new Color(45, 135, 220, 190));
            }
        }
    }

    private void dessinerCheminApercu(Graphics2D g, Box2D box2d, ArrayList<Vec2> points) {
        for (int i = 1; i < points.size(); i++) {
            dessinerLignePointillee(g, box2d, points.get(i - 1), points.get(i));
            int x = box2d.worldToPixelX(points.get(i).x);
            int y = box2d.worldToPixelY(points.get(i).y);
            g.setColor(new Color(55, 180, 105, 170));
            g.fillOval(x - 4, y - 4, 8, 8);
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
