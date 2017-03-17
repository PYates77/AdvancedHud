public class Wall2D {
    private Point edge1;
    private Point edge2;
    private Line line;
    private boolean isValid;
    
    public Wall2D(Point e) {
        edge1 = e;
        isValid = false;
    }
    
    public addPoint(Point p, double angleMargin) {
        if (isValid == false) {
            edge2 = p;
            line = new Line(edge1, edge2);
        }
    }
}
