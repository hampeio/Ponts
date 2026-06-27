package ponts.physique.environnement;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.Shape;
import java.util.LinkedList;

import org.jbox2d.collision.shapes.EdgeShape;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.BodyDef;
import org.jbox2d.dynamics.BodyType;
import org.jbox2d.dynamics.FixtureDef;
import org.jbox2d.dynamics.World;

import ponts.ihm.Box2D;
import ponts.niveau.Niveau;
import ponts.physique.ObjetPhysique;
import ponts.physique.barres.Barre;
import ponts.physique.liaisons.Liaison;
import ponts.physique.voiture.Voiture;

/**
 * Classe des bords du niveau, c'est à dire la berge sur laquelle il faut
 * construire le pont
 */
public class Bord extends ObjetPhysique {

    public static final int CATEGORY = 0b0001;
    public static final int MASK = Voiture.CATEGORY | Barre.CATEGORY | Liaison.CATEGORY;

    private Color couleurContour = Color.decode("#704626");

    private LinkedList<Vec2> posCoins;
    private LinkedList<Vec2> posAncrages;

    /**
     * Constructeur d'un objet bord
     * 
     * @param world
     * @param niveau
     */
    public Bord(World world, Niveau niveau) {
        posCoins = niveau.getPosCoins();
        posAncrages = niveau.getPosLiaisons();
        creerObjetPhysique(world);

    }

    @Override
    protected void creerObjetPhysique(World world) {
        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyType.STATIC;

        FixtureDef fixtureDef = creerFixtureDef(FRICTION, ELASTICITE, DENSITE, CATEGORY, MASK);

        Vec2 prevPosCoin = null;
        for (Vec2 posCoin : posCoins) {

            if (prevPosCoin != null) {
                Body body = world.createBody(bodyDef);
                EdgeShape shape = new EdgeShape();
                shape.set(prevPosCoin, posCoin);
                fixtureDef.shape = shape;
                body.createFixture(fixtureDef);

            }
            prevPosCoin = posCoin;

        }
    }

    /**
     * Détermine si un point est dans le polygone défini par le bord
     * 
     * @param pos
     * @return boolean
     */
    public boolean estDansBord(Vec2 pos) {
        Polygon polygon = new Polygon();
        for (Vec2 posCoin : posCoins) {
            int x = Math.round(posCoin.x * 1000);
            int y = Math.round(posCoin.y * 1000);
            polygon.addPoint(x, y);
        }
        int x = Math.round(pos.x * 1000);
        int y = Math.round(pos.y * 1000);
        return polygon.contains(x, y);
    }

    /**
     * Dessine la berge
     * 
     * @param g
     * @param box2d
     */
    public void dessiner(Graphics2D g, Box2D box2d) {
        Polygon polygon = new Polygon();
        for (Vec2 posCoin : posCoins) {
            int x = box2d.worldToPixelX(posCoin.x);
            int y = box2d.worldToPixelY(posCoin.y);
            polygon.addPoint(x, y);
        }
        Shape ancienClip = g.getClip();
        g.setClip(polygon);

        // Terre et roche chaude, dans la même palette que le décor de canyon.
        int haut = polygon.getBounds().y;
        int bas = polygon.getBounds().y + polygon.getBounds().height;
        g.setPaint(new GradientPaint(0, haut, Color.decode("#c98a43"),
                0, bas, Color.decode("#76502f")));
        g.fillPolygon(polygon);

        // Fines strates rocheuses pour éviter l'ancien aplat vert.
        g.setStroke(new BasicStroke(2f));
        g.setColor(new Color(108, 68, 37, 90));
        for (int y = haut + 34; y < bas; y += 42) {
            g.drawLine(0, y, box2d.getLargeurPixels(), y + 12);
        }

        dessinerHerbe(g, box2d);
        dessinerPlateformesEtRochers(g, box2d);

        g.setClip(ancienClip);
        g.setStroke(new BasicStroke(2f));
        g.setColor(couleurContour);
        g.drawPolygon(polygon);

    }

    /**
     * Dessine une bande d'herbe douce le long de la ligne supérieure du terrain.
     */
    private void dessinerHerbe(Graphics2D g, Box2D box2d) {
        g.setStroke(new BasicStroke(Math.max(7f, box2d.worldToPixel(0.65f)),
                BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.setColor(Color.decode("#78bd3f"));
        Vec2 precedent = null;
        for (Vec2 coin : posCoins) {
            if (precedent != null) {
                g.drawLine(box2d.worldToPixelX(precedent.x), box2d.worldToPixelY(precedent.y),
                        box2d.worldToPixelX(coin.x), box2d.worldToPixelY(coin.y));
            }
            precedent = coin;
        }
    }

    /**
     * Les deux ancrages extrêmes deviennent des têtes de pont goudronnées.
     * Les autres ancrages sont enchâssés dans des rochers.
     */
    private void dessinerPlateformesEtRochers(Graphics2D g, Box2D box2d) {
        if (posAncrages == null || posAncrages.isEmpty()) {
            return;
        }

        Vec2 gauche = posAncrages.getFirst();
        Vec2 droite = posAncrages.getLast();
        dessinerRoute(g, box2d, gauche, true);
        if (droite != gauche) {
            dessinerRoute(g, box2d, droite, false);
        }

        for (int i = 1; i < posAncrages.size() - 1; i++) {
            dessinerRocher(g, box2d, posAncrages.get(i));
        }
    }

    private void dessinerRoute(Graphics2D g, Box2D box2d, Vec2 ancrage, boolean gauche) {
        int x = box2d.worldToPixelX(ancrage.x);
        int y = box2d.worldToPixelY(ancrage.y);
        int longueur = Math.max(100, box2d.worldToPixel(12f));
        int epaisseur = Math.max(16, box2d.worldToPixel(1.25f));
        int debut = gauche ? x - longueur * 3 / 4 : x - longueur / 4;

        g.setColor(Color.decode("#414b55"));
        g.fillRoundRect(debut, y - epaisseur / 2, longueur, epaisseur, epaisseur / 2, epaisseur / 2);
        g.setColor(Color.decode("#e9c94a"));
        g.setStroke(new BasicStroke(Math.max(2f, epaisseur / 10f), BasicStroke.CAP_ROUND,
                BasicStroke.JOIN_ROUND));
        int centreY = y;
        for (int px = debut + 12; px < debut + longueur - 12; px += 30) {
            g.drawLine(px, centreY, Math.min(px + 15, debut + longueur - 10), centreY);
        }
    }

    private void dessinerRocher(Graphics2D g, Box2D box2d, Vec2 ancrage) {
        int x = box2d.worldToPixelX(ancrage.x);
        int y = box2d.worldToPixelY(ancrage.y);
        int r = Math.max(18, box2d.worldToPixel(2.2f));

        Polygon rocher = new Polygon();
        rocher.addPoint(x - r, y + r / 2);
        rocher.addPoint(x - r * 2 / 3, y - r / 3);
        rocher.addPoint(x - r / 4, y - r * 3 / 4);
        rocher.addPoint(x + r / 2, y - r / 2);
        rocher.addPoint(x + r, y + r / 3);
        rocher.addPoint(x + r * 2 / 3, y + r);
        rocher.addPoint(x - r / 2, y + r);

        g.setColor(Color.decode("#8d765f"));
        g.fillPolygon(rocher);
        g.setColor(Color.decode("#5f4b3b"));
        g.setStroke(new BasicStroke(2f));
        g.drawPolygon(rocher);
        g.setColor(new Color(220, 193, 157, 150));
        g.drawLine(x - r / 2, y - r / 3, x + r / 3, y - r / 2);
    }
}
