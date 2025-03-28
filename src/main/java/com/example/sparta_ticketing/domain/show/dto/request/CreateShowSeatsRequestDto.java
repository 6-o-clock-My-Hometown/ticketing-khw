package com.example.sparta_ticketing.domain.show.dto.request;

import com.example.sparta_ticketing.domain.seat.enums.SeatEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class CreateShowSeatsRequestDto {
    private SeatEnum seatName;
    private int seatCount;
    private int seatPrice;
}
