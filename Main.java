class Main {
    public static void main(String[] args) {
        Sim sim = new Sim();

        // used to build simulated arm (must be named root, 1, 2, and end if you want to use sim.test3jAngles)
        // vertices are offset from origin (3rd argument)
        // 4th and 5th arguments are mate_1 and mate_2 respectively (where each part's joint will be)
        sim.addPolygon("root", new float[][]{{-1,-1},{1,-1},{1,1},{-1,1}}, new float[]{0,0}, new float[]{0,0}, new float[]{0f,0});
        sim.addPolygon("1", new float[][]{{-2,-2},{2,-2},{2,4},{-2,4}}, new float[]{-2,8}, new float[]{0,0}, new float[]{0f,4});
        sim.addPolygon("2", new float[][]{{-2,-2},{2,-2},{2,4},{-2,4}}, new float[]{-7,4}, new float[]{0,0}, new float[]{0f,4});
        sim.addPolygon("end", new float[][]{{-2,-2},{2,-2},{2,4},{-2,4}}, new float[]{6,8}, new float[]{0,0}, new float[]{0f,4});

        // what's connected to what (first polygon's mate_1 is constrained to second's mate_2)
        sim.constrain("1", "root");
        sim.constrain("2", "1");
        sim.constrain("end", "2");

        // when used allows visualization of simulation onto JFrame
        sim.createWindow(800, 500, 20);

        // tests a set of angles, angle between root and joint 1, joint 1 and joint 2, joint 2 and end effector respectively
        // assumes that joint 2 is fully passable, nothing is able to pass through root
        System.out.println(sim.test3jAngles(45, 135, 60));

        // test loop for fun
        float a = 0;
        float b = 0;
        float c = 0;
        
        /*
        while (true) {
            a += 0.01f;
            b += 0.05f;
            c += 0.0075f;

            System.out.println(sim.test3jAngles(a, b, c));
        }
        */
    }
}