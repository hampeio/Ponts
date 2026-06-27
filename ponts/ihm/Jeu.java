package ponts.ihm;

import java.awt.BorderLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.Timer;
import javax.swing.event.MouseInputListener;

import org.jbox2d.common.Vec2;

import ponts.niveau.Niveau;
import ponts.physique.Partie;
import ponts.physique.barres.Materiau;
import ponts.physique.voiture.Voiture;

/**
 * JPanel ou est dessiné le jeu
 */
public class Jeu extends JPanel implements ActionListener, MouseInputListener {

    private Fenetre fenetre;
    private Box2D box2d;
    private Partie partie;

    private Map<String, Integer> meilleursPrix;

    private int boutonSouris;
    private boolean clicSouris = false;
    private Vec2 posSouris = new Vec2();

    private static final int TICK_PHYSIQUE = 16;
    private long tempsPhysique;
    private Timer timerGraphique;
    private Timer timerPhysique;

    private JButton boutonPrecedent;
    private JComboBox<String> comboBoxNiveaux;
    private JButton boutonSuivant;
    private JButton boutonMateriauGoudron;
    private JButton boutonMateriauAcier;
    private JButton boutonMateriauBois;
    private JButton boutonDemarrer;
    private JButton boutonRecommencer;
    private JButton boutonEditeur;
    private JComboBox<Voiture.Charge> comboCharge;
    private JComboBox<Voiture.Style> comboStyle;
    private JLabel textePrix;
    private JLabel texteBudget;
    private JLabel texteMeilleur;

    /**
     * Constructeur de Jeu
     * 
     * @param fenetre
     * @param box2d
     * @param refreshRate
     */
    public Jeu(Fenetre fenetre, Box2D box2d, int refreshRate) {

        this.fenetre = fenetre;
        this.box2d = box2d;
        meilleursPrix = new HashMap<String, Integer>();
        ihm();

        majListeNiveaux();

        timerPhysique = new Timer(TICK_PHYSIQUE, this);
        timerPhysique.start();

        int fps = (int) (1.0 / refreshRate * 1000.0);
        timerGraphique = new Timer(fps, this);
        timerGraphique.start();

        addMouseListener(this);
        addMouseMotionListener(this);
    }

