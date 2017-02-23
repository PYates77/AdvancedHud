public class Plane {
  public Plane(Point a, Point b, Point c) {
    Point ab = new Point(a.x - b.x, a.y - b.y, a.z - b.z);
    Point ac = new Point(a.x - c.x, a.y - c.y, a.z - c.z);
    Point ab_x_ac = new Point(ab.y*ac.z - ab.z*ac.y);
  }
}