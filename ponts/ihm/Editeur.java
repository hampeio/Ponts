package ponts.ihm;

import java.awt.BorderLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.Cursor;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.MouseInputListener;

import org.jbox2d.common.Vec2;

import ponts.niveau.Niveau;
import ponts.niveau.ConfigurationVoiture;

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
    private JTextField champLimiteBarres;
    private JButton boutonAnnuler;
    private JButton boutonEffacer;
    private JButton boutonJeu;
    private JButton boutonParametresVoiture;
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

        JPanel ligneHaut = new Ligne(16, box2d.getHauteurPixels() / 100);
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

        JPanel colonneLimite = new Colonne();
        Theme.skinPanel(colonneLimite);
        ligneHaut.add(colonneLimite);
        colonneLimite.add(new JLabel("最大构件数"));
        JPanel ligneLimite = new Ligne();
        Theme.skinPanel(ligneLimite);
        colonneLimite.add(ligneLimite);
        champLimiteBarres = new JTextField("不限", 5);
        champLimiteBarres.setToolTipText("输入正整数，或输入“不限”");
        ligneLimite.add(champLimiteBarres);

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
        boutonParametresVoiture = new JButton("车辆参数");
        boutonParametresVoiture.addActionListener(this);
        ligneJeu.add(boutonParametresVoiture);

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
            niveau.sauvegarder(fenetre, nomNiveau(), champBudget.getText(), champLimiteBarres.getText());
        }
        if (source == boutonCharger) {
            Niveau niveauChargee = Niveau.charger(fenetre, nomNiveau());
            if (niveauChargee != null) {
                niveau = niveauChargee;
            }
            champBudget.setText(Integer.toString(niveau.getBudget()));
            champLimiteBarres.setText(niveau.getLimiteBarres() <= 0
                    ? "不限" : Integer.toString(niveau.getLimiteBarres()));
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
        if (source == boutonParametresVoiture) {
            ouvrirConfigurationVoiture();
        }
        repaint();
    }

    private void ouvrirConfigurationVoiture() {
        ConfigurationVoiture config = niveau.getConfigurationVoiture();
        JTextField masse = new JTextField(Float.toString(config.masse));
        JTextField vitesseInitiale = new JTextField(Float.toString(config.vitesseInitiale));
        JTextField vitesseMax = new JTextField(Float.toString(config.vitesseMax));
        JTextField acceleration = new JTextField(Float.toString(config.acceleration));
        JCheckBox boostAutorise = new JCheckBox("允许加速", config.boostAutorise);
        JTextField boostCooldown = new JTextField(Float.toString(config.boostCooldown));
        JTextField boostDuree = new JTextField(Float.toString(config.boostDuree));
        JTextField boostIntensite = new JTextField(Float.toString(config.boostIntensite));
        JCheckBox volAutorise = new JCheckBox("允许飞行", config.volAutorise);
        JTextField volCooldown = new JTextField(Float.toString(config.volCooldown));
        JTextField volDuree = new JTextField(Float.toString(config.volDuree));
        JTextField volVertical = new JTextField(Float.toString(config.volPousseeVerticale));
        JTextField volHorizontal = new JTextField(Float.toString(config.volPousseeHorizontale));
        JTextField volHauteur = new JTextField(Float.toString(config.volHauteurMax));
        JCheckBox volGravite = new JCheckBox("飞行时受重力", config.volSubitGravite);
        JCheckBox volManuel = new JCheckBox("允许手动触发", config.volDeclenchementManuel);

        JPanel formulaire = new JPanel(new GridLayout(0, 2, 8, 6));
        ajouterChamp(formulaire, "质量倍率", masse);
        ajouterChamp(formulaire, "初始速度", vitesseInitiale);
        ajouterChamp(formulaire, "最大速度", vitesseMax);
        ajouterChamp(formulaire, "加速度/扭矩", acceleration);
        formulaire.add(boostAutorise);
        formulaire.add(new JLabel());
        ajouterChamp(formulaire, "加速冷却（秒）", boostCooldown);
        ajouterChamp(formulaire, "加速持续（秒）", boostDuree);
        ajouterChamp(formulaire, "加速强度倍率", boostIntensite);
        formulaire.add(volAutorise);
        formulaire.add(volManuel);
        ajouterChamp(formulaire, "飞行冷却（秒）", volCooldown);
        ajouterChamp(formulaire, "飞行持续（秒）", volDuree);
        ajouterChamp(formulaire, "飞行上升力", volVertical);
        ajouterChamp(formulaire, "飞行水平推力", volHorizontal);
        ajouterChamp(formulaire, "最大飞行高度", volHauteur);
        formulaire.add(volGravite);
        formulaire.add(new JLabel());

        if (JOptionPane.showConfirmDialog(fenetre, formulaire, "关卡车辆与技能参数",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE) != JOptionPane.OK_OPTION) {
            return;
        }
        try {
            config.masse = positif(masse);
            config.vitesseInitiale = Float.parseFloat(vitesseInitiale.getText().trim());
            config.vitesseMax = positif(vitesseMax);
            config.acceleration = positif(acceleration);
            config.boostAutorise = boostAutorise.isSelected();
            config.boostCooldown = nonNegatif(boostCooldown);
            config.boostDuree = positif(boostDuree);
            config.boostIntensite = positif(boostIntensite);
            config.volAutorise = volAutorise.isSelected();
            config.volDeclenchementManuel = volManuel.isSelected();
            config.volCooldown = nonNegatif(volCooldown);
            config.volDuree = positif(volDuree);
            config.volPousseeVerticale = positif(volVertical);
            config.volPousseeHorizontale = nonNegatif(volHorizontal);
            config.volHauteurMax = positif(volHauteur);
            config.volSubitGravite = volGravite.isSelected();
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(fenetre, "车辆参数必须是有效数字，且不能超出允许范围。",
                    "参数无效", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void ajouterChamp(JPanel panel, String nom, JTextField champ) {
        panel.add(new JLabel(nom));
        panel.add(champ);
    }

    private float positif(JTextField champ) {
        float valeur = Float.parseFloat(champ.getText().trim());
        if (!Float.isFinite(valeur) || valeur <= 0f) {
            throw new NumberFormatException();
        }
        return valeur;
    }

    private float nonNegatif(JTextField champ) {
        float valeur = Float.parseFloat(champ.getText().trim());
        if (!Float.isFinite(valeur) || valeur < 0f) {
            throw new NumberFormatException();
        }
        return valeur;
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
