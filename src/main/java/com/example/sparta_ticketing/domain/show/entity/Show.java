package com.example.sparta_ticketing.domain.show.entity;

import com.example.sparta_ticketing.common.entity.BaseEntity;
import com.example.sparta_ticketing.domain.show.dto.request.CreateShowRequestDto;
import com.example.sparta_ticketing.domain.show.dto.request.UpdateShowRequestDto;
import com.example.sparta_ticketing.domain.show.enums.Category;
import com.example.sparta_ticketing.domain.show.enums.Region;
import com.example.sparta_ticketing.domain.show.enums.ShowStatus;
import com.example.sparta_ticketing.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

import static com.example.sparta_ticketing.domain.show.enums.ShowStatus.*;

@Getter
@Entity
@Table(name = "shows")
@NoArgsConstructor
public class Show extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    @Enumerated(EnumType.STRING)
    private Category category;

    private String content;

    @Enumerated(EnumType.STRING)
    private Region region;

    private LocalDateTime startDate;
    private LocalDateTime endDate;

    private LocalDateTime reservationStartDate;
    private LocalDateTime reservationEndDate;

    private int totalSeats;


    public void sumSeat(int totalSeats) {
        this.totalSeats = totalSeats;
    }

    @Enumerated(EnumType.STRING)
    private ShowStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "directer_id", nullable = false)
    private User user;

    public void updateShow(UpdateShowRequestDto requestDto) {
        this.title = requestDto.getTitle();
        this.category = requestDto.getCategory();
        this.content = requestDto.getContent();
        this.region = requestDto.getRegion();
        this.startDate = requestDto.getStartDate();
        this.endDate = requestDto.getEndDate();
        this.reservationStartDate = requestDto.getReservationStartDate();
        this.reservationEndDate = requestDto.getReservationEndDate();
    }

    public void deleteShow() {
        this.status = DELETED;
    }


    public Show(CreateShowRequestDto createShowRequestDto, int totalSeats, User user) {
        this.title = createShowRequestDto.getTitle();
        this.category = createShowRequestDto.getCategory();
        this.content = createShowRequestDto.getContent();
        this.region = createShowRequestDto.getRegion();
        this.startDate = createShowRequestDto.getStartDate();
        this.endDate = createShowRequestDto.getEndDate();
        this.reservationStartDate = createShowRequestDto.getReservationStartDate();
        this.reservationEndDate = createShowRequestDto.getReservationEndDate();
        this.totalSeats = totalSeats;
        this.user = user;
        this.status = NOT_DELETED;
    }

    public Show(
            String title,
            Category category,
            String content,
            Region region,
            LocalDateTime startDate,
            LocalDateTime endDate,
            LocalDateTime reservationStartDate,
            LocalDateTime reservationEndDate,
            int totalSeats,
            User user
    ) {
        this.title = title;
        this.category = category;
        this.content = content;
        this.region = region;
        this.startDate = startDate;
        this.endDate = endDate;
        this.reservationStartDate = reservationStartDate;
        this.reservationEndDate = reservationEndDate;
        this.totalSeats = totalSeats;
        this.user = user;
        this.status = NOT_DELETED;
    }

}
