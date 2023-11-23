package org.example.persistence;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class Seat {
  private Long id;
  private int seatRow;
  private int column;
  private int distanceToCenter;
  private boolean reserved;

  public Seat(int seatRow, int column, int distanceToCenter, boolean reserved) {
    this.seatRow = seatRow;
    this.column = column;
    this.distanceToCenter = distanceToCenter;
    this.reserved = reserved;
  }

  @Id
  @GeneratedValue
  public Long getId() {
    return id;
  }
}
