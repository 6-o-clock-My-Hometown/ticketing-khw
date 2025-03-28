/*
package com.example.sparta_ticketing.domain.show.entity;

import com.example.sparta_ticketing.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
@Table(
        name = "view_count",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "show_id"})
)
public class ViewCount {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "show_id", nullable = false)
    private Show show;

    public ViewCount(User user, Show show) {
        this.user = user;
        this.show = show;
    }
}
*/
