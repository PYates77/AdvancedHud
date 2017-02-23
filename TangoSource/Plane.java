public class Plane {
  public Point direction;
  public Point intersect;
  
  public Plane(Point a, Point b, Point c) {
    Point ab = new Point(a.x - b.x, a.y - b.y, a.z - b.z);
    Point ac = new Point(a.x - c.x, a.y - c.y, a.z - c.z);
    direction = new Point(ab.y*ac.z - ab.z*ac.y, ab.z*ac.x - ab.x*ac.z, ab.x*ac.y - ab.y*ac.x);
    intersect = a.x*direction*x + a.y*direction*y + a.z*direction*z;
  }

  @Override
  public boolean equals(Object other) {
    boolean result = false;
    if (other instanceof Plane) {
      Plane that = (Plane) other;
      result = direction.equals(result.direction) && intersect.equals(result.intersect);
    }
    return result;
  }
}
