package com.projecttango.examples.java.hellodepthperception;

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
        if (cluster == null)
            return;
        
        if (!isValid) {
            edge2 = cluster.getCetroid();
            isvalid = true;
        }
        
        Point mid = new Point(0.5*edge1.x + 0.5*edge2.x, 0.5*edge1 + 0.5*edge2);
        if (mid.dist2D(cluster.getCentroid()) > mid.dist2D(edge1)) {
            if (edge1.dist2D(cluster.getCentroid()) > edge2.dist2D(cluster.getCentroid()))
                edge2 = cluster.getCentroid();
            else
                edge1 = cluster.getCentroid();
        }
        plane.setDirection(new Point(plane.getDirection().x*0.5 + cluster.getPlane().getDirection().x*0.5, plane.getDirection().x*0.5
                                     + cluster.getPlane().getDirection().y*0.5, plane.getDirection().z*0.5
                                     + cluster..getPlane().getDirection().z*0.5)); // averaging the plane equation
        plane.setShift(cluster.getPlane().getShift()*0.5 + plane.getShift()*0.5);
    }
    
    public double[] sendData() {
        double[] out = {edge1.x, edge1.y, edge2.x, edge2.y};
        
        return out;
    }
}
