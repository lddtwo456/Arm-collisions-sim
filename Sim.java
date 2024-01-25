import java.util.ArrayList;

public class Sim {
    float scale;
    ArrayList<ConvexPolygon> polygons;
    Win win;

    public Sim() {
        polygons = new ArrayList<ConvexPolygon>();
    }

    public void createWindow(int w, int h) {
        win = new Win(w+16, h+38, this.scale, this.polygons);
        
        while (true) {
            win.repaint();
            System.out.println(this.polygons.get(0).isColliding(this.polygons.get(1)));
        }
    }

    public void setScale(float scale) {
        this.scale = scale;
    }

    public void addPolygon(String name, float[][] vertices, float[] origin_pos, float[] mate_1, float[] mate_2) {
        polygons.add(new ConvexPolygon(name, vertices, origin_pos, mate_1, mate_2));
    }

    public void constrain(String name1, String name2) {
        ConvexPolygon p1 = new ConvexPolygon("nul", new float[1][1], new float[1], new float[1], new float[1]);
        ConvexPolygon p2 = new ConvexPolygon("nul", new float[1][1], new float[1], new float[1], new float[1]);

        for (ConvexPolygon p : this.polygons) {
            if (p.name == name1) {
                p1 = p;
            } else if (p.name == name2) {
                p2 = p;
            }
        }

        try {
            p1.constrainTo(p2);
        } catch (Exception e) {
            System.out.println("could not constrain");
        }
    }
}
