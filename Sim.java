import java.util.ArrayList;

public class Sim {
    boolean run;
    boolean visualize;

    float scale;
    ArrayList<ConvexPolygon> polygons;
    Win win;

    float[] rotations = new float[]{0, 0, 0};

    public Sim() {
        this.visualize = false;
        this.polygons = new ArrayList<ConvexPolygon>();
    }

    public void update() {
        if (visualize) {
            this.win.repaint();
        }
    }

    public void createWindow(int w, int h, float scale) {
        this.scale = scale;
        visualize = true;
        win = new Win(w+16, h+38, this.scale, this.polygons);
    }

    public void setScale(float scale) {
        this.scale = scale;
    }

    public void addPolygon(String name, float[][] vertices, float[] origin_pos, float[] mate_1, float[] mate_2) {
        this.polygons.add(new ConvexPolygon(name, vertices, origin_pos, mate_1, mate_2));
    }

    public void constrain(String name1, String name2) {
        ConvexPolygon p1 = new ConvexPolygon("null", null, null, null, null);
        ConvexPolygon p2 = new ConvexPolygon("null", null, null, null, null);

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

    public void movePolygon(String name, float x, float y) {
        for (ConvexPolygon p : this.polygons) {
            if (p.name == name) {
                p.move(x, y);
            }
        }
    }

    public void rotatePolygon(String name, float deg) {
        for (ConvexPolygon p : this.polygons) {
            if (p.name == name) {
                p.rotate(deg);
            }
        }
    }

    public boolean checkCollision(String name1, String name2) {
        ConvexPolygon p1 = new ConvexPolygon("null", null, null, null, null);
        ConvexPolygon p2 = new ConvexPolygon("null", null, null, null, null);

        for (ConvexPolygon p : this.polygons) {
            if (p.name == name1) {
                p1 = p;
            } else if (p.name == name2) {
                p2 = p;
            }
        }

        if (p1.name != "null" && p2.name != "null") {
            return p1.isColliding(p2);
        } else {
            System.out.println("could not find polygons");
            return false;
        }
    }

    public boolean test3jAngles(float a, float b, float c) {
        for (ConvexPolygon p : this.polygons) {
            if (p.name == "1") {
                p.rotate(-this.rotations[0]);
                p.rotate(a);
            } else if (p.name == "2") {
                p.rotate(-this.rotations[1]);
                p.rotate(b);
            } else if (p.name == "end") {
                p.rotate(-this.rotations[2]);
                p.rotate(c);
            }
        }

        this.rotations = new float[]{a, b, c};

        this.update();
        return this.checkCollision("end", "1") || this.checkCollision("end", "root");
    }
}