    /**
     * Methode créant tous les composants de l'IHM
     * Elle utilise avtangeusement les layout managers pour ne pas avoir positionner
     * manuellement tous les boutons
     * cela permet aussi d'adapter l'affichage à l'écran
     */
    private void ihm() {

        this.setLayout(new BorderLayout());
        this.setOpaque(false);

        JPanel ligneHaut = new Ligne(box2d.getLargeurPixels() / 20, box2d.getHauteurPixels() / 100);
        Theme.skinPanel(ligneHaut);
        this.add(ligneHaut, BorderLayout.PAGE_START);

        JPanel colonneNiveau = new Colonne();
        Theme.skinPanel(colonneNiveau);
        ligneHaut.add(colonneNiveau);
        JLabel texteNiveau = new JLabel("关卡");
        colonneNiveau.add(texteNiveau);
        JPanel ligneNiveau = new Ligne();
        Theme.skinPanel(ligneNiveau);
        colonneNiveau.add(ligneNiveau);
        boutonPrecedent = new JButton("上一关");
        Theme.skinButton(boutonPrecedent, Theme.icon(Theme.Symbol.PREVIOUS));
        boutonPrecedent.addActionListener(this);
        ligneNiveau.add(boutonPrecedent);
        comboBoxNiveaux = new JComboBox<String>();
        comboBoxNiveaux.addActionListener(this);
        ligneNiveau.add(comboBoxNiveaux);
        boutonSuivant = new JButton("下一关");
        Theme.skinButton(boutonSuivant, Theme.icon(Theme.Symbol.NEXT));
        boutonSuivant.addActionListener(this);
        ligneNiveau.add(boutonSuivant);

        JPanel colonneMateriau = new Colonne();
        Theme.skinPanel(colonneMateriau);
        ligneHaut.add(colonneMateriau);
        JLabel textMateriau = new JLabel("建材");
        colonneMateriau.add(textMateriau);
        JPanel ligneMateriau = new Ligne();
        Theme.skinPanel(ligneMateriau);
        colonneMateriau.add(ligneMateriau);
        boutonMateriauGoudron = new JButton("路面");
        Theme.skinButton(boutonMateriauGoudron, Theme.icon(Theme.Symbol.ROAD));
        boutonMateriauGoudron.setToolTipText("车辆可以行驶的桥面材料");
        boutonMateriauGoudron.addActionListener(this);
        ligneMateriau.add(boutonMateriauGoudron);
        boutonMateriauBois = new JButton("木材");
        Theme.skinButton(boutonMateriauBois, Theme.icon(Theme.Symbol.WOOD));
        boutonMateriauBois.setToolTipText("价格便宜，但强度较低");
        boutonMateriauBois.addActionListener(this);
        ligneMateriau.add(boutonMateriauBois);
        boutonMateriauAcier = new JButton("钢材");
        Theme.skinButton(boutonMateriauAcier, Theme.icon(Theme.Symbol.STEEL));
        boutonMateriauAcier.setToolTipText("强度更高，造价也更高");
        boutonMateriauAcier.addActionListener(this);
        ligneMateriau.add(boutonMateriauAcier);

        JPanel colonneSimulation = new Colonne();
        Theme.skinPanel(colonneSimulation);
        ligneHaut.add(colonneSimulation);
        JLabel texteControles = new JLabel("模拟");
        colonneSimulation.add(texteControles);
        JPanel ligneControles = new Ligne();
        Theme.skinPanel(ligneControles);
        colonneSimulation.add(ligneControles);
        boutonDemarrer = new JButton();
        Theme.skinButton(boutonDemarrer, Theme.icon(Theme.Symbol.PLAY));
        boutonDemarrer.addActionListener(this);
        ligneControles.add(boutonDemarrer);
        boutonRecommencer = new JButton("重置");
        Theme.skinButton(boutonRecommencer, Theme.icon(Theme.Symbol.RESET));
        boutonRecommencer.addActionListener(this);
        ligneControles.add(boutonRecommencer);

        JPanel colonneEditeur = new Colonne();
        Theme.skinPanel(colonneEditeur);
        ligneHaut.add(colonneEditeur);
        JLabel texteEditeur = new JLabel("工具");
        colonneEditeur.add(texteEditeur);
        JPanel ligneEditeur = new Ligne();
        Theme.skinPanel(ligneEditeur);
        colonneEditeur.add(ligneEditeur);
        boutonEditeur = new JButton("关卡编辑器");
        Theme.skinButton(boutonEditeur, Theme.icon(Theme.Symbol.EDIT));
        boutonEditeur.addActionListener(this);
        ligneEditeur.add(boutonEditeur);

        JPanel colonneVehicule = new Colonne();
        Theme.skinPanel(colonneVehicule);
        ligneHaut.add(colonneVehicule);
        JLabel texteVehicule = new JLabel("车辆配置");
        colonneVehicule.add(texteVehicule);
        JPanel ligneVehicule = new Ligne();
        Theme.skinPanel(ligneVehicule);
        colonneVehicule.add(ligneVehicule);
        comboCharge = new JComboBox<Voiture.Charge>(Voiture.Charge.values());
        comboCharge.setToolTipText("载荷越重，对桥梁强度要求越高");
        comboCharge.addActionListener(this);
        ligneVehicule.add(comboCharge);
        comboStyle = new JComboBox<Voiture.Style>(Voiture.Style.values());
        comboStyle.addActionListener(this);
        ligneVehicule.add(comboStyle);

        JPanel bas = new Ligne(box2d.getLargeurPixels() / 20, box2d.getHauteurPixels() / 50);
        Theme.skinPanel(bas);
        this.add(bas, BorderLayout.PAGE_END);

        JPanel colonnePrix = new Colonne();
        Theme.skinPanel(colonnePrix);
        bas.add(colonnePrix);
        JLabel prix = new JLabel("造价");
        colonnePrix.add(prix);
        textePrix = new JLabel();
        colonnePrix.add(textePrix);

        JPanel colonneBudget = new Colonne();
        Theme.skinPanel(colonneBudget);
        bas.add(colonneBudget);
        JLabel budget = new JLabel("预算");
        colonneBudget.add(budget);
        texteBudget = new JLabel();
        colonneBudget.add(texteBudget);

        JPanel colonneMeilleur = new Colonne();
        Theme.skinPanel(colonneMeilleur);
        bas.add(colonneMeilleur);
        JLabel meilleur = new JLabel("最佳");
        colonneMeilleur.add(meilleur);
        texteMeilleur = new JLabel();
        colonneMeilleur.add(texteMeilleur);
    }

