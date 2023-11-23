package org.example.persistence;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Entity
@Getter
@Setter
public class CinemaHall {

  @Id
  @GeneratedValue
  private Long id;
  private int rows;
  private int columns;
  @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
  private List<Seat> seats;

  public CinemaHall() {}

  public CinemaHall(int rows, int columns, List<Seat> seats) {
    this.rows = rows;
    this.columns = columns;
    this.seats = seats;
  }
}
