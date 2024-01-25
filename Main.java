class Main {
    public static void main(String[] args) {
        Sim sim = new Sim();

        sim.addPolygon("strt", new float[][]{{-2,-2},{2,-2},{2,2},{-2,2}}, new float[]{0,0}, new float[]{0,0}, new float[]{0.5f,2});
        sim.addPolygon("end", new float[][]{{-2,-2},{2,-2},{2,2},{-2,2}}, new float[]{-2,8}, new float[]{0,0}, new float[]{0.5f,2});

        sim.constrain("end", "strt");

        sim.setScale(20);
        sim.createWindow(800, 500);
    }
}