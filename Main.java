class Main {
    public static void main(String[] args) {
        Sim sim = new Sim();

        // used to build simulated arm (must be named root, 1, 2, and end if you want to use sim.test3jAngles)
        // vertices are offset from origin (3rd argument)
        // 4th and 5th arguments are mate_1 and mate_2 respectively (where each part's joint will be)
        sim.addPolygon("root", new double[][]{{0,0.65f},{35.25f,0.65f},{35.25f,5.775f},{0,5.775f}}, new double[]{-17.675f,-12f}, new double[]{0,0}, new double[]{29,5.687f});
        sim.addPolygon("1", new double[][]{{-1,-1},{17,-1},{17,1},{-1,1}}, new double[]{-2,8}, new double[]{0,0}, new double[]{16f,0});
        sim.addPolygon("2", new double[][]{{-1,-1},{17f,-1},{17f,1},{-1,1}}, new double[]{-7,4}, new double[]{0,0}, new double[]{16,0});
        sim.addPolygon("end", new double[][]{{-8.235f, -0.814f}, {-1.501f, 0.356f}, {-0.717f, 1.248f}, {0.269f, 1.414f}, {1.385f, 0.841f}, {3.148f, 1.137f}, {3.999f, 2.483f}, {6.366f, 2.881f}, {7.906f, 1.92f}, {7.906f, -6.208f}, {5.962f, -7.499f}, {1.092f, -7.079f}, {-0.646f, -5.569f}, {-1.559f, -5.49f}, {-2.321f, -6.204f}, {-3.317f, -6.118f}, {-3.902f, -5.288f}, {-9.614f, -4.195f}, {-10.238f, -3.458f}}, new double[]{-3,-3}, new double[]{0,0}, new double[]{4.357f,-2.287f});

        // what's connected to what (first polygon's mate_1 is constrained to second's mate_2)
        sim.constrain("1", "root");
        sim.constrain("2", "1");
        sim.constrain("end", "2");

        // creates ground and bounds
        sim.init();

        // when used allows visualization of simulation onto JFrame
        sim.createWindow(800, 500, 5);

        // tests a set of angles, angle between root and joint 1, joint 1 and joint 2, joint 2 and end effector respectively
        // assumes that joint 2 is fully passable, nothing is able to pass through root
        sim.addPosition("default", 160, 40, 0);
        sim.addPosition("intaking", 110, 190, 45);
        sim.addPosition("shooting", 130, 20, 0);
        sim.addPosition("amp", 80, 80, -60);
        sim.addPosition("broken", 0, 0, 0);
        sim.testPosition("default");

        // what would really be used in move function
        sim.generatePath("intaking", 10, false);
        sim.generatePath("shooting", 10, false);
        sim.generatePath("amp", 10, false);
        sim.generatePath("default", 10, false);

        sim.moveTo("intaking");
        sim.moveTo("shooting");
        sim.moveTo("amp");
        sim.moveTo("default");
        sim.ikAim(50, 20);
    }
}