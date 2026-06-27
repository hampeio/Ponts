package ponts.ihm;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.swing.AbstractButton;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.imageio.ImageIO;
import ponts.Chemins;

/**
 * Visual skin for the game shell. Physics and level logic stay in ponts.physique.
 */
public final class Theme {

    public static final String APP_NAME = "卡通桥梁工程师";
    public static final Color BACKGROUND = Color.decode("#17212b");
    public static final Color BACKGROUND_GRID = Color.decode("#223140");
    public static final Color PANEL = Color.decode("#f8f1df");
    public static final Color PANEL_BORDER = Color.decode("#b97836");
    public static final Color TEXT = Color.decode("#3c2a1d");
    public static final Color ACCENT = Color.decode("#f29d38");
    public static final Color WATER = Color.decode("#2f9cbd");
    public static final Color WOOD = Color.decode("#b77a42");
    public static final Color STEEL = Color.decode("#6f879a");
    public static final Color ROAD = Color.decode("#2f343a");
    private static BufferedImage backdrop;

    static {
        try {
            backdrop = ImageIO.read(Chemins.ressource("images", "distant-panorama.png").toFile());
        } catch (IOException ignored) {
            backdrop = null;
        }
    }

    private Theme() {
    }

    public static void skinPanel(JPanel panel) {
        panel.setBackground(PANEL);
        panel.setForeground(TEXT);
        panel.setOpaque(true);
    }

    public static void skinButton(AbstractButton button, Icon icon) {
        button.setIcon(icon);
        button.setForeground(TEXT);
        button.setBackground(PANEL);
        button.setFocusPainted(false);
        button.setIconTextGap(8);
    }

    public static void drawBackdrop(Graphics2D g, JComponent component) {
        int width = component.getWidth();
        int height = component.getHeight();

        if (backdrop != null) {
            g.drawImage(backdrop, 0, 0, width, height, null);
        } else {
            g.setColor(Color.decode("#63c7ef"));
            g.fillRect(0, 0, width, height);
        }
    }

    public static Icon icon(Symbol symbol) {
        return new LineIcon(symbol, 22, TEXT, ACCENT);
    }

    public enum Symbol {
        PREVIOUS,
        NEXT,
        ROAD,
        WOOD,
        STEEL,
        PLAY,
        RESET,
        EDIT,
        SAVE,
        LOAD,
        DELETE,
        UNDO,
        CLEAR,
        GAME
    }

    private static final class LineIcon implements Icon {

        private final Symbol symbol;
        private final int size;
        private final Color primary;
        private final Color accent;

        LineIcon(Symbol symbol, int size, Color primary, Color accent) {
            this.symbol = symbol;
            this.size = size;
            this.primary = primary;
            this.accent = accent;
        }

        @Override
        public int getIconWidth() {
            return size;
        }

        @Override
        public int getIconHeight() {
            return size;
        }

        @Override
        public void paintIcon(Component c, Graphics graphics, int x, int y) {
            Graphics2D g = (Graphics2D) graphics.create();
            g.translate(x, y);
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setStroke(new BasicStroke(2.2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g.setColor(primary);

            switch (symbol) {
                case PREVIOUS:
                    arrow(g, true);
                    break;
                case NEXT:
                    arrow(g, false);
                    break;
                case ROAD:
                    road(g);
                    break;
                case WOOD:
                    beam(g, WOOD);
                    break;
                case STEEL:
                    beam(g, STEEL);
                    break;
                case PLAY:
                    play(g);
                    break;
                case RESET:
                    reset(g);
                    break;
                case EDIT:
                    pencil(g);
                    break;
                case SAVE:
                    save(g);
                    break;
                case LOAD:
                    load(g);
                    break;
                case DELETE:
                    delete(g);
                    break;
                case UNDO:
                    undo(g);
                    break;
                case CLEAR:
                    clear(g);
                    break;
                case GAME:
                    game(g);
                    break;
                default:
                    break;
            }
            g.dispose();
        }

        private void arrow(Graphics2D g, boolean left) {
            int direction = left ? -1 : 1;
            int center = size / 2;
            Path2D path = new Path2D.Double();
            path.moveTo(center - direction * 5, 5);
            path.lineTo(center + direction * 5, center);
            path.lineTo(center - direction * 5, size - 5);
            g.draw(path);
        }

        private void road(Graphics2D g) {
            g.setColor(ROAD);
            g.drawLine(3, 14, 19, 8);
            g.setColor(accent);
            g.drawLine(8, 14, 15, 11);
        }

        private void beam(Graphics2D g, Color color) {
            g.setColor(color);
            g.drawLine(4, 17, 18, 5);
            g.drawLine(6, 6, 17, 17);
            g.setColor(primary);
            g.draw(new Ellipse2D.Double(3, 15, 4, 4));
            g.draw(new Ellipse2D.Double(15, 3, 4, 4));
        }

        private void play(Graphics2D g) {
            Path2D path = new Path2D.Double();
            path.moveTo(7, 5);
            path.lineTo(17, 11);
            path.lineTo(7, 17);
            path.closePath();
            g.setColor(accent);
            g.fill(path);
        }

        private void reset(Graphics2D g) {
            g.drawArc(5, 5, 12, 12, 25, 285);
            g.drawLine(5, 5, 5, 11);
            g.drawLine(5, 5, 11, 5);
        }

        private void pencil(Graphics2D g) {
            g.drawLine(6, 17, 17, 6);
            g.drawLine(14, 4, 18, 8);
            g.drawLine(5, 18, 8, 15);
        }

        private void save(Graphics2D g) {
            g.draw(new Rectangle2D.Double(5, 4, 12, 14));
            g.drawLine(8, 4, 8, 9);
            g.drawLine(8, 15, 14, 15);
        }

        private void load(Graphics2D g) {
            g.draw(new Rectangle2D.Double(5, 12, 12, 6));
            g.drawLine(11, 4, 11, 13);
            g.drawLine(7, 9, 11, 13);
            g.drawLine(15, 9, 11, 13);
        }

        private void delete(Graphics2D g) {
            g.drawLine(7, 7, 15, 15);
            g.drawLine(15, 7, 7, 15);
            g.drawLine(6, 5, 16, 5);
            g.drawLine(8, 5, 8, 17);
            g.drawLine(14, 5, 14, 17);
        }

        private void undo(Graphics2D g) {
            g.drawArc(5, 6, 13, 11, 25, 245);
            g.drawLine(5, 9, 9, 5);
            g.drawLine(5, 9, 10, 10);
        }

        private void clear(Graphics2D g) {
            Shape rect = new Rectangle2D.Double(5, 6, 12, 10);
            g.draw(rect);
            g.drawLine(5, 16, 17, 6);
        }

        private void game(Graphics2D g) {
            g.drawLine(3, 15, 19, 15);
            g.draw(new Ellipse2D.Double(5, 11, 3, 3));
            g.draw(new Ellipse2D.Double(14, 11, 3, 3));
        }
    }
}
