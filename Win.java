import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.util.ArrayList;

import javax.swing.JFrame;
import javax.swing.JPanel;

public class Win extends JFrame {
    int w;
    int h;

    float scale;

    public Win(int w, int h, float scale, ArrayList<ConvexPolygon> polygons) {
        this.w = w;
        this.h = h;

        setContentPane(new DrawWin(polygons, scale));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setPreferredSize(new Dimension(this.w, this.h));

        pack();

        setLocationRelativeTo(null);
        setVisible(true);
    }

    class DrawWin extends JPanel {
        ArrayList<ConvexPolygon> polygons;
        float scale;

        public DrawWin(ArrayList<ConvexPolygon> polygons, float scale) {
            this.polygons = polygons;
            this.scale = scale;
        }

        @Override
        public void paintComponent(Graphics g) {
            g.setColor(Color.BLACK);
            g.fillRect(0, 0, this.getWidth(), this.getHeight());

            for (ConvexPolygon polygon : this.polygons) {
                polygon.draw(g, true, this.scale, this.getWidth(), this.getHeight());
            }
        }
    }
}
