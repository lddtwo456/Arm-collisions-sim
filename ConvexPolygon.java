import java.awt.Color;
import java.awt.Graphics;
import java.awt.Polygon;

class ConvexPolygon {
    float[][] vertices;

    float[] origin_pos;
    float[] mate_1;
    float[] mate_2;

    boolean is_constrained;
    boolean is_constrained_to;
    ConvexPolygon constrainedTo;
    ConvexPolygon constrainedToThis;

    String name;

    public ConvexPolygon(String name, float[][] vertices, float[] origin_pos, float[] mate_1, float[] mate_2) {
        this.vertices = vertices;
        this.origin_pos = origin_pos;

        if (this.vertices.length > 1) {
            for (float[] vert : this.vertices) {
                vert[0] += this.origin_pos[0];
                vert[1] += this.origin_pos[1];
            }
        }

        this.name = name;

        this.is_constrained = false;
        this.is_constrained_to = false;

        this.mate_1 = mate_1;
        this.mate_2 = mate_2;
        
        if (this.origin_pos.length > 1) {
            this.mate_1[0] += this.origin_pos[0];
            this.mate_1[1] += this.origin_pos[1];
            this.mate_2[0] += this.origin_pos[0];
            this.mate_2[1] += this.origin_pos[1];
        }
    }

    public void draw(Graphics g, boolean draw_mates, float scale, float screen_width, float screen_height) {
        int[] x_points = new int[this.vertices.length];
        int[] y_points = new int[this.vertices.length];

        for (int i = 0; i < this.vertices.length; i++) {
            x_points[i] = Math.round(this.getVertX(i) * scale + (screen_width/2));
            y_points[i] = Math.round(-this.getVertY(i) * scale + (screen_height/2));
        }

        g.setColor(Color.WHITE);
        g.drawPolygon(new Polygon(x_points, y_points, this.vertices.length));

        if (draw_mates) {
            g.setColor(Color.RED);
            g.fillOval(Math.round(this.getMate1X() * scale + (screen_width/2)), Math.round(-this.getMate1Y() * scale + (screen_height/2)), 5, 5);
            g.fillOval(Math.round(this.getMate2X() * scale + (screen_width/2)), Math.round(-this.getMate2Y() * scale + (screen_height/2)), 5, 5);
        }
    }







    // MOVEMENT/CONSTRAINTS THINGS







    public void move(float x, float y) {
        if (!is_constrained) {
            for (float[] vert : this.vertices) {
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

    public void moveIgnoreConstraints(float x, float y) {
        for (float[] vert : this.vertices) {
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

    public static float[] rotatePoint(float x, float y, float cx, float cy, float deg) {
        float nx = (x-cx)*(float) Math.cos(Math.toRadians((double) deg)) - (y-cy)*(float) Math.sin(Math.toRadians((double) deg));
        float ny = (y-cy)*(float) Math.cos(Math.toRadians((double) deg)) + (x-cx)*(float) Math.sin(Math.toRadians((double) deg));

        return new float[]{nx+cx, ny+cy};
    }

    public void rotate(float deg) {
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
        return (this.isOverlapping(p) && p.isOverlapping(this));
    }

    public boolean isOverlapping(ConvexPolygon p) {
        boolean result = true;
        for (int i = 0; i < this.vertices.length; i++) {
            // get perpendicular line
            float[][] l;
            try {
                l = ConvexPolygon.getPerpendicular(new float[][]{{this.getVertX(i), this.getVertY(i)}, {this.getVertX(i+1), this.getVertY(i+1)}});
            } catch (Exception e) {
                l = ConvexPolygon.getPerpendicular(new float[][]{{this.getVertX(i), this.getVertY(i)}, {this.getVertX(0), this.getVertY(0)}});
            }

            // get projected lines
            float[][] this_projection = ConvexPolygon.getBounds(this.projectOnLine(l));
            float[][] p_projection = ConvexPolygon.getBounds(p.projectOnLine(l));

            if (!ConvexPolygon.lineIsOverlapping(this_projection, p_projection)) {
                result = false;
            }
        }
        return result;
    }

    public static float[][] getPerpendicular(float[][] l) {
        float[] midpoint = new float[]{(l[0][0]+l[1][0])/2, (l[0][1]+l[1][1])/2};

        return (new float[][]{{-1*(l[0][1] - midpoint[1]) + midpoint[1], l[0][0]}, {-1*(l[1][1] - midpoint[1]) + midpoint[1], l[1][0]}});
    }

    public float[][] projectOnLine(float[][] l) {
        float d_sqr = (l[0][0]-l[1][0])*(l[0][0]-l[1][0]) + (l[0][1]-l[1][1])*(l[0][1]-l[1][1]);

        // min, max
        float[][] projection = new float[this.vertices.length][2];

        for (int i = 0; i < this.vertices.length; i++) {
            float[] vert = this.getVert(i);

            float t = ((vert[0] - l[0][0]) * (l[1][0] - l[0][0])) / d_sqr + ((vert[1] - l[0][1]) * (l[1][1] - l[0][1])) / d_sqr;
            float[] projected_point = new float[]{l[0][0] + t * (l[1][0] - l[0][0]), l[0][1] + t * (l[1][1] - l[0][1])};

            projection[i] = projected_point;
        }

        return projection;
    }

    public static float[][] getBounds(float[][] l) {
        // min max
        float[][] bounds = new float[][]{l[0], l[0]};

        // check if vertical
        boolean vertical = true;
        if (l[0][0] != l[1][0]) {
            vertical = false;
        }

        // if not vertical, sort by x, else sort by y
        if (!vertical) {
            for (float[] vert : l) {
                if (vert[0] > bounds[1][0]) {
                    bounds[1] = vert;
                } if (vert[0] < bounds[0][0]) {
                    bounds[0] = vert;
                }
            }
        } else {
            for (float[] vert : l) {
                if (vert[1] > bounds[1][1]) {
                    bounds[1] = vert;
                } if (vert[1] < bounds[0][1]) {
                    bounds[0] = vert;
                }
            }
        }

        return bounds;
    }

    public static boolean lineIsOverlapping(float[][] l1, float[][] l2) {
        // if not vertical

        if (l1[0][0] != l1[1][0]) {
            if ((l2[0][0] <= l1[1][0]) && (l2[0][0] >= l1[0][0])) {
                return true;
            } else if ((l2[1][0] <= l1[1][0]) && (l2[1][0] >= l1[0][0])) {
                return true;
            }
        } else {
            if ((l2[0][1] <= l1[1][1]) && (l2[0][1] >= l1[0][1])) {
                return true;
            } else if ((l2[1][1] <= l1[1][1]) && (l2[1][1] >= l1[0][1])) {
                return true;
            }
        }

        return false;
    }







    // GENERAL PURPOSE







    public float[] getVert(int i) {
        return new float[]{this.getVertX(i), this.getVertY(i)};
    }
;
    public float getVertX(int i) {
        return this.vertices[i][0];
    }

    public float getVertY(int i) {
        return this.vertices[i][1];
    }

    public float[] getMate1() {
        return new float[]{this.getMate1X(), this.getMate1Y()};
    }

    public float getMate1X() {
        return this.mate_1[0];
    }

    public float getMate1Y() {
        return this.mate_1[1];
    }

    public float[] getMate2() {
        return new float[]{this.getMate2X(), this.getMate2Y()};
    }

    public float getMate2X() {
        return this.mate_2[0];
    }

    public float getMate2Y() {
        return this.mate_2[1];
    }
}