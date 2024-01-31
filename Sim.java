import java.util.Hashtable;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class Sim {
    boolean run;
    boolean visualize;

    float scale;
    Map<String, ConvexPolygon> polygons = new Hashtable<String, ConvexPolygon>();
    Win win;

    float[] rotations = new float[]{0, 0, 0};
    Map<String, float[]> positions = new Hashtable<String, float[]>();

    String current_test_position = "null";

    float[] target_position = new float[]{0,0};

    public Sim() {
        this.visualize = false;
    }

    public void init() {
        ConvexPolygon root = this.polygons.get("root");

        // extension limits
        this.polygons.put("limits", new ConvexPolygon("limits", new float[][]{{-8.625f, 0f}, {43.875f, 0f}, {43.875f, 48f}, {-8.625f, 48f}}, root.origin_pos, new float[]{0, 0}, new float[]{0, 0}));
        // ground
        this.polygons.put("ground", new ConvexPolygon("ground", new float[][]{{-200f, -0.625f}, {200f, -0.625f}, {200f, -100f}, {-200f, -100f}}, new float[]{root.origin_pos[0] - 50, root.origin_pos[1]}, new float[]{0, 0}, new float[]{0, 0}));
    }

    public void update() {
        if (visualize) {
            this.win.repaint();
        }
    }

    public void createWindow(int w, int h, float scale) {
        this.scale = scale;
        visualize = true;
        win = new Win(w+16, h+38, this.scale, this.polygons, this.target_position);
    }

    public void setScale(float scale) {
        this.scale = scale;
    }

    public void addPolygon(String name, float[][] vertices, float[] origin_pos, float[] mate_1, float[] mate_2) {
        this.polygons.put(name, new ConvexPolygon(name, vertices, origin_pos, mate_1, mate_2));
    }

    public void constrain(String name1, String name2) {
        ConvexPolygon p1 = this.polygons.get(name1);
        ConvexPolygon p2 = this.polygons.get(name2);

        try {
            p1.constrainTo(p2);
        } catch (Exception e) {
            System.out.println("could not constrain");
        }
    }

    public void movePolygon(String name, float x, float y) {
        this.polygons.get(name).move(x, y);
    }

    public void rotatePolygon(String name, float deg) {
        this.polygons.get(name).rotate(deg);
    }

    public boolean checkCollision(String name1, String name2) {
        return this.polygons.get(name1).isColliding(this.polygons.get(name2));
    }

    public boolean checkWithinBounds() {
        boolean j1_within = this.polygons.get("1").isWithin(this.polygons.get("limits"));
        boolean j2_within = this.polygons.get("2").isWithin(this.polygons.get("limits"));
        boolean end_within = this.polygons.get("end").isWithin(this.polygons.get("limits"));

        return j1_within && j2_within && end_within;
    }

    public boolean test3jAngles(float a, float b, float c) {
        this.polygons.get("1").is_colliding = false;
        this.polygons.get("1").is_in_bounds = true;
        this.polygons.get("1").rotate(-this.rotations[0]);
        this.polygons.get("1").rotate(a);

        this.polygons.get("2").is_colliding = false;
        this.polygons.get("2").is_in_bounds = true;
        this.polygons.get("2").rotate(-this.rotations[1]);
        this.polygons.get("2").rotate(b);

        this.polygons.get("end").is_colliding = false;
        this.polygons.get("end").is_in_bounds = true;
        this.polygons.get("end").rotate(-this.rotations[2]);
        this.polygons.get("end").rotate(c);

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


    // POSITIONS


    public void addPosition(String name, float a, float b, float c) {
        this.positions.put(name, new float[]{a, b, c});
    }

    public void testPosition(String name) {
        this.current_test_position = name;
        float[] position = this.positions.get(name);
        this.test3jGlobalAngles(position[0], position[1], position[2]);
    }

    public float getShortestAngularDistance(float a, float b) {
        float diff = b - a;

        if (Math.abs((double) diff) <= 180) {
            return diff;
        } else {
            return diff - Math.copySign(360, diff);
        }
    }

    public float[][] generatePath(String target_position, int steps, boolean visualized) {
        float[][] checks = new float[steps+1][3];

        float start_a = this.getGlobalA();
        float start_b = this.getGlobalB();
        float start_c = this.getGlobalC();

        System.out.println("\n"+start_a+" "+start_b+" "+start_c);
        System.out.println(this.getGlobalA(target_position)+" "+this.getGlobalB(target_position)+" "+this.getGlobalC(target_position));

        float a_dist = this.getShortestAngularDistance(start_a, this.getGlobalA(target_position));
        float b_dist = this.getShortestAngularDistance(start_b, this.getGlobalB(target_position));
        float c_dist = this.getShortestAngularDistance(start_c, this.getGlobalC(target_position));

        System.out.println(a_dist+" "+b_dist+" "+c_dist);

        this.current_test_position = "null";
        for (int i = 0; i < steps; i++) {
            if (!this.test3jGlobalAngles(start_a+((i*a_dist)/steps), start_b+((i*b_dist)/steps), start_c+((i*c_dist)/steps))) {
                checks[i] = new float[]{start_a+((i*a_dist)/steps), start_a+((i*a_dist)/steps), start_a+((i*a_dist)/steps)};
            } else {
                System.out.println("COLLISION");
                // do fix if this ever happens
            }

            if (visualized) {
                try {
                    TimeUnit.MILLISECONDS.sleep(5);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
        if (!this.test3jGlobalAngles(this.getGlobalA(target_position), this.getGlobalB(target_position), this.getGlobalC(target_position))) {
            checks[checks.length-1] = new float[]{this.getGlobalA(target_position), this.getGlobalB(target_position), this.getGlobalC(target_position)};
        } else {
            System.out.println("COLLISION");
            // do fix if this ever happens
        }

        this.current_test_position = target_position;

        return checks;
    }

    public void moveTo(String position) {
        this.generatePath(position, 180, true);
    }


    // TARGETING


    public void ikAim(float d, float h) {
        this.target_position[0] = d;
        this.target_position[1] = h;
        win.repaint();

        this.polygons.get("end").rotateAroundMate2(this.getIkAimAngle(d, h));
    }

    public float getIkAimAngle(float d, float h) {
        if (this.current_test_position != "shooting") {
            System.out.println("CANNOT TARGET RIGHT NOW");
            System.out.println("moving...");
            this.moveTo("shooting");
            System.out.println("moved!");
        }

        d = d - this.polygons.get("end").getMate2X();
        h = h - this.polygons.get("end").getMate2Y();

        float theta = (float) Math.toDegrees(Math.atan((double) h/d));

        return theta;
    }


    // ANGLE GETS


    public float getA() {
        return this.positions.get(this.current_test_position)[0];
    }

    public float getB() {
        return this.positions.get(this.current_test_position)[1]+this.getA();
    }

    public float getC() {
        return this.positions.get(this.current_test_position)[2]+this.getB();
    }

    public float getGlobalA() {
        return this.positions.get(this.current_test_position)[0];
    }

    public float getGlobalB() {
        return this.positions.get(this.current_test_position)[1];
    }

    public float getGlobalC() {
        return this.positions.get(this.current_test_position)[2];
    }

    public float getA(String position) {
        return this.positions.get(position)[0];
    }

    public float getB(String position) {
        return this.positions.get(position)[1]+this.getA(position);
    }

    public float getC(String position) {
        return this.positions.get(position)[2]+this.getB(position);
    }

    public float getGlobalA(String position) {
        return this.positions.get(position)[0];
    }

    public float getGlobalB(String position) {
        return this.positions.get(position)[1];
    }

    public float getGlobalC(String position) {
        return this.positions.get(position)[2];
    }
}
