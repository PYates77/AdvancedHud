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
        double[] out = {edge1.x, edge1.y, edge2.x, edge2.y};
        
        return out;
    }
}
