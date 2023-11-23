package org.example.persistence;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class Seat {
  private Long id;
  private int seatRow;
  private int column;
  private int distanceToCenter;
  private boolean reserved;

  @Id
  @GeneratedValue
  public Long getId() {
    return id;
  }
}
