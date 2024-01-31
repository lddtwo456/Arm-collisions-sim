import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.util.Map;

import javax.swing.JFrame;
import javax.swing.JPanel;

public class Win extends JFrame {
    int w;
    int h;

    float scale;

    public Win(int w, int h, float scale, Map<String, ConvexPolygon> polygons, float[] target_position) {
        this.w = w;
        this.h = h;

        setContentPane(new DrawWin(polygons, scale, target_position));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setPreferredSize(new Dimension(this.w, this.h));

        pack();

        setLocationRelativeTo(null);
        setVisible(true);
    }

    class DrawWin extends JPanel {
        Map<String, ConvexPolygon> polygons;
        float[] target_position;
        float scale;

        public DrawWin(Map<String, ConvexPolygon> polygons, float scale, float[] target_position) {
            this.polygons = polygons;
            this.scale = scale;
            this.target_position = target_position;
        }

        @Override
        public void paintComponent(Graphics g) {
            g.setColor(Color.BLACK);
            g.fillRect(0, 0, this.getWidth(), this.getHeight());

            for (Map.Entry<String, ConvexPolygon> entry : this.polygons.entrySet()) {
                entry.getValue().draw(g, true, this.scale, this.getWidth(), this.getHeight());
            }

            g.setColor(Color.WHITE);
            g.fillOval(Math.round(this.target_position[0]*this.scale + (this.getWidth()/2)), Math.round(-this.target_position[1]*this.scale + (this.getHeight()/2)), 10, 10);
        }
    }
}
