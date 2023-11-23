package org.example.rest;

import org.example.domain.CinemaHall;
import org.example.domain.Point;
import org.example.service.TicketService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.Optional;

@RestController
public class TicketController {

  private final CinemaHall cinemaHall;
  private final TicketService ticketService;

  public TicketController(TicketService ticketService) {
    this.ticketService = ticketService;
    this.cinemaHall = new CinemaHall(20, 100);
  }

  @PostMapping("/ticket")
  public Mono<Seat> getNextAvailableSeat(@RequestBody(required = false) Seat preferredSeat) {
    CinemaHall.Seat reservedSeat = Optional.ofNullable(preferredSeat)
      .map(seat -> new Point(seat.row(), seat.column()))
      .map(cinemaHall::getNextAvailableTicket)
      .orElseGet(cinemaHall::getNextAvailableTicket);

    return Mono.just(new Seat(reservedSeat.getPoint().x(), reservedSeat.getPoint().y()));
  }
}
