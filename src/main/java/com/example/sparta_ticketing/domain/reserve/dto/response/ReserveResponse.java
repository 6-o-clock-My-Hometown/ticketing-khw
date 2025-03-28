package com.example.sparta_ticketing.domain.reserve.dto.response;

import com.example.sparta_ticketing.domain.reserve.entity.Reserve;
import com.example.sparta_ticketing.domain.seat.enums.SeatEnum;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class ReserveResponse {
    private String nickName;
    private String showTitle;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm")
    private LocalDateTime startTime;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm")
    private LocalDateTime endTime;
    private SeatEnum seat;
    private int price;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm")
    private LocalDateTime reservedAt;

    public static ReserveResponse toDto(Reserve reserve) {
        return new ReserveResponse(
                reserve.getUser().getNickname(),
                reserve.getShow().getTitle(),
                reserve.getShow().getStartDate(),
                reserve.getShow().getEndDate(),
                reserve.getSeatName(),
                reserve.getPrice(),
                reserve.getReservedAt()
        );
    }
}