    /**
     * Méthode pour dessiner le jeu, héritée de JPanel
     */
    @Override
    public void paintComponent(Graphics g0) {

        // Activer l'anti-aliasing
        Graphics2D g = (Graphics2D) g0;
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        Toolkit.getDefaultToolkit().sync(); // Fonction pour améliorer l'affichage sur Mac/Linux

        Theme.drawBackdrop(g, this);

        if (partie != null) {
            majTextLabels();
            partie.dessiner(g, box2d, posSouris);
        }
    }

    /**
     * Traite les évenements des timers et des composants de l'IHM
     */
    public void actionPerformed(ActionEvent e) {
        Object source = e.getSource();

        if (partie != null) {
            if (source == timerPhysique) {

                long nouveauTempsPhysique = System.currentTimeMillis();
                float dt = (nouveauTempsPhysique - tempsPhysique) / 1000f;
                tempsPhysique = nouveauTempsPhysique;

                partie.tickPhysique(posSouris, boutonSouris, clicSouris, dt);
                clicSouris = false;

            }

            if (source == timerGraphique) {
                repaint();
            }

            if (source == boutonPrecedent) {
                int numero = comboBoxNiveaux.getSelectedIndex();
                if (numero > 0) {
                    numero--;
                    comboBoxNiveaux.setSelectedIndex(numero);
                }
            }

            if (source == boutonSuivant) {
                int numero = comboBoxNiveaux.getSelectedIndex();
                if (numero < comboBoxNiveaux.getItemCount() - 1) {
                    numero++;
                    comboBoxNiveaux.setSelectedIndex(numero);
                }
            }

            if (source == comboBoxNiveaux) {
                nouvellePartie();
            }
            if (source == comboCharge || source == comboStyle) {
                nouvellePartie();
            }

            Materiau materiau = null;
            if (source == boutonMateriauBois) {
                materiau = Materiau.BOIS;
            }
            if (source == boutonMateriauGoudron) {
                materiau = Materiau.GOUDRON;
            }
            if (source == boutonMateriauAcier) {
                materiau = Materiau.ACIER;
            }
            if (materiau != null) {
                partie.changementMateriau(materiau);
            }

            if (source == boutonDemarrer) {
                partie.toggleSimulationPhysique();
                majSimulation();
            }

            if (source == boutonRecommencer) {
                nouvellePartie();
            }
        }

        if (source == boutonEditeur) {
            if (partie != null) {
                partie.setSimulationPhsyique(false);
                majSimulation();
            }
            fenetre.lancerEditeur();
        }

    }

    /**
     * Met à jour les labels concernant le prix en bas de l'écran
     */
    private void majTextLabels() {
        textePrix.setText(partie.getPrix() + " 元");
        texteBudget.setText(partie.getBuget() + " 元");
        int meilleurPrix = recupererMeilleurPrix();
        if (meilleurPrix == -1) {
            texteMeilleur.setText("暂无");
        } else {
            texteMeilleur.setText(meilleurPrix + " 元");
        }
    }

    /**
     * Met à jour la liste des niveaux
     */
    public void majListeNiveaux() {
        String niveauSelectionne = recupererNomNiveau();
        String[] nomsNiveaux = new File(Niveau.CHEMIN_NIVEAUX.toString()).list();
        Arrays.sort(nomsNiveaux);
        DefaultComboBoxModel<String> model = new DefaultComboBoxModel<String>(nomsNiveaux);
        comboBoxNiveaux.setModel(model);
        if (niveauSelectionne != null && Arrays.asList(nomsNiveaux).contains(niveauSelectionne)) {
            comboBoxNiveaux.setSelectedItem(niveauSelectionne);
        } else {
            nouvellePartie();
        }
    }

    /**
     * Met à jour le bouton controlant la simulation en fonction de l'état du jeu
     */
    private void majSimulation() {
        reinitialiserTemps();
        if (partie.getSimulationPhysique()) {
            boutonDemarrer.setText("暂停");
        } else {
            boutonDemarrer.setText("继续");
        }
    }

    /**
     * Reccupére un niveau
     * 
     * @return niveau
     */
    private Niveau recupererNiveau() {
        boutonDemarrer.setText("开始");
        String nomNiveau = recupererNomNiveau();
        return Niveau.charger(fenetre, nomNiveau);

    }

    /**
     * Reccupère le nom du niveau sélectionné dans la liste
     * 
     * @return
     */
    private String recupererNomNiveau() {
        String nomNiveau = (String) comboBoxNiveaux.getSelectedItem();
        return nomNiveau;
    }

