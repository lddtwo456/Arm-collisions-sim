import java.awt.Color;
import java.awt.Graphics;
import java.awt.Polygon;

class ConvexPolygon {
    double[][] vertices;

    double[] origin_pos;
    double[] mate_1;
    double[] mate_2;

    double angle;

    boolean is_constrained;
    boolean is_constrained_to;
    ConvexPolygon constrainedTo;
    ConvexPolygon constrainedToThis;

    boolean is_colliding;
    boolean is_in_bounds;

    String name;

    public ConvexPolygon(String name, double[][] vertices, double[] origin_pos, double[] mate_1, double[] mate_2) {
        this.vertices = vertices;
        this.origin_pos = origin_pos;
        this.is_colliding = false;
        this.is_in_bounds = true;
        this.angle = 0;

        if (this.vertices != null) {
            for (double[] vert : this.vertices) {
                vert[0] += this.origin_pos[0];
                vert[1] += this.origin_pos[1];
            }
        }

        this.name = name;

        this.is_constrained = false;
        this.is_constrained_to = false;

        this.mate_1 = mate_1;
        this.mate_2 = mate_2;
        
        if (this.origin_pos != null) {
            this.mate_1[0] += this.origin_pos[0];
            this.mate_1[1] += this.origin_pos[1];
            this.mate_2[0] += this.origin_pos[0];
            this.mate_2[1] += this.origin_pos[1];
        }
    }

    public void draw(Graphics g, boolean draw_mates, double scale, double screen_width, double screen_height) {
        int[] x_points = new int[this.vertices.length];
        int[] y_points = new int[this.vertices.length];

        for (int i = 0; i < this.vertices.length; i++) {
            x_points[i] = (int) Math.round(this.getVertX(i) * scale + (screen_width/2));
            y_points[i] = (int) Math.round(-this.getVertY(i) * scale + (screen_height/2));
        }

        if (!is_colliding) {
            if (this.name == "limits" || !this.is_in_bounds) {
                g.setColor(Color.BLUE);
            } else {
                g.setColor(Color.WHITE);
            }
            g.drawPolygon(new Polygon(x_points, y_points, this.vertices.length));
        } else {
            g.setColor(Color.RED);
            g.drawPolygon(new Polygon(x_points, y_points, this.vertices.length));
        }

        if (draw_mates) {
            g.setColor(Color.RED);
            g.fillOval((int) Math.round(this.getMate1X() * scale + (screen_width/2)), (int) Math.round(-this.getMate1Y() * scale + (screen_height/2)), 5, 5);
            g.fillOval((int) Math.round(this.getMate2X() * scale + (screen_width/2)), (int) Math.round(-this.getMate2Y() * scale + (screen_height/2)), 5, 5);
        }
    }


    // MOVEMENT/CONSTRAINTS THINGS


    public void move(double x, double y) {
        if (!is_constrained) {
            for (double[] vert : this.vertices) {
                vert[0] += x;
                vert[1] += y;
            }

            this.origin_pos[0] += x;
            this.origin_pos[1] += y;
            this.mate_1[0] += x;
            this.mate_1[1] += y;
            this.mate_2[0] += x;
            this.mate_2[1] += y;

            if (is_constrained_to) {
                this.updateConstrainedToThis();
            }
        }
    }

    public void moveIgnoreConstraints(double x, double y) {
        for (double[] vert : this.vertices) {
            vert[0] += x;
            vert[1] += y;
        }

        this.origin_pos[0] += x;
        this.origin_pos[1] += y;
        this.mate_1[0] += x;
        this.mate_1[1] += y;
        this.mate_2[0] += x;
        this.mate_2[1] += y;
    }

    public static double[] rotatePoint(double x, double y, double cx, double cy, double deg) {
        double nx = (x-cx)*Math.cos(Math.toRadians(deg)) - (y-cy)*Math.sin(Math.toRadians(deg));
        double ny = (y-cy)*Math.cos(Math.toRadians(deg)) + (x-cx)*Math.sin(Math.toRadians(deg));

        return new double[]{nx+cx, ny+cy};
    }

    public void rotate(double deg) {
        this.angle += deg;

        for (int i = 0; i < this.vertices.length; i++) {
            this.vertices[i] = ConvexPolygon.rotatePoint(this.getVertX(i), this.getVertY(i), this.getMate1X(), this.getMate1Y(), deg);
        }

        this.mate_2 = ConvexPolygon.rotatePoint(this.getMate2X(), this.getMate2Y(), this.getMate1X(), this.getMate1Y(), deg);
        this.origin_pos = ConvexPolygon.rotatePoint(this.origin_pos[0], this.origin_pos[1], this.getMate1X(), this.getMate1Y(), deg);

        if (is_constrained_to) {
            constrainedToThis.rotate(deg);
            this.updateConstrainedToThis();
        }
    }

    public void rotateAroundMate2(double deg) {
        this.angle += deg;

        for (int i = 0; i < this.vertices.length; i++) {
            this.vertices[i] = ConvexPolygon.rotatePoint(this.getVertX(i), this.getVertY(i), this.getMate2X(), this.getMate2Y(), deg);
        }

        this.mate_1 = ConvexPolygon.rotatePoint(this.getMate1X(), this.getMate1Y(), this.getMate2X(), this.getMate2Y(), deg);
        this.origin_pos = ConvexPolygon.rotatePoint(this.origin_pos[0], this.origin_pos[1], this.getMate2X(), this.getMate2Y(), deg);

        if (is_constrained_to) {
            constrainedToThis.rotate(deg);
            this.updateConstrainedToThis();
        }
    }

