package com.example.sparta_ticketing.domain.show.dto.response;

import com.example.sparta_ticketing.domain.show.entity.Show;
import com.example.sparta_ticketing.domain.show.enums.Category;
import com.example.sparta_ticketing.domain.show.enums.Region;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ShowResponseDto {
    private String title;

    private Category category;

    private String content;

    private Region region;

    private LocalDateTime startDate;

    private LocalDateTime endDate;

    private LocalDateTime reservationStartDate;

    private LocalDateTime reservationEndDate;

    private int totalSeats;

    private long viewCount;

    public static ShowResponseDto toDto(Show show, long viewCount) {
        return ShowResponseDto.builder()
                .title(show.getTitle())
                .category(show.getCategory())
                .region(show.getRegion())
                .startDate(show.getStartDate())
                .endDate(show.getEndDate())
                .reservationStartDate(show.getReservationStartDate())
                .reservationEndDate(show.getReservationEndDate())
                .totalSeats(show.getTotalSeats())
                .viewCount(viewCount)
                .build();
    }

    public static ShowResponseDto toDtos(Show show) {
        return ShowResponseDto.builder()
                .title(show.getTitle())
                .category(show.getCategory())
                .region(show.getRegion())
                .startDate(show.getStartDate())
                .endDate(show.getEndDate())
                .reservationStartDate(show.getReservationStartDate())
                .reservationEndDate(show.getReservationEndDate())
                .totalSeats(show.getTotalSeats())
                .build();
    }

}
