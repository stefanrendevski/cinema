package org.example.service;

import org.example.domain.CinemaHall;
import org.example.domain.Point;
import org.example.persistence.CinemaHallRepository;
import org.example.persistence.Seat;
import org.example.persistence.SeatRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TicketService {

  private final CinemaHallRepository cinemaHallRepository;
  private final SeatRepository seatRepository;

  public TicketService(CinemaHallRepository cinemaHallRepository, SeatRepository seatRepository) {
    this.cinemaHallRepository = cinemaHallRepository;
    this.seatRepository = seatRepository;

    CinemaHall initialHall = new CinemaHall(50, 100);
    org.example.persistence.CinemaHall persistentHall = new org.example.persistence.CinemaHall(
      initialHall.rows(),
      initialHall.cols(),
      initialHall.getSeats()
        .stream()
        .map(CinemaHall.Seat::getSeatData)
        .map(seatData -> new Seat(seatData.x(), seatData.y(), seatData.distanceToCenter(), seatData.reserved()))
        .toList()
    );

    cinemaHallRepository.save(persistentHall);
  }

  public CinemaHall.Seat getNextAvailableTicket(Point point) {
    org.example.persistence.CinemaHall persistentHall = cinemaHallRepository.findAll().get(0);
    CinemaHall domainHall = toDomainHall(persistentHall);
    CinemaHall.Seat reservedSeat = domainHall.getNextAvailableTicket(point);

    if (reservedSeat == null) {
      return null; // or throw exception
    }

    saveSeat(persistentHall, reservedSeat);
    return reservedSeat;
  }

  public CinemaHall.Seat getNextAvailableTicket() {
    org.example.persistence.CinemaHall persistentHall = cinemaHallRepository.findAll().get(0);
    CinemaHall domainHall = toDomainHall(persistentHall);
    CinemaHall.Seat reservedSeat = domainHall.getNextAvailableTicket();

    if (reservedSeat == null) {
      return null; // or throw exception
    }

    saveSeat(persistentHall, reservedSeat);
    return reservedSeat;
  }

  private void saveSeat(org.example.persistence.CinemaHall persistentHall, CinemaHall.Seat reservedSeat) {
    Seat persistentSeat = persistentHall.getSeats()
      .stream()
      .filter(seat -> seat.getSeatRow() == reservedSeat.getPoint().x() && seat.getColumn() == reservedSeat.getPoint().y())
      .findAny().orElseThrow();

    persistentSeat.setReserved(true);
    seatRepository.save(persistentSeat);
  }

  private CinemaHall toDomainHall(org.example.persistence.CinemaHall cinemaHall) {
    List<Seat> persistentSeats = cinemaHall.getSeats();
    List<CinemaHall.SeatData> domainSeats = persistentSeats.stream()
      .map(seat -> new CinemaHall.SeatData(seat.getSeatRow(), seat.getColumn(), seat.isReserved(), seat.getDistanceToCenter()))
      .toList();

    return new CinemaHall(cinemaHall.getRows(), cinemaHall.getColumns(), domainSeats);
  }
}
