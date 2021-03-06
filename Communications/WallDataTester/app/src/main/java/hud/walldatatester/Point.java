package hud.walldatatester;

public class Point {
  
 private int index = -1; //denotes which Cluster it belongs to
 public double x, y, z;
  
 public Point(double x, double y, double z) {
  this.x = x;
  this.y = y;
  this.z = z;
 }

 public void add(Point p) {
  x += p.x;
  y += p.y;
  z += p.z;
 }

 public double dist2D(Point p) {
   double out = 0;
   
   return Math.sqrt(Math.pow(x - p.x, 2) + Math.pow(y - p.y, 2));
 }
  
 public Double getSquareOfDistance(Point anotherPoint){
  return  (x - anotherPoint.x) * (x - anotherPoint.x)
    + (y - anotherPoint.y) *  (y - anotherPoint.y) 
    + (z - anotherPoint.z) *  (z - anotherPoint.z);
 }
 
 public int getIndex() {
  return index;
 }
 
 public void setIndex(int index) {
  this.index = index;
 }
  
 public String toString(){
  return "(" + x + "," + y + "," + z + ")";
 }

 @Override
 public boolean equals(Object other) {
  boolean result = false;
  if (other instanceof Point) {
   Point that = (Point) other;
   result = x == that.x && y == that.y && z == that.z;
  }
  return result;
 }
}