    public void constrainTo(ConvexPolygon p) {
        this.constrainedTo = p;
        p.constrainedToThis = this;
        p.is_constrained_to = true;
        this.move((p.mate_2[0] - this.mate_1[0]), (p.mate_2[1] - this.mate_1[1]));

        this.is_constrained = true;
    }

    public void updateConstrainedToThis() {
        constrainedToThis.moveIgnoreConstraints((this.mate_2[0] - constrainedToThis.mate_1[0]), (this.mate_2[1] - constrainedToThis.mate_1[1]));

        if (constrainedToThis.is_constrained_to) {
            constrainedToThis.updateConstrainedToThis();
        }
    }


    // COLLISIONS MATH


    public boolean isColliding(ConvexPolygon p) {
        if (!this.is_colliding) {
            this.is_colliding = this.isOverlapping(p, false) && p.isOverlapping(this, false);
        }
        if (!p.is_colliding) {
            p.is_colliding = this.is_colliding;
        }

        if (this.name == "1") {
            if (this.angle % 360 < 15 || this.angle % 360 > 165) {
                this.is_colliding = true;
            }
        }

        return this.is_colliding;
    }

    public boolean isWithin(ConvexPolygon p) {
        this.is_in_bounds = this.isOverlapping(p, true) && p.isOverlapping(this, true);
        return this.is_in_bounds;
    }

    public boolean isOverlapping(ConvexPolygon p, boolean fully) {
        for (int i = 0; i < this.vertices.length; i++) {
            // get perpendicular line
            double[][] l;
            try {
                l = ConvexPolygon.getPerpendicular(new double[][]{{this.getVertX(i), this.getVertY(i)}, {this.getVertX(i+1), this.getVertY(i+1)}});
            } catch (Exception e) {
                l = ConvexPolygon.getPerpendicular(new double[][]{{this.getVertX(i), this.getVertY(i)}, {this.getVertX(0), this.getVertY(0)}});
            }

            // get projected lines
            double[] this_projection = ConvexPolygon.getBounds(this.getTOnLine(l));
            double[] p_projection = ConvexPolygon.getBounds(p.getTOnLine(l));

            if (fully) {
                if (!ConvexPolygon.tIsFullyWithin(this_projection, p_projection)) {
                    return false;
                }
            } else {
                if (!ConvexPolygon.tIsOverlapping(this_projection, p_projection)) {
                    return false;
                }
            }
        }

        return true;
    }

    public static double[][] getPerpendicular(double[][] l) {
        double[] midpoint = new double[]{(l[0][0]+l[1][0])/2, (l[0][1]+l[1][1])/2};

        return (new double[][]{{-1*(l[0][1] - midpoint[1]) + midpoint[1], l[0][0]}, {-1*(l[1][1] - midpoint[1]) + midpoint[1], l[1][0]}});
    }

    public double[] getTOnLine(double[][] l) {
        double d_sqr = (l[0][0]-l[1][0])*(l[0][0]-l[1][0]) + (l[0][1]-l[1][1])*(l[0][1]-l[1][1]);

        // min, max
        double[] projection_t = new double[this.vertices.length];

        for (int i = 0; i < this.vertices.length; i++) {
            double[] vert = this.getVert(i);
            
            projection_t[i] = ((vert[0] - l[0][0]) * (l[1][0] - l[0][0])) / d_sqr + ((vert[1] - l[0][1]) * (l[1][1] - l[0][1])) / d_sqr;
        }

        return projection_t;
    }

    public static double[] getBounds(double[] l) {
        // min max
        double[] bounds = new double[]{l[0], l[0]};

        // sort by t
        for (double t : l) {
            if (t > bounds[1]) {
                bounds[1] = t;
            } if (t < bounds[0]) {
                bounds[0] = t;
            }
        }

        return bounds;
    }

    public static boolean tIsOverlapping(double[] l1, double[] l2) {
        if (l1[0] <= l2[1] && l1[0] >= l2[0]) {
            return true;
        } if (l1[1] <= l2[1] && l1[1] >= l2[0]) {
            return true;
        } if (l2[0] <= l1[1] && l2[0] >= l1[0]) {
            return true;
        } if (l2[1] <= l1[1] && l2[1] >= l1[0]) {
            return true;
        }

        return false;
    }

    public static boolean tIsFullyWithin(double[] l1, double[] l2) {
        if ((l1[0] <= l2[1] && l1[0] >= l2[0]) && (l1[1] <= l2[1] && l1[1] >= l2[0])) {
            return true;
        } if ((l2[0] <= l1[1] && l2[0] >= l1[0]) && (l2[1] <= l1[1] && l2[1] >= l1[0])) {
            return true;
        }

        return false;
    }


    // GENERAL PURPOSE


    public double[] getVert(int i) {
        return new double[]{this.getVertX(i), this.getVertY(i)};
    }
;
    public double getVertX(int i) {
        return this.vertices[i][0];
    }

    public double getVertY(int i) {
        return this.vertices[i][1];
    }

    public double[] getMate1() {
        return new double[]{this.getMate1X(), this.getMate1Y()};
    }

    public double getMate1X() {
        return this.mate_1[0];
    }

    public double getMate1Y() {
        return this.mate_1[1];
    }

    public double[] getMate2() {
        return new double[]{this.getMate2X(), this.getMate2Y()};
    }

    public double getMate2X() {
        return this.mate_2[0];
    }

    public double getMate2Y() {
        return this.mate_2[1];
    }
}