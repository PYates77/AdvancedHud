package com.projecttango.examples.java.motiontracking;

public class Plane {
  private Point direction;
  private double shift;

  public Plane() {
    direction = new Point(0, 0, 0);
    shift = 0;
  }

  public Plane(Point a, Point b, Point c) {
    Point ab = new Point(a.x - b.x, a.y - b.y, a.z - b.z);
    Point ac = new Point(a.x - c.x, a.y - c.y, a.z - c.z);
    direction = new Point(ab.y*ac.z - ab.z*ac.y, ab.z*ac.x - ab.x*ac.z, ab.x*ac.y - ab.y*ac.x);
    shift = a.x*direction.x + a.y*direction.y + a.z*direction.z;
  }

  Point getDirection() {
    return direction;
  }

  double getShift() {
    return shift;
  }

  @Override
  public boolean equals(Object other) {
    boolean result = false;
    if (other instanceof Plane) {
      Plane that = (Plane) other;
      result = direction.equals(that.direction) && shift == that.shift;
    }
    return result;
  }

  @Override
  public String toString() {
    return direction.x + "*x + " + direction.y + "*y + " + direction.z + "*z = " + shift;
  }
}