package ponts.ihm;

import java.awt.BorderLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.Cursor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.MouseInputListener;

import org.jbox2d.common.Vec2;

import ponts.niveau.Niveau;

/**
 * Classe de l'éditeur de niveaux
 */
public class Editeur extends JPanel implements ActionListener, MouseInputListener {

    private Fenetre fenetre;
    private Box2D box2d;
    private Niveau niveau;

    private JButton boutonSauvegarder;
    private JButton boutonCharger;
    private JButton boutonSupprimer;
    private JTextField champNomNiveau;
    private JTextField champBudget;
    private JButton boutonAnnuler;
    private JButton boutonEffacer;
    private JButton boutonJeu;
    private int dernierXGlisser;
    private boolean cameraEnGlissement;

    /**
     * Constructeur de l'éditeur
     * 
     * @param fenetre
     * @param box2d
     * @param refreshRate
     */
    public Editeur(Fenetre fenetre, Box2D box2d, int refreshRate) {

        this.fenetre = fenetre;
        this.box2d = box2d;
        ihm();

        niveau = new Niveau();

        addMouseListener(this);
        addMouseMotionListener(this);
    }

    /**
     * Méthode créeant tous les composants de l'IHM
     */
    private void ihm() {

        this.setLayout(new BorderLayout());
        this.setOpaque(false);

        JPanel ligneHaut = new Ligne(box2d.getLargeurPixels() / 20, box2d.getHauteurPixels() / 100);
        Theme.skinPanel(ligneHaut);
        this.add(ligneHaut, BorderLayout.PAGE_START);

        JPanel colonneFichier = new Colonne();
        Theme.skinPanel(colonneFichier);
        ligneHaut.add(colonneFichier);
        JLabel texteFichier = new JLabel("文件");
        colonneFichier.add(texteFichier);
        JPanel ligneFichier = new Ligne();
        Theme.skinPanel(ligneFichier);
        colonneFichier.add(ligneFichier);
        boutonSauvegarder = new JButton("保存");
        Theme.skinButton(boutonSauvegarder, Theme.icon(Theme.Symbol.SAVE));
        boutonSauvegarder.addActionListener(this);
        ligneFichier.add(boutonSauvegarder);
        boutonCharger = new JButton("加载");
        Theme.skinButton(boutonCharger, Theme.icon(Theme.Symbol.LOAD));
        boutonCharger.addActionListener(this);
        ligneFichier.add(boutonCharger);
        boutonSupprimer = new JButton("删除");
        Theme.skinButton(boutonSupprimer, Theme.icon(Theme.Symbol.DELETE));
        boutonSupprimer.addActionListener(this);
        ligneFichier.add(boutonSupprimer);

        JPanel colonneNom = new Colonne();
        Theme.skinPanel(colonneNom);
        ligneHaut.add(colonneNom);
        JLabel texteNom = new JLabel("名称");
        colonneNom.add(texteNom);
        JPanel ligneNom = new Ligne();
        Theme.skinPanel(ligneNom);
        colonneNom.add(ligneNom);
        champNomNiveau = new JTextField(6);
        ligneNom.add(champNomNiveau);

        JPanel colonneBudget = new Colonne();
        Theme.skinPanel(colonneBudget);
        ligneHaut.add(colonneBudget);
        JLabel texteBudget = new JLabel("预算");
        colonneBudget.add(texteBudget);
        JPanel ligneBudget = new Ligne();
        Theme.skinPanel(ligneBudget);
        colonneBudget.add(ligneBudget);
        champBudget = new JTextField("0", 5);
        ligneBudget.add(champBudget);

        JPanel colonneCreation = new Colonne();
        Theme.skinPanel(colonneCreation);
        ligneHaut.add(colonneCreation);
        JLabel texteCreation = new JLabel("编辑");
        colonneCreation.add(texteCreation);
        JPanel ligneCreation = new Ligne();
        Theme.skinPanel(ligneCreation);
        colonneCreation.add(ligneCreation);
        boutonAnnuler = new JButton("撤销");
        Theme.skinButton(boutonAnnuler, Theme.icon(Theme.Symbol.UNDO));
        boutonAnnuler.addActionListener(this);
        ligneCreation.add(boutonAnnuler);
        boutonEffacer = new JButton("清空");
        Theme.skinButton(boutonEffacer, Theme.icon(Theme.Symbol.CLEAR));
        boutonEffacer.addActionListener(this);
        ligneCreation.add(boutonEffacer);

        JPanel colonneJeu = new Colonne();
        Theme.skinPanel(colonneJeu);
        ligneHaut.add(colonneJeu);
        JLabel texteJeu = new JLabel("游戏");
        colonneJeu.add(texteJeu);
        JPanel ligneJeu = new Ligne();
        Theme.skinPanel(ligneJeu);
        colonneJeu.add(ligneJeu);
        boutonJeu = new JButton("返回游戏");
        Theme.skinButton(boutonJeu, Theme.icon(Theme.Symbol.GAME));
        boutonJeu.addActionListener(this);
        ligneJeu.add(boutonJeu);

        JPanel bas = new Ligne(box2d.getLargeurPixels() / 20, box2d.getHauteurPixels() / 50);
        Theme.skinPanel(bas);
        this.add(bas, BorderLayout.PAGE_END);

        JPanel colonnePoint = new Colonne();
        Theme.skinPanel(colonnePoint);
        bas.add(colonnePoint);
        JLabel pointCommande = new JLabel("鼠标左键：");
        colonnePoint.add(pointCommande);
        JLabel point = new JLabel("添加地形点");
        colonnePoint.add(point);

        JPanel colonneLiaison = new Colonne();
        Theme.skinPanel(colonneLiaison);
        bas.add(colonneLiaison);
        JLabel liaisonCommande = new JLabel("鼠标右键：");
        colonneLiaison.add(liaisonCommande);
        JLabel liaison = new JLabel("添加锚点");
        colonneLiaison.add(liaison);

        JPanel colonneCamera = new Colonne();
        Theme.skinPanel(colonneCamera);
        bas.add(colonneCamera);
        colonneCamera.add(new JLabel("鼠标中键："));
        colonneCamera.add(new JLabel("拖动视角"));
    }

