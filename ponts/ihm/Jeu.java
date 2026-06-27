package ponts.ihm;

import java.awt.BorderLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Point;
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
import javax.swing.SwingUtilities;
import javax.swing.event.MouseInputListener;

import org.jbox2d.common.Vec2;

import ponts.niveau.Niveau;
import ponts.physique.Partie;
import ponts.physique.OutilConstruction;
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
    private JComboBox<Materiau> comboMateriau;
    private JButton boutonDemarrer;
    private JButton boutonRecommencer;
    private JButton boutonBoost;
    private JButton boutonEditeur;
    private JComboBox<OutilConstruction> comboOutil;
    private JButton boutonConfirmerOutil;
    private JButton boutonAnnulerOutil;
    private JComboBox<Voiture.Charge> comboCharge;
    private JComboBox<Voiture.Style> comboStyle;
    private JLabel textePrix;
    private JLabel texteBudget;
    private JLabel texteMeilleur;
    private JLabel texteBarres;
    private JLabel texteNoeud;
    private JLabel texteSupports;
    private JPanel zoneInteraction;
    private int dernierXGlisser;
    private boolean cameraEnGlissement;
    private int sourisPixelX;
    private int sourisPixelY;
    private String dernierClic = "";

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
        zoneInteraction.addMouseListener(this);
        zoneInteraction.addMouseMotionListener(this);
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

        JPanel ligneHaut = new Ligne(16, box2d.getHauteurPixels() / 100);
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
        comboMateriau = new JComboBox<Materiau>(Materiau.values());
        comboMateriau.setToolTipText("路面板可供车辆行驶，其余构件用于承重支撑");
        comboMateriau.addActionListener(this);
        ligneMateriau.add(comboMateriau);

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
        boutonDemarrer.setPreferredSize(new Dimension(118, 44));
        boutonDemarrer.addActionListener(this);
        ligneControles.add(boutonDemarrer);
        boutonRecommencer = new JButton("重置");
        Theme.skinButton(boutonRecommencer, Theme.icon(Theme.Symbol.RESET));
        boutonRecommencer.addActionListener(this);
        ligneControles.add(boutonRecommencer);
        boutonBoost = new JButton("冲刺");
        Theme.skinButton(boutonBoost, Theme.icon(Theme.Symbol.PLAY));
        boutonBoost.setPreferredSize(new Dimension(104, 44));
        boutonBoost.setToolTipText("模拟过程中可使用一次短时加速");
        boutonBoost.addActionListener(this);
        ligneControles.add(boutonBoost);

        JPanel colonneEditeur = new Colonne();
        Theme.skinPanel(colonneEditeur);
        ligneHaut.add(colonneEditeur);
        JLabel texteEditeur = new JLabel("工具");
        colonneEditeur.add(texteEditeur);
        JPanel ligneEditeur = new Ligne();
        Theme.skinPanel(ligneEditeur);
        colonneEditeur.add(ligneEditeur);
        comboOutil = new JComboBox<OutilConstruction>(OutilConstruction.values());
        comboOutil.addActionListener(this);
        ligneEditeur.add(comboOutil);
        boutonConfirmerOutil = new JButton("确认");
        boutonConfirmerOutil.setToolTipText("确认生成曲线填充");
        boutonConfirmerOutil.addActionListener(this);
        ligneEditeur.add(boutonConfirmerOutil);
        boutonAnnulerOutil = new JButton("取消");
        boutonAnnulerOutil.addActionListener(this);
        ligneEditeur.add(boutonAnnulerOutil);
        boutonEditeur = new JButton("编辑器");
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

        JPanel bas = new Ligne(24, box2d.getHauteurPixels() / 50);
        Theme.skinPanel(bas);
        this.add(bas, BorderLayout.PAGE_END);

        JPanel colonnePrix = new Colonne();
        Theme.skinPanel(colonnePrix);
        bas.add(colonnePrix);
        JLabel prix = new JLabel("造价");
        colonnePrix.add(prix);
        textePrix = new JLabel();
        textePrix.setPreferredSize(new Dimension(110, 24));
        textePrix.setHorizontalAlignment(JLabel.CENTER);
        colonnePrix.add(textePrix);

        JPanel colonneBudget = new Colonne();
        Theme.skinPanel(colonneBudget);
        bas.add(colonneBudget);
        JLabel budget = new JLabel("预算");
        colonneBudget.add(budget);
        texteBudget = new JLabel();
        texteBudget.setPreferredSize(new Dimension(110, 24));
        texteBudget.setHorizontalAlignment(JLabel.CENTER);
        colonneBudget.add(texteBudget);

        JPanel colonneMeilleur = new Colonne();
        Theme.skinPanel(colonneMeilleur);
        bas.add(colonneMeilleur);
        JLabel meilleur = new JLabel("最佳");
        colonneMeilleur.add(meilleur);
        texteMeilleur = new JLabel();
        texteMeilleur.setPreferredSize(new Dimension(110, 24));
        texteMeilleur.setHorizontalAlignment(JLabel.CENTER);
        colonneMeilleur.add(texteMeilleur);

        JPanel colonneBarres = new Colonne();
        Theme.skinPanel(colonneBarres);
        bas.add(colonneBarres);
        JLabel barres = new JLabel("杆件");
        colonneBarres.add(barres);
        texteBarres = new JLabel();
        texteBarres.setPreferredSize(new Dimension(90, 24));
        texteBarres.setHorizontalAlignment(JLabel.CENTER);
        colonneBarres.add(texteBarres);

        JPanel colonneAide = new Colonne();
        Theme.skinPanel(colonneAide);
        bas.add(colonneAide);
        JLabel aide = new JLabel("视角");
        colonneAide.add(aide);
        JLabel aideTexte = new JLabel("按住中键拖动");
        colonneAide.add(aideTexte);

        JPanel colonneDebug = new Colonne();
        Theme.skinPanel(colonneDebug);
        bas.add(colonneDebug);
        colonneDebug.add(new JLabel("选点状态"));
        texteNoeud = new JLabel("未锁定");
        texteNoeud.setPreferredSize(new Dimension(150, 24));
        texteNoeud.setHorizontalAlignment(JLabel.CENTER);
        colonneDebug.add(texteNoeud);

        JPanel colonneSupports = new Colonne();
        Theme.skinPanel(colonneSupports);
        bas.add(colonneSupports);
        colonneSupports.add(new JLabel("支撑/节点"));
        texteSupports = new JLabel();
        texteSupports.setPreferredSize(new Dimension(190, 24));
        texteSupports.setHorizontalAlignment(JLabel.CENTER);
        colonneSupports.add(texteSupports);

        // 子组件明确占据中央游戏区，保证空白画布也能接收全部鼠标按键。
        zoneInteraction = new JPanel();
        zoneInteraction.setOpaque(false);
        zoneInteraction.setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
        this.add(zoneInteraction, BorderLayout.CENTER);
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
                majTextLabels();

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

            if (source == comboMateriau) {
                partie.changementMateriau((Materiau) comboMateriau.getSelectedItem());
            }
            if (source == comboOutil) {
                partie.changementOutil((OutilConstruction) comboOutil.getSelectedItem());
            }
            if (source == boutonConfirmerOutil) {
                partie.confirmerOutil();
            }
            if (source == boutonAnnulerOutil) {
                partie.annulerOutil();
            }

            if (source == boutonDemarrer) {
                partie.toggleSimulationPhysique();
                majSimulation();
            }

            if (source == boutonRecommencer) {
                nouvellePartie();
            }
            if (source == boutonBoost) {
                partie.activerBoost();
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
        setTextSiChange(textePrix, partie.getPrix() + " 元");
        setTextSiChange(texteBudget, partie.getBuget() + " 元");
        int meilleurPrix = recupererMeilleurPrix();
        if (meilleurPrix == -1) {
            setTextSiChange(texteMeilleur, "暂无");
        } else {
            setTextSiChange(texteMeilleur, meilleurPrix + " 元");
        }
        setTextSiChange(texteBarres, partie.getNombreBarres() + " / " + partie.getLimiteBarres());
        setTextSiChange(texteSupports,
                "锚 " + partie.getNombreAncragesUtilisateur()
                        + "  墩 " + partie.getNombrePiliers()
                        + "  节点 " + partie.getNombreNoeudsGeneres()
                        + "  ¥" + partie.getCoutSupports());
        boolean boostActif = partie.getSimulationPhysique() && partie.boostDisponible();
        if (boutonBoost.isEnabled() != boostActif) {
            boutonBoost.setEnabled(boostActif);
        }
        boolean verrouille = partie.noeudSousCurseurPixel(sourisPixelX, sourisPixelY);
        String statut = verrouille ? "● 已锁定" : "○ 未锁定";
        if (!dernierClic.isEmpty()) {
            statut += " " + dernierClic;
        }
        setTextSiChange(texteNoeud, statut);
    }

    private void setTextSiChange(JLabel label, String texte) {
        if (!texte.equals(label.getText())) {
            label.setText(texte);
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
            if (comboOutil != null && comboOutil.getSelectedItem() != null) {
                partie.changementOutil((OutilConstruction) comboOutil.getSelectedItem());
            }
            majTextLabels();
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
        Point point = pointDansJeu(e);
        sourisPixelX = point.x;
        sourisPixelY = point.y;
        posSouris = box2d.pixelToWorld(point.x, point.y);
    }

    private Point pointDansJeu(MouseEvent e) {
        Point point = e.getLocationOnScreen();
        SwingUtilities.convertPointFromScreen(point, this);
        return point;
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
        Point point = pointDansJeu(e);
        boutonSouris = e.getButton();
        if (e.getButton() == MouseEvent.BUTTON2) {
            cameraEnGlissement = true;
            dernierXGlisser = point.x;
            setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
            zoneInteraction.setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
            return;
        }
        if (partie != null) {
            dernierClic = e.getButton() == MouseEvent.BUTTON1 ? "左键" : "右键";
            partie.clicConstructionPixel(point.x, point.y, e.getButton());
            majTextLabels();
            repaint();
        }
        // 建造点击已经同步处理，避免物理 timer 再执行一次。
        clicSouris = false;
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        majPosSouris(e);
        if (e.getButton() == MouseEvent.BUTTON2) {
            cameraEnGlissement = false;
            setCursor(Cursor.getDefaultCursor());
            zoneInteraction.setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
        }
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        Point point = pointDansJeu(e);
        if (cameraEnGlissement) {
            int delta = point.x - dernierXGlisser;
            box2d.deplacerCameraPixels(delta);
            dernierXGlisser = point.x;
        }
        majPosSouris(e);
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        majPosSouris(e);
        if (partie != null) {
            partie.deplacerConstructionPixel(sourisPixelX, sourisPixelY);
            repaint();
        }
    }

}
