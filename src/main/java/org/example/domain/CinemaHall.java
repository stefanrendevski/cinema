package org.example.domain;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Stream;

public class CinemaHall {

  public record SeatData(int x, int y, boolean reserved, int distanceToCenter) {
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

    public Seat(SeatData seatData) {
      this.point = new Point(seatData.x(), seatData.y());
      this.reserved = new AtomicBoolean(seatData.reserved());
      this.distanceToCenter = seatData.distanceToCenter();
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

    public SeatData getSeatData() {
      return new SeatData(point.x(), point.y(), isReserved(), distanceToCenter);
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

    this.centerSeat = seats[center.x()][center.y()];
  }

  public CinemaHall(int rows, int cols, List<SeatData> seats) {
    this.seats = new Seat[rows][cols];
    this.center = new Point(rows / 2, cols / 2);

    for (SeatData seat : seats) {
      this.seats[seat.x()][seat.y()] = new Seat(seat);
    }

    this.centerSeat = this.seats[center.x()][center.y()];
  }

  public Seat center() {
    return centerSeat;
  }

  public int rows() {
    return seats.length;
  }

  public int cols() {
    return seats[0].length;
  }

  public Collection<Seat> getSeats() {
    Collection<Seat> allSeats = new ArrayList<>();

    for (int i = 0; i < rows(); i++) {
      allSeats.addAll(Arrays.asList(seats[i]).subList(0, cols()));
    }

    return allSeats;
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
    DistanceFinder finder = new DistanceFinder(seats.length, seats[0].length, origin.point);
    for (int i = 1; i <= distance; i++) {
      finder.advanceDistance();
    }

    return finder.getAllAtDistance()
      .stream()
      .map(point -> get(point.x(), point.y()))
      .filter(Objects::nonNull);
  }

  // Search from center
  public Seat getNextAvailableTicket() {
    return getNextAvailableTicket(center);
  }

  public Seat getNextAvailableTicket(Point preferredSeat) {
    int rows = seats.length;
    int cols = seats[0].length;
    int maxPossibleDistance = rows + cols - 2;
    DistanceFinder finder = new DistanceFinder(rows, cols, preferredSeat);

    for (int distance = 0; distance <= maxPossibleDistance; distance++) {
      Seat found = finder.getAllAtDistance()
        .stream()
        .map(point -> get(point.x(), point.y()))
        .filter(Objects::nonNull)
        .sorted(Comparator.comparingInt(seat -> seat.distanceToCenter))
        .filter(Seat::isAvailable)
        .filter(Seat::reserve)
        .findAny()
        .orElse(null);

      if (found != null) {
        return found;
      }

      finder.advanceDistance();
    }

    return null;
  }

  private static class DistanceFinder {
    private final int[][] distances;
    private final Point origin;
    private int currentMaxDistance;

    public DistanceFinder(int rows, int cols, Point origin) {
      this.distances = new int[rows][cols];
      this.origin = origin;
      this.currentMaxDistance = 0;

      for (int i = 0; i < rows; i++) {
        for (int j = 0; j < cols; j++) {
          distances[i][j] = -1;
        }
      }

      distances[origin.x()][origin.y()] = 0;
    }

    private int rows() {
      return distances.length;
    }

    private int cols() {
      return distances[0].length;
    }

    private Point getUpperLeftBoundary() {
      int minX = Math.max(0, origin.x() - currentMaxDistance);
      int minY = Math.max(0, origin.y() - currentMaxDistance);
      return new Point(minX, minY);
    }

    private Point getDownRightBoundary() {
      int maxX = Math.min(rows() - 1, origin.x() + currentMaxDistance);
      int maxY = Math.min(cols() - 1, origin.y() + currentMaxDistance);
      return new Point(maxX, maxY);
    }

    public void advanceDistance() {
      currentMaxDistance++;

      Point upperBoundary = getUpperLeftBoundary();
      Point downBoundary = getDownRightBoundary();

      for (int i = upperBoundary.x(); i <= downBoundary.x(); i++) {
        for (int j = upperBoundary.y(); j <= downBoundary.y(); j++) {
          if (distances[i][j] == -1) {
            continue;
          }

          if (i + 1 < rows() && distances[i + 1][j] == -1) {
            distances[i + 1][j] = distances[i][j] + 1;
          }

          if (i - 1 >= 0 && distances[i - 1][j] == -1) {
            distances[i - 1][j] = distances[i][j] + 1;
          }

          if (j + 1 < cols() && distances[i][j + 1] == -1) {
            distances[i][j + 1] = distances[i][j] + 1;
          }

          if (j - 1 >= 0 && distances[i][j - 1] == -1) {
            distances[i][j - 1] = distances[i][j] + 1;
          }
        }
      }
    }

    public Collection<Point> getAllAtDistance() {
      Collection<Point> result = new HashSet<>();

      Point upperBoundary = getUpperLeftBoundary();
      Point downBoundary = getDownRightBoundary();

      for (int i = upperBoundary.x(); i <= downBoundary.x(); i++) {
        for (int j = upperBoundary.y(); j <= downBoundary.y(); j++) {
          if (distances[i][j] == currentMaxDistance) {
            result.add(new Point(i, j));
          }
        }
      }

      return result;
    }
  }
}
