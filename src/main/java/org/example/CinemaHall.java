package org.example;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class CinemaHall {

  public record Point(int x, int y) {
    public int distanceTo(Point other) {
      return Math.abs(x - other.x) + Math.abs(y - other.y);
    }
  }

  @Getter
  @ToString
  @EqualsAndHashCode
  public final class Seat {
    private final Point point;
    private final AtomicBoolean reserved;

    public Seat(int x, int y) {
      this.point = new Point(x, y);
      this.reserved = new AtomicBoolean(false);
    }

    public int distanceTo(Seat other) {
      return point.distanceTo(other.point);
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
  }

  private final Seat[][] seats;
  private final Seat center;

  public CinemaHall(int rows, int cols) {
    this.seats = new Seat[rows][cols];
    for (int i = 0; i < rows; i++) {
      for (int j = 0; j < cols; j++) {
        seats[i][j] = new Seat(i, j);
      }
    }

    this.center = seats[rows / 2][cols / 2];
  }

  public Seat center() {
    return center;
  }

  public Seat get(int x, int y) {
    return seats[x][y];
  }

  public Stream<Seat> allAtDistance(Seat origin, int distance) {
    Point point = origin.point;

    int minX = Math.max(0, point.x - distance);
    int maxX = Math.min(seats.length - 1, point.x + distance);
    int minY = Math.max(0, point.y - distance);
    int maxY = Math.min(seats.length - 1, point.y + distance);

    return IntStream.rangeClosed(minX, maxX)
      .mapToObj(i -> IntStream.rangeClosed(minY, maxY)
        .mapToObj(j -> seats[i][j]))
      .flatMap(Function.identity())
      .filter(seat -> seat.distanceTo(origin) == distance);
  }

  // Search from center
  public Seat getNextAvailableTicket() {
    int rows = seats.length;
    int cols = seats[0].length;
    int maxPossibleDistance = rows + cols - 2;

    for (int distance = 0; distance <= maxPossibleDistance; distance++) {
      Seat found = allAtDistance(center, distance)
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
