package com.example.sparta_ticketing.domain.user.repository;

import com.example.sparta_ticketing.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class UserBulkRepository {
    private final JdbcTemplate jdbcTemplate;

    private static final int BATCH_SIZE = 1000;

    public void bulkInsert(List<User> users) {
        String sql = "INSERT INTO users (email, password, nickname, birthday, phone_number, user_role, created_at, modified_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        LocalDateTime now = LocalDateTime.now();

        jdbcTemplate.batchUpdate(sql, users, BATCH_SIZE, (ps, user) -> {
            ps.setString(1, user.getEmail());
            ps.setString(2, user.getPassword());
            ps.setString(3, user.getNickname());
            ps.setString(4, user.getBirthday());
            ps.setString(5, user.getPhoneNumber());
            ps.setString(6, user.getUserRole().name());
            ps.setObject(7, now);
            ps.setObject(8, now);

        });
    }
}
