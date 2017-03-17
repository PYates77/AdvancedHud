public class Line {
    private double slope;
    private double intercept;
    
    public Line(double s, double i) {
        slope = s;
        intercept = i;
    }
    
    public Line() {
        slope = 0;
        intercept = 0;
    }
    
    public Line(Line rhs) {
        slope = rhs.slope;
        intercept = rhs.intercept;
    }
    
    public double getSlope() {
        return slope;
    }
    
    public double getIntercept() {
        return intercept;
    }
    
    public void setSlope(double s) {
        slope = s;
    }
    
    public void setIntercept(double i) {
        intercept = i;
    }
    
    public double getAngle(Line rhs) {
        // add code here
    }
}
