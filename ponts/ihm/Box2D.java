package ponts.ihm;

import org.jbox2d.common.Vec2;

/**
 * Singleton intialisé au début du jeu, qui gère les conversions entre les
 * coordonnées en pixels de l'IHM, aux float (mètres) de la simulation physiques
 */
public class Box2D {

    private Fenetre fenetre;
    private int largeurPixelsFixe = -1;
    private int hauteurPixelsFixe = -1;

    private static final float LARGEUR_VUE = 100f;
    private float largeur;
    private float hauteur;
    private float cameraX;

    /**
     * Constructeur de l'objet Box2D
     * 
     * @param fenetre
     */
    public Box2D(Fenetre fenetre) {
        this.fenetre = fenetre;
        initialiser();
    }

    /**
     * Constructeur sans fenêtre pour les tests de conversion souris/direction.
     */
    public Box2D(int largeurPixels, int hauteurPixels) {
        this.fenetre = null;
        this.largeurPixelsFixe = largeurPixels;
        this.hauteurPixelsFixe = hauteurPixels;
        initialiser();
    }

    private void initialiser() {
        largeur = 180f;
        hauteur = getHauteurPixels() * coeff();
        cameraX = 0f;
    }

    /**
     * Calcul du coefficient du nombre de mètres par pixel
     * 
     * @return coefficient
     */
    private float coeff() {
        return LARGEUR_VUE / getLargeurPixels();
    }

    /**
     * Convertit une distance en pixels en mètres
     * Des fonctions spécifiques sont définies pour une coordonnées en X et en Y
     * 
     * @param scalaire
     * @return distance en mètres
     */
    public float pixelToWorld(int scalaire) {
        return coeff() * scalaire;
    }

    public float pixelToWorldX(int xP) {
        return pixelToWorld(xP) + cameraX;
    }

    public float pixelToWorldY(int yP) {
        return pixelToWorld(getHauteurPixels() - yP); // l'axe y de l'IHM et de la simulation sont inversés
    }

    public Vec2 pixelToWorld(int xP, int yP) {
        return new Vec2(pixelToWorld(xP), pixelToWorldY(yP));
    }

    /**
     * Convertit une distance en mètres en pixels
     * Des fonctions spécifiques sont définies pour une coordonnées en X et en Y
     * 
     * @param scalaire
     * @return distance en pixels
     */
    public int worldToPixel(float scalaire) {
        return Math.round(scalaire / coeff());
    }

    public int worldToPixelX(float x) {
        return worldToPixel(x - cameraX);
    }

    public int worldToPixelY(float y) {
        return getHauteurPixels() - worldToPixel(y);
    }

    public float getLargeur() {
        return largeur;
    }

    public float getHauteur() {
        return hauteur;
    }

    public int getLargeurPixels() {
        return largeurPixelsFixe > 0 ? largeurPixelsFixe : fenetre.getWidth();
    }

    public int getHauteurPixels() {
        return hauteurPixelsFixe > 0 ? hauteurPixelsFixe : fenetre.getHeight();
    }

    public float getCameraX() {
        return cameraX;
    }

    public void setCameraX(float cameraX) {
        this.cameraX = Math.max(0f, Math.min(largeur - LARGEUR_VUE, cameraX));
    }

    public void deplacerCameraPixels(int deltaPixels) {
        setCameraX(cameraX - pixelToWorld(deltaPixels));
    }

}
