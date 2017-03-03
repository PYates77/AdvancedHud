public class Wall {
    private Point edge1;
    private Point edge2;
    private Plane plane;
    private boolean isValid;
    
    public Wall(Cluster cluster) {
        isValid = false;
        plane = cluster.getPlane();
        edge1 = cluster.getCentroid();
    }
    
    public void update(Cluster cluster) {
        
    }
    
    public double[] sendData() {
        double[] out = {0, 0, 0, 0};
        
        return out;
    }
}
