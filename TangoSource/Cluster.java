import java.util.*;
 
public class Cluster {
 
 private final List<point> points;
 private Point centroid;
  
 public Cluster(Point firstPoint) {
  points = new ArrayList<point>();
  centroid = firstPoint;
 }
  
 public Point getCentroid(){
  return centroid;
 }
  
 public void updateCentroid(){
  double newx = 0d, newy = 0d, newz = 0d;
  for (Point point : points){
   newx += point.x; newy += point.y; newz += point.z;
  }
  centroid = new Point(newx / points.size(), newy / points.size(), newz / points.size());
 }
  
 public List<point> getPoints() {
  return points;
 }
  
 public String toString(){
  StringBuilder builder = new StringBuilder("This cluster contains the following points:\n");
  for (Point point : points)
   builder.append(point.toString() + ",\n");
  return builder.deleteCharAt(builder.length() - 2).toString(); 
 }
}