    /**
     * Méthode paint, pour mettre à jour l'affichage de l'éditeur
     */
    @Override
    protected void paintComponent(Graphics g0) {
        Graphics2D g = (Graphics2D) g0;
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        Toolkit.getDefaultToolkit().sync();

        Theme.drawBackdrop(g, this);

        niveau.dessiner(g, box2d);

    }

    /**
     * Gère les évenements des boutons
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        Object source = e.getSource();

        if (source == boutonSauvegarder) {
            niveau.sauvegarder(fenetre, nomNiveau(), champBudget.getText());
        }
        if (source == boutonCharger) {
            Niveau niveauChargee = Niveau.charger(fenetre, nomNiveau());
            if (niveauChargee != null) {
                niveau = niveauChargee;
            }
            champBudget.setText(Integer.toString(niveau.getBudget()));
        }
        if (source == boutonSupprimer) {
            Niveau.supprimer(fenetre, nomNiveau());
        }
        if (source == boutonAnnuler) {
            niveau.undo();
        }
        if (source == boutonEffacer) {
            niveau = new Niveau();
        }
        if (source == boutonJeu) {
            fenetre.lancerJeu();
        }
        repaint();
    }

    /**
     * Renvoie le nom du niveau
     * 
     * @return nom du niveau
     */
    private String nomNiveau() {
        return champNomNiveau.getText();
    }

    @Override
    public void mouseClicked(MouseEvent e) {
    }

    @Override
    public void mouseEntered(MouseEvent e) {
    }

    @Override
    public void mouseExited(MouseEvent e) {
    }

    @Override
    public void mousePressed(MouseEvent e) {
        if (e.getButton() == MouseEvent.BUTTON2) {
            cameraEnGlissement = true;
            dernierXGlisser = e.getX();
            setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
            return;
        }
        Vec2 posSouris = box2d.pixelToWorld(e.getX(), e.getY());
        switch (e.getButton()) {
            case 1: // clic gauche
                niveau.ajouterPoint(posSouris);
                break;
            case 3: // clic droit
                niveau.ajouterLiaison(posSouris);
                break;
        }
        repaint();
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        if (e.getButton() == MouseEvent.BUTTON2) {
            cameraEnGlissement = false;
            setCursor(Cursor.getDefaultCursor());
        }
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        if (cameraEnGlissement) {
            int delta = e.getX() - dernierXGlisser;
            box2d.deplacerCameraPixels(delta);
            dernierXGlisser = e.getX();
            repaint();
        }
    }

    @Override
    public void mouseMoved(MouseEvent e) {
    }

}
