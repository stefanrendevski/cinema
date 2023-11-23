package org.example;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Stream;

public class CinemaHall {

  public record Point(int x, int y) {
    public int distanceTo(Point other) {
      return Math.abs(x - other.x) + Math.abs(y - other.y);
    }

    public Collection<Point> withinDistance() {
      return List.of(
        new Point(x + 1, y),
        new Point(x - 1, y),
        new Point(x, y + 1),
        new Point(x, y - 1)
      );
    }
  }

  @Getter
  @ToString
  @EqualsAndHashCode
  public final class Seat {
    private final Point point;
    @EqualsAndHashCode.Exclude
    private final AtomicBoolean reserved;
    @EqualsAndHashCode.Exclude
    private final int distanceToCenter;

    public Seat(int x, int y) {
      this.point = new Point(x, y);
      this.reserved = new AtomicBoolean(false);
      this.distanceToCenter = CinemaHall.this.center.distanceTo(point);
    }

    public int distanceTo(Seat other) {
      if (other == center() && this == center()) {
        return distanceToCenter;
      } else {
        return point.distanceTo(other.point);
      }
    }

    public boolean isReserved() {
      return reserved.get();
    }

    public boolean reserve() {
      return reserved.compareAndSet(false, true);
    }

    public boolean isAvailable() {
      return !isReserved() && CinemaHall.this
        .allAtDistance(this, 1)
        .noneMatch(Seat::isReserved);
    }

    public Collection<Seat> withinDistance() {
      return point.withinDistance().stream()
        .map(point -> CinemaHall.this.get(point.x, point.y))
        .filter(Objects::nonNull)
        .toList();
    }
  }

  private final Seat[][] seats;
  private final Point center;
  private final Seat centerSeat;

  public CinemaHall(int rows, int cols) {
    this.seats = new Seat[rows][cols];
    this.center = new Point(rows / 2, cols / 2);

    for (int i = 0; i < rows; i++) {
      for (int j = 0; j < cols; j++) {
        seats[i][j] = new Seat(i, j);
      }
    }

    this.centerSeat = seats[center.x][center.y];
  }

  public Seat center() {
    return centerSeat;
  }

  public Seat get(int x, int y) {
    int rows = seats.length;
    int cols = seats[0].length;

    if (x < 0 || y < 0) {
      return null;
    }

    if (x >= rows || y >= cols) {
      return null;
    }

    return seats[x][y];
  }

  public Stream<Seat> allAtDistance(Seat origin, int distance) {
    Point point = origin.point;

    int minX = Math.max(0, point.x - distance);
    int maxX = Math.min(seats.length - 1, point.x + distance);
    int minY = Math.max(0, point.y - distance);
    int maxY = Math.min(seats.length - 1, point.y + distance);

    List<Seat> result = new ArrayList<>();
    for (int i = minX; i <= maxX; i++) {
      for (int j = minY; j <= maxY; j++) {
        if (seats[i][j].distanceTo(origin) == distance) {
          result.add(seats[i][j]);
        }
      }
    }

    return result.stream();
  }

  // Search from center
  public Seat getNextAvailableTicket() {
    int rows = seats.length;
    int cols = seats[0].length;
    int maxPossibleDistance = rows + cols - 2;

    for (int distance = 0; distance <= maxPossibleDistance; distance++) {
      Seat found = allAtDistance(center(), distance)
        .filter(Seat::isAvailable)
        .filter(Seat::reserve)
        .findAny()
        .orElse(null);

      if (found != null) {
        return found;
      }
    }

    return null;
  }

  public Seat getNextAvailableTicket(Point preferredSeat) {
    int rows = seats.length;
    int cols = seats[0].length;
    int maxPossibleDistance = rows + cols - 2;

    for (int distance = 0; distance <= maxPossibleDistance; distance++) {
      Seat found = allAtDistance(get(preferredSeat.x, preferredSeat.y), distance)
        .sorted(Comparator.comparingInt(a -> a.distanceToCenter))
        .filter(Seat::isAvailable)
        .filter(Seat::reserve)
        .findAny()
        .orElse(null);

      if (found != null) {
        return found;
      }
    }

    return null;
  }
}
