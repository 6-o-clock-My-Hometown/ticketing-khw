package com.example.sparta_ticketing.domain.reserve.dto.request;

import com.example.sparta_ticketing.domain.seat.enums.SeatEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ReserveRequest {
    private Long showId;
    private SeatEnum seatName;
}
