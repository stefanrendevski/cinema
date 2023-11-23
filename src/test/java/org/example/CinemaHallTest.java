package org.example;

import org.example.domain.CinemaHall;
import org.example.domain.Point;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class CinemaHallTest {

  @Test
  void center_WhenEvenSquare() {
    var cinemaHall = new CinemaHall(2, 2);
    CinemaHall.Seat center = cinemaHall.center();
    assertEquals(new Point(1, 1), center.getPoint());
  }

  @Test
  void center_WhenUnevenSquare() {
    var cinemaHall = new CinemaHall(3, 3);
    CinemaHall.Seat center = cinemaHall.center();
    assertEquals(new Point(1, 1), center.getPoint());
  }

  @Test
  void center_WhenRectangle() {
    var cinemaHall = new CinemaHall(3, 4);
    CinemaHall.Seat center = cinemaHall.center();
    assertEquals(new Point(1, 2), center.getPoint());

  }

  @Test
  void allWithinRange_HappyPath() {
    var cinemaHall = new CinemaHall(4, 4);

    List<CinemaHall.Seat> withinRange = cinemaHall.allAtDistance(cinemaHall.center(), 1)
      .toList();

    assertThat(withinRange)
      .hasSize(4)
      .extracting(CinemaHall.Seat::getPoint)
      .containsExactlyInAnyOrder(
        new Point(2, 1),
        new Point(1, 2),
        new Point(2, 3),
        new Point(3, 2)
      );
  }

  @Test
  void allWithinRange_WhenNotAllInRange() {
    var cinemaHall = new CinemaHall(4, 4);

    List<CinemaHall.Seat> withinRange = cinemaHall.allAtDistance(cinemaHall.center(), 2)
      .toList();

    assertThat(withinRange)
      .hasSize(6)
      .extracting(CinemaHall.Seat::getPoint)
      .containsExactlyInAnyOrder(
        new Point(0, 2),
        new Point(1, 1),
        new Point(1, 3),
        new Point(2, 0),
        new Point(3, 1),
        new Point(3, 3)
      );
  }

  @Test
  void allWithinRange_WhenNoneInRange() {
    var cinemaHall = new CinemaHall(4, 4);

    List<CinemaHall.Seat> withinRange = cinemaHall.allAtDistance(cinemaHall.center(), 5)
      .toList();

    assertThat(withinRange).hasSize(0);
  }

  @Test
  void getNextAvailableTicket_WhenCenterIsFree() {
    var cinemaHall = new CinemaHall(4, 4);
    CinemaHall.Seat taken = cinemaHall.getNextAvailableTicket();
    assertEquals(cinemaHall.center(), taken);
  }

  @Test
  void getNextAvailableTicket_WhenCenterNotFree() {
    var cinemaHall = new CinemaHall(4, 4);
    cinemaHall.center().reserve();

    CinemaHall.Seat taken = cinemaHall.getNextAvailableTicket();
    assertEquals(2, taken.distanceTo(cinemaHall.center()));
  }

  @Test
  void getNextAvailableTicket_WhenNothingFreeWithinInitialRange() {
    var cinemaHall = new CinemaHall(2, 2);
    cinemaHall.center().reserve();

    CinemaHall.Seat taken = cinemaHall.getNextAvailableTicket();
    assertEquals(new Point(0, 0), taken.getPoint());
  }

  @Test
  void getNextAvailableTicket_WhenNoSeatFree() {
    var cinemaHall = new CinemaHall(2, 2);
    cinemaHall.center().reserve();
    cinemaHall.get(0, 0).reserve();

    CinemaHall.Seat taken = cinemaHall.getNextAvailableTicket();
    assertNull(taken);
  }

  @Test
  void getNextAvailableTicket_BiggerHall() {
    var cinemaHall = new CinemaHall(3000, 2000);
    for (int i = 200; i <= 2800; i++) {
      for (int j = 200; j <= 1800; j++) {
        cinemaHall.get(i, j).reserve();
      }
    }

    long startTime = System.nanoTime();
    CinemaHall.Seat taken = cinemaHall.getNextAvailableTicket(new Point(1900, 1000));
    long endTime = System.nanoTime() - startTime;
    System.out.println(taken + " at distance " + taken.distanceTo(cinemaHall.center()));
    System.out.println("Duration: " + Duration.ofNanos(endTime).toMillis() + " ms");
  }

  @Test
  void getNextAvailableTicket_WhenPreferredSeatFree() {
    var cinemaHall = new CinemaHall(4, 4);
    CinemaHall.Seat taken = cinemaHall.getNextAvailableTicket(new Point(0, 0));
    assertEquals(cinemaHall.get(0, 0), taken);
  }

  @Test
  void getNextAvailableTicket_WhenPreferredSeatNotFree() {
    var cinemaHall = new CinemaHall(4, 4);
    cinemaHall.get(0, 0).reserve();

    CinemaHall.Seat taken = cinemaHall.getNextAvailableTicket(new Point(0, 0));
    assertEquals(cinemaHall.get(1, 1), taken);
  }
}