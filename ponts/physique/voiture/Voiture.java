package ponts.physique.voiture;

import java.awt.Graphics;

import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.World;

import ponts.ihm.Box2D;
import ponts.niveau.Niveau;
import ponts.niveau.ConfigurationVoiture;
import ponts.physique.barres.BarreGoudron;
import ponts.physique.environnement.Bord;

/**
 * Classe de la voiture
 */
public class Voiture {

    public enum Charge {
        STANDARD("标准载荷", 1.0f, 0),
        CAISSES("木箱载荷", 1.55f, 1),
        ACIER("钢材载荷", 2.2f, 2);

        public final String nom;
        public final float densite;
        public final int niveauVisuel;

        Charge(String nom, float densite, int niveauVisuel) {
            this.nom = nom;
            this.densite = densite;
            this.niveauVisuel = niveauVisuel;
        }

        @Override
        public String toString() {
            return nom;
        }
    }

    public enum Style {
        CLASSIQUE("经典红", new java.awt.Color(220, 45, 45)),
        OCEAN("活力蓝", new java.awt.Color(35, 125, 220)),
        CHANTIER("工程黄", new java.awt.Color(242, 172, 35));

        public final String nom;
        public final java.awt.Color couleur;

        Style(String nom, java.awt.Color couleur) {
            this.nom = nom;
            this.couleur = couleur;
        }

        @Override
        public String toString() {
            return nom;
        }
    }

    public static final int CATEGORY = 0b0010;
    public static final int MASK = Bord.CATEGORY | BarreGoudron.CATEGORY;

    private Roue roueAvant;
    private Roue roueArriere;
    private Carrosserie carrosserie;

    private boolean arretee = false;
    private boolean arrivee = false;
    private float xDepart;
    private float xArrivee;
    private ConfigurationVoiture config;
    private float boostRestant;
    private float boostCooldownRestant;
    private float volRestant;
    private float volCooldownRestant;
    private boolean volDebloque;
    private float hauteurDepart;
    /**
     * Constructeur d'une voiture
     * 
     * @param world
     * @param niveau
     */
    public Voiture(World world, Niveau niveau, Charge charge, Style style) {
        this(world, niveau, charge, style, false);
    }

    public Voiture(World world, Niveau niveau, Charge charge, Style style, boolean volDebloque) {

        xDepart = niveau.calculerDepart();
        xArrivee = niveau.calculerArrivee();
        config = niveau.getConfigurationVoiture();
        this.volDebloque = volDebloque && config.volAutorise;

        float ecartRoues = 6f;
        Vec2 posRoueAvant = new Vec2(xDepart - ecartRoues, niveau.getPosCoins().get(1).y + Roue.RAYON);
        Vec2 posRoueArriere = posRoueAvant.sub(new Vec2(ecartRoues, 0f));
        Vec2 posCarrosserie = new Vec2(0.5f * (posRoueArriere.x + posRoueAvant.x), posRoueArriere.y + 1.4f);

        carrosserie = new Carrosserie(world, posCarrosserie, charge, style, config.masse);
        roueArriere = new Roue(world, posRoueArriere, config.masse);
        roueAvant = new Roue(world, posRoueAvant, config.masse);
        hauteurDepart = posCarrosserie.y;

        roueArriere.lierVoiture(world, carrosserie, config.vitesseMax, config.acceleration);
        roueAvant.lierVoiture(world, carrosserie, config.vitesseMax, config.acceleration);
        Vec2 vitesseInitiale = new Vec2(config.vitesseInitiale, 0f);
        carrosserie.getBody().setLinearVelocity(vitesseInitiale);
        roueArriere.getBody().setLinearVelocity(vitesseInitiale);
        roueAvant.getBody().setLinearVelocity(vitesseInitiale);

    }

    /**
     * Dessine la voiture
     * 
     * @param g
     * @param box2d
     */
    public void dessiner(Graphics g, Box2D box2d) {
        carrosserie.dessiner(g, box2d);
        roueArriere.dessiner(g, box2d);
        roueAvant.dessiner(g, box2d);
    }

    /**
     * Teste si la voiture est arrivée à la fin du niveau
     * 
     * @return
     */
    public boolean testArrivee() {
        return carrosserie.getBody().getLinearVelocity().x <= 0.001f && arrivee;
    }