    /**
     * Gestion de la fin de partie
     * 
     * @param niveauReussi
     * @param prix
     */
    public void finPartie(boolean niveauReussi, int prix) {
        majMeilleurPrix(prix);
        messageFinPartie(niveauReussi);
        reinitialiserTemps();
    }

    /**
     * Affiche le tutoriel
     */
    public void messageDebutJeu() {
        String texte = "欢迎来到《" + Theme.APP_NAME + "》！";
        texte += "\n\n点击圆形锚点来搭建桥梁。";
        texte += "\n请根据造价和强度选择合适的材料。";
        texte += "\n还可以选择车辆载荷与外观；载荷越重，挑战越大。";
        texte += "\n桥梁准备好后，点击“" + boutonDemarrer.getText() + "”启动模拟。";
        texte += "\n\n车辆安全抵达对岸且总造价不超预算，即可过关。";
        JOptionPane.showMessageDialog(fenetre, texte, "快速入门", JOptionPane.PLAIN_MESSAGE);
    }

    /**
     * Affiche le message de fin de partie
     * 
     * @param niveauReussi
     */
    private void messageFinPartie(boolean niveauReussi) {
        String texte = "";
        String titre = "";
        if (niveauReussi) {
            titre = "挑战成功";
            texte += "干得漂亮！关卡 " + recupererNomNiveau() + " 已完成。";
            texte += "\n\n造价：" + partie.getPrix() + " 元";
            texte += "\n最佳：" + recupererMeilleurPrix() + " 元";
            texte += "\n\n可以进入下一关，或尝试更省钱的方案。";
        } else {
            titre = "超出预算";
            texte += "关卡 " + recupererNomNiveau() + " 未能满足预算要求。";
            texte += "\n\n当前造价：" + partie.getPrix() + " 元";
            texte += "\n预算：" + partie.getBuget() + " 元";
            texte += "\n\n点击“" + boutonRecommencer.getText() + "”再试一次。";
        }
        JOptionPane.showMessageDialog(fenetre, texte, titre, JOptionPane.PLAIN_MESSAGE);
    }

    /**
     * Met à jour l'affichage du meilleur prix réalisé
     */
    private void majMeilleurPrix(int prix) {
        int meilleurPrix = recupererMeilleurPrix();
        if (prix <= meilleurPrix || meilleurPrix == -1) {
            meilleursPrix.put(recupererNomNiveau(), prix);
        }
    }

    /**
     * Reccupère le meilleur prix associé au niveau
     * 
     * @return meilleur prix
     */
    private int recupererMeilleurPrix() {
        if (meilleursPrix.containsKey(recupererNomNiveau())) {
            return meilleursPrix.get(recupererNomNiveau());
        } else {
            return -1;
        }
    }

    /**
     * Reinitilise le temps écoulé depuis la dernière mise à jour de la physique,
     * typiquement après avoir mis le jeu en pause
     */
    private void reinitialiserTemps() {
        tempsPhysique = System.currentTimeMillis();
    }

    /**
     * Creer une nouvelle partie
     */
    private void nouvellePartie() {
        Niveau niveau = recupererNiveau();
        if (niveau != null) {
            Voiture.Charge charge = comboCharge == null ? Voiture.Charge.STANDARD
                    : (Voiture.Charge) comboCharge.getSelectedItem();
            Voiture.Style style = comboStyle == null ? Voiture.Style.CLASSIQUE
                    : (Voiture.Style) comboStyle.getSelectedItem();
            partie = new Partie(this, box2d, niveau, charge, style);
        } else {
            partie = null;
        }
    }

    /**
     * Verifie si le niveau a bien été chargé et affiche un message d'erreur dans le
     * cas contraire
     */
    public void verifierExistanceNiveaux() {
        if (partie == null) {
            String texte = "未检测到可用关卡。";
            texte += "\n请在“" + boutonEditeur.getText() + "”中创建关卡。";
            JOptionPane.showMessageDialog(fenetre, texte, "缺少关卡", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Met à jour la position de la souris dans un vecteur
     * 
     * @param e
     */
    private void majPosSouris(MouseEvent e) {
        posSouris = box2d.pixelToWorld(e.getX(), e.getY());
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
        majPosSouris(e);
        boutonSouris = e.getButton();
        clicSouris = true;
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        majPosSouris(e);
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        majPosSouris(e);
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        majPosSouris(e);
    }

}
