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

    public void init() {
        ConvexPolygon root = new ConvexPolygon("null", null, null, null, null);
        for (ConvexPolygon p : this.polygons) {
            if (p.name == "root") {
                root = p;
            }
        }

        // extension limits
        this.polygons.add(new ConvexPolygon("limits", new float[][]{{-8.625f, -0.65f}, {43.875f, -0.65f}, {43.875f, 48f}, {-8.625f, 48f}}, root.origin_pos, new float[]{0, 0}, new float[]{0, 0}));
        // ground
        this.polygons.add(new ConvexPolygon("ground", new float[][]{{-200f, -0.625f}, {200f, -0.625f}, {200f, -100f}, {-200f, -100f}}, new float[]{root.origin_pos[0] - 50, root.origin_pos[1]}, new float[]{0, 0}, new float[]{0, 0}));
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

    public boolean checkWithinBounds() {
        ConvexPolygon end = new ConvexPolygon("null", null, null, null, null);
        ConvexPolygon j2 = new ConvexPolygon("null", null, null, null, null);
        ConvexPolygon j1 = new ConvexPolygon("null", null, null, null, null);
        ConvexPolygon limits = new ConvexPolygon("null", null, null, null, null);
        for (ConvexPolygon p : this.polygons) {
            if (p.name == "1") {
                j1 = p;
            } else if (p.name == "2") {
                j2 = p;
            } else if (p.name == "end") {
                end = p;
            } else if (p.name == "limits") {
                limits = p;
            }
        }

        boolean j1_within = j1.isWithin(limits);
        boolean j2_within = j2.isWithin(limits);
        boolean end_within = end.isWithin(limits);

        return j1_within && j2_within && end_within;
    }

    public boolean test3jAngles(float a, float b, float c) {
        for (ConvexPolygon p : this.polygons) {
            p.is_colliding = false;
            p.is_in_bounds = true;
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

        boolean end1 = this.checkCollision("end", "1");
        boolean endroot = this.checkCollision("end", "root");
        boolean endground = this.checkCollision("end", "ground");
        boolean dosroot = this.checkCollision("2", "root");
        boolean unoground = this.checkCollision("1", "ground");
        boolean dosground = this.checkCollision("2", "ground");

        boolean within = this.checkWithinBounds();
        
        return end1 || endroot || endground || dosroot || dosground || unoground || !within;
    }

    public boolean test3jGlobalAngles(float a, float b, float c) {
        return this.test3jAngles(a, b-a, c-b);
    }
}