    /**
     * Arrête la voiture si elle arrive à la fin où si elle ne bouge plus
     */
    public void arreter() {
        if (arretee) {
            return;
        }
        float vitesse = carrosserie.getBody().getLinearVelocity().length();
        if (roueArriere.getX() > xArrivee) {
            roueArriere.arreter();
            roueAvant.arreter();
            arretee = true;
            arrivee = true;
        } else if (vitesse <= 0.001f && roueArriere.getX() > xDepart) {
            roueArriere.arreter();
            roueAvant.arreter();
            arretee = true;
        }
    }

    public boolean activerBoost() {
        if (!config.boostAutorise || boostCooldownRestant > 0f || arretee) {
            return false;
        }
        boostRestant = config.boostDuree;
        boostCooldownRestant = config.boostCooldown;
        float vitesseBoost = config.vitesseMax * config.boostIntensite;
        float coupleBoost = config.acceleration * config.boostIntensite * 1.5f;
        roueArriere.reglerMoteur(vitesseBoost, coupleBoost);
        roueAvant.reglerMoteur(vitesseBoost, coupleBoost);
        float impulsionVitesse = Math.min(vitesseBoost, config.vitesseMax * 1.65f);
        appliquerVitesseMinimale(carrosserie.getBody(), impulsionVitesse);
        appliquerVitesseMinimale(roueArriere.getBody(), impulsionVitesse);
        appliquerVitesseMinimale(roueAvant.getBody(), impulsionVitesse);
        return true;
    }

    public void tick(float dt) {
        boostCooldownRestant = Math.max(0f, boostCooldownRestant - dt);
        volCooldownRestant = Math.max(0f, volCooldownRestant - dt);

        if (boostRestant > 0f) {
            boostRestant -= dt;
            if (boostRestant <= 0f && !arretee) {
                roueArriere.reglerMoteur(config.vitesseMax, config.acceleration);
                roueAvant.reglerMoteur(config.vitesseMax, config.acceleration);
            }
        }

        if (volRestant > 0f) {
            volRestant -= dt;
            if (carrosserie.getY() < hauteurDepart + config.volHauteurMax) {
                float forceY = config.volPousseeVerticale;
                if (!config.volSubitGravite) {
                    forceY += carrosserie.getBody().getMass() * 9.81f;
                }
                carrosserie.getBody().applyForceToCenter(new Vec2(config.volPousseeHorizontale, forceY));
            }
        }

        float limite = boostRestant > 0f ? config.vitesseMax * config.boostIntensite : config.vitesseMax;
        limiterVitesse(carrosserie.getBody(), limite);
        limiterVitesse(roueArriere.getBody(), limite);
        limiterVitesse(roueAvant.getBody(), limite);
    }

    public boolean boostDisponible() {
        return config.boostAutorise && boostCooldownRestant <= 0f;
    }

    public boolean activerVol() {
        if (!volDebloque || !config.volDeclenchementManuel || volCooldownRestant > 0f || arretee) {
            return false;
        }
        volRestant = config.volDuree;
        volCooldownRestant = config.volCooldown;
        return true;
    }

    public boolean volDisponible() {
        return volDebloque && config.volDeclenchementManuel && volCooldownRestant <= 0f;
    }

    public boolean volEstDebloque() {
        return volDebloque;
    }

    private void limiterVitesse(org.jbox2d.dynamics.Body body, float limite) {
        Vec2 vitesse = body.getLinearVelocity();
        if (vitesse.x > limite) {
            body.setLinearVelocity(new Vec2(limite, vitesse.y));
        } else if (vitesse.x < -limite) {
            body.setLinearVelocity(new Vec2(-limite, vitesse.y));
        }
    }

    private void appliquerVitesseMinimale(org.jbox2d.dynamics.Body body, float vitesseX) {
        Vec2 vitesse = body.getLinearVelocity();
        if (vitesse.x < vitesseX) {
            body.setLinearVelocity(new Vec2(vitesseX, vitesse.y));
        }
    }

    public float getX() {
        return carrosserie.getX();
    }

    public float getY() {
        return carrosserie.getY();
    }

    public float getVitesseX() {
        return carrosserie.getBody().getLinearVelocity().x;
    }

    public float getMasse() {
        return carrosserie.getBody().getMass();
    }

    public boolean objectifAtteint() {
        return carrosserie.getX() >= xArrivee - 1.5f;
    }

}
