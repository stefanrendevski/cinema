package org.example.domain;


public record Point(int x, int y) {
  public int distanceTo(Point other) {
    return Math.abs(x - other.x) + Math.abs(y - other.y);
  }
}
