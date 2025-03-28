package com.example.sparta_ticketing.domain.reserve.entity;

import com.example.sparta_ticketing.domain.seat.enums.SeatEnum;
import com.example.sparta_ticketing.domain.show.entity.Show;
import com.example.sparta_ticketing.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@NoArgsConstructor
@Getter
public class Reserve {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "show_id", nullable = false)
    private Show show;

    private SeatEnum seatName;
    private int price;
    private LocalDateTime reservedAt;

    public Reserve(User user, Show show, SeatEnum seatName, int price) {
        this.user = user;
        this.show = show;
        this.seatName = seatName;
        this.price = price;
        this.reservedAt = LocalDateTime.now();
    }

}
