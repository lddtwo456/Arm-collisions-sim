import java.awt.Graphics;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

class Main {
    public static void main(String[] args) {
        // ppi (pixels per inch)
        float scale = 20;

        ArrayList<ConvexPolygon> polygons = new ArrayList<ConvexPolygon>();
        polygons.add(new ConvexPolygon("john", new float[][]{{-2,-2},{2,-2},{2,2},{-2,2}}, new float[]{0,0}, new float[]{0,0}, new float[]{0.5f,2}));
        polygons.add(new ConvexPolygon("end", new float[][]{{-2,-2},{2,-2},{2,2},{-2,2}}, new float[]{-2,8}, new float[]{0,0}, new float[]{0.5f,2}));

        polygons.get(1).constrainTo(polygons.get(0));

        Win win = new Win(800+16, 600+38, scale, polygons);
        
        while (true) {
            win.repaint();
        }
    }
}